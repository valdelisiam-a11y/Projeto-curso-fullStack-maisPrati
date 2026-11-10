package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.TrabalhoAtualizacaoDTO;
import br.com.docflow.backend.dto.TrabalhoCadastroDTO;
import br.com.docflow.backend.dto.TrabalhoRespostaDTO;
import br.com.docflow.backend.dto.HistoricoTrabalhoRespostaDTO;
import br.com.docflow.backend.entity.Empresa;
import br.com.docflow.backend.entity.StatusTrabalho;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.EmpresaRepository;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import br.com.docflow.backend.dto.TrabalhoStatusDTO;
import br.com.docflow.backend.entity.HistoricoTrabalho;
import br.com.docflow.backend.repository.HistoricoTrabalhoRepository;
import org.springframework.transaction.annotation.Transactional;
import br.com.docflow.backend.entity.UsuarioTrabalho;
import br.com.docflow.backend.repository.UsuarioTrabalhoRepository;
import org.springframework.security.access.AccessDeniedException;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
import br.com.docflow.backend.entity.TipoTrabalho;
import br.com.docflow.backend.repository.EtapaRepository;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.entity.EventoTrabalho;
import br.com.docflow.backend.entity.HistoricoEtapa;
import br.com.docflow.backend.entity.TipoEventoTrabalho;
import br.com.docflow.backend.dto.LinhaDoTempoRespostaDTO;
import br.com.docflow.backend.repository.EventoTrabalhoRepository;
import br.com.docflow.backend.repository.HistoricoEtapaRepository;
import java.util.ArrayList;
import java.util.Comparator;

import java.time.LocalDateTime;
import java.util.List;



@Service
public class TrabalhoService {

    private final TrabalhoRepository trabalhoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoTrabalhoRepository historicoTrabalhoRepository;
    private final UsuarioTrabalhoRepository usuarioTrabalhoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final EtapaRepository etapaRepository;
    private final EventoTrabalhoRepository eventoTrabalhoRepository;
    private final HistoricoEtapaRepository historicoEtapaRepository;


    public TrabalhoService(
            TrabalhoRepository trabalhoRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            HistoricoTrabalhoRepository historicoTrabalhoRepository,
            UsuarioTrabalhoRepository usuarioTrabalhoRepository,
            UsuarioAutenticadoService usuarioAutenticadoService,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            EtapaRepository etapaRepository,
            EventoTrabalhoRepository eventoTrabalhoRepository,
            HistoricoEtapaRepository historicoEtapaRepository) {

        this.trabalhoRepository = trabalhoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoTrabalhoRepository = historicoTrabalhoRepository;
        this.usuarioTrabalhoRepository = usuarioTrabalhoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.etapaRepository = etapaRepository;
        this.eventoTrabalhoRepository = eventoTrabalhoRepository;
        this.historicoEtapaRepository = historicoEtapaRepository;
    }


    @Transactional
    public TrabalhoRespostaDTO cadastrar(TrabalhoCadastroDTO dto) {

        if (dto.getTipo() == null) {
            throw new IllegalArgumentException(
                    "O tipo do trabalho deve ser informado."
            );
        }

        Usuario usuario = usuarioAutenticadoService.obterUsuario();
        Empresa empresa = null;

        if (dto.getTipo() == TipoTrabalho.EMPRESA) {

            if (dto.getEmpresaId() == null) {
                throw new IllegalArgumentException(
                        "O ID da empresa é obrigatório para trabalhos empresariais."
                );
            }

            empresa = empresaRepository.findById(dto.getEmpresaId())
                    .orElseThrow(() ->
                            new RecursoNaoEncontradoException("Empresa não encontrada"));

            if (!usuarioEmpresaRepository.existsByUsuarioAndEmpresa(usuario, empresa)) {
                throw new UsuarioEmpresaDiferenteException(
                        "Usuário não pertence à empresa informada para o trabalho."
                );
            }

        } else if (dto.getTipo() == TipoTrabalho.PESSOAL) {

            if (dto.getEmpresaId() != null) {
                throw new IllegalArgumentException(
                        "Um trabalho pessoal não pode estar vinculado a uma empresa."
                );
            }
        }

        Trabalho trabalho = new Trabalho();

        trabalho.setTitulo(dto.getTitulo());
        trabalho.setDescricao(dto.getDescricao());
        trabalho.setTipo(dto.getTipo());
        trabalho.setStatus(StatusTrabalho.PENDENTE);
        trabalho.setDataCriacao(LocalDateTime.now());
        trabalho.setDataAtualizacao(LocalDateTime.now());
        trabalho.setEmpresa(empresa);
        trabalho.setCriadoPor(usuario);

        Trabalho trabalhoSalvo = trabalhoRepository.save(trabalho);

        EventoTrabalho evento = new EventoTrabalho();

        evento.setTipo(TipoEventoTrabalho.TRABALHO_CRIADO);
        evento.setDescricao("Trabalho criado: " + trabalhoSalvo.getTitulo());
        evento.setValorNovo(trabalhoSalvo.getTitulo());
        evento.setTrabalho(trabalhoSalvo);
        evento.setUsuario(usuario);

        eventoTrabalhoRepository.save(evento);

        HistoricoTrabalho historico = new HistoricoTrabalho();
        historico.setStatusAnterior(null);
        historico.setStatusNovo(trabalhoSalvo.getStatus());
        historico.setDataAlteracao(LocalDateTime.now());
        historico.setTrabalho(trabalhoSalvo);
        historico.setUsuario(usuario);

        historicoTrabalhoRepository.save(historico);

        return converterParaResposta(trabalhoSalvo);
    }

    public List<TrabalhoRespostaDTO> listarTodos() {
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        return trabalhoRepository.findAcessiveisPorUsuario(usuario, TipoTrabalho.EMPRESA)
                .stream()
                .map(this::converterParaResposta)
                .toList();
    }

    public TrabalhoRespostaDTO buscarPorId(Long id) {
        Trabalho trabalho = validarAcesso(id);
        return converterParaResposta(trabalho);
    }

    public List<TrabalhoRespostaDTO> buscarPorTitulo(String titulo) {
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        return trabalhoRepository.findAcessiveisPorUsuario(usuario, TipoTrabalho.EMPRESA)
                .stream()
                .filter(trabalho -> trabalho.getTitulo() != null
                        && trabalho.getTitulo().toLowerCase()
                        .contains(titulo.toLowerCase()))
                .map(this::converterParaResposta)
                .toList();
    }

    @Transactional
    public TrabalhoRespostaDTO atualizar(Long id, TrabalhoAtualizacaoDTO dto) {

        Trabalho trabalho = validarAcesso(id);
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        String tituloAnterior = trabalho.getTitulo();
        String descricaoAnterior = trabalho.getDescricao();

        boolean tituloAlterado =
                !java.util.Objects.equals(tituloAnterior, dto.getTitulo());

        boolean descricaoAlterada =
                !java.util.Objects.equals(descricaoAnterior, dto.getDescricao());

        if (!tituloAlterado && !descricaoAlterada) {
            return converterParaResposta(trabalho);
        }

        if (tituloAlterado) {
            EventoTrabalho evento = new EventoTrabalho();
            evento.setTipo(TipoEventoTrabalho.TRABALHO_EDITADO);
            evento.setDescricao("Título do trabalho alterado");
            evento.setValorAnterior(tituloAnterior);
            evento.setValorNovo(dto.getTitulo());
            evento.setTrabalho(trabalho);
            evento.setUsuario(usuario);

            eventoTrabalhoRepository.save(evento);

            trabalho.setTitulo(dto.getTitulo());
        }

        if (descricaoAlterada) {
            EventoTrabalho evento = new EventoTrabalho();
            evento.setTipo(TipoEventoTrabalho.TRABALHO_EDITADO);
            evento.setDescricao("Descrição do trabalho alterada");
            evento.setValorAnterior(descricaoAnterior);
            evento.setValorNovo(dto.getDescricao());
            evento.setTrabalho(trabalho);
            evento.setUsuario(usuario);

            eventoTrabalhoRepository.save(evento);

            trabalho.setDescricao(dto.getDescricao());
        }

        trabalho.setDataAtualizacao(LocalDateTime.now());

        Trabalho trabalhoAtualizado = trabalhoRepository.save(trabalho);

        return converterParaResposta(trabalhoAtualizado);
    }

    @Transactional
    public void excluir(Long id) {

        Trabalho trabalho = validarAcesso(id);

        historicoTrabalhoRepository.deleteByTrabalhoId(id);

        trabalhoRepository.delete(trabalho);
    }

    @Transactional
    public TrabalhoRespostaDTO alterarStatus(Long id, TrabalhoStatusDTO dto) {

        Trabalho trabalho = validarAcesso(id);
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        StatusTrabalho statusAnterior = trabalho.getStatus();

        if (statusAnterior == dto.getStatus()) {
            throw new RuntimeException("O Trabalho já está com este status");
        }

        trabalho.setStatus(dto.getStatus());
        trabalho.setDataAtualizacao(LocalDateTime.now());

        if (dto.getStatus() == StatusTrabalho.CONCLUIDO) {
            trabalho.setDataConclusao(LocalDateTime.now());
        } else {
            trabalho.setDataConclusao(null);
        }

        Trabalho trabalhoAtualizado = trabalhoRepository.save(trabalho);

        HistoricoTrabalho historico = new HistoricoTrabalho();

        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(dto.getStatus());
        historico.setDataAlteracao(LocalDateTime.now());
        historico.setTrabalho(trabalhoAtualizado);
        historico.setUsuario(usuario);

        historicoTrabalhoRepository.save(historico);

        return converterParaResposta(trabalhoAtualizado);
    }

    public List<HistoricoTrabalhoRespostaDTO> listarHistorico(Long trabalhoId) {

        Trabalho trabalho = validarAcesso(trabalhoId);

        List<HistoricoTrabalhoRespostaDTO> historicoStatus =
                historicoTrabalhoRepository
                        .findByTrabalhoIdOrderByDataAlteracaoDesc(trabalho.getId())
                        .stream()
                        .map(this::converterHistoricoParaResposta)
                        .toList();

        List<HistoricoTrabalhoRespostaDTO> historicoEventos =
                eventoTrabalhoRepository
                        .findByTrabalhoIdOrderByDataEventoDesc(trabalho.getId())
                        .stream()
                        .map(this::converterEventoParaResposta)
                        .toList();

        return java.util.stream.Stream
                .concat(historicoStatus.stream(), historicoEventos.stream())
                .sorted((a, b) -> {
                    LocalDateTime dataA = a.getDataAlteracao();
                    LocalDateTime dataB = b.getDataAlteracao();

                    return dataB.compareTo(dataA);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LinhaDoTempoRespostaDTO> listarLinhaDoTempo(Long trabalhoId) {

        Trabalho trabalho = validarAcesso(trabalhoId);

        List<LinhaDoTempoRespostaDTO> linhaDoTempo = new ArrayList<>();

        // Eventos gerais: criação, edição, observações, responsáveis etc.
        List<EventoTrabalho> eventos =
                eventoTrabalhoRepository
                        .findByTrabalhoIdOrderByDataEventoDesc(trabalho.getId());

        for (EventoTrabalho evento : eventos) {
            LinhaDoTempoRespostaDTO dto = new LinhaDoTempoRespostaDTO();

            dto.setId(evento.getId());
            dto.setTipo(evento.getTipo().name());
            dto.setTexto(evento.getDescricao());
            dto.setValorAnterior(evento.getValorAnterior());
            dto.setValorNovo(evento.getValorNovo());
            dto.setData(evento.getDataEvento());
            dto.setUsuarioId(evento.getUsuario().getId());
            dto.setUsuarioNome(evento.getUsuario().getNome());

            if (evento.getEtapa() != null) {
                dto.setEtapaId(evento.getEtapa().getId());
                dto.setEtapaTitulo(evento.getEtapa().getTitulo());
            }

            linhaDoTempo.add(dto);
        }

        // Histórico de alterações de status do trabalho.
        historicoTrabalhoRepository
                .findByTrabalhoIdOrderByDataAlteracaoDesc(trabalho.getId())
                .forEach(historico -> {

                    // Ignora o registro inicial de status.
                    if (historico.getStatusAnterior() == null) {
                        return;
                    }

                    LinhaDoTempoRespostaDTO dto = new LinhaDoTempoRespostaDTO();

                    dto.setId(historico.getId());
                    dto.setTipo("STATUS_TRABALHO");
                    dto.setTexto(
                            "Status do trabalho alterado de "
                                    + historico.getStatusAnterior()
                                    + " para "
                                    + historico.getStatusNovo()
                    );
                    dto.setValorAnterior(historico.getStatusAnterior().name());
                    dto.setValorNovo(historico.getStatusNovo().name());
                    dto.setData(historico.getDataAlteracao());
                    dto.setUsuarioId(historico.getUsuario().getId());
                    dto.setUsuarioNome(historico.getUsuario().getNome());

                    linhaDoTempo.add(dto);
                });

// Histórico de alterações de status de cada etapa.
        List<Etapa> etapas =
                etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalho.getId());

        for (Etapa etapa : etapas) {
            historicoEtapaRepository
                    .findByEtapaIdOrderByDataAlteracaoDesc(etapa.getId())
                    .forEach(historico -> {

                        // Ignora o registro inicial, se houver.
                        if (historico.getStatusAnterior() == null) {
                            return;
                        }

                        LinhaDoTempoRespostaDTO dto = new LinhaDoTempoRespostaDTO();

                        dto.setId(historico.getId());
                        dto.setTipo("STATUS_ETAPA");
                        dto.setTexto(
                                "Status da etapa alterado de "
                                        + historico.getStatusAnterior()
                                        + " para "
                                        + historico.getStatusNovo()
                        );
                        dto.setValorAnterior(historico.getStatusAnterior().name());
                        dto.setValorNovo(historico.getStatusNovo().name());
                        dto.setData(historico.getDataAlteracao());
                        dto.setUsuarioId(historico.getUsuario().getId());
                        dto.setUsuarioNome(historico.getUsuario().getNome());
                        dto.setEtapaId(etapa.getId());
                        dto.setEtapaTitulo(etapa.getTitulo());

                        linhaDoTempo.add(dto);
                    });
        }

// Mais recentes primeiro.
        linhaDoTempo.sort(
                Comparator.comparing(
                        LinhaDoTempoRespostaDTO::getData,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        return linhaDoTempo;
    }

    private Trabalho validarAcesso(Long trabalhoId) {
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Trabalho não encontrado"));

        boolean criador = trabalho.getCriadoPor().getId()
                .equals(usuario.getId());

        // Trabalhos pessoais são exclusivos do criador.
        if (trabalho.getTipo() == TipoTrabalho.PESSOAL) {
            if (!criador) {
                throw new AccessDeniedException(
                        "Você não tem permissão para acessar este trabalho pessoal."
                );
            }

            return trabalho;
        }

        // Trabalhos empresariais permitem acesso ao criador ou participante.
        boolean participante = usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .isPresent();

        if (!criador && !participante) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar este trabalho."
            );
        }

        return trabalho;
    }

    @Transactional
    public void sincronizarStatusComEtapas(Long trabalhoId) {

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Trabalho não encontrado"));

        boolean criador = trabalho.getCriadoPor().getId()
                .equals(usuario.getId());

        boolean participante = trabalho.getTipo() == TipoTrabalho.EMPRESA
                && usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .isPresent();

        boolean responsavel = false;

        if (trabalho.getTipo() == TipoTrabalho.EMPRESA
                && trabalho.getEmpresa() != null
                && usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                usuario, trabalho.getEmpresa())) {

            responsavel = etapaRepository
                    .findByResponsavelIdAndTrabalhoEmpresaIdOrderByOrdemAsc(
                            usuario.getId(),
                            trabalho.getEmpresa().getId()
                    )
                    .stream()
                    .anyMatch(etapa ->
                            etapa.getTrabalho().getId().equals(trabalhoId));
        }

        if (!criador && !participante && !responsavel) {
            throw new AccessDeniedException(
                    "Você não tem permissão para sincronizar este trabalho."
            );
        }

        // Um trabalho cancelado não deve ser reaberto automaticamente.
        if (trabalho.getStatus() == StatusTrabalho.CANCELADO) {
            return;
        }

        List<Etapa> etapas =
                etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalhoId);

        System.out.println("SINCRONIZAÇÃO - trabalho ID: " + trabalhoId);
        System.out.println("SINCRONIZAÇÃO - status atual: " + trabalho.getStatus());
        System.out.println("SINCRONIZAÇÃO - quantidade de etapas: " + etapas.size());

        StatusTrabalho statusAnterior = trabalho.getStatus();
        StatusTrabalho statusNovo;

        if (etapas.isEmpty()) {
            statusNovo = StatusTrabalho.PENDENTE;

        } else if (etapas.stream().allMatch(
                etapa -> etapa.getStatus() == StatusTrabalho.CONCLUIDO)) {

            statusNovo = StatusTrabalho.CONCLUIDO;

        } else if (etapas.stream().anyMatch(
                etapa -> etapa.getStatus() == StatusTrabalho.EM_ANDAMENTO
                        || etapa.getStatus() == StatusTrabalho.CONCLUIDO
                        || etapa.getPercentualConclusao() > 0)) {

            statusNovo = StatusTrabalho.EM_ANDAMENTO;

        } else {
            statusNovo = StatusTrabalho.PENDENTE;
        }
        System.out.println("SINCRONIZAÇÃO - status calculado: " + statusNovo);

        if (statusAnterior == statusNovo) {
            return;
        }

        trabalho.setStatus(statusNovo);
        trabalho.setDataAtualizacao(LocalDateTime.now());

        if (statusNovo == StatusTrabalho.CONCLUIDO) {
            trabalho.setDataConclusao(LocalDateTime.now());
        } else {
            trabalho.setDataConclusao(null);
        }

        Trabalho trabalhoAtualizado = trabalhoRepository.save(trabalho);

        HistoricoTrabalho historico = new HistoricoTrabalho();
        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(statusNovo);
        historico.setDataAlteracao(LocalDateTime.now());
        historico.setTrabalho(trabalhoAtualizado);
        historico.setUsuario(usuario);

        historicoTrabalhoRepository.save(historico);
    }

    private TrabalhoRespostaDTO converterParaResposta(Trabalho trabalho) {

        TrabalhoRespostaDTO resposta = new TrabalhoRespostaDTO();

        resposta.setId(trabalho.getId());
        resposta.setTitulo(trabalho.getTitulo());
        resposta.setDescricao(trabalho.getDescricao());
        resposta.setTipo(trabalho.getTipo());
        resposta.setStatus(trabalho.getStatus());
        resposta.setDataCriacao(trabalho.getDataCriacao());
        resposta.setDataAtualizacao(trabalho.getDataAtualizacao());
        resposta.setDataConclusao(trabalho.getDataConclusao());

        List<Etapa> etapas = etapaRepository
                .findByTrabalhoIdOrderByOrdemAsc(trabalho.getId());

        Etapa etapaAtual = etapas.stream()
                .filter(etapa -> etapa.getStatus() != StatusTrabalho.CONCLUIDO
                        && etapa.getStatus() != StatusTrabalho.CANCELADO)
                .findFirst()
                .orElse(null);

        resposta.setEtapaAtualId(
                etapaAtual != null ? etapaAtual.getId() : null
        );

        resposta.setEtapaAtualTitulo(
                etapaAtual != null
                        ? etapaAtual.getTitulo()
                        : null
        );

        int progresso = etapas.isEmpty()
                ? 0
                : (int) Math.round(
                etapas.stream()
                        .mapToInt(Etapa::getPercentualConclusao)
                        .average()
                        .orElse(0)
        );

        resposta.setProgresso(progresso);

        resposta.setEmpresaId(
                trabalho.getEmpresa() != null
                        ? trabalho.getEmpresa().getId()
                        : null
        );

        resposta.setCriadoPorId(trabalho.getCriadoPor().getId());
        resposta.setCriadoPorNome(trabalho.getCriadoPor().getNome());

        return resposta;
    }

    private HistoricoTrabalhoRespostaDTO converterHistoricoParaResposta(
            HistoricoTrabalho historico) {

        HistoricoTrabalhoRespostaDTO resposta =
                new HistoricoTrabalhoRespostaDTO();

        resposta.setId(historico.getId());
        resposta.setTipo("STATUS");
        resposta.setStatusAnterior(historico.getStatusAnterior());
        resposta.setStatusNovo(historico.getStatusNovo());

        resposta.setTexto(
                "Status alterado de " +
                        historico.getStatusAnterior() +
                        " para " +
                        historico.getStatusNovo() + "."
        );

        resposta.setDataAlteracao(historico.getDataAlteracao());
        resposta.setUsuarioId(historico.getUsuario().getId());
        resposta.setUsuarioNome(historico.getUsuario().getNome());

        return resposta;
    }

    private HistoricoTrabalhoRespostaDTO converterEventoParaResposta(
            EventoTrabalho evento) {

        HistoricoTrabalhoRespostaDTO resposta =
                new HistoricoTrabalhoRespostaDTO();

        resposta.setId(evento.getId());
        resposta.setTipo(evento.getTipo().name());
        resposta.setTexto(evento.getDescricao());
        resposta.setDataAlteracao(evento.getDataEvento());
        resposta.setUsuarioId(evento.getUsuario().getId());
        resposta.setUsuarioNome(evento.getUsuario().getNome());

        if (evento.getEtapa() != null) {
            resposta.setEtapaTitulo(evento.getEtapa().getTitulo());
        }

        return resposta;
    }
}