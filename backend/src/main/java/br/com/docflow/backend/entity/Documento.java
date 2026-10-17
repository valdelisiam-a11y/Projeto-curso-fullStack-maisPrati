package br.com.docflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeArquivo;

    @Column(nullable = false, length = 500)
    private String urlArquivo;

    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "etapa_id", nullable = false)
    private Etapa etapa; // Conforme alinhado na modelagem do banco
}
