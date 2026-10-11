package br.com.docflow.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Etapa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
}
