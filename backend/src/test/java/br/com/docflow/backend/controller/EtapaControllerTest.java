
package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.EtapaAtualizacaoDTO;
import br.com.docflow.backend.dto.EtapaCadastroDTO;
import br.com.docflow.backend.dto.EtapaOrdemDTO;
import br.com.docflow.backend.dto.EtapaPercentualDTO;
import br.com.docflow.backend.dto.EtapaRespostaDTO;
import br.com.docflow.backend.dto.EtapaResponsavelDTO;
import br.com.docflow.backend.dto.EtapaStatusDTO;
import br.com.docflow.backend.dto.HistoricoEtapaRespostaDTO;
import br.com.docflow.backend.dto.MinhaEtapaRespostaDTO;
import br.com.docflow.backend.dto.ObservacaoEtapaDTO;
import br.com.docflow.backend.service.EtapaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtapaControllerTest {

    @Mock
    private EtapaService etapaService;

    @InjectMocks
    private EtapaController etapaController;

    @Mock
    private EtapaRespostaDTO etapaResposta;

    @Mock
    private MinhaEtapaRespostaDTO minhaEtapaResposta;

    @Mock
    private HistoricoEtapaRespostaDTO historicoResposta;

    @Mock
    private EtapaCadastroDTO cadastroDTO;

    @Mock
    private EtapaAtualizacaoDTO atualizacaoDTO;

    @Mock
    private EtapaStatusDTO statusDTO;

    @Mock
    private EtapaPercentualDTO percentualDTO;

    @Mock
    private EtapaOrdemDTO ordemDTO;

    @Mock
    private EtapaResponsavelDTO responsavelDTO;

    @Mock
    private ObservacaoEtapaDTO observacaoDTO;

    @Test
    void deveCadastrarEtapaERetornarCreated() {

        when(etapaService.cadastrar(1L, cadastroDTO))
                .thenReturn(etapaResposta);

        ResponseEntity<EtapaRespostaDTO> resposta =
                etapaController.cadastrar(1L, cadastroDTO);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertSame(etapaResposta, resposta.getBody());

        verify(etapaService).cadastrar(1L, cadastroDTO);
    }

    @Test
    void deveListarEtapasPorTrabalhoERetornarOk() {

        List<EtapaRespostaDTO> etapas = List.of(etapaResposta);

        when(etapaService.listarPorTrabalho(1L))
                .thenReturn(etapas);

        ResponseEntity<List<EtapaRespostaDTO>> resposta =
                etapaController.listarPorTrabalho(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(etapas, resposta.getBody());

        verify(etapaService).listarPorTrabalho(1L);
    }

    @Test
    void deveListarMinhasEtapasERetornarOk() {

        List<MinhaEtapaRespostaDTO> etapas =
                List.of(minhaEtapaResposta);

        when(etapaService.listarMinhasEtapas(1L))
                .thenReturn(etapas);

        ResponseEntity<List<MinhaEtapaRespostaDTO>> resposta =
                etapaController.listarMinhasEtapas(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(etapas, resposta.getBody());

        verify(etapaService).listarMinhasEtapas(1L);
    }

    @Test
    void deveBuscarEtapaPorIdERetornarOk() {

        when(etapaService.buscarPorId(1L))
                .thenReturn(etapaResposta);

        ResponseEntity<EtapaRespostaDTO> resposta =
                etapaController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(etapaResposta, resposta.getBody());

        verify(etapaService).buscarPorId(1L);
    }

    @Test
    void deveAtualizarEtapaERetornarOk() {

        when(etapaService.atualizar(1L, atualizacaoDTO))
                .thenReturn(etapaResposta);

        ResponseEntity<EtapaRespostaDTO> resposta =
                etapaController.atualizar(1L, atualizacaoDTO);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(etapaResposta, resposta.getBody());

        verify(etapaService).atualizar(1L, atualizacaoDTO);
    }

    @Test
    void deveAlterarStatusDaEtapaERetornarOk() {

        when(etapaService.alterarStatus(1L, statusDTO))
                .thenReturn(etapaResposta);

        ResponseEntity<EtapaRespostaDTO> resposta =
                etapaController.alterarStatus(1L, statusDTO);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(etapaResposta, resposta.getBody());

        verify(etapaService).alterarStatus(1L, statusDTO);
    }

    @Test
    void deveAtualizarPercentualDaEtapaERetornarOk() {

        when(etapaService.atualizarPercentual(1L, percentualDTO))
                .thenReturn(etapaResposta);

        ResponseEntity<EtapaRespostaDTO> resposta =
                etapaController.atualizarPercentual(1L, percentualDTO);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(etapaResposta, resposta.getBody());

        verify(etapaService).atualizarPercentual(1L, percentualDTO);
    }

    @Test
    void deveAlterarOrdemDaEtapaERetornarOk() {

        when(etapaService.alterarOrdem(1L, ordemDTO))
                .thenReturn(etapaResposta);

        ResponseEntity<EtapaRespostaDTO> resposta =
                etapaController.alterarOrdem(1L, ordemDTO);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(etapaResposta, resposta.getBody());

        verify(etapaService).alterarOrdem(1L, ordemDTO);
    }

    @Test
    void deveDefinirResponsavelDaEtapaERetornarOk() {

        when(etapaService.definirResponsavel(1L, responsavelDTO))
                .thenReturn(etapaResposta);

        ResponseEntity<EtapaRespostaDTO> resposta =
                etapaController.definirResponsavel(1L, responsavelDTO);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(etapaResposta, resposta.getBody());

        verify(etapaService).definirResponsavel(1L, responsavelDTO);
    }

    @Test
    void deveListarHistoricoDaEtapaERetornarOk() {

        List<HistoricoEtapaRespostaDTO> historico =
                List.of(historicoResposta);

        when(etapaService.listarHistorico(1L))
                .thenReturn(historico);

        ResponseEntity<List<HistoricoEtapaRespostaDTO>> resposta =
                etapaController.listarHistorico(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(historico, resposta.getBody());

        verify(etapaService).listarHistorico(1L);
    }

    @Test
    void deveRegistrarObservacaoERetornarCreated() {

        when(etapaService.registrarObservacao(1L, observacaoDTO))
                .thenReturn(etapaResposta);

        ResponseEntity<EtapaRespostaDTO> resposta =
                etapaController.registrarObservacao(1L, observacaoDTO);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertSame(etapaResposta, resposta.getBody());

        verify(etapaService).registrarObservacao(1L, observacaoDTO);
    }

    @Test
    void deveExcluirEtapaERetornarNoContent() {

        ResponseEntity<Void> resposta =
                etapaController.excluir(1L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());

        verify(etapaService).excluir(1L);
    }
}
