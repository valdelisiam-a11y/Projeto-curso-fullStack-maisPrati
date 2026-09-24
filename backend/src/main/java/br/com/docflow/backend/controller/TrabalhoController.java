package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.TrabalhoAtualizacaoDTO;
import br.com.docflow.backend.dto.TrabalhoCadastroDTO;
import br.com.docflow.backend.dto.TrabalhoRespostaDTO;
import br.com.docflow.backend.dto.TrabalhoStatusDTO;
import br.com.docflow.backend.service.TrabalhoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trabalhos")
public class TrabalhoController {

    private final TrabalhoService trabalhoService;

    public TrabalhoController(TrabalhoService trabalhoService) {
        this.trabalhoService = trabalhoService;
    }

    @PostMapping
    public ResponseEntity<TrabalhoRespostaDTO> cadastrar(
            @RequestBody TrabalhoCadastroDTO dto) {

        TrabalhoRespostaDTO resposta = trabalhoService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public ResponseEntity<List<TrabalhoRespostaDTO>> listarTodos() {

        List<TrabalhoRespostaDTO> trabalhos = trabalhoService.listarTodos();

        return ResponseEntity.ok(trabalhos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrabalhoRespostaDTO> buscarPorId(@PathVariable Long id) {

        TrabalhoRespostaDTO trabalho = trabalhoService.buscarPorId(id);

        return ResponseEntity.ok(trabalho);
    }

    @GetMapping("/busca")
    public ResponseEntity<List<TrabalhoRespostaDTO>> buscarPorTitulo(
            @RequestParam String titulo) {

        List<TrabalhoRespostaDTO> trabalhos =
                trabalhoService.buscarPorTitulo(titulo);

        return ResponseEntity.ok(trabalhos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrabalhoRespostaDTO> atualizar(
            @PathVariable Long id,
            @RequestBody TrabalhoAtualizacaoDTO dto) {

        TrabalhoRespostaDTO trabalhoAtualizado =
                trabalhoService.atualizar(id, dto);

        return ResponseEntity.ok(trabalhoAtualizado);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TrabalhoRespostaDTO> alterarStatus(
            @PathVariable Long id,
            @RequestBody TrabalhoStatusDTO dto) {

        TrabalhoRespostaDTO trabalhoAtualizado =
                trabalhoService.alterarStatus(id, dto);

        return ResponseEntity.ok(trabalhoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        trabalhoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}