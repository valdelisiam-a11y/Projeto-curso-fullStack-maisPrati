
package br.com.docflow.backend.controller;

import br.com.docflow.backend.dto.AdicionarMembroDTO;
import br.com.docflow.backend.dto.CadastroEmpresaDTO;
import br.com.docflow.backend.entity.Empresa;
import br.com.docflow.backend.service.UsuarioEmpresaService;
import br.com.docflow.backend.dto.MembroEmpresaDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.docflow.backend.dto.EmpresaResumoDTO;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    private final UsuarioEmpresaService usuarioEmpresaService;

    public EmpresaController(UsuarioEmpresaService usuarioEmpresaService) {
        this.usuarioEmpresaService = usuarioEmpresaService;
    }

    @PostMapping("/{empresaId}/membros")
    public ResponseEntity<Void> adicionarMembro(
            @PathVariable Long empresaId,
            @Valid @RequestBody AdicionarMembroDTO dto) {

        usuarioEmpresaService.adicionarMembro(
                empresaId,
                dto.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{empresaId}/membros/{usuarioId}")
    public ResponseEntity<Void> removerMembro(
            @PathVariable Long empresaId,
            @PathVariable Long usuarioId) {

        usuarioEmpresaService.removerMembro(empresaId, usuarioId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Void> cadastrarEmpresa(
            @Valid @RequestBody CadastroEmpresaDTO dto) {

        usuarioEmpresaService.cadastrarEmpresa(
                dto.getNome(),
                dto.getCnpj()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/minhas")
    public ResponseEntity<java.util.List<EmpresaResumoDTO>> listarMinhasEmpresas() {
        return ResponseEntity.ok(
                usuarioEmpresaService.listarMinhasEmpresas()
        );
    }

    @GetMapping("/{empresaId}/membros")
    public ResponseEntity<java.util.List<MembroEmpresaDTO>> listarMembros(
            @PathVariable Long empresaId) {

        return ResponseEntity.ok(
                usuarioEmpresaService.listarMembros(empresaId)
        );
    }
}
