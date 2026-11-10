package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.*;
import br.com.docflow.backend.service.TrabalhoService;
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
class TrabalhoControllerTest {

    @Mock
    private TrabalhoService trabalhoService;

    @InjectMocks
    private TrabalhoController trabalhoController;

    @Mock
    private TrabalhoCadastroDTO cadastroDTO;

    @Mock
    private TrabalhoAtualizacaoDTO atualizacaoDTO;

    @Mock
    private TrabalhoStatusDTO statusDTO;

    @Mock
    private TrabalhoRespostaDTO trabalhoResposta;

    @Mock
    private HistoricoTrabalhoRespostaDTO historicoResposta;

    @Mock
    private LinhaDoTempoRespostaDTO linhaDoTempoResposta;

    @Test
    void deveCadastrarTrabalho() {
        when(trabalhoService.cadastrar(cadastroDTO))
                .thenReturn(trabalhoResposta);

        ResponseEntity<TrabalhoRespostaDTO> resposta =
                trabalhoController.cadastrar(cadastroDTO);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertSame(trabalhoResposta, resposta.getBody());
        verify(trabalhoService).cadastrar(cadastroDTO);
    }

    @Test
    void deveListarTodosOsTrabalhos() {
        List<TrabalhoRespostaDTO> trabalhos =
                List.of(trabalhoResposta);

        when(trabalhoService.listarTodos()).thenReturn(trabalhos);

        ResponseEntity<List<TrabalhoRespostaDTO>> resposta =
                trabalhoController.listarTodos();

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(trabalhos, resposta.getBody());
        verify(trabalhoService).listarTodos();
    }

    @Test
    void deveBuscarTrabalhoPorId() {
        when(trabalhoService.buscarPorId(1L))
                .thenReturn(trabalhoResposta);

        ResponseEntity<TrabalhoRespostaDTO> resposta =
                trabalhoController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(trabalhoResposta, resposta.getBody());
        verify(trabalhoService).buscarPorId(1L);
    }

    @Test
    void deveBuscarTrabalhosPorTitulo() {
        List<TrabalhoRespostaDTO> trabalhos =
                List.of(trabalhoResposta);

        when(trabalhoService.buscarPorTitulo("Projeto"))
                .thenReturn(trabalhos);

        ResponseEntity<List<TrabalhoRespostaDTO>> resposta =
                trabalhoController.buscarPorTitulo("Projeto");

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(trabalhos, resposta.getBody());
        verify(trabalhoService).buscarPorTitulo("Projeto");
    }

    @Test
    void deveAtualizarTrabalho() {
        when(trabalhoService.atualizar(1L, atualizacaoDTO))
                .thenReturn(trabalhoResposta);

        ResponseEntity<TrabalhoRespostaDTO> resposta =
                trabalhoController.atualizar(1L, atualizacaoDTO);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(trabalhoResposta, resposta.getBody());
        verify(trabalhoService).atualizar(1L, atualizacaoDTO);
    }

    @Test
    void deveAlterarStatusDoTrabalho() {
        when(trabalhoService.alterarStatus(1L, statusDTO))
                .thenReturn(trabalhoResposta);

        ResponseEntity<TrabalhoRespostaDTO> resposta =
                trabalhoController.alterarStatus(1L, statusDTO);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(trabalhoResposta, resposta.getBody());
        verify(trabalhoService).alterarStatus(1L, statusDTO);
    }

    @Test
    void deveListarHistoricoDoTrabalho() {
        List<HistoricoTrabalhoRespostaDTO> historico =
                List.of(historicoResposta);

        when(trabalhoService.listarHistorico(1L))
                .thenReturn(historico);

        ResponseEntity<List<HistoricoTrabalhoRespostaDTO>> resposta =
                trabalhoController.listarHistorico(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(historico, resposta.getBody());
        verify(trabalhoService).listarHistorico(1L);
    }

    @Test
    void deveListarLinhaDoTempoDoTrabalho() {
        List<LinhaDoTempoRespostaDTO> linhaDoTempo =
                List.of(linhaDoTempoResposta);

        when(trabalhoService.listarLinhaDoTempo(1L))
                .thenReturn(linhaDoTempo);

        ResponseEntity<List<LinhaDoTempoRespostaDTO>> resposta =
                trabalhoController.listarLinhaDoTempo(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(linhaDoTempo, resposta.getBody());
        verify(trabalhoService).listarLinhaDoTempo(1L);
    }

    @Test
    void deveExcluirTrabalho() {
        ResponseEntity<Void> resposta =
                trabalhoController.excluir(1L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        verify(trabalhoService).excluir(1L);
    }
}