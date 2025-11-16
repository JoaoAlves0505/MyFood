package myfood.models;

import java.io.Serializable;

// Modelo do Dono.
// É um Usuario que também tem um CPF.
public class DonoDeEmpresa extends Usuario implements Serializable {

    // Pra poder salvar
    private static final long serialVersionUID = 1L;

    private String cpf;

    public DonoDeEmpresa(int id, String nome, String email, String senha, String endereco, String cpf) {
        // Chama o construtor da classe "pai" (Usuario)
        super(id, nome, email, senha, endereco);
        this.cpf = cpf;
    }

    // Getter específico para o CPF
    public String getCpf() {
        return cpf;
    }
}