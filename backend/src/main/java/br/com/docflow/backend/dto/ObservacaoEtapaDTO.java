package br.com.docflow.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ObservacaoEtapaDTO {

    @NotBlank(message = "A observação não pode estar vazia.")
    private String texto;

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}