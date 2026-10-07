/**
 * Classe que representa um nó na Árvore Binária de Pesquisa (ABP).
 * Serve como classe base para os nós da Árvore AVL através de herança.
 *
 * @author Larissa Samara
 */
public class NoABP {
    public int chave;
    public NoABP esquerdo;
    public NoABP direito;

    /**
     * Construtor do nó básico de ABP.
     * @param chave Valor inteiro armazenado no nó.
     */
    public NoABP(int chave) {
        this.chave = chave;
        this.esquerdo = null;
        this.direito = null;
    }
}
