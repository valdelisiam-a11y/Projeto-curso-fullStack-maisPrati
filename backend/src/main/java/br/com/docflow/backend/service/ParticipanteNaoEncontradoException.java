package br.com.docflow.backend.service;

public class ParticipanteNaoEncontradoException extends RuntimeException {

    public ParticipanteNaoEncontradoException(String message) {
        super(message);
    }
}