/**
 * Implementação da Árvore AVL através de herança de ArvoreBinariaPesquisa (ABP),
 * estritamente alinhada ao material e slides do Prof. Robinson Alves (árvoreAVL.pdf).
 *
 * Características principais:
 * 1. Herança direta de ArvoreBinariaPesquisa.
 * 2. Operações básicas em O(log n).
 * 3. Fator de Balanceamento:
 *    FB(v) = he(v) - hd(v)
 *    +1: subárvore esquerda mais alta que a direita
 *     0: subárvore esquerda igual a direita
 *    -1: subárvore direita mais alta do que a esquerda
 * 4. Fórmulas exatas de recálculo dos slides do Prof. Robinson Alves:
 *    - Rotação Esquerda Simples (RES):
 *        FB_B_novo = FB_B + 1 - min(FB_A, 0);
 *        FB_A_novo = FB_A + 1 + max(FB_B_novo, 0);
 *    - Rotação Simples a Direita (RSD):
 *        FB_B_novo = FB_B - 1 - max(FB_A, 0);
 *        FB_A_novo = FB_A - 1 + min(FB_B_novo, 0);
 * 5. Critérios de parada na atualização do FB dos antecessores:
 *    - Inserção: "Se FB(Vantecessor) == 0 pare"
 *    - Remoção:  "Se FB(Vantecessor) != 0 pare"
 *
 * @author Larissa Samara
 */
public class ArvoreAVL extends ArvoreBinariaPesquisa {

    // Flag que indica se a altura da subárvore variou durante a recursão
    private boolean mudouAltura;

    public ArvoreAVL() {
        super();
    }

    /**
     * Inserção com balanceamento AVL em O(log n).
     * @param chave Valor a ser inserido.
     */
    @Override
    public void inserir(int chave) {
        this.mudouAltura = false;
        this.raiz = inserirAVL((NoAVL) this.raiz, chave);
    }

    private NoAVL inserirAVL(NoAVL atual, int chave) {
        if (atual == null) {
            this.mudouAltura = true;
            return new NoAVL(chave);
        }

        if (chave < atual.chave) {
            atual.esquerdo = inserirAVL((NoAVL) atual.esquerdo, chave);
            if (this.mudouAltura) {
                // Inserção na ArvEsq: soma +1 ao FB conforme tabela do slide
                switch (atual.fb) {
                    case -1:
                        atual.fb = 0;
                        this.mudouAltura = false; // "Se FB(Vantecessor) == 0 pare"
                        break;
                    case 0:
                        atual.fb = 1;
                        this.mudouAltura = true;  // Continua propagando
                        break;
                    case 1:
                        atual.fb = 2;             // Desbalanceou à esquerda!
                        atual = balancearEsquerda(atual);
                        this.mudouAltura = false; // Após rotação na inserção, altura volta à original: pare
                        break;
                }
            }
        } else if (chave > atual.chave) {
            atual.direito = inserirAVL((NoAVL) atual.direito, chave);
            if (this.mudouAltura) {
                // Inserção na ArvDir: subtrai -1 do FB conforme tabela do slide
                switch (atual.fb) {
                    case 1:
                        atual.fb = 0;
                        this.mudouAltura = false; // "Se FB(Vantecessor) == 0 pare"
                        break;
                    case 0:
                        atual.fb = -1;
                        this.mudouAltura = true;  // Continua propagando
                        break;
                    case -1:
                        atual.fb = -2;            // Desbalanceou à direita!
                        atual = balancearDireita(atual);
                        this.mudouAltura = false; // Após rotação na inserção, altura volta à original: pare
                        break;
                }
            }
        } else {
            // Chave duplicada: não permite duplicatas na árvore
            this.mudouAltura = false;
        }

        return atual;
    }

    /**
     * Trata o desbalanceamento após inserção ou remoção na subárvore esquerda (FB = +2).
     * Regra do slide do Prof. Robinson Alves:
     * - Se FB >= 0 na subárvore esquerda: Rotação Simples a Direita (RSD)
     * - Se FB < 0 na subárvore esquerda: Rotação Dupla a Direita (RDD)
     */
    private NoAVL balancearEsquerda(NoAVL pivo) {
        NoAVL filhoEsq = (NoAVL) pivo.esquerdo;
        if (filhoEsq.fb >= 0) {
            return rotacaoDireitaSimples(pivo);
        } else {
            return rotacaoDuplaDireita(pivo);
        }
    }

    /**
     * Trata o desbalanceamento após inserção ou remoção na subárvore direita (FB = -2).
     * Regra do slide do Prof. Robinson Alves:
     * - Se FB <= 0 na subárvore direita: Rotação Esquerda Simples (RES)
     * - Se FB > 0 na subárvore direita: Rotação Dupla a Esquerda (RDE)
     */
    private NoAVL balancearDireita(NoAVL pivo) {
        NoAVL filhoDir = (NoAVL) pivo.direito;
        if (filhoDir.fb <= 0) {
            return rotacaoEsquerdaSimples(pivo);
        } else {
            return rotacaoDuplaEsquerda(pivo);
        }
    }

    /**
     * Rotação Esquerda Simples (RES).
     * Conforme os slides do Prof. Robinson Alves:
     *
     * Estrutura inicial:
     *        B (desbalanceado, FB = -2)
     *         \
     *          A (filho direito, FB <= 0)
     *         / \
     *        T2  T3
     *
     * Passos do slide:
     * 1. Guarde a subárvore direita (A).
     * 2. Troque a subárvore guardada pela subárvore esquerda da árvore guardada (B.direito = A.esquerdo).
     * 3. Ponha na subárvore esquerda da subárvore guardada a árvore restante (A.esquerdo = B).
     * 4. Atualize o FB pelas fórmulas dos slides:
     *      FB_B_novo = FB_B + 1 - min(FB_A, 0);
     *      FB_A_novo = FB_A + 1 + max(FB_B_novo, 0);
     *
     * @param B Nó desbalanceado (pivô original)
     * @return Nova raiz da subárvore (A)
     */
    public NoAVL rotacaoEsquerdaSimples(NoAVL B) {
        NoAVL A = (NoAVL) B.direito;
        B.direito = A.esquerdo;
        A.esquerdo = B;

        // Fórmulas exatas do slide do Prof. Robinson Alves:
        int FB_B_novo = B.fb + 1 - Math.min(A.fb, 0);
        int FB_A_novo = A.fb + 1 + Math.max(FB_B_novo, 0);

        B.fb = FB_B_novo;
        A.fb = FB_A_novo;

        return A;
    }

    /**
     * Rotação Simples a Direita (RSD).
     * Conforme os slides do Prof. Robinson Alves (simétrica à RES):
     *
     * Estrutura inicial:
     *          B (desbalanceado, FB = +2)
     *         /
     *        A (filho esquerdo, FB >= 0)
     *       / \
     *      T1  T2
     *
     * Fórmulas do slide:
     *   FB_B_novo = FB_B - 1 - max(FB_A, 0);
     *   FB_A_novo = FB_A - 1 + min(FB_B_novo, 0);
     *
     * @param B Nó desbalanceado (pivô original)
     * @return Nova raiz da subárvore (A)
     */
    public NoAVL rotacaoDireitaSimples(NoAVL B) {
        NoAVL A = (NoAVL) B.esquerdo;
        B.esquerdo = A.direito;
        A.direito = B;

        // Fórmulas exatas do slide do Prof. Robinson Alves:
        int FB_B_novo = B.fb - 1 - Math.max(A.fb, 0);
        int FB_A_novo = A.fb - 1 + Math.min(FB_B_novo, 0);

        B.fb = FB_B_novo;
        A.fb = FB_A_novo;

        return A;
    }

    /**
     * Rotação Dupla a Esquerda (RDE).
     * Passos conforme o slide do Prof. Robinson Alves:
     * 1. Efetua-se uma rotação simples direita na subárvore direita do nó desbalanceado (RSD).
     * 2. Realiza-se uma rotação simples esquerda no nó desbalanceado (RES).
     *
     * @param B Nó desbalanceado
     * @return Nova raiz da subárvore
     */
    public NoAVL rotacaoDuplaEsquerda(NoAVL B) {
        B.direito = rotacaoDireitaSimples((NoAVL) B.direito);
        return rotacaoEsquerdaSimples(B);
    }

    /**
     * Rotação Dupla a Direita (RDD).
     * Passos conforme o slide do Prof. Robinson Alves:
     * 1. Efetuar uma rotação simples esquerda na subárvore esquerda do nó desbalanceado (RES).
     * 2. Realizar uma rotação simples direita no nó desbalanceado (RSD).
     *
     * @param B Nó desbalanceado
     * @return Nova raiz da subárvore
     */
    public NoAVL rotacaoDuplaDireita(NoAVL B) {
        B.esquerdo = rotacaoEsquerdaSimples((NoAVL) B.esquerdo);
        return rotacaoDireitaSimples(B);
    }

    // Aliases utilizando os acrônimos dos slides (RES, RSD, RDE, RDD)
    public NoAVL RES(NoAVL B) { return rotacaoEsquerdaSimples(B); }
    public NoAVL RSD(NoAVL B) { return rotacaoDireitaSimples(B); }
    public NoAVL RDE(NoAVL B) { return rotacaoDuplaEsquerda(B); }
    public NoAVL RDD(NoAVL B) { return rotacaoDuplaDireita(B); }

    // Aliases para compatibilidade de nomenclaturas
    public NoAVL rotacaoSimplesEsquerda(NoAVL B) { return rotacaoEsquerdaSimples(B); }
    public NoAVL rotacaoSimplesDireita(NoAVL B) { return rotacaoDireitaSimples(B); }

    /**
     * Remoção com balanceamento AVL em O(log n).
     * @param chave Valor a ser removido.
     */
    @Override
    public void remover(int chave) {
        this.mudouAltura = false;
        this.raiz = removerAVL((NoAVL) this.raiz, chave);
    }

    private NoAVL removerAVL(NoAVL atual, int chave) {
        if (atual == null) {
            this.mudouAltura = false;
            return null;
        }

        if (chave < atual.chave) {
            atual.esquerdo = removerAVL((NoAVL) atual.esquerdo, chave);
            if (this.mudouAltura) {
                // Remoção na ArvEsq: subtrai -1 do FB conforme tabela do slide
                atual = aposRemocaoEsquerda(atual);
            }
        } else if (chave > atual.chave) {
            atual.direito = removerAVL((NoAVL) atual.direito, chave);
            if (this.mudouAltura) {
                // Remoção na ArvDir: soma +1 ao FB conforme tabela do slide
                atual = aposRemocaoDireita(atual);
            }
        } else {
            // Encontrou o nó a ser removido
            if (atual.esquerdo == null) {
                this.mudouAltura = true;
                return (NoAVL) atual.direito;
            } else if (atual.direito == null) {
                this.mudouAltura = true;
                return (NoAVL) atual.esquerdo;
            } else {
                // Nó com 2 filhos: substitui pelo sucessor (menor da subárvore direita)
                NoAVL sucessor = (NoAVL) obterMinimo(atual.direito);
                atual.chave = sucessor.chave;
                atual.direito = removerAVL((NoAVL) atual.direito, sucessor.chave);
                if (this.mudouAltura) {
                    atual = aposRemocaoDireita(atual);
                }
            }
        }
        return atual;
    }

    /**
     * Trata o ajuste de FB e eventuais rotações quando a subárvore esquerda diminui de altura.
     * Conforme tabela do slide: ArvEsq -1, "Se FB(Vantecessor) != 0 pare".
     */
    private NoAVL aposRemocaoEsquerda(NoAVL atual) {
        switch (atual.fb) {
            case 1:
                atual.fb = 0;
                this.mudouAltura = true;  // Altura diminuiu, continua propagando
                break;
            case 0:
                atual.fb = -1;
                this.mudouAltura = false; // "Se FB(Vantecessor) != 0 pare"
                break;
            case -1:
                atual.fb = -2;            // Desbalanceou à direita!
                NoAVL dir = (NoAVL) atual.direito;
                if (dir.fb <= 0) {
                    // FB <= 0 na subárvore direita: RES
                    atual = rotacaoEsquerdaSimples(atual);
                    this.mudouAltura = (atual.fb == 0); // Se virou 0 encolheu; se != 0 pare
                } else {
                    // FB > 0 na subárvore direita: RDE
                    atual = rotacaoDuplaEsquerda(atual);
                    this.mudouAltura = true;
                }
                break;
        }
        return atual;
    }

    /**
     * Trata o ajuste de FB e eventuais rotações quando a subárvore direita diminui de altura.
     * Conforme tabela do slide: ArvDir +1, "Se FB(Vantecessor) != 0 pare".
     */
    private NoAVL aposRemocaoDireita(NoAVL atual) {
        switch (atual.fb) {
            case -1:
                atual.fb = 0;
                this.mudouAltura = true;  // Altura diminuiu, continua propagando
                break;
            case 0:
                atual.fb = 1;
                this.mudouAltura = false; // "Se FB(Vantecessor) != 0 pare"
                break;
            case 1:
                atual.fb = 2;             // Desbalanceou à esquerda!
                NoAVL esq = (NoAVL) atual.esquerdo;
                if (esq.fb >= 0) {
                    // FB >= 0 na subárvore esquerda: RSD
                    atual = rotacaoDireitaSimples(atual);
                    this.mudouAltura = (atual.fb == 0); // Se virou 0 encolheu; se != 0 pare
                } else {
                    // FB < 0 na subárvore esquerda: RDD
                    atual = rotacaoDuplaDireita(atual);
                    this.mudouAltura = true;
                }
                break;
        }
        return atual;
    }
}
