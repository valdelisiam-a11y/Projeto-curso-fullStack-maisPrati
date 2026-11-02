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

    public TrabalhoService(
            TrabalhoRepository trabalhoRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            HistoricoTrabalhoRepository historicoTrabalhoRepository,
            UsuarioTrabalhoRepository usuarioTrabalhoRepository,
            UsuarioAutenticadoService usuarioAutenticadoService,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            EtapaRepository etapaRepository) {

        this.trabalhoRepository = trabalhoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoTrabalhoRepository = historicoTrabalhoRepository;
        this.usuarioTrabalhoRepository = usuarioTrabalhoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.etapaRepository = etapaRepository;
    }

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

    public TrabalhoRespostaDTO atualizar(Long id, TrabalhoAtualizacaoDTO dto) {

        Trabalho trabalho = validarAcesso(id);
        trabalho.setTitulo(dto.getTitulo());
        trabalho.setDescricao(dto.getDescricao());
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

        return historicoTrabalhoRepository
                .findByTrabalhoIdOrderByDataAlteracaoDesc(trabalho.getId())
                .stream()
                .map(this::converterHistoricoParaResposta)
                .toList();
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

        Trabalho trabalho = validarAcesso(trabalhoId);

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

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

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

        return resposta;
    }

    private HistoricoTrabalhoRespostaDTO converterHistoricoParaResposta(HistoricoTrabalho historico) {

        HistoricoTrabalhoRespostaDTO resposta = new HistoricoTrabalhoRespostaDTO();

        resposta.setId(historico.getId());
        resposta.setStatusAnterior(historico.getStatusAnterior());
        resposta.setStatusNovo(historico.getStatusNovo());
        resposta.setDataAlteracao(historico.getDataAlteracao());
        resposta.setUsuarioId(historico.getUsuario().getId());
        resposta.setUsuarioNome(historico.getUsuario().getNome());

        return resposta;
    }
}