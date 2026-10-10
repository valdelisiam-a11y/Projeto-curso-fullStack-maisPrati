package br.com.docflow.backend.dto;

import lombok.Data;

@Data
public class DocumentoRequestDTO {
    private String nome;
    private String urlArquivo;
}
