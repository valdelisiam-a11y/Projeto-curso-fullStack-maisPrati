package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusTrabalho;

import java.time.LocalDateTime;

public class EtapaRespostaDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private StatusTrabalho status;
    private Integer percentualConclusao;
    private Integer ordem;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private Long trabalhoId;
    private Long responsavelId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public StatusTrabalho getStatus() { return status; }
    public void setStatus(StatusTrabalho status) { this.status = status; }

    public Integer getPercentualConclusao() { return percentualConclusao; }
    public void setPercentualConclusao(Integer percentualConclusao) { this.percentualConclusao = percentualConclusao; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public Long getTrabalhoId() { return trabalhoId; }
    public void setTrabalhoId(Long trabalhoId) { this.trabalhoId = trabalhoId; }

    public Long getResponsavelId() { return responsavelId; }
    public void setResponsavelId(Long responsavelId) { this.responsavelId = responsavelId; }
}
