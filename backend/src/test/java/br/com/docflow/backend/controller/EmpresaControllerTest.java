
package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.AdicionarMembroDTO;
import br.com.docflow.backend.dto.CadastroEmpresaDTO;
import br.com.docflow.backend.dto.EmpresaResumoDTO;
import br.com.docflow.backend.dto.MembroEmpresaDTO;
import br.com.docflow.backend.service.UsuarioEmpresaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaControllerTest {

    @Mock
    private UsuarioEmpresaService usuarioEmpresaService;

    @InjectMocks
    private EmpresaController empresaController;

    @Test
    void deveAdicionarMembro() {
        Long empresaId = 1L;
        AdicionarMembroDTO dto = mock(AdicionarMembroDTO.class);

        when(dto.getEmail()).thenReturn("membro@email.com");

        ResponseEntity<Void> resposta =
                empresaController.adicionarMembro(empresaId, dto);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        verify(usuarioEmpresaService)
                .adicionarMembro(empresaId, "membro@email.com");
    }

    @Test
    void deveRemoverMembro() {
        Long empresaId = 1L;
        Long usuarioId = 2L;

        ResponseEntity<Void> resposta =
                empresaController.removerMembro(empresaId, usuarioId);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        verify(usuarioEmpresaService)
                .removerMembro(empresaId, usuarioId);
    }

    @Test
    void deveCadastrarEmpresa() {
        CadastroEmpresaDTO dto = mock(CadastroEmpresaDTO.class);

        when(dto.getNome()).thenReturn("Empresa Teste");
        when(dto.getCnpj()).thenReturn("12345678000199");

        ResponseEntity<Void> resposta =
                empresaController.cadastrarEmpresa(dto);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        verify(usuarioEmpresaService)
                .cadastrarEmpresa("Empresa Teste", "12345678000199");
    }

    @Test
    void deveListarMinhasEmpresas() {
        List<EmpresaResumoDTO> empresas =
                List.of(mock(EmpresaResumoDTO.class));

        when(usuarioEmpresaService.listarMinhasEmpresas())
                .thenReturn(empresas);

        ResponseEntity<List<EmpresaResumoDTO>> resposta =
                empresaController.listarMinhasEmpresas();

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(empresas, resposta.getBody());
        verify(usuarioEmpresaService).listarMinhasEmpresas();
    }

    @Test
    void deveListarMembros() {
        Long empresaId = 1L;
        List<MembroEmpresaDTO> membros =
                List.of(mock(MembroEmpresaDTO.class));

        when(usuarioEmpresaService.listarMembros(empresaId))
                .thenReturn(membros);

        ResponseEntity<List<MembroEmpresaDTO>> resposta =
                empresaController.listarMembros(empresaId);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(membros, resposta.getBody());
        verify(usuarioEmpresaService).listarMembros(empresaId);
    }

    @Test
    void deveListarMembrosDisponiveis() {
        Long empresaId = 1L;
        List<MembroEmpresaDTO> membros =
                List.of(mock(MembroEmpresaDTO.class));

        when(usuarioEmpresaService.listarMembrosDisponiveis(empresaId))
                .thenReturn(membros);

        ResponseEntity<List<MembroEmpresaDTO>> resposta =
                empresaController.listarMembrosDisponiveis(empresaId);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(membros, resposta.getBody());
        verify(usuarioEmpresaService)
                .listarMembrosDisponiveis(empresaId);
    }
}
