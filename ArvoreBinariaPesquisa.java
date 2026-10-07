import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação da Árvore Binária de Pesquisa (ABP).
 * Fornece as operações fundamentais (busca, inserção, remoção, exibição)
 * e serve como superclasse para a ArvoreAVL.
 *
 * @author Larissa Samara
 */
public class ArvoreBinariaPesquisa {
    protected NoABP raiz;

    public ArvoreBinariaPesquisa() {
        this.raiz = null;
    }

    public NoABP getRaiz() {
        return this.raiz;
    }

    public boolean estaVazia() {
        return this.raiz == null;
    }

    /**
     * Busca por uma chave na árvore.
     * Complexidade de tempo: O(h), sendo O(log n) se balanceada.
     * @param chave Valor a ser buscado.
     * @return true se a chave existe na árvore, false caso contrário.
     */
    public boolean buscar(int chave) {
        return buscarRec(this.raiz, chave) != null;
    }

    protected NoABP buscarRec(NoABP atual, int chave) {
        if (atual == null || atual.chave == chave) {
            return atual;
        }
        if (chave < atual.chave) {
            return buscarRec(atual.esquerdo, chave);
        } else {
            return buscarRec(atual.direito, chave);
        }
    }

    /**
     * Inserção padrão de ABP.
     * @param chave Valor a ser inserido.
     */
    public void inserir(int chave) {
        this.raiz = inserirRec(this.raiz, chave);
    }

    protected NoABP inserirRec(NoABP atual, int chave) {
        if (atual == null) {
            return new NoABP(chave);
        }
        if (chave < atual.chave) {
            atual.esquerdo = inserirRec(atual.esquerdo, chave);
        } else if (chave > atual.chave) {
            atual.direito = inserirRec(atual.direito, chave);
        }
        return atual;
    }

    /**
     * Remoção padrão de ABP.
     * @param chave Valor a ser removido.
     */
    public void remover(int chave) {
        this.raiz = removerRec(this.raiz, chave);
    }

    protected NoABP removerRec(NoABP atual, int chave) {
        if (atual == null) {
            return null;
        }

        if (chave < atual.chave) {
            atual.esquerdo = removerRec(atual.esquerdo, chave);
        } else if (chave > atual.chave) {
            atual.direito = removerRec(atual.direito, chave);
        } else {
            // Caso 1 e Caso 2: nó folha ou com apenas 1 filho
            if (atual.esquerdo == null) {
                return atual.direito;
            } else if (atual.direito == null) {
                return atual.esquerdo;
            }

            // Caso 3: nó com 2 filhos
            // Substitui pelo sucessor (menor elemento da subárvore direita)
            NoABP sucessor = obterMinimo(atual.direito);
            atual.chave = sucessor.chave;
            atual.direito = removerRec(atual.direito, sucessor.chave);
        }
        return atual;
    }

    /**
     * Encontra o nó com menor valor na subárvore.
     */
    protected NoABP obterMinimo(NoABP atual) {
        NoABP temp = atual;
        while (temp != null && temp.esquerdo != null) {
            temp = temp.esquerdo;
        }
        return temp;
    }

    /**
     * Exibe a árvore visualmente por níveis, alinhando as colunas
     * de acordo com a ordem simétrica (in-order).
     */
    public void mostrar() {
        mostrarFormatado(this.raiz);
    }

    protected void mostrarFormatado(NoABP raizSubarvore) {
        if (raizSubarvore == null) {
            System.out.println("   (Árvore vazia)");
            return;
        }

        Map<NoABP, Integer> xPos = new HashMap<>();
        Map<NoABP, Integer> yPos = new HashMap<>();
        int[] col = new int[]{0};
        calcularPosicoes(raizSubarvore, 0, col, xPos, yPos);

        int profundidadeMaxima = 0;
        for (int d : yPos.values()) {
            if (d > profundidadeMaxima) {
                profundidadeMaxima = d;
            }
        }

        int larguraColuna = 10;
        for (int d = 0; d <= profundidadeMaxima; d++) {
            StringBuilder linha = new StringBuilder();
            List<NoABP> nosDoNivel = new ArrayList<>();
            for (Map.Entry<NoABP, Integer> entry : yPos.entrySet()) {
                if (entry.getValue() == d) {
                    nosDoNivel.add(entry.getKey());
                }
            }
            nosDoNivel.sort(Comparator.comparingInt(xPos::get));

            for (NoABP n : nosDoNivel) {
                int colunaAlvo = xPos.get(n) * larguraColuna + 4;
                while (linha.length() < colunaAlvo) {
                    linha.append(" ");
                }
                String rotulo;
                if (n instanceof NoAVL) {
                    rotulo = n.chave + "[" + ((NoAVL) n).fb + "]";
                } else {
                    rotulo = String.valueOf(n.chave);
                }
                linha.append(rotulo);
            }
            System.out.println(linha.toString());
        }
    }

    private void calcularPosicoes(NoABP n, int profundidade, int[] col, Map<NoABP, Integer> xPos, Map<NoABP, Integer> yPos) {
        if (n == null) return;
        calcularPosicoes(n.esquerdo, profundidade + 1, col, xPos, yPos);
        xPos.put(n, col[0]++);
        yPos.put(n, profundidade);
        calcularPosicoes(n.direito, profundidade + 1, col, xPos, yPos);
    }
}
