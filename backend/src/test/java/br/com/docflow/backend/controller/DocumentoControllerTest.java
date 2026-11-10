
package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.DocumentoRespostaDTO;
import br.com.docflow.backend.service.DocumentoService;
import br.com.docflow.backend.service.DocumentoService.DocumentoDownload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentoControllerTest {

    @Mock
    private DocumentoService documentoService;

    @InjectMocks
    private DocumentoController documentoController;

    @Mock
    private MultipartFile arquivo;

    @Mock
    private DocumentoRespostaDTO documentoResposta;

    @Mock
    private DocumentoDownload documentoDownload;

    @Test
    void deveEnviarDocumento() {
        when(documentoService.enviar(1L, arquivo))
                .thenReturn(documentoResposta);

        ResponseEntity<DocumentoRespostaDTO> resposta =
                documentoController.enviar(1L, arquivo);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertSame(documentoResposta, resposta.getBody());

        verify(documentoService).enviar(1L, arquivo);
    }

    @Test
    void deveListarDocumentosDaEtapa() {
        List<DocumentoRespostaDTO> documentos =
                List.of(documentoResposta);

        when(documentoService.listar(1L))
                .thenReturn(documentos);

        ResponseEntity<List<DocumentoRespostaDTO>> resposta =
                documentoController.listar(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(documentos, resposta.getBody());

        verify(documentoService).listar(1L);
    }

    @Test
    void deveBaixarDocumento() {
        byte[] conteudo =
                "Conteudo do documento".getBytes(StandardCharsets.UTF_8);

        when(documentoService.baixar(1L, 2L))
                .thenReturn(documentoDownload);

        when(documentoDownload.getNomeOriginal())
                .thenReturn("contrato.pdf");

        when(documentoDownload.getContentType())
                .thenReturn("application/pdf");

        when(documentoDownload.getConteudo())
                .thenReturn(conteudo);

        ResponseEntity<byte[]> resposta =
                documentoController.baixar(1L, 2L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertSame(conteudo, resposta.getBody());
        assertEquals(
                MediaType.APPLICATION_PDF,
                resposta.getHeaders().getContentType()
        );
        assertEquals(
                conteudo.length,
                resposta.getHeaders().getContentLength()
        );
        assertEquals(
                "attachment; filename=\"contrato.pdf\"; filename*=UTF-8''contrato.pdf",
                resposta.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
        );

        verify(documentoService).baixar(1L, 2L);
    }

    @Test
    void deveExcluirDocumento() {
        ResponseEntity<Void> resposta =
                documentoController.excluir(1L, 2L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());

        verify(documentoService).excluir(1L, 2L);
    }
}
