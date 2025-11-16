package myfood.models;

import java.io.Serializable;

// Modelo do Restaurante (e outras empresas)
public class Restaurante implements Serializable {

    // Pra poder salvar
    private static final long serialVersionUID = 1L;

    private int id;
    private int idDono;
    private String nome;
    private String endereco;
    private String tipoCozinha;

    public Restaurante(int id, int idDono, String nome, String endereco, String tipoCozinha) {
        this.id = id;
        this.idDono = idDono;
        this.nome = nome;
        this.endereco = endereco;
        this.tipoCozinha = tipoCozinha;
    }

    // --- GETTERS ---

    public int getId() {
        return id;
    }

    public int getIdDono() {
        return idDono;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getTipoCozinha() {
        return tipoCozinha;
    }

    // Usado para a validação de "mesmo nome e local" no ControladorEmpresa
    public boolean temMesmoNomeEndereco(String nome, String endereco) {
        return this.nome.equals(nome) && this.endereco.equals(endereco);
    }
}