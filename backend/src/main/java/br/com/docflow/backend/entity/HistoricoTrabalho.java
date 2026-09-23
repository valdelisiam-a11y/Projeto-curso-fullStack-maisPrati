package br.com.docflow.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class HistoricoTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusTrabalho status;

    @Column(nullable = false)
    private LocalDateTime dataAlteracao;

    @ManyToOne
    @JoinColumn(name = "trabalho_id", nullable = false)
    private Trabalho trabalho;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatusTrabalho getStatus() {
        return status;
    }

    public void setStatus(StatusTrabalho status) {
        this.status = status;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(LocalDateTime dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public Trabalho getTrabalho() {
        return trabalho;
    }

    public void setTrabalho(Trabalho trabalho) {
        this.trabalho = trabalho;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}