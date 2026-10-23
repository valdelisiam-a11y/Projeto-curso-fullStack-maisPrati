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

    public TrabalhoService(
            TrabalhoRepository trabalhoRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            HistoricoTrabalhoRepository historicoTrabalhoRepository,
            UsuarioTrabalhoRepository usuarioTrabalhoRepository,
            UsuarioAutenticadoService usuarioAutenticadoService) {

        this.trabalhoRepository = trabalhoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoTrabalhoRepository = historicoTrabalhoRepository;
        this.usuarioTrabalhoRepository = usuarioTrabalhoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    public TrabalhoRespostaDTO cadastrar(TrabalhoCadastroDTO dto) {

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        if (usuario.getEmpresa() == null ||
        !usuario.getEmpresa().getId().equals(empresa.getId())) {
        throw new UsuarioEmpresaDiferenteException(
            "Usuário não pertence à empresa informada para o trabalho."
    );
}

Trabalho trabalho = new Trabalho();

        trabalho.setTitulo(dto.getTitulo());
        trabalho.setDescricao(dto.getDescricao());
        trabalho.setStatus(StatusTrabalho.PENDENTE);
        trabalho.setDataCriacao(LocalDateTime.now());
        trabalho.setDataAtualizacao(LocalDateTime.now());
        trabalho.setEmpresa(empresa);
        trabalho.setCriadoPor(usuario);

        Trabalho trabalhoSalvo = trabalhoRepository.save(trabalho);

        return converterParaResposta(trabalhoSalvo);
    }

    public List<TrabalhoRespostaDTO> listarTodos() {
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        return trabalhoRepository.findAcessiveisPorUsuario(usuario)
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

        return trabalhoRepository.findAcessiveisPorUsuario(usuario)
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

        boolean criador = trabalho.getCriadoPor().getId().equals(usuario.getId());

        boolean participante = usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .isPresent();

        if (!criador && !participante) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar este trabalho.");
        }

        return trabalho;
    }

    private TrabalhoRespostaDTO converterParaResposta(Trabalho trabalho) {

        TrabalhoRespostaDTO resposta = new TrabalhoRespostaDTO();

        resposta.setId(trabalho.getId());
        resposta.setTitulo(trabalho.getTitulo());
        resposta.setDescricao(trabalho.getDescricao());
        resposta.setStatus(trabalho.getStatus());
        resposta.setDataCriacao(trabalho.getDataCriacao());
        resposta.setDataAtualizacao(trabalho.getDataAtualizacao());
        resposta.setDataConclusao(trabalho.getDataConclusao());
        resposta.setEmpresaId(trabalho.getEmpresa().getId());
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