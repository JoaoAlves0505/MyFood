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
import myfood.models.Cliente;
import myfood.models.Restaurante;
import myfood.models.Usuario;

// "Cérebro" que gerencia as Empresas/Restaurantes
public class ControladorEmpresa {

    // Mapas pra guardar as empresas
    private Map<Integer, Restaurante> empresasPorId;
    private Map<String, Restaurante> empresasPorNome; // Pra checar nome duplicado global
    private Map<Integer, List<Restaurante>> empresasPorDono; // Pra listar rápido
    private int proximoId;

    private static final String ARQUIVO_DADOS_EMPRESA = "empresas_data.dat";

    // Referência pro ControladorUsuario (pra checar se é Dono)
    private ControladorUsuario controladorUsuario;

    public ControladorEmpresa(ControladorUsuario controladorUsuario) {
        this.controladorUsuario = controladorUsuario; // Recebe a referência
        this.carregarDados();
    }

    // --- LÓGICA DE PERSISTÊNCIA ---

    public void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_DADOS_EMPRESA))) {
            oos.writeObject(this.empresasPorId);
            oos.writeObject(this.empresasPorNome);
            oos.writeObject(this.empresasPorDono);
            oos.writeObject(this.proximoId);
        } catch (Exception e) {
            System.err.println("Erro ao salvar dados de empresas: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void carregarDados() {
        File f = new File(ARQUIVO_DADOS_EMPRESA);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARQUIVO_DADOS_EMPRESA))) {
                this.empresasPorId = (Map<Integer, Restaurante>) ois.readObject();
                this.empresasPorNome = (Map<String, Restaurante>) ois.readObject();
                this.empresasPorDono = (Map<Integer, List<Restaurante>>) ois.readObject();
                this.proximoId = (int) ois.readObject();
            } catch (Exception e) {
                System.err.println("Erro ao carregar dados de empresas, começando do zero: " + e.getMessage());
                this.zerar();
            }
        } else {
            this.zerar();
        }
    }

    public void zerar() {
        this.empresasPorId = new HashMap<>();
        this.empresasPorNome = new HashMap<>();
        this.empresasPorDono = new HashMap<>();
        this.proximoId = 1;

        File f = new File(ARQUIVO_DADOS_EMPRESA);
        if (f.exists()) {
            f.delete();
        }
    }

    // --- MÉTODOS DA US2 ---

    public int criarEmpresa(String tipoEmpresa, int dono, String nome, String endereco, String tipoCozinha) throws Exception {
        // Validação 1: O usuário é um Dono? Pede pro outro controlador.
        Usuario u = this.controladorUsuario.getUsuario(dono);
        if (u instanceof Cliente) {
            throw new Exception("Usuario nao pode criar uma empresa");
        }

        // Validação 2: MESMO DONO, MESMO NOME, MESMO ENDEREÇO?
        List<Restaurante> listaDoDono = this.empresasPorDono.getOrDefault(dono, new ArrayList<>());
        for (Restaurante r : listaDoDono) {
            if (r.temMesmoNomeEndereco(nome, endereco)) {
                throw new Exception("Proibido cadastrar duas empresas com o mesmo nome e local");
            }
        }

        // Validação 3: NOME JÁ EXISTE EM OUTRO DONO?
        // Se o nome existe E o dono é diferente, falha.
        // Se o nome existe E o dono é o mesmo, o passo 2 já tratou.
        if (this.empresasPorNome.containsKey(nome)) {
            Restaurante empresaExistente = this.empresasPorNome.get(nome);
            if (empresaExistente.getIdDono() != dono) {
                throw new Exception("Empresa com esse nome ja existe");
            }
        }

        // 4. Se passou em tudo, cria a empresa
        int id = this.proximoId++;
        Restaurante r = new Restaurante(id, dono, nome, endereco, tipoCozinha);

        this.empresasPorId.put(id, r);
        // Só bota no mapa global se for a primeira vez que esse nome aparece
        if (!this.empresasPorNome.containsKey(nome)) {
            this.empresasPorNome.put(nome, r);
        }

        listaDoDono.add(r);
        this.empresasPorDono.put(dono, listaDoDono);

        return id;
    }

    public String getEmpresasDoUsuario(int idDono) throws Exception {
        // Validação: O usuário é um Dono?
        Usuario u = this.controladorUsuario.getUsuario(idDono);
        if (u instanceof Cliente) {
            throw new Exception("Usuario nao pode criar uma empresa");
        }

        List<Restaurante> lista = this.empresasPorDono.getOrDefault(idDono, new ArrayList<>());

        // Formata a String do jeito que o teste espera: "{[[...], [...]]}"
        StringBuilder sb = new StringBuilder("{[");
        for (int i = 0; i < lista.size(); i++) {
            Restaurante r = lista.get(i);
            sb.append("[").append(r.getNome()).append(", ").append(r.getEndereco()).append("]");
            if (i < lista.size() - 1) {
                sb.append(", "); // Vírgula entre os itens
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    public String getAtributoEmpresa(int empresa, String atributo) throws Exception {
        Restaurante r = this.empresasPorId.get(empresa);

        if (r == null) {
            throw new Exception("Empresa nao cadastrada");
        }

        // Checa se o atributo veio nulo (teste da US2)
        if (atributo == null) {
            throw new Exception("Atributo invalido");
        }

        switch (atributo) {
            case "nome":
                return r.getNome();
            case "endereco":
                return r.getEndereco();
            case "tipoCozinha":
                return r.getTipoCozinha();
            case "dono":
                // Busca o nome do dono no outro controlador
                Usuario dono = this.controladorUsuario.getUsuario(r.getIdDono());
                return dono.getNome();
            default:
                validarStringNulaOuVazia(atributo, "Atributo invalido");
                throw new Exception("Atributo invalido");
        }
    }

    public int getIdEmpresa(int idDono, String nome, int indice) throws Exception {
        validarStringNulaOuVazia(nome, "Nome invalido");
        if (indice < 0) {
            throw new Exception("Indice invalido");
        }

        // Filtra a lista do dono pra pegar só as com o nome certo
        List<Restaurante> listaDoDono = this.empresasPorDono.getOrDefault(idDono, new ArrayList<>());
        List<Restaurante> empresasComEsseNome = new ArrayList<>();
        for (Restaurante r : listaDoDono) {
            if (r.getNome().equals(nome)) {
                empresasComEsseNome.add(r);
            }
        }

        // Validações de índice
        if (empresasComEsseNome.isEmpty()) {
            throw new Exception("Nao existe empresa com esse nome");
        }
        if (indice >= empresasComEsseNome.size()) {
            throw new Exception("Indice maior que o esperado");
        }

        // Retorna o ID da empresa naquele índice
        return empresasComEsseNome.get(indice).getId();
    }

    // Helper pro ControladorProduto poder buscar uma empresa
    public Restaurante getEmpresa(int id) {
        return this.empresasPorId.get(id); // Retorna o objeto ou null
    }

    // Helper pra checar string nula/vazia
    private void validarStringNulaOuVazia(String valor, String mensagemErro) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception(mensagemErro);
        }
    }
}