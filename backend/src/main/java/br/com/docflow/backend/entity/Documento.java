package br.com.docflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documento") // Conforme o banco definido
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

    @Column(name = "etapa_id", nullable = false)
    private Long etapaId;

    // OBS: Quando o Erick subir a branch dele, mudaremos este long para:
    // @ManyToOne
    // @JoinColumn(name = "etapa_id")
    // private Etapa etapa;
}
