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

import myfood.models.*; // Importa todos os modelos

// "Cérebro" que gerencia os Pedidos
public class ControladorPedido {

    private Map<Integer, Pedido> pedidosPorNumero;
    private Map<Integer, List<Pedido>> pedidosPorCliente; // (idCliente -> Lista de Pedidos)
    private int proximoNumero;

    private static final String ARQUIVO_DADOS_PEDIDO = "pedidos_data.dat";

    // Referências para os outros controladores
    private ControladorUsuario controladorUsuario;
    private ControladorEmpresa controladorEmpresa;
    private ControladorProduto controladorProduto;

    public ControladorPedido(ControladorUsuario cu, ControladorEmpresa ce, ControladorProduto cp) {
        this.controladorUsuario = cu;
        this.controladorEmpresa = ce;
        this.controladorProduto = cp;
        this.carregarDados();
    }

    // --- LÓGICA DE PERSISTÊNCIA ---

    public void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_DADOS_PEDIDO))) {
            oos.writeObject(this.pedidosPorNumero);
            oos.writeObject(this.pedidosPorCliente);
            oos.writeObject(this.proximoNumero);
        } catch (Exception e) {
            System.err.println("Erro ao salvar dados de pedidos: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void carregarDados() {
        File f = new File(ARQUIVO_DADOS_PEDIDO);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARQUIVO_DADOS_PEDIDO))) {
                this.pedidosPorNumero = (Map<Integer, Pedido>) ois.readObject();
                this.pedidosPorCliente = (Map<Integer, List<Pedido>>) ois.readObject();
                this.proximoNumero = (int) ois.readObject();
            } catch (Exception e) {
                // Se der erro, zera
                this.zerar();
            }
        } else {
            // Se não existe, zera
            this.zerar();
        }
    }

    public void zerar() {
        this.pedidosPorNumero = new HashMap<>();
        this.pedidosPorCliente = new HashMap<>();
        this.proximoNumero = 1;

        File f = new File(ARQUIVO_DADOS_PEDIDO);
        if (f.exists()) {
            f.delete();
        }
    }

    // --- MÉTODOS DA US4 ---

    public int criarPedido(int cliente, int empresa) throws Exception {
        // Validação 1: Dono não pode fazer pedido
        Usuario u = this.controladorUsuario.getUsuario(cliente);
        if (u instanceof DonoDeEmpresa) {
            throw new Exception("Dono de empresa nao pode fazer um pedido");
        }

        // Validação 2: Cliente já tem pedido aberto nessa empresa?
        List<Pedido> pedidosDoCliente = this.pedidosPorCliente.getOrDefault(cliente, new ArrayList<>());
        for (Pedido p : pedidosDoCliente) {
            if (p.getIdEmpresa() == empresa && p.getEstado().equals("aberto")) {
                throw new Exception("Nao e permitido ter dois pedidos em aberto para a mesma empresa");
            }
        }

        // Se passou, cria o pedido
        int numero = this.proximoNumero++;
        Pedido p = new Pedido(numero, cliente, empresa);

        this.pedidosPorNumero.put(numero, p);
        pedidosDoCliente.add(p); // Adiciona na lista do cliente
        this.pedidosPorCliente.put(cliente, pedidosDoCliente);

        return numero;
    }

    public void adicionarProduto(int numero, int produto) throws Exception {
        Pedido p = this.pedidosPorNumero.get(numero);

        // Valida se o pedido existe
        if (p == null) {
            // O teste us1_1.txt espera essa msg exata
            throw new Exception("Nao existe pedido em aberto");
        }
        // Valida se o pedido já foi fechado
        if (!p.getEstado().equals("aberto")) {
            throw new Exception("Nao e possivel adcionar produtos a um pedido fechado");
        }

        // Busca o produto
        Produto prod = this.controladorProduto.getProdutoById(produto);
        if (prod == null) {
            throw new Exception("Produto nao encontrado");
        }

        // Validação: O produto é da mesma empresa do pedido?
        if (prod.getIdEmpresa() != p.getIdEmpresa()) {
            throw new Exception("O produto nao pertence a essa empresa");
        }

        // Se tudo OK, adiciona
        p.adicionarProduto(prod);
    }

    // Pega um atributo do pedido
    public String getPedidos(int pedido, String atributo) throws Exception {
        Pedido p = this.pedidosPorNumero.get(pedido);

        if (p == null) {
            throw new Exception("Pedido nao encontrado");
        }
        if (atributo == null || atributo.trim().isEmpty()) {
            throw new Exception("Atributo invalido");
        }

        switch (atributo) {
            case "cliente":
                Usuario u = this.controladorUsuario.getUsuario(p.getIdCliente());
                return u.getNome();
            case "empresa":
                Restaurante r = this.controladorEmpresa.getEmpresa(p.getIdEmpresa());
                return r.getNome();
            case "estado":
                return p.getEstado();
            case "produtos":
                // Formata a lista de produtos: "{[Prod1, Prod2]}"
                StringBuilder sb = new StringBuilder("{[");
                List<Produto> produtos = p.getProdutos();
                for (int i = 0; i < produtos.size(); i++) {
                    sb.append(produtos.get(i).getNome());
                    if (i < produtos.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]}");
                return sb.toString();
            case "valor":
                // Formata o valor total pra "15.00"
                return String.format("%.2f", p.getValorTotal()).replace(",", ".");
            default:
                throw new Exception("Atributo nao existe");
        }
    }

    public void fecharPedido(int numero) throws Exception {
        Pedido p = this.pedidosPorNumero.get(numero);
        if (p == null) {
            throw new Exception("Pedido nao encontrado");
        }
        // Apenas muda o estado
        p.setEstado("preparando");
    }

    public void removerProduto(int pedido, String produto) throws Exception {
        Pedido p = this.pedidosPorNumero.get(pedido);

        if (p == null) {
            throw new Exception("Pedido nao encontrado");
        }
        if (produto == null || produto.trim().isEmpty()) {
            throw new Exception("Produto invalido");
        }
        // Não pode remover se o pedido já foi fechado
        if (!p.getEstado().equals("aberto")) {
            throw new Exception("Nao e possivel remover produtos de um pedido fechado");
        }

        // Tenta remover (metodo no modelo Pedido.java)
        boolean removeu = p.removerProduto(produto);
        if (!removeu) {
            // Se não removeu, é porque não achou
            throw new Exception("Produto nao encontrado");
        }
    }

    public int getNumeroPedido(int cliente, int empresa, int indice) throws Exception {
        List<Pedido> pedidosDoCliente = this.pedidosPorCliente.getOrDefault(cliente, new ArrayList<>());

        // Filtra só os pedidos para a empresa correta
        List<Pedido> pedidosFiltrados = new ArrayList<>();
        for (Pedido p : pedidosDoCliente) {
            if (p.getIdEmpresa() == empresa) {
                pedidosFiltrados.add(p);
            }
        }

        // A lista já está na ordem de criação (FIFO)
        if (indice < 0 || indice >= pedidosFiltrados.size()) {
            throw new Exception("Indice invalido ou pedido nao existe");
        }

        // Retorna o número do pedido naquele índice
        return pedidosFiltrados.get(indice).getNumero();
    }
}