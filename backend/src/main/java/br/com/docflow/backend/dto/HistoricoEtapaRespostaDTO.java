package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusEtapa;

import java.time.LocalDateTime;

public class HistoricoEtapaRespostaDTO {

    private Long id;
    private StatusEtapa statusAnterior;
    private StatusEtapa statusNovo;
    private LocalDateTime dataAlteracao;
    private Long usuarioId;
    private String usuarioNome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatusEtapa getStatusAnterior() { return statusAnterior; }
    public void setStatusAnterior(StatusEtapa statusAnterior) { this.statusAnterior = statusAnterior; }

    public StatusEtapa getStatusNovo() { return statusNovo; }
    public void setStatusNovo(StatusEtapa statusNovo) { this.statusNovo = statusNovo; }

    public LocalDateTime getDataAlteracao() { return dataAlteracao; }
    public void setDataAlteracao(LocalDateTime dataAlteracao) { this.dataAlteracao = dataAlteracao; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
}
