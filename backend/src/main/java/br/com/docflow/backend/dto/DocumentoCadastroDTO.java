package br.com.docflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoCadastroDTO(
        @NotBlank(message = "O nome do arquivo é obrigatório.") String nomeArquivo,
        @NotBlank(message = "A URL do arquivo é obrigatória.") String urlArquivo,
        @NotNull(message = "O ID da etapa é obrigatório.") Long etapaId,
        @NotNull(message = "O ID do trabalho é obrigatório.") Long trabalhoId
) {}
