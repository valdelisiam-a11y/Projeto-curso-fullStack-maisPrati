package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.TipoTrabalho;

public class TrabalhoCadastroDTO {
    private String titulo;
    private String descricao;
    private Long empresaId;
    private Long criadoPorId;
    private TipoTrabalho tipo;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getCriadoPorId() {
        return criadoPorId;
    }

    public void setCriadoPorId(Long criadoPorId) {
        this.criadoPorId = criadoPorId;
    }

    public TipoTrabalho getTipo() {
        return tipo;
    }

    public void setTipo(TipoTrabalho tipo) {
        this.tipo = tipo;
    }
}
