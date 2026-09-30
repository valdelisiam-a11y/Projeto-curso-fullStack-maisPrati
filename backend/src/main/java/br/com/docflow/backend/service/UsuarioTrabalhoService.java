package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.entity.UsuarioTrabalho;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import br.com.docflow.backend.repository.UsuarioTrabalhoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioTrabalhoService {

    @Autowired
    private UsuarioTrabalhoRepository usuarioTrabalhoRepository;

    @Autowired
    private TrabalhoRepository trabalhoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public UsuarioTrabalho adicionarParticipante(Long trabalhoId, Long usuarioId) {
        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Validar se o usuário pertence à mesma empresa do trabalho
        if (usuario.getEmpresa() == null || trabalho.getEmpresa() == null ||
            !usuario.getEmpresa().equals(trabalho.getEmpresa())) {
            throw new RuntimeException("Usuário não pertence à mesma empresa do trabalho");
        }

        // Impedir associação duplicada do mesmo usuário ao mesmo trabalho
        if (usuarioTrabalhoRepository.findByTrabalhoAndUsuario(trabalho, usuario).isPresent()) {
            throw new RuntimeException("Usuário já está associado ao trabalho");
        }

        UsuarioTrabalho usuarioTrabalho = new UsuarioTrabalho();
        usuarioTrabalho.setTrabalho(trabalho);
        usuarioTrabalho.setUsuario(usuario);

        return usuarioTrabalhoRepository.save(usuarioTrabalho);
    }

    public List<UsuarioTrabalho> listarParticipantes(Long trabalhoId) {
        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));
        return usuarioTrabalhoRepository.findByTrabalho(trabalho);
    }

    @Transactional
    public void removerParticipante(Long trabalhoId, Long usuarioId) {
        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        UsuarioTrabalho participante = usuarioTrabalhoRepository.findByTrabalhoAndUsuario(trabalho, usuario)
                .orElseThrow(() -> new RuntimeException("Participante não encontrado para este trabalho"));

        usuarioTrabalhoRepository.delete(participante);
    }
}