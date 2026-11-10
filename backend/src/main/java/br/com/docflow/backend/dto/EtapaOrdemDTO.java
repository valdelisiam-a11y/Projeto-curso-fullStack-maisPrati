package br.com.docflow.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EtapaOrdemDTO {

    @NotNull(message = "A nova ordem é obrigatória")
    @Min(value = 1, message = "A ordem deve ser maior ou igual a 1")
    private Integer ordem;

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
}
