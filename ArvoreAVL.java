/**
 * Implementação da Árvore AVL através de herança de ArvoreBinariaPesquisa (ABP).
 *
 * Todas as operações básicas (busca, inserção, remoção) rodam estritamente em O(log n).
 * O recálculo dos fatores de balanceamento após as rotações é feito em O(1) utilizando
 * as fórmulas matemáticas deduzidas em aula.
 *
 * Convenção do Fator de Balanceamento (FB):
 * FB(p) = altura(subárvore esquerda) - altura(subárvore direita)
 *
 * Fórmulas de Atualização de FB após Rotação Simples:
 * 1. Rotação Simples para a Esquerda (S.E.):
 *    FB'(A) = FB(A) + 1 - min(FB(B), 0)
 *    FB'(B) = FB(B) + 1 + max(FB'(A), 0)
 *
 * 2. Rotação Simples para a Direita (S.D.):
 *    FB'(A) = FB(A) - 1 - max(FB(B), 0)
 *    FB'(B) = FB(B) - 1 + min(FB'(A), 0)
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
                switch (atual.fb) {
                    case -1:
                        // Tinha subárvore direita mais alta; inseriu na esquerda -> agora equilibrado
                        atual.fb = 0;
                        this.mudouAltura = false;
                        break;
                    case 0:
                        // Estava equilibrado; inseriu na esquerda -> subárvore esquerda ficou mais alta
                        atual.fb = 1;
                        this.mudouAltura = true;
                        break;
                    case 1:
                        // Já estava +1; inseriu na esquerda -> FB iria para +2 (desbalanceamento)
                        atual = balancearEsquerda(atual);
                        this.mudouAltura = false;
                        break;
                }
            }
        } else if (chave > atual.chave) {
            atual.direito = inserirAVL((NoAVL) atual.direito, chave);
            if (this.mudouAltura) {
                switch (atual.fb) {
                    case 1:
                        // Tinha subárvore esquerda mais alta; inseriu na direita -> agora equilibrado
                        atual.fb = 0;
                        this.mudouAltura = false;
                        break;
                    case 0:
                        // Estava equilibrado; inseriu na direita -> subárvore direita ficou mais alta
                        atual.fb = -1;
                        this.mudouAltura = true;
                        break;
                    case -1:
                        // Já estava -1; inseriu na direita -> FB iria para -2 (desbalanceamento)
                        atual = balancearDireita(atual);
                        this.mudouAltura = false;
                        break;
                }
            }
        } else {
            // Chave duplicada: não permite duplicidade
            this.mudouAltura = false;
        }

        return atual;
    }

    /**
     * Trata o desbalanceamento após inserção na subárvore esquerda (FB iria para +2).
     */
    private NoAVL balancearEsquerda(NoAVL pivo) {
        NoAVL filhoEsq = (NoAVL) pivo.esquerdo;
        if (filhoEsq.fb >= 0) {
            // Sinais iguais (+2 e +1): Rotação Simples à Direita (S.D.)
            return rotacaoSimplesDireita(pivo);
        } else {
            // Sinais opostos (+2 e -1): Rotação Dupla à Direita (D.D.)
            return rotacaoDuplaDireita(pivo);
        }
    }

    /**
     * Trata o desbalanceamento após inserção na subárvore direita (FB iria para -2).
     */
    private NoAVL balancearDireita(NoAVL pivo) {
        NoAVL filhoDir = (NoAVL) pivo.direito;
        if (filhoDir.fb <= 0) {
            // Sinais iguais (-2 e -1): Rotação Simples à Esquerda (S.E.)
            return rotacaoSimplesEsquerda(pivo);
        } else {
            // Sinais opostos (-2 e +1): Rotação Dupla à Esquerda (D.E.)
            return rotacaoDuplaEsquerda(pivo);
        }
    }

    /**
     * Rotação Simples para a Esquerda (S.E.).
     *
     * Estrutura:
     *      A                 B
     *       \               / \
     *        B     ==>     A   T3
     *       / \             \
     *      T2  T3           T2
     *
     * Fórmulas de recálculo:
     * FB'(A) = FB(A) + 1 - min(FB(B), 0)
     * FB'(B) = FB(B) + 1 + max(FB'(A), 0)
     *
     * @param A Nó desbalanceado (pivô)
     * @return Nova raiz da subárvore (B)
     */
    public NoAVL rotacaoSimplesEsquerda(NoAVL A) {
        NoAVL B = (NoAVL) A.direito;
        A.direito = B.esquerdo;
        B.esquerdo = A;

        // Se A.fb estava em -1 no momento que detectou desbalanceamento na inserção,
        // o valor efetivo antes da rotação é -2.
        int fbA_antes = (A.fb == -1) ? -2 : A.fb;

        int novoFbA = fbA_antes + 1 - Math.min(B.fb, 0);
        int novoFbB = B.fb + 1 + Math.max(novoFbA, 0);

        A.fb = novoFbA;
        B.fb = novoFbB;

        return B;
    }

    /**
     * Rotação Simples para a Direita (S.D.).
     *
     * Estrutura:
     *        A               B
     *       /               / \
     *      B       ==>     T1  A
     *     / \                 /
     *    T1  T2              T2
     *
     * Fórmulas de recálculo:
     * FB'(A) = FB(A) - 1 - max(FB(B), 0)
     * FB'(B) = FB(B) - 1 + min(FB'(A), 0)
     *
     * @param A Nó desbalanceado (pivô)
     * @return Nova raiz da subárvore (B)
     */
    public NoAVL rotacaoSimplesDireita(NoAVL A) {
        NoAVL B = (NoAVL) A.esquerdo;
        A.esquerdo = B.direito;
        B.direito = A;

        // Se A.fb estava em +1 no momento que detectou desbalanceamento na inserção,
        // o valor efetivo antes da rotação é +2.
        int fbA_antes = (A.fb == 1) ? 2 : A.fb;

        int novoFbA = fbA_antes - 1 - Math.max(B.fb, 0);
        int novoFbB = B.fb - 1 + Math.min(novoFbA, 0);

        A.fb = novoFbA;
        B.fb = novoFbB;

        return B;
    }

    /**
     * Rotação Dupla para a Esquerda (D.E. ou Direita-Esquerda).
     * Primeiro executa uma rotação simples à direita no filho direito,
     * e em seguida uma rotação simples à esquerda no pai.
     *
     * @param A Nó desbalanceado
     * @return Nova raiz da subárvore
     */
    public NoAVL rotacaoDuplaEsquerda(NoAVL A) {
        A.direito = rotacaoSimplesDireita((NoAVL) A.direito);
        return rotacaoSimplesEsquerda(A);
    }

    /**
     * Rotação Dupla para a Direita (D.D. ou Esquerda-Direita).
     * Primeiro executa uma rotação simples à esquerda no filho esquerdo,
     * e em seguida uma rotação simples à direita no pai.
     *
     * @param A Nó desbalanceado
     * @return Nova raiz da subárvore
     */
    public NoAVL rotacaoDuplaDireita(NoAVL A) {
        A.esquerdo = rotacaoSimplesEsquerda((NoAVL) A.esquerdo);
        return rotacaoSimplesDireita(A);
    }

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
                atual = aposRemocaoEsquerda(atual);
            }
        } else if (chave > atual.chave) {
            atual.direito = removerAVL((NoAVL) atual.direito, chave);
            if (this.mudouAltura) {
                atual = aposRemocaoDireita(atual);
            }
        } else {
            // Encontrou o nó a remover
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
     */
    private NoAVL aposRemocaoEsquerda(NoAVL atual) {
        switch (atual.fb) {
            case 1:
                // Tinha esquerda mais alta (+1); a esquerda diminuiu -> agora equilibrado (0)
                atual.fb = 0;
                this.mudouAltura = true;
                break;
            case 0:
                // Estava equilibrado (0); a esquerda diminuiu -> subárvore direita ficou mais alta (-1)
                atual.fb = -1;
                this.mudouAltura = false; // Altura máxima da subárvore não diminuiu
                break;
            case -1:
                // Já tinha subárvore direita mais alta (-1); esquerda diminuiu -> desbalanceou (iria para -2)
                atual.fb = -2;
                NoAVL dir = (NoAVL) atual.direito;
                if (dir.fb <= 0) {
                    // Rotação Simples à Esquerda (S.E.)
                    atual = rotacaoSimplesEsquerda(atual);
                    this.mudouAltura = (atual.fb == 0);
                } else {
                    // Rotação Dupla à Esquerda (D.E.)
                    atual = rotacaoDuplaEsquerda(atual);
                    this.mudouAltura = true;
                }
                break;
        }
        return atual;
    }

    /**
     * Trata o ajuste de FB e eventuais rotações quando a subárvore direita diminui de altura.
     */
    private NoAVL aposRemocaoDireita(NoAVL atual) {
        switch (atual.fb) {
            case -1:
                // Tinha direita mais alta (-1); a direita diminuiu -> agora equilibrado (0)
                atual.fb = 0;
                this.mudouAltura = true;
                break;
            case 0:
                // Estava equilibrado (0); a direita diminuiu -> subárvore esquerda ficou mais alta (+1)
                atual.fb = 1;
                this.mudouAltura = false; // Altura máxima da subárvore não diminuiu
                break;
            case 1:
                // Já tinha subárvore esquerda mais alta (+1); direita diminuiu -> desbalanceou (iria para +2)
                atual.fb = 2;
                NoAVL esq = (NoAVL) atual.esquerdo;
                if (esq.fb >= 0) {
                    // Rotação Simples à Direita (S.D.)
                    atual = rotacaoSimplesDireita(atual);
                    this.mudouAltura = (atual.fb == 0);
                } else {
                    // Rotação Dupla à Direita (D.D.)
                    atual = rotacaoDuplaDireita(atual);
                    this.mudouAltura = true;
                }
                break;
        }
        return atual;
    }
}
