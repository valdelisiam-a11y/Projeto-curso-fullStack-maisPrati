package br.com.docflow.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nomeOriginal;

    @Column(nullable = false, unique = true, length = 500)
    private String chaveArquivo;

    @Column(nullable = false, length = 150)
    private String contentType;

    @Column(nullable = false)
    private Long tamanho;

    @Column(nullable = false)
    private LocalDateTime dataUpload;

    // Muitos documentos pertencem a uma etapa.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Etapa etapa;

    @PrePersist
    void aoCriar() {
        this.dataUpload = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getChaveArquivo() {
        return chaveArquivo;
    }

    public void setChaveArquivo(String chaveArquivo) {
        this.chaveArquivo = chaveArquivo;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getTamanho() {
        return tamanho;
    }

    public void setTamanho(Long tamanho) {
        this.tamanho = tamanho;
    }

    public LocalDateTime getDataUpload() {
        return dataUpload;
    }

    public void setDataUpload(LocalDateTime dataUpload) {
        this.dataUpload = dataUpload;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }
}