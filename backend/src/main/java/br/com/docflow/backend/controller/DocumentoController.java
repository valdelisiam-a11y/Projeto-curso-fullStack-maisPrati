package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.DocumentoRequestDTO;
import br.com.docflow.backend.dto.DocumentoResponseDTO;
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
    public ResponseEntity<DocumentoResponseDTO> adicionar(
            @PathVariable Long trabalhoId,
            @PathVariable Long etapaId,
            @RequestBody DocumentoRequestDTO dto) {
        DocumentoResponseDTO novoDocumento = documentoService.adicionarDocumento(trabalhoId, etapaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoDocumento);
    }

    @GetMapping
    public ResponseEntity<List<DocumentoResponseDTO>> listar(
            @PathVariable Long trabalhoId,
            @PathVariable Long etapaId) {
        List<DocumentoResponseDTO> documentos = documentoService.listarDocumentosDaEtapa(trabalhoId, etapaId);
        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/{documentoId}")
    public ResponseEntity<DocumentoResponseDTO> consultar(
            @PathVariable Long trabalhoId,
            @PathVariable Long etapaId,
            @PathVariable Long documentoId) {
        DocumentoResponseDTO documento = documentoService.consultarDocumento(trabalhoId, etapaId, documentoId);
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
