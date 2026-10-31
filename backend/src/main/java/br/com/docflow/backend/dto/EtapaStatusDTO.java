package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusEtapa;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EtapaStatusDTO {

    @NotNull(message = "O status é obrigatório")
    private StatusEtapa status;

    // Obrigatório apenas quando a etapa está CONCLUIDO e passa para outro status:
    // é o percentual ao qual ela retorna (menor que 100). Nos demais casos é ignorado.
    @Min(value = 0, message = "O percentual deve ser no mínimo 0")
    @Max(value = 99, message = "O percentual de retorno deve ser menor que 100")
    private Integer percentualConclusao;

    // Mantido temporariamente para não quebrar o front.
    // O backend ignora este valor e usa o usuário autenticado (JWT).
    private Long usuarioId;

    public StatusEtapa getStatus() { return status; }
    public void setStatus(StatusEtapa status) { this.status = status; }

    public Integer getPercentualConclusao() { return percentualConclusao; }
    public void setPercentualConclusao(Integer percentualConclusao) { this.percentualConclusao = percentualConclusao; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
