package myfood.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import myfood.models.Produto;
import myfood.models.Restaurante;

// "Cérebro" que gerencia os Produtos
public class ControladorProduto {

    // Mapas de armazenamento
    private Map<Integer, Produto> produtosPorId;
    private Map<Integer, List<Produto>> produtosPorEmpresa; // (idEmpresa -> Lista de Produtos)
    private int proximoId;

    private static final String ARQUIVO_DADOS_PRODUTO = "produtos_data.dat";

    // Referência ao ControladorEmpresa (pra validar)
    private ControladorEmpresa controladorEmpresa;

    public ControladorProduto(ControladorEmpresa controladorEmpresa) {
        this.controladorEmpresa = controladorEmpresa;
        this.carregarDados();
    }

    // --- LÓGICA DE PERSISTÊNCIA (Salvar, Carregar, Zerar) ---

    public void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_DADOS_PRODUTO))) {
            oos.writeObject(this.produtosPorId);
            oos.writeObject(this.produtosPorEmpresa);
            oos.writeObject(this.proximoId);
        } catch (Exception e) {
            System.err.println("Erro ao salvar dados de produtos: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void carregarDados() {
        File f = new File(ARQUIVO_DADOS_PRODUTO);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARQUIVO_DADOS_PRODUTO))) {
                this.produtosPorId = (Map<Integer, Produto>) ois.readObject();
                this.produtosPorEmpresa = (Map<Integer, List<Produto>>) ois.readObject();
                this.proximoId = (int) ois.readObject();
            } catch (Exception e) {
                System.err.println("Erro ao carregar dados de produtos, começando do zero: " + e.getMessage());
                this.zerar();
            }
        } else {
            this.zerar();
        }
    }

    public void zerar() {
        this.produtosPorId = new HashMap<>();
        this.produtosPorEmpresa = new HashMap<>();
        this.proximoId = 1;

        File f = new File(ARQUIVO_DADOS_PRODUTO);
        if (f.exists()) {
            f.delete();
        }
    }

    // --- MÉTODOS DA US3 ---

    public int criarProduto(int empresa, String nome, float valor, String categoria) throws Exception {
        // Validações de entrada
        validarStringNulaOuVazia(nome, "Nome invalido");
        validarStringNulaOuVazia(categoria, "Categoria invalido");
        if (valor < 0) {
            throw new Exception("Valor invalido");
        }

        // Validação de regra: Mesmo produto (nome) na mesma empresa
        List<Produto> lista = this.produtosPorEmpresa.getOrDefault(empresa, new ArrayList<>());
        for (Produto p : lista) {
            if (p.getNome().equals(nome)) {
                throw new Exception("Ja existe um produto com esse nome para essa empresa");
            }
        }

        // Se passou, cria e armazena
        int id = this.proximoId++;
        Produto p = new Produto(id, empresa, nome, valor, categoria);

        this.produtosPorId.put(id, p);
        lista.add(p);
        this.produtosPorEmpresa.put(empresa, lista);

        return id;
    }

    public void editarProduto(int produto, String nome, float valor, String categoria) throws Exception {
        // Busca o produto
        Produto p = this.produtosPorId.get(produto);
        if (p == null) {
            throw new Exception("Produto nao cadastrado");
        }

        // Validações (mesmas da criação)
        validarStringNulaOuVazia(nome, "Nome invalido");
        validarStringNulaOuVazia(categoria, "Categoria invalido");
        if (valor < 0) {
            throw new Exception("Valor invalido");
        }

        // Atualiza os dados do objeto (que já tá no mapa)
        p.setNome(nome);
        p.setValor(valor);
        p.setCategoria(categoria);
    }

    public String getProduto(String nome, int empresa, String atributo) throws Exception {
        // Busca o produto pelo nome dentro da lista da empresa
        List<Produto> lista = this.produtosPorEmpresa.getOrDefault(empresa, new ArrayList<>());
        Produto p = null;
        for (Produto prod : lista) {
            if (prod.getNome().equals(nome)) {
                p = prod;
                break; // Achou
            }
        }

        if (p == null) {
            throw new Exception("Produto nao encontrado");
        }

        // Retorna o atributo
        switch (atributo) {
            case "valor":
                // Formata o float pra "4.40" como o teste espera
                return String.format("%.2f", p.getValor()).replace(",", ".");
            case "categoria":
                return p.getCategoria();
            case "empresa":
                // Pede o nome da empresa pro outro controlador
                Restaurante r = this.controladorEmpresa.getEmpresa(p.getIdEmpresa());
                return r.getNome();
            default:
                throw new Exception("Atributo nao existe");
        }
    }

    public String listarProdutos(int empresa) throws Exception {
        // Valida se a empresa existe (usando o helper do outro controlador)
        if (this.controladorEmpresa.getEmpresa(empresa) == null) {
            throw new Exception("Empresa nao encontrada");
        }

        List<Produto> lista = this.produtosPorEmpresa.getOrDefault(empresa, new ArrayList<>());

        // Formata a string de saída: "{[Prod1, Prod2]}"
        StringBuilder sb = new StringBuilder("{[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(lista.get(i).getNome());
            if (i < lista.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    // Helper pro ControladorPedido poder buscar um produto
    public Produto getProdutoById(int id) {
        return this.produtosPorId.get(id); // Retorna o objeto Produto ou null
    }

    // Helper pra checar string nula/vazia
    private void validarStringNulaOuVazia(String valor, String mensagemErro) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception(mensagemErro);
        }
    }
}