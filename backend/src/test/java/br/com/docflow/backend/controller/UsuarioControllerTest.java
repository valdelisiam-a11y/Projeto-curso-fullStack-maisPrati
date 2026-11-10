
package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.AtualizarPerfilDTO;
import br.com.docflow.backend.dto.AtualizarPerfilRespostaDTO;
import br.com.docflow.backend.dto.CadastroUsuarioDTO;
import br.com.docflow.backend.dto.UsuarioRespostaDTO;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.service.EmailJaCadastradoException;
import br.com.docflow.backend.service.UsuarioAutenticadoService;
import br.com.docflow.backend.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void deveCadastrarUsuarioComSucesso() {
        CadastroUsuarioDTO dto = mock(CadastroUsuarioDTO.class);
        Usuario usuario = mock(Usuario.class);

        when(usuarioService.cadastrar(dto)).thenReturn(usuario);
        when(usuario.getId()).thenReturn(1L);
        when(usuario.getNome()).thenReturn("Maria Silva");
        when(usuario.getEmail()).thenReturn("maria@email.com");

        ResponseEntity<UsuarioRespostaDTO> resposta =
                usuarioController.cadastrar(dto);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(1L, resposta.getBody().getId());
        assertEquals("Maria Silva", resposta.getBody().getNome());
        assertEquals("maria@email.com", resposta.getBody().getEmail());

        verify(usuarioService).cadastrar(dto);
    }

    @Test
    void deveRetornarConflitoQuandoEmailJaEstiverCadastrado() {
        CadastroUsuarioDTO dto = mock(CadastroUsuarioDTO.class);

        when(usuarioService.cadastrar(dto))
                .thenThrow(new EmailJaCadastradoException("E-mail já cadastrado"));

        ResponseEntity<UsuarioRespostaDTO> resposta =
                usuarioController.cadastrar(dto);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        verify(usuarioService).cadastrar(dto);
    }

    @Test
    void deveAtualizarPerfilDoUsuarioAutenticado() {
        AtualizarPerfilDTO dto = mock(AtualizarPerfilDTO.class);
        Usuario usuario = mock(Usuario.class);
        AtualizarPerfilRespostaDTO respostaEsperada =
                mock(AtualizarPerfilRespostaDTO.class);

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(usuarioService.atualizarPerfil(usuario, dto))
                .thenReturn(respostaEsperada);

        ResponseEntity<AtualizarPerfilRespostaDTO> resposta =
                usuarioController.atualizarPerfil(dto);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(respostaEsperada, resposta.getBody());

        verify(usuarioAutenticadoService).obterUsuario();
        verify(usuarioService).atualizarPerfil(usuario, dto);
    }
}
