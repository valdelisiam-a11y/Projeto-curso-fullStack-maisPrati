package br.com.docflow.backend.config;

import br.com.docflow.backend.dto.ErroRespostaDTO;
import br.com.docflow.backend.service.ParticipanteJaAssociadoException;
import br.com.docflow.backend.service.UsuarioEmpresaDiferenteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ParticipanteJaAssociadoException.class)
    public ResponseEntity<ErroRespostaDTO> handleParticipanteJaAssociado(ParticipanteJaAssociadoException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
            HttpStatus.CONFLICT.value(),
            "Conflito de Associação",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(UsuarioEmpresaDiferenteException.class)
    public ResponseEntity<ErroRespostaDTO> handleUsuarioEmpresaDiferente(UsuarioEmpresaDiferenteException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
            HttpStatus.CONFLICT.value(),
            "Conflito de Empresa",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }
}