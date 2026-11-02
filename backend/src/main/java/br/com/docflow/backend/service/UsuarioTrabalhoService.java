package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.UsuarioTrabalhoRespostaDTO;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.entity.UsuarioTrabalho;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import br.com.docflow.backend.repository.UsuarioTrabalhoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
import br.com.docflow.backend.entity.TipoTrabalho;

import java.util.List;

@Service
public class UsuarioTrabalhoService {

    @Autowired
    private UsuarioTrabalhoRepository usuarioTrabalhoRepository;

    @Autowired
    private TrabalhoRepository trabalhoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Transactional
    public UsuarioTrabalhoRespostaDTO adicionarParticipante(Long trabalhoId, Long usuarioId) {

        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Trabalhos pessoais não permitem adicionar participantes.
        if (trabalho.getTipo() == TipoTrabalho.PESSOAL) {
            throw new IllegalArgumentException(
                    "Trabalhos pessoais não permitem adicionar participantes."
            );
        }

        // O participante deve pertencer à empresa do trabalho.
        if (trabalho.getEmpresa() == null ||
                !usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                        usuario, trabalho.getEmpresa())) {
            throw new UsuarioEmpresaDiferenteException(
                    "O usuário e o trabalho pertencem a empresas diferentes."
            );
        }

        // Validação de duplicidade (Retorna mensagem padronizada no 409 Conflict)
        if (usuarioTrabalhoRepository.findByTrabalhoAndUsuario(trabalho, usuario).isPresent()) {
            throw new ParticipanteJaAssociadoException("Usuário já está associado ao trabalho");
        }

        UsuarioTrabalho usuarioTrabalho = new UsuarioTrabalho();
        usuarioTrabalho.setTrabalho(trabalho);
        usuarioTrabalho.setUsuario(usuario);

        UsuarioTrabalho salvo = usuarioTrabalhoRepository.save(usuarioTrabalho);

        return new UsuarioTrabalhoRespostaDTO(salvo);
    }

    public List<UsuarioTrabalhoRespostaDTO> listarParticipantes(Long trabalhoId) {

        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));

        return usuarioTrabalhoRepository.findByTrabalho(trabalho)
                .stream()
                .map(UsuarioTrabalhoRespostaDTO::new)
                .toList();
    }

    @Transactional
    public void removerParticipante(Long trabalhoId, Long usuarioId) {

        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UsuarioTrabalho participante = usuarioTrabalhoRepository.findByTrabalhoAndUsuario(trabalho, usuario)
                .orElseThrow(() -> new ParticipanteNaoEncontradoException("Participante não encontrado para este trabalho"));

        usuarioTrabalhoRepository.delete(participante);
    }
}