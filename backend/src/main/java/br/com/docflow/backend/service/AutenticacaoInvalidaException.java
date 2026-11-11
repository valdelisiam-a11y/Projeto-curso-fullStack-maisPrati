package br.com.docflow.backend.service;

public class AutenticacaoInvalidaException extends RuntimeException {

    public AutenticacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
