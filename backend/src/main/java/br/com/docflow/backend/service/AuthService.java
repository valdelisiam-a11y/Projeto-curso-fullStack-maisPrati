package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.LoginDTO;
import br.com.docflow.backend.dto.LoginRespostaDTO;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginRespostaDTO autenticar(LoginDTO dto) {

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("E-mail ou senha inválidos.");
        }

        if (!usuario.getAtivo()) {
            throw new RuntimeException("Usuário inativo.");
        }

        String token = jwtService.gerarToken(usuario);

        return new LoginRespostaDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                token
        );
    }
}
