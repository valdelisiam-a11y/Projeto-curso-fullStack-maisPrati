package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Usuario usuario;

    private final String secret =
            "docflow-chave-secreta-para-testes-com-mais-de-32-caracteres";

    @BeforeEach
    void configurar() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", 60000L);

        usuario = new Usuario();
        usuario.setEmail("usuario@docflow.com");
    }

    @Test
    void deveGerarTokenComEmailDoUsuario() {
        String token = jwtService.gerarToken(usuario);

        assertNotNull(token);
        assertEquals("usuario@docflow.com", jwtService.extrairEmail(token));
    }

    @Test
    void deveExtrairEmailDeTokenValido() {
        String token = jwtService.gerarToken(usuario);

        String email = jwtService.extrairEmail(token);

        assertEquals(usuario.getEmail(), email);
    }

    @Test
    void deveValidarTokenQuandoEmailCorrespondeAoUsuario() {
        String token = jwtService.gerarToken(usuario);

        boolean valido = jwtService.validarToken(token, usuario);

        assertTrue(valido);
    }

    @Test
    void naoDeveValidarTokenQuandoEmailNaoCorrespondeAoUsuario() {
        String token = jwtService.gerarToken(usuario);

        Usuario outroUsuario = new Usuario();
        outroUsuario.setEmail("outro@docflow.com");

        boolean valido = jwtService.validarToken(token, outroUsuario);

        assertFalse(valido);
    }

    @Test
    void deveRejeitarTokenExpirado() {
        SecretKey key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        Date agora = new Date();

        String tokenExpirado = Jwts.builder()
                .subject(usuario.getEmail())
                .issuedAt(new Date(agora.getTime() - 10000))
                .expiration(new Date(agora.getTime() - 5000))
                .signWith(key)
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.extrairEmail(tokenExpirado)
        );
    }
}