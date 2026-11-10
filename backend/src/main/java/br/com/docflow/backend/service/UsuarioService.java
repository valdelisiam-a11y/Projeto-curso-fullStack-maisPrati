
package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.CadastroUsuarioDTO;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import br.com.docflow.backend.dto.AtualizarPerfilDTO;
import br.com.docflow.backend.dto.AtualizarPerfilRespostaDTO;

import java.time.LocalDateTime;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Usuario cadastrar(CadastroUsuarioDTO dto) {

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EmailJaCadastradoException("E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario();

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setDataCadastro(LocalDateTime.now());
        usuario.setAtivo(true);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void alterarSenha(
            Usuario usuario,
            String senhaAtual,
            String novaSenha) {

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new AccessDeniedException("A senha atual está incorreta.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public AtualizarPerfilRespostaDTO atualizarPerfil(
            Usuario usuario,
            AtualizarPerfilDTO dto) {

        usuarioRepository.findByEmail(dto.getEmail())
                .ifPresent(usuarioExistente -> {
                    if (!usuarioExistente.getId().equals(usuario.getId())) {
                        throw new EmailJaCadastradoException(
                                "E-mail já cadastrado."
                        );
                    }
                });

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        String novoToken = jwtService.gerarToken(usuarioAtualizado);

        return new AtualizarPerfilRespostaDTO(
                usuarioAtualizado.getId(),
                usuarioAtualizado.getNome(),
                usuarioAtualizado.getEmail(),
                novoToken
        );
    }
}
