package br.com.docflow.backend.controller;

import br.com.docflow.backend.service.UsuarioService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.docflow.backend.dto.CadastroUsuarioDTO;
import br.com.docflow.backend.entity.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import br.com.docflow.backend.dto.UsuarioRespostaDTO;
import jakarta.validation.Valid;
import br.com.docflow.backend.service.EmailJaCadastradoException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.docflow.backend.dto.AtualizarPerfilDTO;
import br.com.docflow.backend.dto.AtualizarPerfilRespostaDTO;
import br.com.docflow.backend.service.UsuarioAutenticadoService;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/usuarios")

public class UsuarioController {
    private final UsuarioService usuarioService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private static final Logger log =
            LoggerFactory.getLogger(UsuarioController.class);

    public UsuarioController(
            UsuarioService usuarioService,
            UsuarioAutenticadoService usuarioAutenticadoService) {
        this.usuarioService = usuarioService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @PostMapping
    public ResponseEntity<UsuarioRespostaDTO> cadastrar(
            @Valid @RequestBody CadastroUsuarioDTO dto) {

        try {
            Usuario usuario = usuarioService.cadastrar(dto);

            UsuarioRespostaDTO resposta = new UsuarioRespostaDTO();

            resposta.setId(usuario.getId());
            resposta.setNome(usuario.getNome());
            resposta.setEmail(usuario.getEmail());

            return ResponseEntity.ok(resposta);

        } catch (EmailJaCadastradoException e) {
            log.warn("Falha no cadastro de usuário: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }
    }

    @PutMapping("/me")
    public ResponseEntity<AtualizarPerfilRespostaDTO> atualizarPerfil(
            @Valid @RequestBody AtualizarPerfilDTO dto) {

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        AtualizarPerfilRespostaDTO resposta =
                usuarioService.atualizarPerfil(usuario, dto);

        return ResponseEntity.ok(resposta);
    }
}
