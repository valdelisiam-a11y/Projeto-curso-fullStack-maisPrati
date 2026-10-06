package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusTrabalho;

import java.time.LocalDateTime;

public class HistoricoTrabalhoRespostaDTO {

    private Long id;
    private StatusTrabalho statusAnterior;
    private StatusTrabalho statusNovo;
    private LocalDateTime dataAlteracao;
    private Long usuarioId;
    private String usuarioNome;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatusTrabalho getStatusAnterior() {
        return statusAnterior;
    }

    public void setStatusAnterior(StatusTrabalho statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    public StatusTrabalho getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(StatusTrabalho statusNovo) {
        this.statusNovo = statusNovo;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(LocalDateTime dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }
}