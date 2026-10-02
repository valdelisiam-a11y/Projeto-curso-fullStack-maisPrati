package br.com.docflow.backend.controller;

import br.com.docflow.backend.entity.Documento;
import br.com.docflow.backend.service.DocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/trabalhos/{trabalhoId}/etapas/{etapaId}/documentos")
public class DocumentoController {

    @Autowired
    private DocumentoService documentoService;

    @PostMapping
    public ResponseEntity<Documento> adicionar(
            @PathVariable Long trabalhoId,
            @PathVariable Long etapaId,
            @RequestBody Documento documento) {
        Documento novoDocumento = documentoService.adicionarDocumento(trabalhoId, etapaId, documento);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoDocumento);
    }

    @GetMapping
    public ResponseEntity<List<Documento>> listar(
            @PathVariable Long trabalhoId,
            @PathVariable Long etapaId) {
        List<Documento> documentos = documentoService.listarDocumentosDaEtapa(trabalhoId, etapaId);
        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/{documentoId}")
    public ResponseEntity<Documento> consultar(
            @PathVariable Long trabalhoId,
            @PathVariable Long etapaId,
            @PathVariable Long documentoId) {
        Documento documento = documentoService.consultarDocumento(trabalhoId, etapaId, documentoId);
        return ResponseEntity.ok(documento);
    }

    @DeleteMapping("/{documentoId}")
    public ResponseEntity<Void> remover(
            @PathVariable Long trabalhoId,
            @PathVariable Long etapaId,
            @PathVariable Long documentoId) {
        documentoService.removerDocumento(trabalhoId, etapaId, documentoId);
        return ResponseEntity.noContent().build();
    }
}
