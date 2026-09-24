package br.com.docflow.backend.service;

public class EmailJaCadastradoException  extends RuntimeException{

    public EmailJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}
