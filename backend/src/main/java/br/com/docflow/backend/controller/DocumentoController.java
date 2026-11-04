
package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.DocumentoRespostaDTO;
import br.com.docflow.backend.service.DocumentoService;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @PostMapping(
            value = "/etapas/{etapaId}/documentos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoRespostaDTO> enviar(
            @PathVariable Long etapaId,
            @RequestParam("arquivo") MultipartFile arquivo) {

        DocumentoRespostaDTO resposta =
                documentoService.enviar(etapaId, arquivo);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/etapas/{etapaId}/documentos")
    public ResponseEntity<List<DocumentoRespostaDTO>> listar(
            @PathVariable Long etapaId) {

        return ResponseEntity.ok(documentoService.listar(etapaId));
    }

    @GetMapping(
            "/etapas/{etapaId}/documentos/{documentoId}/download")
    public ResponseEntity<byte[]> baixar(
            @PathVariable Long etapaId,
            @PathVariable Long documentoId) {

        DocumentoService.DocumentoDownload documento =
                documentoService.baixar(etapaId, documentoId);

        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(
                                documento.getNomeOriginal(),
                                StandardCharsets.UTF_8)
                        .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(
                MediaType.parseMediaType(documento.getContentType()));
        headers.setContentLength(documento.getConteudo().length);

        return new ResponseEntity<>(
                documento.getConteudo(),
                headers,
                HttpStatus.OK);
    }

    @DeleteMapping("/etapas/{etapaId}/documentos/{documentoId}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long etapaId,
            @PathVariable Long documentoId) {

        documentoService.excluir(etapaId, documentoId);

        return ResponseEntity.noContent().build();
    }
}
