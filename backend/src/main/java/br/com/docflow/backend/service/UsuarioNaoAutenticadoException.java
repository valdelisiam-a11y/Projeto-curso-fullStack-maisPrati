
package br.com.docflow.backend.service;

public class UsuarioNaoAutenticadoException extends RuntimeException {

    public UsuarioNaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
