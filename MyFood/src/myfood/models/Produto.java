package myfood.models;

import java.io.Serializable;

// Modelo do Produto
public class Produto implements Serializable {

    // Pra poder salvar
    private static final long serialVersionUID = 1L;

    private int id;
    private int idEmpresa; // Pra saber de qual empresa é
    private String nome;
    private float valor;
    private String categoria;

    public Produto(int id, int idEmpresa, String nome, float valor, String categoria) {
        this.id = id;
        this.idEmpresa = idEmpresa;
        this.nome = nome;
        this.valor = valor;
        this.categoria = categoria;
    }

    // --- Getters ---
    public int getId() { return id; }
    public int getIdEmpresa() { return idEmpresa; }
    public String getNome() { return nome; }
    public float getValor() { return valor; }
    public String getCategoria() { return categoria; }

    // --- Setters (usado no editarProduto) ---
    public void setNome(String nome) { this.nome = nome; }
    public void setValor(float valor) { this.valor = valor; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}