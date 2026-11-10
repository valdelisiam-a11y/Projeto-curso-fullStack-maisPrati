package br.com.docflow.backend.dto;

import java.time.LocalDateTime;

public class LinhaDoTempoRespostaDTO {

    private Long id;
    private String tipo;
    private String texto;
    private String valorAnterior;
    private String valorNovo;
    private LocalDateTime data;
    private Long usuarioId;
    private String usuarioNome;
    private Long etapaId;
    private String etapaTitulo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getValorAnterior() { return valorAnterior; }
    public void setValorAnterior(String valorAnterior) { this.valorAnterior = valorAnterior; }

    public String getValorNovo() { return valorNovo; }
    public void setValorNovo(String valorNovo) { this.valorNovo = valorNovo; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public Long getEtapaId() { return etapaId; }
    public void setEtapaId(Long etapaId) { this.etapaId = etapaId; }

    public String getEtapaTitulo() { return etapaTitulo; }
    public void setEtapaTitulo(String etapaTitulo) { this.etapaTitulo = etapaTitulo; }
}