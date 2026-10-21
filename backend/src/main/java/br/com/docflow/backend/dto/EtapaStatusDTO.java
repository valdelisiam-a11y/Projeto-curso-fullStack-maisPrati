package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusTrabalho;
import jakarta.validation.constraints.NotNull;

public class EtapaStatusDTO {

    @NotNull(message = "O status é obrigatório")
    private StatusTrabalho status;

    // Usuário que está fazendo a alteração (registrado no histórico)
    @NotNull(message = "O usuário é obrigatório")
    private Long usuarioId;

    public StatusTrabalho getStatus() { return status; }
    public void setStatus(StatusTrabalho status) { this.status = status; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
