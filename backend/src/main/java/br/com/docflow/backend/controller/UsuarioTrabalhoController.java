package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.UsuarioTrabalhoDTO;
import br.com.docflow.backend.dto.UsuarioTrabalhoRespostaDTO;
import br.com.docflow.backend.service.UsuarioTrabalhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuario-trabalho")
public class UsuarioTrabalhoController {

    @Autowired
    private UsuarioTrabalhoService usuarioTrabalhoService;

    @PostMapping
    public ResponseEntity<UsuarioTrabalhoRespostaDTO> adicionarParticipante(
            @RequestBody UsuarioTrabalhoDTO usuarioTrabalhoDTO) {

        
        UsuarioTrabalhoRespostaDTO resposta = usuarioTrabalhoService.adicionarParticipante(
                usuarioTrabalhoDTO.getTrabalhoId(),
                usuarioTrabalhoDTO.getUsuarioId());

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/{trabalhoId}")
    public ResponseEntity<List<UsuarioTrabalhoRespostaDTO>> listarParticipantes(
            @PathVariable Long trabalhoId) {

        List<UsuarioTrabalhoRespostaDTO> participantes = usuarioTrabalhoService.listarParticipantes(trabalhoId);
        return ResponseEntity.ok(participantes);
    }

    @DeleteMapping("/{trabalhoId}/{usuarioId}")
    public ResponseEntity<Void> removerParticipante(
            @PathVariable Long trabalhoId, 
            @PathVariable Long usuarioId) {

        usuarioTrabalhoService.removerParticipante(trabalhoId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}