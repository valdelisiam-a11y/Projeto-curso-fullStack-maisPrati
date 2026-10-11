package br.com.docflow.backend.dto;

public class EtapaResponsavelDTO {

    // ID do usuário responsável; null remove o responsável da etapa
    private Long responsavelId;

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
}