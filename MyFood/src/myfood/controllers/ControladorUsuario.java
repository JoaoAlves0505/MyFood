package myfood.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import myfood.models.Cliente;
import myfood.models.DonoDeEmpresa;
import myfood.models.Usuario;

// "Cérebro" que gerencia os usuários (criação, login, busca).
public class ControladorUsuario {

    // Mapas pra guardar os usuários e achar rápido
    private Map<Integer, Usuario> usuariosPorId;
    private Map<String, Usuario> usuariosPorEmail;
    private int proximoId; // Contador pro ID ficar único

    // Nome do arquivo de save
    private static final String ARQUIVO_DADOS = "myfood_data.dat";

    public ControladorUsuario() {
        // Tenta carregar os dados salvos quando o sistema inicia
        this.carregarDados();
    }

    // --- LÓGICA DE PERSISTÊNCIA ---

    // Salva os mapas e o contador no arquivo
    public void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_DADOS))) {
            oos.writeObject(this.usuariosPorId);
            oos.writeObject(this.usuariosPorEmail);
            oos.writeObject(this.proximoId);
        } catch (Exception e) {
            // Se der erro, só avisa no console
            System.err.println("Erro ao salvar dados de Usuario: " + e.getMessage());
        }
    }

    // Carrega os dados do arquivo
    @SuppressWarnings("unchecked")
    private void carregarDados() {
        File f = new File(ARQUIVO_DADOS);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARQUIVO_DADOS))) {
                // A ordem de leitura tem que ser a MESMA da escrita
                this.usuariosPorId = (Map<Integer, Usuario>) ois.readObject();
                this.usuariosPorEmail = (Map<String, Usuario>) ois.readObject();
                this.proximoId = (int) ois.readObject();
            } catch (Exception e) {
                // Se o arquivo quebrou ou mudou, começa do zero
                System.err.println("Erro ao carregar dados de Usuario, começando do zero: " + e.getMessage());
                this.zerar();
            }
        } else {
            // Se não tem arquivo de save, começa do zero
            this.zerar();
        }
    }

    // Limpa os mapas e apaga o arquivo de save.
    public void zerar() {
        this.usuariosPorId = new HashMap<>();
        this.usuariosPorEmail = new HashMap<>();
        this.proximoId = 1; // Reseta o ID

        File f = new File(ARQUIVO_DADOS);
        if (f.exists()) {
            f.delete();
        }
    }

    // --- MÉTODOS DA US1 ---

    // Cria um Cliente
    public void criarCliente(String nome, String email, String senha, String endereco) throws Exception {
        // Validações
        validarCamposComuns(nome, email, senha, endereco);
        validarEmailUnico(email);

        // Se passou, cria e armazena
        int id = this.proximoId++;
        Cliente cliente = new Cliente(id, nome, email, senha, endereco);

        this.usuariosPorId.put(id, cliente);
        this.usuariosPorEmail.put(email, cliente);
    }

    // Cria um Dono de Empresa
    public void criarDono(String nome, String email, String senha, String endereco, String cpf) throws Exception {
        // Valida campos comuns
        validarCamposComuns(nome, email, senha, endereco);

        // Validação do CPF (tem que vir ANTES de checar email duplicado)
        validarStringNulaOuVazia(cpf, "CPF invalido");
        if (cpf.length() != 14) {
            throw new Exception("CPF invalido");
        }

        // Validação do email (só depois que os outros dados são válidos)
        validarEmailUnico(email);

        // Se passou, cria e armazena
        int id = this.proximoId++;
        DonoDeEmpresa dono = new DonoDeEmpresa(id, nome, email, senha, endereco, cpf);

        this.usuariosPorId.put(id, dono);
        this.usuariosPorEmail.put(email, dono);
    }

    // Tenta fazer login
    public int login(String email, String senha) throws Exception {
        // Valida se não tão nulos/vazios
        validarStringNulaOuVazia(email, "Login ou senha invalidos");
        validarStringNulaOuVazia(senha, "Login ou senha invalidos");

        Usuario u = this.usuariosPorEmail.get(email);

        // Checa se o usuário existe e se a senha tá certa
        if (u == null || !u.getSenha().equals(senha)) {
            throw new Exception("Login ou senha invalidos");
        }

        return u.getId(); // Sucesso, retorna o ID
    }

    // Pega um atributo (nome, email, etc.) do usuário
    public String getAtributoUsuario(int id, String atributo) throws Exception {
        Usuario u = this.usuariosPorId.get(id);

        if (u == null) {
            throw new Exception("Usuario nao cadastrado.");
        }

        // Retorna o atributo que o teste pediu
        switch (atributo) {
            case "nome":
                return u.getNome();
            case "email":
                return u.getEmail();
            case "senha":
                return u.getSenha(); // US1_2 precisa disso
            case "endereco":
                return u.getEndereco();
            case "cpf":
                if (u instanceof DonoDeEmpresa) {
                    // Se for Dono, faz o "cast" e pega o CPF
                    return ((DonoDeEmpresa) u).getCpf();
                } else {
                    // Cliente não tem CPF
                    throw new Exception("Atributo 'cpf' nao existe para este usuario.");
                }
            default:
                throw new Exception("Atributo invalido.");
        }
    }

    // Metodo "helper" pros outros controladores poderem buscar um usuário
    public Usuario getUsuario(int id) {
        return this.usuariosPorId.get(id);
    }

    // --- MÉTODOS DE AJUDA (VALIDAÇÃO) ---

    // Valida os campos que todo usuário tem
    private void validarCamposComuns(String nome, String email, String senha, String endereco) throws Exception {
        validarStringNulaOuVazia(nome, "Nome invalido");
        validarStringNulaOuVazia(email, "Email invalido");
        validarStringNulaOuVazia(senha, "Senha invalido");
        validarStringNulaOuVazia(endereco, "Endereco invalido");

        // Validação de email "mínima"
        if (!email.contains("@")) {
            throw new Exception("Email invalido");
        }
    }

    // Checa se o email já tá no mapa
    private void validarEmailUnico(String email) throws Exception {
        if (this.usuariosPorEmail.containsKey(email)) {
            throw new Exception("Conta com esse email ja existe");
        }
    }

    // Helper pra não ficar repetindo if (valor == null || valor.trim().isEmpty())
    private void validarStringNulaOuVazia(String valor, String mensagemErro) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception(mensagemErro);
        }
    }
}