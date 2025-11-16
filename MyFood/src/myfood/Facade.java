package myfood;

import myfood.controllers.ControladorUsuario;
import myfood.controllers.ControladorEmpresa;
import myfood.controllers.ControladorProduto;
import myfood.controllers.ControladorPedido;

// A Facade é o ponto de entrada, ela só repassa as chamadas
// pros controladores certos.

public class Facade {

    private ControladorUsuario controladorUsuario;
    private ControladorEmpresa controladorEmpresa;
    private ControladorProduto controladorProduto;
    private ControladorPedido controladorPedido;

    public Facade() {
        // Inicializa os "cérebros" do sistema
        this.controladorUsuario = new ControladorUsuario();
        this.controladorEmpresa = new ControladorEmpresa(this.controladorUsuario);
        this.controladorProduto = new ControladorProduto(this.controladorEmpresa);
        this.controladorPedido = new ControladorPedido(this.controladorUsuario, this.controladorEmpresa, this.controladorProduto);
    }

    // --- Comandos do Sistema ---

    // Zera tudo, limpa os mapas e apaga os arquivos de save.
    public void zerarSistema() {
        this.controladorUsuario.zerar();
        this.controladorEmpresa.zerar();
        this.controladorProduto.zerar();
        this.controladorPedido.zerar();
    }

    // Salva o estado atual em arquivos.
    public void encerrarSistema() {
        this.controladorUsuario.salvarDados();
        this.controladorEmpresa.salvarDados();
        this.controladorProduto.salvarDados();
        this.controladorPedido.salvarDados();
    }

    // --- Comandos de Usuário ---

    // Cria um Cliente (sem CPF)
    public void criarUsuario(String nome, String email, String senha, String endereco) throws Exception {
        this.controladorUsuario.criarCliente(nome, email, senha, endereco);
    }

    // Cria um Dono (com CPF)
    public void criarUsuario(String nome, String email, String senha, String endereco, String cpf) throws Exception {
        this.controladorUsuario.criarDono(nome, email, senha, endereco, cpf);
    }

    // Faz o login e retorna o ID do usuário.
    public int login(String email, String senha) throws Exception {
        return this.controladorUsuario.login(email, senha);
    }

    // Pega um dado específico do usuário.
    public String getAtributoUsuario(int id, String atributo) throws Exception {
        return this.controladorUsuario.getAtributoUsuario(id, atributo);
    }

    // --- Comandos de Empresa ---

    public int criarEmpresa(String tipoEmpresa, int dono, String nome, String endereco, String tipoCozinha) throws Exception {
        return this.controladorEmpresa.criarEmpresa(tipoEmpresa, dono, nome, endereco, tipoCozinha);
    }

    public String getEmpresasDoUsuario(int idDono) throws Exception {
        return this.controladorEmpresa.getEmpresasDoUsuario(idDono);
    }

    public String getAtributoEmpresa(int empresa, String atributo) throws Exception {
        return this.controladorEmpresa.getAtributoEmpresa(empresa, atributo);
    }

    public int getIdEmpresa(int idDono, String nome, int indice) throws Exception {
        return this.controladorEmpresa.getIdEmpresa(idDono, nome, indice);
    }

    // --- Comandos de Produto ---

    public int criarProduto(int empresa, String nome, float valor, String categoria) throws Exception {
        return this.controladorProduto.criarProduto(empresa, nome, valor, categoria);
    }

    public void editarProduto(int produto, String nome, float valor, String categoria) throws Exception {
        this.controladorProduto.editarProduto(produto, nome, valor, categoria);
    }

    public String getProduto(String nome, int empresa, String atributo) throws Exception {
        return this.controladorProduto.getProduto(nome, empresa, atributo);
    }

    public String listarProdutos(int empresa) throws Exception {
        return this.controladorProduto.listarProdutos(empresa);
    }

    // --- Comandos de Pedido ---

    public int criarPedido(int cliente, int empresa) throws Exception {
        return this.controladorPedido.criarPedido(cliente, empresa);
    }

    public int getNumeroPedido(int cliente, int empresa, int indice) throws Exception {
        return this.controladorPedido.getNumeroPedido(cliente, empresa, indice);
    }

    public void adicionarProduto(int numero, int produto) throws Exception {
        this.controladorPedido.adicionarProduto(numero, produto);
    }

    public String getPedidos(int pedido, String atributo) throws Exception {
        return this.controladorPedido.getPedidos(pedido, atributo);
    }

    public void fecharPedido(int numero) throws Exception {
        this.controladorPedido.fecharPedido(numero);
    }

    public void removerProduto(int pedido, String produto) throws Exception {
        this.controladorPedido.removerProduto(pedido, produto);
    }
}