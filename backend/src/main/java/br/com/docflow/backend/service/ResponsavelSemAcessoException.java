package br.com.docflow.backend.service;

public class ResponsavelSemAcessoException extends RuntimeException {

    public ResponsavelSemAcessoException(String mensagem) {
        super(mensagem);
    }
}
