package br.com.docflow.backend.controller;

import br.com.docflow.backend.entity.UsuarioTrabalho;
import br.com.docflow.backend.service.UsuarioTrabalhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuario-trabalho")
public class UsuarioTrabalhoController {

    @Autowired
    private UsuarioTrabalhoService usuarioTrabalhoService;

    @PostMapping
    public ResponseEntity<UsuarioTrabalho> adicionarParticipante(@RequestBody UsuarioTrabalhoDTO usuarioTrabalhoDTO) {
        UsuarioTrabalho usuarioTrabalho = usuarioTrabalhoService.adicionarParticipante(usuarioTrabalhoDTO.getTrabalhoId(), usuarioTrabalhoDTO.getUsuarioId());
        return ResponseEntity.ok(usuarioTrabalho);
    }

    @GetMapping("/{trabalhoId}")
    public ResponseEntity<List<UsuarioTrabalho>> listarParticipantes(@PathVariable Long trabalhoId) {
        List<UsuarioTrabalho> usuarioTrabalhos = usuarioTrabalhoService.listarParticipantes(trabalhoId);
        return ResponseEntity.ok(usuarioTrabalhos);
    }

    @DeleteMapping("/{trabalhoId}/{usuarioId}")
    public ResponseEntity<Void> removerParticipante(@PathVariable Long trabalhoId, @PathVariable Long usuarioId) {
        usuarioTrabalhoService.removerParticipante(trabalhoId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}