package br.com.docflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoCadastroDTO(
        @NotBlank String nomeArquivo,
        @NotBlank String urlArquivo,
        @NotNull Long etapaId,
        @NotNull Long trabalhoId
) {}
