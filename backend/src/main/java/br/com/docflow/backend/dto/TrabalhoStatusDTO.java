package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusTrabalho;

public class TrabalhoStatusDTO {

    private StatusTrabalho status;
    private Long usuarioId;

    public StatusTrabalho getStatus() {
        return status;
    }

    public void setStatus(StatusTrabalho status) {
        this.status = status;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}