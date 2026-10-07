/**
 * Classe que representa um nó na Árvore AVL, estendendo NoABP por herança.
 * Adiciona o atributo de Fator de Balanceamento (FB).
 *
 * Convenção adotada na disciplina:
 * FB(nó) = altura(subárvore esquerda) - altura(subárvore direita)
 *
 * Valores possíveis para nós balanceados em AVL: -1, 0, +1.
 * Desbalanceamento ocorre quando FB atinge +2 ou -2.
 *
 * @author Larissa Samara
 */
public class NoAVL extends NoABP {
    public int fb;

    /**
     * Construtor do nó AVL.
     * @param chave Valor inteiro a ser armazenado.
     */
    public NoAVL(int chave) {
        super(chave);
        this.fb = 0;
    }
}
