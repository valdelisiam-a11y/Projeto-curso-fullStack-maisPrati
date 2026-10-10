package br.com.docflow.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentoResponseDTO {
    private Long id;
    private String nome;
    private String urlArquivo;
    private LocalDateTime dataCriacao;
}
