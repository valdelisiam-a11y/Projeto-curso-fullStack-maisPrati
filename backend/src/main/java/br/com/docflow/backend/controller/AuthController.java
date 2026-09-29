package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.LoginDTO;
import br.com.docflow.backend.dto.LoginRespostaDTO;
import br.com.docflow.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespostaDTO> login(@RequestBody LoginDTO dto) {

        LoginRespostaDTO resposta = authService.autenticar(dto);

        return ResponseEntity.ok(resposta);
    }

}
