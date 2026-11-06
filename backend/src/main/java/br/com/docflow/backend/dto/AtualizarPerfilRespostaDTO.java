package br.com.docflow.backend.dto;

public class AtualizarPerfilRespostaDTO {

    private Long id;
    private String nome;
    private String email;
    private String token;

    public AtualizarPerfilRespostaDTO(
            Long id,
            String nome,
            String email,
            String token) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}
