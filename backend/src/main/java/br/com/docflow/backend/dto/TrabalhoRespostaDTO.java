package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.Trabalho;
import java.time.LocalDateTime;

public class TrabalhoRespostaDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private String status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private LocalDateTime dataConclusao;
    private Long empresaId;
    private Long criadoPorId;

    // Construtor padrão necessário para o TrabalhoService instanciar sem argumentos
    public TrabalhoRespostaDTO() {
    }

    // Construtor completo a partir da entidade Trabalho (para os métodos de listagem/mapeamento)
    public TrabalhoRespostaDTO(Trabalho trabalho) {
        if (trabalho != null) {
            this.id = trabalho.getId();
            this.titulo = trabalho.getTitulo();
            this.descricao = trabalho.getDescricao();
            this.status = trabalho.getStatus() != null ? trabalho.getStatus().toString() : null;
            this.dataCriacao = trabalho.getDataCriacao();
            this.dataAtualizacao = trabalho.getDataAtualizacao();
            this.dataConclusao = trabalho.getDataConclusao();
            this.empresaId = trabalho.getEmpresa() != null ? trabalho.getEmpresa().getId() : null;
            this.criadoPorId = trabalho.getCriadoPor() != null ? trabalho.getCriadoPor().getId() : null;
        }
    }

    // Getters e Setters para que os métodos .set() do seu TrabalhoService voltem a funcionar
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Suporte caso o seu service passe o ENUM do status diretamente
    public void setStatus(Object status) {
        this.status = status != null ? status.toString() : null;
    }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime dataConclusao) { this.dataConclusao = dataConclusao; }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public Long getCriadoPorId() { return criadoPorId; }
    public void setCriadoPorId(Long criadoPorId) { this.criadoPorId = criadoPorId; }
}
