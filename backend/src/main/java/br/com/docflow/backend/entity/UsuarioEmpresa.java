
package br.com.docflow.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "usuario_empresa",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_usuario_empresa",
                        columnNames = {"usuario_id", "empresa_id"}
                )
        }
)
public class UsuarioEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private LocalDateTime dataVinculo;

    @PrePersist
    protected void prePersist() {
        if (dataVinculo == null) {
            dataVinculo = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public LocalDateTime getDataVinculo() {
        return dataVinculo;
    }

    public void setDataVinculo(LocalDateTime dataVinculo) {
        this.dataVinculo = dataVinculo;
    }
}
