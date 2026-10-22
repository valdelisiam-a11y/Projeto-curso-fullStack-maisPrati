package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.EtapaAtualizacaoDTO;
import br.com.docflow.backend.dto.EtapaCadastroDTO;
import br.com.docflow.backend.dto.EtapaOrdemDTO;
import br.com.docflow.backend.dto.EtapaPercentualDTO;
import br.com.docflow.backend.dto.EtapaRespostaDTO;
import br.com.docflow.backend.dto.EtapaResponsavelDTO;
import br.com.docflow.backend.dto.EtapaStatusDTO;
import br.com.docflow.backend.dto.HistoricoEtapaRespostaDTO;
import br.com.docflow.backend.service.EtapaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EtapaController {

    private final EtapaService etapaService;

    public EtapaController(EtapaService etapaService) {
        this.etapaService = etapaService;
    }

    @PostMapping("/trabalhos/{trabalhoId}/etapas")
    public ResponseEntity<EtapaRespostaDTO> cadastrar(
            @PathVariable Long trabalhoId,
            @Valid @RequestBody EtapaCadastroDTO dto) {

        EtapaRespostaDTO resposta = etapaService.cadastrar(trabalhoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/trabalhos/{trabalhoId}/etapas")
    public ResponseEntity<List<EtapaRespostaDTO>> listarPorTrabalho(
            @PathVariable Long trabalhoId) {

        return ResponseEntity.ok(etapaService.listarPorTrabalho(trabalhoId));
    }

    @GetMapping("/etapas/{id}")
    public ResponseEntity<EtapaRespostaDTO> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(etapaService.buscarPorId(id));
    }

    @PutMapping("/etapas/{id}")
    public ResponseEntity<EtapaRespostaDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EtapaAtualizacaoDTO dto) {

        return ResponseEntity.ok(etapaService.atualizar(id, dto));
    }

    @PatchMapping("/etapas/{id}/status")
    public ResponseEntity<EtapaRespostaDTO> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody EtapaStatusDTO dto) {

        return ResponseEntity.ok(etapaService.alterarStatus(id, dto));
    }

    @PatchMapping("/etapas/{id}/percentual")
    public ResponseEntity<EtapaRespostaDTO> atualizarPercentual(
            @PathVariable Long id,
            @Valid @RequestBody EtapaPercentualDTO dto) {

        return ResponseEntity.ok(etapaService.atualizarPercentual(id, dto));
    }

    @PatchMapping("/etapas/{id}/ordem")
    public ResponseEntity<EtapaRespostaDTO> alterarOrdem(
            @PathVariable Long id,
            @Valid @RequestBody EtapaOrdemDTO dto) {

        return ResponseEntity.ok(etapaService.alterarOrdem(id, dto));
    }

    @PatchMapping("/etapas/{id}/responsavel")
    public ResponseEntity<EtapaRespostaDTO> definirResponsavel(
            @PathVariable Long id,
            @RequestBody EtapaResponsavelDTO dto) {

        return ResponseEntity.ok(etapaService.definirResponsavel(id, dto));
    }

    @GetMapping("/etapas/{id}/historico")
    public ResponseEntity<List<HistoricoEtapaRespostaDTO>> listarHistorico(
            @PathVariable Long id) {

        return ResponseEntity.ok(etapaService.listarHistorico(id));
    }

    @DeleteMapping("/etapas/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        etapaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
