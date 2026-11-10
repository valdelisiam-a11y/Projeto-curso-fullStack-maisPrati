package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.UsuarioTrabalho;

public class UsuarioTrabalhoRespostaDTO {

    private Long id;
    private Long usuarioId;
    private String nomeUsuario;
    private String emailUsuario;
    private Long trabalhoId;

    public UsuarioTrabalhoRespostaDTO() {
    }

    public UsuarioTrabalhoRespostaDTO(UsuarioTrabalho usuarioTrabalho) {
        this.id = usuarioTrabalho.getId();
        this.usuarioId = usuarioTrabalho.getUsuario().getId();
        this.nomeUsuario = usuarioTrabalho.getUsuario().getNome();
        this.emailUsuario = usuarioTrabalho.getUsuario().getEmail();
        this.trabalhoId = usuarioTrabalho.getTrabalho().getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public Long getTrabalhoId() {
        return trabalhoId;
    }

    public void setTrabalhoId(Long trabalhoId) {
        this.trabalhoId = trabalhoId;
    }
}