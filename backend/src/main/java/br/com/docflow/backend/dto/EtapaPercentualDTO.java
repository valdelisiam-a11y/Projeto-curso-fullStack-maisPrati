package br.com.docflow.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EtapaPercentualDTO {

    @NotNull(message = "O percentual de conclusão é obrigatório")
    @Min(value = 0, message = "O percentual deve ser no mínimo 0")
    @Max(value = 100, message = "O percentual deve ser no máximo 100")
    private Integer percentualConclusao;

    // Usuário que está fazendo a alteração: o percentual pode mudar o status
    // da etapa automaticamente, e essa mudança vai para o histórico
    @NotNull(message = "O usuário é obrigatório")
    private Long usuarioId;

    public Integer getPercentualConclusao() { return percentualConclusao; }
    public void setPercentualConclusao(Integer percentualConclusao) { this.percentualConclusao = percentualConclusao; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
