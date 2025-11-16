package myfood.models;

import java.io.Serializable;

// Modelo do Cliente.
// Por enquanto, só herda de Usuario e não tem nada a mais.
public class Cliente extends Usuario implements Serializable {

    // Pra poder salvar
    private static final long serialVersionUID = 1L;

    public Cliente(int id, String nome, String email, String senha, String endereco) {
        // Chama o construtor da classe "pai" (Usuario)
        super(id, nome, email, senha, endereco);
    }

}