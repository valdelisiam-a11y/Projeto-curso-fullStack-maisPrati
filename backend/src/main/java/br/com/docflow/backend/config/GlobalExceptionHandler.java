package br.com.docflow.backend.config;

import br.com.docflow.backend.dto.ErroRespostaDTO;
import br.com.docflow.backend.service.ParticipanteJaAssociadoException;
import br.com.docflow.backend.service.UsuarioEmpresaDiferenteException;
import br.com.docflow.backend.service.ParticipanteNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import br.com.docflow.backend.service.LimiteMembrosEmpresaException;
import br.com.docflow.backend.service.EmailJaCadastradoException;
import br.com.docflow.backend.service.AutenticacaoInvalidaException;

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

    @ExceptionHandler(ParticipanteNaoEncontradoException.class)
    public ResponseEntity<ErroRespostaDTO> handleParticipanteNaoEncontrado(
        ParticipanteNaoEncontradoException ex) {

    ErroRespostaDTO erro = new ErroRespostaDTO(
            HttpStatus.NOT_FOUND.value(),
            "Participante não encontrado",
            ex.getMessage()
    );

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(erro);
}
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroRespostaDTO> handleAcessoNegado(
            AccessDeniedException ex) {

        ErroRespostaDTO erro = new ErroRespostaDTO(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(erro);
    }

    @ExceptionHandler(LimiteMembrosEmpresaException.class)
    public ResponseEntity<ErroRespostaDTO> handleLimiteMembrosEmpresa(
            LimiteMembrosEmpresaException ex) {

        ErroRespostaDTO erro = new ErroRespostaDTO(
                HttpStatus.CONFLICT.value(),
                "Limite de membros atingido",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroRespostaDTO> handleEmailJaCadastrado(
            EmailJaCadastradoException ex) {

        ErroRespostaDTO erro = new ErroRespostaDTO(
                HttpStatus.CONFLICT.value(),
                "E-mail já cadastrado",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(AutenticacaoInvalidaException.class)
    public ResponseEntity<ErroRespostaDTO> handleAutenticacaoInvalida(
            AutenticacaoInvalidaException ex) {

        ErroRespostaDTO erro = new ErroRespostaDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Não autorizado",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(erro);
    }
}
