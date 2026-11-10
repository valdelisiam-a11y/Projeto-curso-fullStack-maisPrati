
package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.LoginDTO;
import br.com.docflow.backend.dto.LoginRespostaDTO;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private LoginDTO criarLoginDTO() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("usuario@docflow.com");
        dto.setSenha("senha123");
        return dto;
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("usuario@docflow.com");
        usuario.setSenha("senha-criptografada");
        usuario.setAtivo(true);
        return usuario;
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {
        LoginDTO dto = criarLoginDTO();
        Usuario usuario = criarUsuario();

        when(usuarioRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(dto.getSenha(), usuario.getSenha()))
                .thenReturn(true);
        when(jwtService.gerarToken(usuario))
                .thenReturn("token-teste");

        LoginRespostaDTO resposta = authService.autenticar(dto);

        assertEquals(1L, resposta.getId());
        assertEquals("Usuário Teste", resposta.getNome());
        assertEquals("usuario@docflow.com", resposta.getEmail());
        assertEquals("token-teste", resposta.getToken());

        verify(jwtService).gerarToken(usuario);
    }

    @Test
    void deveRejeitarLoginQuandoEmailNaoEstiverCadastrado() {
        LoginDTO dto = criarLoginDTO();

        when(usuarioRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.autenticar(dto)
        );

        assertEquals(
                "E-mail ou senha inválidos.",
                exception.getMessage()
        );

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void deveRejeitarLoginQuandoSenhaEstiverIncorreta() {
        LoginDTO dto = criarLoginDTO();
        Usuario usuario = criarUsuario();

        when(usuarioRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(dto.getSenha(), usuario.getSenha()))
                .thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.autenticar(dto)
        );

        assertEquals(
                "E-mail ou senha inválidos.",
                exception.getMessage()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void deveRejeitarLoginQuandoUsuarioEstiverInativo() {
        LoginDTO dto = criarLoginDTO();
        Usuario usuario = criarUsuario();
        usuario.setAtivo(false);

        when(usuarioRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(dto.getSenha(), usuario.getSenha()))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.autenticar(dto)
        );

        assertEquals("Usuário inativo.", exception.getMessage());

        verifyNoInteractions(jwtService);
    }
}
