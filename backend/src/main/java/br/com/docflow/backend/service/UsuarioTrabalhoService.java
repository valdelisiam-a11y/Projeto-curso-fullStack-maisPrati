
package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.UsuarioTrabalhoRespostaDTO;
import br.com.docflow.backend.entity.TipoTrabalho;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.entity.UsuarioTrabalho;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import br.com.docflow.backend.repository.UsuarioTrabalhoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioTrabalhoService {

    private final UsuarioTrabalhoRepository usuarioTrabalhoRepository;
    private final TrabalhoRepository trabalhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public UsuarioTrabalhoService(
            UsuarioTrabalhoRepository usuarioTrabalhoRepository,
            TrabalhoRepository trabalhoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            UsuarioAutenticadoService usuarioAutenticadoService) {

        this.usuarioTrabalhoRepository = usuarioTrabalhoRepository;
        this.trabalhoRepository = trabalhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public UsuarioTrabalhoRespostaDTO adicionarParticipante(
            Long trabalhoId, Long usuarioId) {

        Usuario autenticado = usuarioAutenticadoService.obterUsuario();

        Trabalho trabalho = buscarTrabalho(trabalhoId);

        validarCriador(trabalho, autenticado);
        validarTrabalhoEmpresarial(trabalho);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (!usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                usuario, trabalho.getEmpresa())) {

            throw new UsuarioEmpresaDiferenteException(
                    "O usuário e o trabalho pertencem a empresas diferentes.");
        }

        if (usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario).isPresent()) {

            throw new ParticipanteJaAssociadoException(
                    "Usuário já está associado ao trabalho");
        }

        UsuarioTrabalho usuarioTrabalho = new UsuarioTrabalho();
        usuarioTrabalho.setTrabalho(trabalho);
        usuarioTrabalho.setUsuario(usuario);

        UsuarioTrabalho salvo =
                usuarioTrabalhoRepository.save(usuarioTrabalho);

        return new UsuarioTrabalhoRespostaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioTrabalhoRespostaDTO> listarParticipantes(
            Long trabalhoId) {

        Usuario autenticado = usuarioAutenticadoService.obterUsuario();

        Trabalho trabalho = buscarTrabalho(trabalhoId);

        validarAcesso(trabalho, autenticado);
        validarTrabalhoEmpresarial(trabalho);

        return usuarioTrabalhoRepository.findByTrabalho(trabalho)
                .stream()
                .map(UsuarioTrabalhoRespostaDTO::new)
                .toList();
    }

    @Transactional
    public void removerParticipante(Long trabalhoId, Long usuarioId) {

        Usuario autenticado = usuarioAutenticadoService.obterUsuario();

        Trabalho trabalho = buscarTrabalho(trabalhoId);

        validarCriador(trabalho, autenticado);
        validarTrabalhoEmpresarial(trabalho);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Usuário não encontrado"));

        UsuarioTrabalho participante = usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .orElseThrow(() ->
                        new ParticipanteNaoEncontradoException(
                                "Participante não encontrado para este trabalho"));

        usuarioTrabalhoRepository.delete(participante);
    }

    private Trabalho buscarTrabalho(Long trabalhoId) {
        return trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Trabalho não encontrado"));
    }

    private void validarCriador(Trabalho trabalho, Usuario usuario) {

        if (!trabalho.getCriadoPor().getId().equals(usuario.getId())) {
            throw new AccessDeniedException(
                    "Somente o criador pode gerenciar participantes.");
        }
    }

    private void validarAcesso(Trabalho trabalho, Usuario usuario) {

        boolean criador = trabalho.getCriadoPor().getId()
                .equals(usuario.getId());

        boolean participante = trabalho.getTipo() == TipoTrabalho.EMPRESA
                && usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .isPresent();

        if (!criador && !participante) {
            throw new AccessDeniedException(
                    "Você não tem permissão para acessar os participantes deste trabalho.");
        }
    }

    private void validarTrabalhoEmpresarial(Trabalho trabalho) {

        if (trabalho.getTipo() != TipoTrabalho.EMPRESA
                || trabalho.getEmpresa() == null) {

            throw new IllegalArgumentException(
                    "Esta operação só é permitida para trabalhos empresariais.");
        }
    }
}
