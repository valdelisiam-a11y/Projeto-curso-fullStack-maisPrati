package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.TrabalhoAtualizacaoDTO;
import br.com.docflow.backend.dto.TrabalhoCadastroDTO;
import br.com.docflow.backend.dto.TrabalhoRespostaDTO;
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

import java.util.List;



@Service
public class TrabalhoService {

    private final TrabalhoRepository trabalhoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoTrabalhoRepository historicoTrabalhoRepository;

    public TrabalhoService(
            TrabalhoRepository trabalhoRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            HistoricoTrabalhoRepository historicoTrabalhoRepository) {

        this.trabalhoRepository = trabalhoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoTrabalhoRepository = historicoTrabalhoRepository;
    }

    public TrabalhoRespostaDTO cadastrar(TrabalhoCadastroDTO dto) {

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        Usuario usuario = usuarioRepository.findById(dto.getCriadoPorId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Trabalho trabalho = new Trabalho();

        trabalho.setTitulo(dto.getTitulo());
        trabalho.setDescricao(dto.getDescricao());
        trabalho.setStatus(StatusTrabalho.PENDENTE);
        trabalho.setEmpresa(empresa);
        trabalho.setCriadoPor(usuario);

        Trabalho trabalhoSalvo = trabalhoRepository.save(trabalho);

        return converterParaResposta(trabalhoSalvo);
    }

    public List<TrabalhoRespostaDTO> listarTodos() {

        return trabalhoRepository.findAll()
                .stream()
                .map(this::converterParaResposta)
                .toList();
    }

    public TrabalhoRespostaDTO buscarPorId(Long id) {

        Trabalho trabalho = trabalhoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));

        return converterParaResposta(trabalho);
    }

    public List<TrabalhoRespostaDTO> buscarPorTitulo(String titulo) {

        return trabalhoRepository.findByTituloContainingIgnoreCase(titulo)
                .stream()
                .map(this::converterParaResposta)
                .toList();
    }

    public TrabalhoRespostaDTO atualizar(Long id, TrabalhoAtualizacaoDTO dto) {

        Trabalho trabalho = trabalhoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));

        trabalho.setTitulo(dto.getTitulo());
        trabalho.setDescricao(dto.getDescricao());

        Trabalho trabalhoAtualizado = trabalhoRepository.save(trabalho);

        return converterParaResposta(trabalhoAtualizado);
    }

    public void excluir(Long id) {

        Trabalho trabalho = trabalhoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));

        trabalhoRepository.delete(trabalho);
    }

    public TrabalhoRespostaDTO alterarStatus(Long id, TrabalhoStatusDTO dto) {

        Trabalho trabalho = trabalhoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        trabalho.setStatus(dto.getStatus());

        if (dto.getStatus() == StatusTrabalho.CONCLUIDO) {
            trabalho.setDataConclusao(java.time.LocalDateTime.now());
        }

        Trabalho trabalhoAtualizado = trabalhoRepository.save(trabalho);

        HistoricoTrabalho historico = new HistoricoTrabalho();

        historico.setStatus(dto.getStatus());
        historico.setDataAlteracao(java.time.LocalDateTime.now());
        historico.setTrabalho(trabalhoAtualizado);
        historico.setUsuario(usuario);

        historicoTrabalhoRepository.save(historico);

        return converterParaResposta(trabalhoAtualizado);
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
}