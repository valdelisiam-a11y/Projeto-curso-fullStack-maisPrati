
package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UsuarioAutenticadoServiceTest {

    private UsuarioAutenticadoService usuarioAutenticadoService;

    @BeforeEach
    void configurar() {
        usuarioAutenticadoService = new UsuarioAutenticadoService();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveRetornarUsuarioQuandoEstiverAutenticado() {

        Usuario usuario = mock(Usuario.class);

        var autenticacao =
                UsernamePasswordAuthenticationToken.authenticated(
                        usuario,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacao);

        Usuario resultado = usuarioAutenticadoService.obterUsuario();

        assertSame(usuario, resultado);
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverAutenticacao() {

        SecurityContextHolder.clearContext();

        assertThrows(
                UsuarioNaoAutenticadoException.class,
                () -> usuarioAutenticadoService.obterUsuario()
        );
    }

    @Test
    void deveLancarExcecaoQuandoAutenticacaoNaoEstiverValidada() {

        var autenticacao =
                new UsernamePasswordAuthenticationToken(
                        "usuario@docflow.com",
                        "senha"
                );

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacao);

        assertFalse(autenticacao.isAuthenticated());

        assertThrows(
                UsuarioNaoAutenticadoException.class,
                () -> usuarioAutenticadoService.obterUsuario()
        );
    }

    @Test
    void deveLancarExcecaoQuandoPrincipalNaoForUsuario() {

        var autenticacao =
                UsernamePasswordAuthenticationToken.authenticated(
                        "usuario@docflow.com",
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacao);

        assertThrows(
                UsuarioNaoAutenticadoException.class,
                () -> usuarioAutenticadoService.obterUsuario()
        );
    }
}
