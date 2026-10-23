package com.example.barbertime;

public class Usuario {

    private Long id;
    private String username;
    private String email;
    private String senhaHash;

    // Construtor padrão
    public Usuario() {
    }

    // Cria um usuário antes de salvá-lo no banco
    public Usuario(String username, String email, String senhaHash) {
        this.username = username;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    // Cria um usuário com identificador existente
    public Usuario(Long id, String username, String email, String senhaHash) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
}