package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusTrabalho;
import jakarta.validation.constraints.NotNull;

public class EtapaStatusDTO {

    @NotNull(message = "O status é obrigatório")
    private StatusTrabalho status;

    public StatusTrabalho getStatus() {
        return status;
    }

    public void setStatus(StatusTrabalho status) {
        this.status = status;
    }
}