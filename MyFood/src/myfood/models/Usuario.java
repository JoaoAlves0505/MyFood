package myfood.models;

import java.io.Serializable;

// Classe base para Cliente e DonoDeEmpresa.
// Tem os dados que os dois compartilham.

public abstract class Usuario implements Serializable {

    // Precisa disso pra poder salvar em arquivo (persistência)
    private static final long serialVersionUID = 1L;

    protected int id;
    protected String nome;
    protected String email;
    protected String senha;
    protected String endereco;

    public Usuario(int id, String nome, String email, String senha, String endereco) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.endereco = endereco;
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getEndereco() {
        return endereco;
    }
}