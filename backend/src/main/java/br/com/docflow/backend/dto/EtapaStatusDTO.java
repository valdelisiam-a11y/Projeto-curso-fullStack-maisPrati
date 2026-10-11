package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusEtapa;
import jakarta.validation.constraints.NotNull;

public class EtapaStatusDTO {

    @NotNull(message = "O status é obrigatório")
    private StatusEtapa status;

    public StatusEtapa getStatus() { return status; }
    public void setStatus(StatusEtapa status) { this.status = status; }
}