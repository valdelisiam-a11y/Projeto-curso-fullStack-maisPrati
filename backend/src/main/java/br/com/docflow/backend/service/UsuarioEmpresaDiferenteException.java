package br.com.docflow.backend.service;

public class UsuarioEmpresaDiferenteException extends RuntimeException {

    public UsuarioEmpresaDiferenteException(String mensagem) {
        super(mensagem);
    }
}