package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusTrabalho;

public class MinhaEtapaRespostaDTO {

    private Long etapaId;
    private String etapaTitulo;
    private StatusTrabalho status;
    private Integer percentualConclusao;
    private Integer ordem;
    private Long trabalhoId;
    private String trabalhoTitulo;
    private Long empresaId;
    private String empresaNome;

    public Long getEtapaId() {
        return etapaId;
    }

    public void setEtapaId(Long etapaId) {
        this.etapaId = etapaId;
    }

    public String getEtapaTitulo() {
        return etapaTitulo;
    }

    public void setEtapaTitulo(String etapaTitulo) {
        this.etapaTitulo = etapaTitulo;
    }

    public StatusTrabalho getStatus() {
        return status;
    }

    public void setStatus(StatusTrabalho status) {
        this.status = status;
    }

    public Integer getPercentualConclusao() {
        return percentualConclusao;
    }

    public void setPercentualConclusao(Integer percentualConclusao) {
        this.percentualConclusao = percentualConclusao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Long getTrabalhoId() {
        return trabalhoId;
    }

    public void setTrabalhoId(Long trabalhoId) {
        this.trabalhoId = trabalhoId;
    }

    public String getTrabalhoTitulo() {
        return trabalhoTitulo;
    }

    public void setTrabalhoTitulo(String trabalhoTitulo) {
        this.trabalhoTitulo = trabalhoTitulo;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getEmpresaNome() {
        return empresaNome;
    }

    public void setEmpresaNome(String empresaNome) {
        this.empresaNome = empresaNome;
    }
}