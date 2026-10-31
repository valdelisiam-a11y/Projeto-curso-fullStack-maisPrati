package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.DocumentoCadastroDTO;
import br.com.docflow.backend.dto.DocumentoRespostaDTO;
import br.com.docflow.backend.service.DocumentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    @Autowired
    private DocumentoService documentoService;

    @PostMapping
    public ResponseEntity<DocumentoRespostaDTO> adicionar(@RequestBody @Valid DocumentoCadastroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentoService.adicionar(dto));
    }

    @GetMapping("/etapa/{etapaId}")
    public ResponseEntity<List<DocumentoRespostaDTO>> listarPorEtapa(@PathVariable Long etapaId) {
        return ResponseEntity.ok(documentoService.listarPorEtapa(etapaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoRespostaDTO> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(documentoService.consultar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        documentoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
