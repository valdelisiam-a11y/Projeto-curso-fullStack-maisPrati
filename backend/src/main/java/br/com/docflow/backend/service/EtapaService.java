package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.EtapaAtualizacaoDTO;
import br.com.docflow.backend.dto.EtapaCadastroDTO;
import br.com.docflow.backend.dto.EtapaOrdemDTO;
import br.com.docflow.backend.dto.EtapaPercentualDTO;
import br.com.docflow.backend.dto.EtapaRespostaDTO;
import br.com.docflow.backend.dto.EtapaResponsavelDTO;
import br.com.docflow.backend.dto.EtapaStatusDTO;
import br.com.docflow.backend.dto.HistoricoEtapaRespostaDTO;
import br.com.docflow.backend.dto.MinhaEtapaRespostaDTO;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.entity.HistoricoEtapa;
import br.com.docflow.backend.entity.StatusTrabalho;
import br.com.docflow.backend.entity.TipoTrabalho;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.EtapaRepository;
import br.com.docflow.backend.repository.HistoricoEtapaRepository;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import br.com.docflow.backend.repository.UsuarioTrabalhoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.docflow.backend.dto.ObservacaoEtapaDTO;
import br.com.docflow.backend.entity.EventoTrabalho;
import br.com.docflow.backend.entity.TipoEventoTrabalho;
import br.com.docflow.backend.repository.EventoTrabalhoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EtapaService {

    private final EtapaRepository etapaRepository;
    private final HistoricoEtapaRepository historicoEtapaRepository;
    private final TrabalhoRepository trabalhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final UsuarioTrabalhoRepository usuarioTrabalhoRepository;
    private final TrabalhoService trabalhoService;
    private final EventoTrabalhoRepository eventoTrabalhoRepository;

    public EtapaService(
            EtapaRepository etapaRepository,
            HistoricoEtapaRepository historicoEtapaRepository,
            TrabalhoRepository trabalhoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioAutenticadoService usuarioAutenticadoService,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            UsuarioTrabalhoRepository usuarioTrabalhoRepository,
            TrabalhoService trabalhoService,
            EventoTrabalhoRepository eventoTrabalhoRepository) {

        this.etapaRepository = etapaRepository;
        this.historicoEtapaRepository = historicoEtapaRepository;
        this.trabalhoRepository = trabalhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.usuarioTrabalhoRepository = usuarioTrabalhoRepository;
        this.trabalhoService = trabalhoService;
        this.eventoTrabalhoRepository = eventoTrabalhoRepository;


    }

    @Transactional
    public EtapaRespostaDTO cadastrar(Long trabalhoId, EtapaCadastroDTO dto) {

        Trabalho trabalho = validarAcessoTrabalho(trabalhoId);

        Etapa etapa = new Etapa();

        etapa.setTitulo(dto.getTitulo());
        etapa.setDescricao(dto.getDescricao());
        etapa.setStatus(StatusTrabalho.PENDENTE);
        etapa.setPercentualConclusao(0);
        etapa.setTrabalho(trabalho);

        // Responsável é opcional, mas precisa ser da mesma empresa do trabalho
        if (dto.getResponsavelId() != null) {
            etapa.setResponsavel(
                    buscarResponsavel(dto.getResponsavelId(), trabalho)
            );
        }

        // Entra sempre no final; se uma ordem foi pedida, reposicionamos em seguida
        etapa.setOrdem(etapaRepository.buscarMaiorOrdem(trabalhoId) + 1);

        Etapa etapaSalva = etapaRepository.save(etapa);

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        EventoTrabalho evento = new EventoTrabalho();

        evento.setTipo(TipoEventoTrabalho.ETAPA_CRIADA);
        evento.setDescricao("Etapa criada: " + etapaSalva.getTitulo());
        evento.setValorNovo(etapaSalva.getTitulo());
        evento.setTrabalho(trabalho);
        evento.setEtapa(etapaSalva);
        evento.setUsuario(usuario);

        eventoTrabalhoRepository.save(evento);

        if (dto.getOrdem() != null) {
            posicionar(etapaSalva, dto.getOrdem());
        }

        trabalhoService.sincronizarStatusComEtapas(trabalho.getId());

        return converterParaResposta(etapaSalva);
    }

    @Transactional(readOnly = true)
    public List<EtapaRespostaDTO> listarPorTrabalho(Long trabalhoId) {

        validarAcessoTrabalho(trabalhoId);

        return etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalhoId)
                .stream()
                .map(this::converterParaResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MinhaEtapaRespostaDTO> listarMinhasEtapas(Long empresaId) {

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        // Confirma que o usuário pertence à empresa selecionada
        boolean pertenceEmpresa = usuarioEmpresaRepository
                .findByUsuario(usuario)
                .stream()
                .anyMatch(vinculo ->
                        vinculo.getEmpresa().getId().equals(empresaId)
                );

        if (!pertenceEmpresa) {
            throw new AccessDeniedException(
                    "Você não tem acesso a esta empresa."
            );
        }

        // Busca somente as etapas atribuídas ao usuário nessa empresa
        return etapaRepository
                .findByResponsavelIdAndTrabalhoEmpresaIdOrderByOrdemAsc(
                        usuario.getId(),
                        empresaId
                )
                .stream()
                .map(etapa -> {
                    MinhaEtapaRespostaDTO resposta =
                            new MinhaEtapaRespostaDTO();

                    resposta.setEtapaId(etapa.getId());
                    resposta.setEtapaTitulo(etapa.getTitulo());
                    resposta.setStatus(etapa.getStatus());
                    resposta.setPercentualConclusao(
                            etapa.getPercentualConclusao()
                    );
                    resposta.setOrdem(etapa.getOrdem());

                    resposta.setTrabalhoId(
                            etapa.getTrabalho().getId()
                    );
                    resposta.setTrabalhoTitulo(
                            etapa.getTrabalho().getTitulo()
                    );

                    resposta.setEmpresaId(
                            etapa.getTrabalho().getEmpresa().getId()
                    );
                    resposta.setEmpresaNome(
                            etapa.getTrabalho().getEmpresa().getNome()
                    );

                    return resposta;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public EtapaRespostaDTO buscarPorId(Long id) {

        return converterParaResposta(buscarEntidadeComAcesso(id));
    }

    @Transactional
    public EtapaRespostaDTO atualizar(Long id, EtapaAtualizacaoDTO dto) {

        Etapa etapa = buscarEntidadeComAcesso(id);

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        String tituloAnterior = etapa.getTitulo();
        String descricaoAnterior = etapa.getDescricao();

        boolean tituloAlterado =
                !java.util.Objects.equals(tituloAnterior, dto.getTitulo());

        boolean descricaoAlterada =
                !java.util.Objects.equals(descricaoAnterior, dto.getDescricao());

        if (!tituloAlterado && !descricaoAlterada) {
            return converterParaResposta(etapa);
        }

        if (tituloAlterado) {
            EventoTrabalho evento = new EventoTrabalho();

            evento.setTipo(TipoEventoTrabalho.ETAPA_EDITADA);
            evento.setDescricao("Título da etapa alterado");
            evento.setValorAnterior(tituloAnterior);
            evento.setValorNovo(dto.getTitulo());
            evento.setTrabalho(etapa.getTrabalho());
            evento.setEtapa(etapa);
            evento.setUsuario(usuario);

            eventoTrabalhoRepository.save(evento);

            etapa.setTitulo(dto.getTitulo());
        }

        if (descricaoAlterada) {
            EventoTrabalho evento = new EventoTrabalho();

            evento.setTipo(TipoEventoTrabalho.ETAPA_EDITADA);
            evento.setDescricao("Descrição da etapa alterada");
            evento.setValorAnterior(descricaoAnterior);
            evento.setValorNovo(dto.getDescricao());
            evento.setTrabalho(etapa.getTrabalho());
            evento.setEtapa(etapa);
            evento.setUsuario(usuario);

            eventoTrabalhoRepository.save(evento);

            etapa.setDescricao(dto.getDescricao());
        }

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        return converterParaResposta(etapaAtualizada);
    }

    @Transactional
    public EtapaRespostaDTO alterarStatus(Long id, EtapaStatusDTO dto) {
        Etapa etapa = buscarEtapaComAcessoOperacional(id);
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        StatusTrabalho statusAnterior = etapa.getStatus();
        int percentualAnterior = etapa.getPercentualConclusao();

        if (statusAnterior == dto.getStatus()) {
            throw new EtapaStatusInalteradoException("A etapa já está com este status");
        }

        if (statusAnterior == StatusTrabalho.CANCELADO
                && dto.getStatus() == StatusTrabalho.CONCLUIDO) {
            throw new IllegalArgumentException(
                    "Uma etapa cancelada deve ser reaberta como pendente ou em andamento."
            );
        }

        etapa.setStatus(dto.getStatus());

        // Etapa concluída tem sempre 100% de conclusão
        // Mantém o percentual coerente com o status da etapa
        if (dto.getStatus() == StatusTrabalho.CONCLUIDO) {
            etapa.setPercentualConclusao(100);

        } else if (dto.getStatus() == StatusTrabalho.PENDENTE) {
            etapa.setPercentualConclusao(0);

        } else if (dto.getStatus() == StatusTrabalho.EM_ANDAMENTO) {
            if (statusAnterior == StatusTrabalho.CONCLUIDO) {
                etapa.setPercentualConclusao(99);
            } else if (percentualAnterior == 0) {
                etapa.setPercentualConclusao(1);
            }
        }

        int percentualNovo = etapa.getPercentualConclusao();

        // Registra o evento somente se o percentual realmente mudou
        if (percentualAnterior != percentualNovo) {
            EventoTrabalho evento = new EventoTrabalho();
            evento.setTipo(TipoEventoTrabalho.PERCENTUAL_ALTERADO);
            evento.setDescricao("Percentual de conclusão da etapa alterado");
            evento.setValorAnterior(String.valueOf(percentualAnterior));
            evento.setValorNovo(String.valueOf(percentualNovo));
            evento.setTrabalho(etapa.getTrabalho());
            evento.setEtapa(etapa);
            evento.setUsuario(usuario);

            eventoTrabalhoRepository.save(evento);
        }

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        // O histórico de status continua sendo registrado separadamente
        registrarHistorico(etapaAtualizada, statusAnterior, dto.getStatus(), usuario);

        trabalhoService.sincronizarStatusComEtapas(etapaAtualizada.getTrabalho().getId());

        return converterParaResposta(etapaAtualizada);
    }

    @Transactional
    public EtapaRespostaDTO atualizarPercentual(Long id, EtapaPercentualDTO dto) {

        Etapa etapa = buscarEtapaComAcessoOperacional(id);

        if (etapa.getStatus() == StatusTrabalho.CANCELADO) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Não é possível atualizar o percentual de uma etapa cancelada. Reabra a etapa primeiro."
            );
        }

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        StatusTrabalho statusAnterior = etapa.getStatus();
        int percentualAnterior = etapa.getPercentualConclusao();
        int percentualNovo = dto.getPercentualConclusao();

        boolean percentualAlterado = percentualAnterior != percentualNovo;

        etapa.setPercentualConclusao(percentualNovo);

        // Mantém o status coerente com o percentual
        if (percentualNovo == 100) {
            etapa.setStatus(StatusTrabalho.CONCLUIDO);

        } else if (percentualNovo == 0) {
            etapa.setStatus(StatusTrabalho.PENDENTE);

        } else {
            etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        }

        // Registra o percentual somente quando houver alteração real
        if (percentualAlterado) {
            EventoTrabalho evento = new EventoTrabalho();

            evento.setTipo(TipoEventoTrabalho.PERCENTUAL_ALTERADO);
            evento.setDescricao("Percentual de conclusão da etapa alterado");
            evento.setValorAnterior(String.valueOf(percentualAnterior));
            evento.setValorNovo(String.valueOf(percentualNovo));
            evento.setTrabalho(etapa.getTrabalho());
            evento.setEtapa(etapa);
            evento.setUsuario(usuario);

            eventoTrabalhoRepository.save(evento);
        }

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        // O histórico de status continua sendo registrado separadamente
        if (etapaAtualizada.getStatus() != statusAnterior) {
            registrarHistorico(
                    etapaAtualizada,
                    statusAnterior,
                    etapaAtualizada.getStatus(),
                    usuario
            );
        }

        trabalhoService.sincronizarStatusComEtapas(
                etapaAtualizada.getTrabalho().getId()
        );

        return converterParaResposta(etapaAtualizada);
    }

    @Transactional
    public EtapaRespostaDTO definirResponsavel(Long id, EtapaResponsavelDTO dto) {

        Etapa etapa = buscarEntidadeComAcesso(id);

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        Usuario responsavelAnterior = etapa.getResponsavel();
        Usuario responsavelNovo = null;

        if (dto.getResponsavelId() != null) {
            responsavelNovo = buscarResponsavel(
                    dto.getResponsavelId(),
                    etapa.getTrabalho()
            );
        }

        Long idAnterior = responsavelAnterior != null
                ? responsavelAnterior.getId()
                : null;

        Long idNovo = responsavelNovo != null
                ? responsavelNovo.getId()
                : null;

        // Não registra evento se o responsável não mudou.
        if (java.util.Objects.equals(idAnterior, idNovo)) {
            return converterParaResposta(etapa);
        }

        EventoTrabalho evento = new EventoTrabalho();

        evento.setTipo(TipoEventoTrabalho.RESPONSAVEL_ALTERADO);
        evento.setTrabalho(etapa.getTrabalho());
        evento.setEtapa(etapa);
        evento.setUsuario(usuario);

        evento.setValorAnterior(
                responsavelAnterior != null
                        ? responsavelAnterior.getNome()
                        : "Sem responsável"
        );

        evento.setValorNovo(
                responsavelNovo != null
                        ? responsavelNovo.getNome()
                        : "Sem responsável"
        );

        if (responsavelAnterior == null) {
            evento.setDescricao("Responsável atribuído à etapa");
        } else if (responsavelNovo == null) {
            evento.setDescricao("Responsável removido da etapa");
        } else {
            evento.setDescricao("Responsável da etapa alterado");
        }

        eventoTrabalhoRepository.save(evento);

        etapa.setResponsavel(responsavelNovo);

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        return converterParaResposta(etapaAtualizada);
    }

    @Transactional
    public EtapaRespostaDTO alterarOrdem(Long id, EtapaOrdemDTO dto) {

        Etapa etapa = buscarEntidadeComAcesso(id);

        int ordemAnterior = etapa.getOrdem();

        // Reposiciona a etapa e renumera as demais automaticamente.
        posicionar(etapa, dto.getOrdem());

        int ordemNova = etapa.getOrdem();

        // Registra somente se a posição realmente mudou.
        if (ordemAnterior != ordemNova) {

            Usuario usuario = usuarioAutenticadoService.obterUsuario();

            EventoTrabalho evento = new EventoTrabalho();

            evento.setTipo(TipoEventoTrabalho.ORDEM_ALTERADA);
            evento.setDescricao("Ordem da etapa alterada");
            evento.setValorAnterior(String.valueOf(ordemAnterior));
            evento.setValorNovo(String.valueOf(ordemNova));
            evento.setTrabalho(etapa.getTrabalho());
            evento.setEtapa(etapa);
            evento.setUsuario(usuario);

            eventoTrabalhoRepository.save(evento);
        }

        return converterParaResposta(etapa);
    }

    @Transactional
    public void excluir(Long id) {

        Etapa etapa = buscarEntidadeComAcesso(id);
        Long trabalhoId = etapa.getTrabalho().getId();

        // O histórico da etapa é excluído pelo banco (ON DELETE CASCADE)
        etapaRepository.delete(etapa);

        // Fecha o buraco deixado na sequência
        renumerar(
                new ArrayList<>(
                        etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalhoId)
                )
        );

        trabalhoService.sincronizarStatusComEtapas(trabalhoId);
    }

    @Transactional(readOnly = true)
    public List<HistoricoEtapaRespostaDTO> listarHistorico(Long etapaId) {

        Etapa etapa = buscarEntidadeComAcesso(etapaId);

        return historicoEtapaRepository
                .findByEtapaIdOrderByDataAlteracaoDesc(etapa.getId())
                .stream()
                .map(this::converterHistoricoParaResposta)
                .toList();
    }

    // Move a etapa para a nova posição.
    // Se a ordem pedida for maior que o total, a etapa vai para o final.
    private void posicionar(Etapa etapa, int novaOrdem) {

        Long trabalhoId = etapa.getTrabalho().getId();

        List<Etapa> etapas = new ArrayList<>(
                etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalhoId)
        );

        etapas.removeIf(e -> e.getId().equals(etapa.getId()));

        int total = etapas.size() + 1;
        int indice = Math.min(novaOrdem, total) - 1;

        etapas.add(indice, etapa);

        renumerar(etapas);
    }

    // Reescreve a ordem como 1, 2, 3... conforme a posição na lista
    private void renumerar(List<Etapa> etapas) {

        for (int i = 0; i < etapas.size(); i++) {
            etapas.get(i).setOrdem(i + 1);
        }

        etapaRepository.saveAll(etapas);
    }

    private void registrarHistorico(
            Etapa etapa,
            StatusTrabalho statusAnterior,
            StatusTrabalho statusNovo,
            Usuario usuario) {

        HistoricoEtapa historico = new HistoricoEtapa();

        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(statusNovo);
        historico.setDataAlteracao(LocalDateTime.now());
        historico.setEtapa(etapa);
        historico.setUsuario(usuario);

        historicoEtapaRepository.save(historico);
    }

    // Busca o usuário responsável e garante que ele é da mesma empresa do trabalho
    private Usuario buscarResponsavel(Long usuarioId, Trabalho trabalho) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário responsável não encontrado"
                        )
                );

        if (!mesmaEmpresa(usuario, trabalho)) {
            throw new UsuarioEmpresaDiferenteException(
                    "O responsável deve pertencer à mesma empresa do trabalho"
            );
        }

        return usuario;
    }

    private boolean mesmaEmpresa(Usuario usuario, Trabalho trabalho) {

        return trabalho.getEmpresa() != null
                && usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                usuario,
                trabalho.getEmpresa()
        );
    }

    private Trabalho validarAcessoTrabalho(Long trabalhoId) {

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Trabalho não encontrado"
                        )
                );

        boolean criador = trabalho.getCriadoPor().getId()
                .equals(usuario.getId());

        boolean participante = trabalho.getTipo() == TipoTrabalho.EMPRESA
                && usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .isPresent();

        if (!criador && !participante) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar este trabalho."
            );
        }

        return trabalho;
    }

    // Permite que o responsável consulte e atualize operacionalmente sua etapa
    private Etapa buscarEtapaComAcessoOperacional(Long id) {

        Etapa etapa = etapaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Etapa não encontrada"
                        )
                );

        Trabalho trabalho = etapa.getTrabalho();
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        boolean criador = trabalho.getCriadoPor().getId()
                .equals(usuario.getId());

        boolean participante = trabalho.getTipo() == TipoTrabalho.EMPRESA
                && usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .isPresent();

        boolean responsavel = etapa.getResponsavel() != null
                && etapa.getResponsavel().getId().equals(usuario.getId())
                && mesmaEmpresa(usuario, trabalho);

        if (!criador && !participante && !responsavel) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar esta etapa."
            );
        }

        return etapa;
    }

    // Busca a etapa e valida se o usuário pode acessar o trabalho relacionado.
    @Transactional(readOnly = true)
    public Etapa buscarEntidadeComAcesso(Long id) {

        Etapa etapa = etapaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Etapa não encontrada"
                        )
                );

        Trabalho trabalho = etapa.getTrabalho();
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        boolean criador = trabalho.getCriadoPor().getId()
                .equals(usuario.getId());

        boolean participante = trabalho.getTipo() == TipoTrabalho.EMPRESA
                && usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .isPresent();

        if (!criador && !participante) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar esta etapa."
            );
        }

        return etapa;
    }


    // Valida o acesso específico às operações com documentos da etapa.
// O responsável pode consultar, enviar e baixar documentos,
// mas não recebe permissão para excluir documentos.
    @Transactional(readOnly = true)
    public Etapa buscarEtapaComAcessoAosDocumentos(Long id) {

        Etapa etapa = etapaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Etapa não encontrada"
                        )
                );

        Trabalho trabalho = etapa.getTrabalho();
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        boolean criador = trabalho.getCriadoPor().getId()
                .equals(usuario.getId());

        boolean responsavel = etapa.getResponsavel() != null
                && etapa.getResponsavel().getId().equals(usuario.getId())
                && mesmaEmpresa(usuario, trabalho);

        if (!criador && !responsavel) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar os documentos desta etapa."
            );
        }

        return etapa;
    }


    private EtapaRespostaDTO converterParaResposta(Etapa etapa) {

        EtapaRespostaDTO resposta = new EtapaRespostaDTO();

        resposta.setId(etapa.getId());
        resposta.setTitulo(etapa.getTitulo());
        resposta.setDescricao(etapa.getDescricao());
        resposta.setStatus(etapa.getStatus());
        resposta.setPercentualConclusao(etapa.getPercentualConclusao());
        resposta.setOrdem(etapa.getOrdem());
        resposta.setDataCriacao(etapa.getDataCriacao());
        resposta.setDataAtualizacao(etapa.getDataAtualizacao());
        resposta.setTrabalhoId(etapa.getTrabalho().getId());

        if (etapa.getResponsavel() != null) {
            resposta.setResponsavelId(etapa.getResponsavel().getId());
            resposta.setResponsavelNome(etapa.getResponsavel().getNome());
        }
        eventoTrabalhoRepository
                .findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
                        etapa.getId(),
                        TipoEventoTrabalho.OBSERVACAO_REGISTRADA
                )
                .ifPresent(evento -> {
                    resposta.setObservacaoRecente(evento.getDescricao());
                    resposta.setDataObservacao(evento.getDataEvento());
                    resposta.setAutorObservacao(evento.getUsuario().getNome());
                });
        return resposta;
    }

    private HistoricoEtapaRespostaDTO converterHistoricoParaResposta(
            HistoricoEtapa historico) {

        HistoricoEtapaRespostaDTO resposta = new HistoricoEtapaRespostaDTO();

        resposta.setId(historico.getId());
        resposta.setStatusAnterior(historico.getStatusAnterior());
        resposta.setStatusNovo(historico.getStatusNovo());
        resposta.setDataAlteracao(historico.getDataAlteracao());
        resposta.setUsuarioId(historico.getUsuario().getId());
        resposta.setUsuarioNome(historico.getUsuario().getNome());

        return resposta;
    }

    @Transactional
    public EtapaRespostaDTO registrarObservacao(
            Long id,
            ObservacaoEtapaDTO dto) {

        Etapa etapa = buscarEtapaComAcessoOperacional(id);

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        EventoTrabalho evento = new EventoTrabalho();

        evento.setTipo(TipoEventoTrabalho.OBSERVACAO_REGISTRADA);
        evento.setDescricao(dto.getTexto().trim());
        evento.setTrabalho(etapa.getTrabalho());
        evento.setEtapa(etapa);
        evento.setUsuario(usuario);

        eventoTrabalhoRepository.save(evento);

        return converterParaResposta(etapa);
    }
}