import java.util.Scanner;

/**
 * Classe principal de teste para a Árvore AVL.
 *
 * Totalmente alinhada ao material e slides do Prof. Robinson Alves (árvoreAVL.pdf).
 *
 * Funcionalidades interativas via terminal:
 *  - Inclusão de nós
 *  - Remoção de nós
 *  - Busca de nós
 *  - Visualização da árvore com chaves e Fatores de Balanceamento [FB]
 *  - Demonstração passo a passo da especificação da tarefa
 *  - Execução dos exercícios práticos dos slides do Prof. Robinson Alves (Slide 38 e Slide 42)
 *
 * @author Larissa Samara
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArvoreAVL avl = new ArvoreAVL();
        int opcao = -1;

        System.out.println("======================================================================");
        System.out.println("          SISTEMA DE TESTES - ÁRVORE AVL (HERANÇA DE ABP)            ");
        System.out.println("           Conforme Slides e Aulas do Prof. Robinson Alves            ");
        System.out.println("                         Aluna: Larissa Samara                        ");
        System.out.println("======================================================================");

        while (opcao != 0) {
            System.out.println("\n----------------------------- MENU -----------------------------------");
            System.out.println("  1 - Inserir nó");
            System.out.println("  2 - Remover nó");
            System.out.println("  3 - Buscar nó");
            System.out.println("  4 - Mostrar árvore (com chaves e FB)");
            System.out.println("  5 - Executar exemplo da especificação (10, 5, 15, 2, 8, 22 -> 25 -> -5)");
            System.out.println("  6 - Executar exercício do Slide 38 (Inserir: 10, 20..90)");
            System.out.println("  7 - Executar exercício do Slide 42 (Remover: 40, 25, 50..60)");
            System.out.println("  8 - Limpar / Reiniciar árvore");
            System.out.println("  0 - Sair");
            System.out.print("Escolha uma opção: ");

            try {
                String entrada = scanner.nextLine().trim();
                if (entrada.isEmpty()) continue;
                opcao = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println(">> Opção inválida! Digite um número inteiro.");
                continue;
            }

            switch (opcao) {
                case 1:
                    System.out.print("Digite o valor inteiro para inserir: ");
                    try {
                        int valor = Integer.parseInt(scanner.nextLine().trim());
                        if (avl.buscar(valor)) {
                            System.out.println(">> A chave " + valor + " já existe na árvore (duplicatas não permitidas).");
                        } else {
                            avl.inserir(valor);
                            System.out.println(">> Valor " + valor + " inserido com sucesso!");
                            System.out.println("\nÁrvore atual:");
                            avl.mostrar();
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(">> Valor inválido! Digite um número inteiro.");
                    }
                    break;

                case 2:
                    System.out.print("Digite o valor inteiro para remover: ");
                    try {
                        int valor = Integer.parseInt(scanner.nextLine().trim());
                        if (!avl.buscar(valor)) {
                            System.out.println(">> A chave " + valor + " não foi encontrada na árvore.");
                        } else {
                            avl.remover(valor);
                            System.out.println(">> Valor " + valor + " removido com sucesso!");
                            System.out.println("\nÁrvore atual:");
                            avl.mostrar();
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(">> Valor inválido! Digite um número inteiro.");
                    }
                    break;

                case 3:
                    System.out.print("Digite o valor inteiro para buscar: ");
                    try {
                        int valor = Integer.parseInt(scanner.nextLine().trim());
                        boolean achou = avl.buscar(valor);
                        if (achou) {
                            System.out.println(">> CHAVE ENCONTRADA: " + valor + " está presente na árvore.");
                        } else {
                            System.out.println(">> CHAVE NÃO ENCONTRADA: " + valor + " não existe na árvore.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(">> Valor inválido! Digite um número inteiro.");
                    }
                    break;

                case 4:
                    System.out.println("\nVisualização da Árvore AVL (Chave [FB]):");
                    avl.mostrar();
                    break;

                case 5:
                    executarExemploEspecificacao();
                    avl = new ArvoreAVL();
                    int[] nos = {10, 8, 15, 2, 22, 25};
                    for (int n : nos) avl.inserir(n);
                    break;

                case 6:
                    executarExercicioSlide38();
                    break;

                case 7:
                    executarExercicioSlide42();
                    break;

                case 8:
                    avl = new ArvoreAVL();
                    System.out.println(">> Árvore reiniciada com sucesso! (Vazia)");
                    break;

                case 0:
                    System.out.println(">> Encerrando o programa. Até logo!");
                    break;

                default:
                    System.out.println(">> Opção inválida. Escolha entre 0 e 8.");
            }
        }

        scanner.close();
    }

    /**
     * Demonstração passo a passo do exemplo fornecido na especificação:
     * Inserção de 10, 5, 15, 2, 8, 22 -> Inserir 25 com RES em 15 -> Remover 5.
     */
    private static void executarExemploEspecificacao() {
        System.out.println("\n======================================================================");
        System.out.println("            DEMONSTRAÇÃO DO EXEMPLO DA ESPECIFICAÇÃO DA TAREFA        ");
        System.out.println("======================================================================");

        ArvoreAVL arvore = new ArvoreAVL();
        int[] chavesIniciais = {10, 5, 15, 2, 8, 22};

        System.out.println("\n[PASSO 1] Inserindo chaves iniciais: 10, 5, 15, 2, 8, 22...");
        for (int c : chavesIniciais) {
            arvore.inserir(c);
        }

        System.out.println("\nÁrvore resultante após inserções iniciais:");
        arvore.mostrar();

        System.out.println("\n[PASSO 2] Inserindo chave 25...");
        System.out.println("Ao inserir 25 na subárvore direita de 22 (que é filho direito de 15):");
        System.out.println(" - 22 passa para FB = -1");
        System.out.println(" - 15 passa para FB = -2 (DESBALANCEAMENTO detectado à direita)");
        System.out.println("\nEstado conceitual antes da rotação:");
        System.out.println("                                  10[0]");
        System.out.println("              5[0]                          15[-2]");
        System.out.println("    2[0]                8[0]                          22[-1]");
        System.out.println("                                                                25[0]");

        System.out.println("\nComo FB(15) = -2 e FB(22) = -1 (FB <= 0 na subárvore direita):");
        System.out.println(">> Rotação Esquerda Simples (RES) aplicada no nó 15.");
        System.out.println("Fórmulas do Slide do Prof. Robinson Alves:");
        System.out.println("  FB_B_novo = FB_B + 1 - min(FB_A, 0) = -2 + 1 - (-1) = 0");
        System.out.println("  FB_A_novo = FB_A + 1 + max(FB_B_novo, 0) = -1 + 1 + 0 = 0");

        arvore.inserir(25);
        System.out.println("\nÁrvore após rotação RES em 15:");
        arvore.mostrar();

        System.out.println("\n[PASSO 3] Removendo chave 5...");
        System.out.println("Detalhe da implementação:");
        System.out.println(" 1. O nó 5 possui dois filhos (2 à esquerda e 8 à direita).");
        System.out.println(" 2. Localiza o sucessor in-order: o menor elemento da subárvore direita (nó 8).");
        System.out.println(" 3. Copia a chave 8 para a posição do 5.");
        System.out.println(" 4. Remove o nó folha original 8 da subárvore direita.");
        System.out.println(" 5. A subárvore direita de 8 diminuiu. Recalcula FB(8):");
        System.out.println("    FB(8) = he(2) - hd(vazio) = 1 - 0 = +1.");
        System.out.println(" 6. Regra do slide do Prof. Robinson Alves na remoção:");
        System.out.println("    'Se FB(Vantecessor) != 0 pare'.");
        System.out.println("    Como FB(8) = +1 != 0, a alteração de altura NÃO se propaga para a raiz 10 (FB(10) continua 0).");

        arvore.remover(5);
        System.out.println("\nÁrvore resultante após remover 5:");
        arvore.mostrar();
        System.out.println("\n======================================================================");
    }

    /**
     * Exercício do Slide 38 do Prof. Robinson Alves:
     * Inserir na árvore AVL inicialmente vazia os seguintes elementos:
     * 10, 20, 30, 40, 50, 25, 60, 70, 80 e 90.
     */
    private static void executarExercicioSlide38() {
        System.out.println("\n======================================================================");
        System.out.println("             EXERCÍCIO DO SLIDE 38 - PROF. ROBINSON ALVES             ");
        System.out.println("    Inserção sucessiva: 10, 20, 30, 40, 50, 25, 60, 70, 80 e 90       ");
        System.out.println("======================================================================");

        ArvoreAVL arv38 = new ArvoreAVL();
        int[] valores = {10, 20, 30, 40, 50, 25, 60, 70, 80, 90};

        for (int v : valores) {
            System.out.println("\n>> Inserindo: " + v);
            arv38.inserir(v);
            arv38.mostrar();
        }
        System.out.println("\n>> Árvore final do Slide 38 balanceada com sucesso!");
    }

    /**
     * Exercício do Slide 42 do Prof. Robinson Alves:
     * Remover na árvore: 40, 25, 50, 10, 35, 30, 20, 70 e 60.
     */
    private static void executarExercicioSlide42() {
        System.out.println("\n======================================================================");
        System.out.println("             EXERCÍCIO DO SLIDE 42 - PROF. ROBINSON ALVES             ");
        System.out.println("======================================================================");

        ArvoreAVL arv42 = new ArvoreAVL();
        // Árvore do Slide 42 tem os nós: 30, 20, 10, 50, 25, 40, 35, 70, 60, 80
        int[] base = {30, 20, 10, 50, 25, 40, 35, 70, 60, 80};
        for (int n : base) arv42.inserir(n);

        System.out.println("Árvore inicial do Slide 42:");
        arv42.mostrar();

        int[] remocoes = {40, 25, 50, 10, 35, 30, 20, 70, 60};
        for (int r : remocoes) {
            System.out.println("\n>> Removendo: " + r);
            arv42.remover(r);
            arv42.mostrar();
        }
        System.out.println("\n>> Remoções do Slide 42 executadas com sucesso!");
    }
}
