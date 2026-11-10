
package br.com.docflow.backend.security;

import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.UsuarioRepository;
import br.com.docflow.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void configurar() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarUsuarioQuandoTokenForValido() throws Exception {

        String token = "token-valido";
        String email = "usuario@docflow.com";

        Usuario usuario = mock(Usuario.class);

        when(jwtService.extrairEmail(token)).thenReturn(email);
        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(usuario));
        when(usuario.getAtivo()).thenReturn(true);
        when(jwtService.validarToken(token, usuario)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNotNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        assertEquals(
                usuario,
                SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoTokenNaoForInformado()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService, usuarioRepository);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoTokenForInvalido()
            throws Exception {

        String token = "token-invalido";

        when(jwtService.extrairEmail(token))
                .thenThrow(new RuntimeException("Token inválido"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoUsuarioNaoForEncontrado()
            throws Exception {

        String token = "token-valido";
        String email = "inexistente@docflow.com";

        when(jwtService.extrairEmail(token)).thenReturn(email);
        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService, never()).validarToken(anyString(), any(Usuario.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveImpedirAutenticacaoQuandoUsuarioEstiverInativo()
            throws Exception {

        String token = "token-valido";
        String email = "usuario@docflow.com";

        Usuario usuario = mock(Usuario.class);

        when(jwtService.extrairEmail(token)).thenReturn(email);
        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(usuario));
        when(usuario.getAtivo()).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService, never()).validarToken(anyString(), any(Usuario.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveImpedirAutenticacaoQuandoTokenNaoForValidado()
            throws Exception {

        String token = "token-rejeitado";
        String email = "usuario@docflow.com";

        Usuario usuario = mock(Usuario.class);

        when(jwtService.extrairEmail(token)).thenReturn(email);
        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(usuario));
        when(usuario.getAtivo()).thenReturn(true);
        when(jwtService.validarToken(token, usuario)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService).validarToken(token, usuario);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void devePreservarAutenticacaoJaExistente()
            throws Exception {

        String token = "token-valido";

        var autenticacaoExistente =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "usuario-ja-autenticado",
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacaoExistente);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertSame(
                autenticacaoExistente,
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService).extrairEmail(token);
        verifyNoInteractions(usuarioRepository);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoEsquemaNaoForBearer()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic credenciais");

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request, response, filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService, usuarioRepository);
        verify(filterChain).doFilter(request, response);
    }
}
