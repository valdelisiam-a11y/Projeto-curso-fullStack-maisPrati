package br.com.docflow.backend.config;

import br.com.docflow.backend.controller.EtapaController;
import br.com.docflow.backend.dto.ErroRespostaDTO;
import br.com.docflow.backend.service.EtapaStatusInalteradoException;
import br.com.docflow.backend.service.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// Vale apenas para o EtapaController. Usa o mesmo ErroRespostaDTO do GlobalExceptionHandler.
// A UsuarioEmpresaDiferenteException (409) continua sendo tratada pelo GlobalExceptionHandler.
@RestControllerAdvice(assignableTypes = EtapaController.class)
public class EtapaExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroRespostaDTO> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(EtapaStatusInalteradoException.class)
    public ResponseEntity<ErroRespostaDTO> handleStatusInalterado(EtapaStatusInalteradoException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
                HttpStatus.CONFLICT.value(),
                "Conflito de Status",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroRespostaDTO> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErroRespostaDTO erro = new ErroRespostaDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                mensagem
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroRespostaDTO> handleCorpoInvalido(HttpMessageNotReadableException ex) {
        ErroRespostaDTO erro = new ErroRespostaDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                "Corpo da requisição ausente, malformado ou com valor não reconhecido"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }
}
