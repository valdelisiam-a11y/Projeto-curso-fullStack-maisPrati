package br.com.docflow.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
public class HistoricoEtapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusTrabalho statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusTrabalho statusNovo;

    @Column(nullable = false)
    private LocalDateTime dataAlteracao;

    // ON DELETE CASCADE no banco: ao excluir a etapa, o histórico dela é excluído junto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Etapa etapa;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatusTrabalho getStatusAnterior() { return statusAnterior; }
    public void setStatusAnterior(StatusTrabalho statusAnterior) { this.statusAnterior = statusAnterior; }

    public StatusTrabalho getStatusNovo() { return statusNovo; }
    public void setStatusNovo(StatusTrabalho statusNovo) { this.statusNovo = statusNovo; }

    public LocalDateTime getDataAlteracao() { return dataAlteracao; }
    public void setDataAlteracao(LocalDateTime dataAlteracao) { this.dataAlteracao = dataAlteracao; }

    public Etapa getEtapa() { return etapa; }
    public void setEtapa(Etapa etapa) { this.etapa = etapa; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
