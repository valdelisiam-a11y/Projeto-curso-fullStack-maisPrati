package br.com.docflow.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EtapaPercentualDTO {

    @NotNull(message = "O percentual de conclusão é obrigatório")
    @Min(value = 0, message = "O percentual deve ser no mínimo 0")
    @Max(value = 100, message = "O percentual deve ser no máximo 100")
    private Integer percentualConclusao;

    // Mantido temporariamente para não quebrar o front.
    // O backend ignora este valor e usa o usuário autenticado (JWT).
    private Long usuarioId;

    public Integer getPercentualConclusao() { return percentualConclusao; }
    public void setPercentualConclusao(Integer percentualConclusao) { this.percentualConclusao = percentualConclusao; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
