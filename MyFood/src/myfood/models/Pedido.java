package myfood.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Modelo do Pedido
public class Pedido implements Serializable {

    // Pra poder salvar
    private static final long serialVersionUID = 1L;

    private int numero;
    private int idCliente;
    private int idEmpresa;
    private String estado;
    private List<Produto> produtos; // Lista de produtos do pedido

    public Pedido(int numero, int idCliente, int idEmpresa) {
        this.numero = numero;
        this.idCliente = idCliente;
        this.idEmpresa = idEmpresa;
        this.estado = "aberto"; // Pedido sempre começa "aberto"
        this.produtos = new ArrayList<>();
    }

    // --- Getters ---
    public int getNumero() { return this.numero; }
    public int getIdCliente() { return this.idCliente; }
    public int getIdEmpresa() { return this.idEmpresa; }
    public String getEstado() { return this.estado; }
    public List<Produto> getProdutos() { return this.produtos; }

    // --- Setters ---
    public void setEstado(String estado) {
        this.estado = estado;
    }

    // --- Lógica do Pedido ---

    public void adicionarProduto(Produto p) {
        this.produtos.add(p);
    }

    // Remove o primeiro produto da lista com esse nome.
    // Retorna true se achou e removeu, false se não achou.
    public boolean removerProduto(String nomeProduto) {
        Produto paraRemover = null;
        for (Produto p : this.produtos) {
            if (p.getNome().equals(nomeProduto)) {
                paraRemover = p;
                break; // Acha o primeiro e para
            }
        }

        if (paraRemover != null) {
            this.produtos.remove(paraRemover);
            return true;
        }
        return false;
    }

    // Calcula o valor total somando todos os produtos da lista.
    public float getValorTotal() {
        float total = 0.0f;
        for (Produto p : this.produtos) {
            total += p.getValor();
        }
        return total;
    }
}