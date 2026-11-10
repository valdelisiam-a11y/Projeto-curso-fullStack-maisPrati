package br.com.docflow.backend.dto;

import java.time.LocalDateTime;

public class ErroRespostaDTO {
    private LocalDateTime timestamp;
    private int status;
    private String erro;
    private String mensagem;

    public ErroRespostaDTO(int status, String erro, String mensagem) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }
}