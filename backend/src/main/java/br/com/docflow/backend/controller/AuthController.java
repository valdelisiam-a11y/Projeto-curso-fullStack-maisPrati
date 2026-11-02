package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.LoginDTO;
import br.com.docflow.backend.dto.LoginRespostaDTO;
import br.com.docflow.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.docflow.backend.dto.AlterarSenhaDTO;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.service.UsuarioAutenticadoService;
import br.com.docflow.backend.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public AuthController(
            AuthService authService,
            UsuarioService usuarioService,
            UsuarioAutenticadoService usuarioAutenticadoService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespostaDTO> login(@RequestBody LoginDTO dto) {

        LoginRespostaDTO resposta = authService.autenticar(dto);

        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(
            @Valid @RequestBody AlterarSenhaDTO dto) {

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        usuarioService.alterarSenha(
                usuario,
                dto.getSenhaAtual(),
                dto.getNovaSenha()
        );

        return ResponseEntity.noContent().build();
    }

}
