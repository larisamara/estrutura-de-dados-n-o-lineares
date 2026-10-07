import java.util.Scanner;

/**
 * Classe principal de teste para a Árvore AVL.
 *
 * Permite ao usuário interagir via terminal realizando:
 *  - Inclusão de nós
 *  - Remoção de nós
 *  - Busca de nós
 *  - Visualização da árvore com chaves e Fatores de Balanceamento [FB]
 *  - Execução automatizada do caso de teste da aula com explicação passo a passo
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
        System.out.println("                   Aluna: Larissa Samara                              ");
        System.out.println("======================================================================");

        while (opcao != 0) {
            System.out.println("\n----------------------------- MENU -----------------------------------");
            System.out.println("  1 - Inserir nó");
            System.out.println("  2 - Remover nó");
            System.out.println("  3 - Buscar nó");
            System.out.println("  4 - Mostrar árvore (com chaves e FB)");
            System.out.println("  5 - Executar exemplo completo da aula (passo a passo)");
            System.out.println("  6 - Limpar / Reiniciar árvore");
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
                    executarExemploAula(scanner);
                    // Atualiza a árvore em memória com o exemplo da aula
                    avl = new ArvoreAVL();
                    int[] nos = {10, 8, 15, 2, 22, 25}; // Estado final após remoção do 5
                    for (int n : nos) avl.inserir(n);
                    break;

                case 6:
                    avl = new ArvoreAVL();
                    System.out.println(">> Árvore reiniciada com sucesso! (Vazia)");
                    break;

                case 0:
                    System.out.println(">> Encerrando o programa. Até logo!");
                    break;

                default:
                    System.out.println(">> Opção inválida. Escolha entre 0 e 6.");
            }
        }

        scanner.close();
    }

    /**
     * Executa passo a passo a demonstração exigida na especificação do trabalho:
     * 1. Inserção de 10, 5, 15, 2, 8, 22.
     * 2. Inserção de 25 com rotação S.E. em 15.
     * 3. Remoção de 5 com substituição por sucessor (8).
     */
    private static void executarExemploAula(Scanner scanner) {
        System.out.println("\n======================================================================");
        System.out.println("            DEMONSTRAÇÃO DO EXEMPLO DA ESPECIFICAÇÃO DA AULA          ");
        System.out.println("======================================================================");

        ArvoreAVL arvoreExemplo = new ArvoreAVL();
        int[] chavesIniciais = {10, 5, 15, 2, 8, 22};

        System.out.println("\n[PASSO 1] Inserindo chaves iniciais: 10, 5, 15, 2, 8, 22...");
        for (int c : chavesIniciais) {
            arvoreExemplo.inserir(c);
        }

        System.out.println("\nÁrvore resultante após inserções iniciais:");
        arvoreExemplo.mostrar();

        System.out.println("\n[PASSO 2] Inserindo chave 25...");
        System.out.println("Ao inserir 25 na subárvore direita de 22 (que é filho de 15):");
        System.out.println(" - 22 fica com FB = -1");
        System.out.println(" - 15 fica com FB = -2 (DESBALANCEAMENTO detectado!)");
        System.out.println("\nEstado conceitual antes da rotação:");
        System.out.println("                                  10[0]");
        System.out.println("              5[0]                          15[-2]");
        System.out.println("    2[0]                8[0]                          22[-1]");
        System.out.println("                                                                25[0]");

        System.out.println("\nComo FB(15) = -2 e FB(22) = -1 (sinais iguais negativos):");
        System.out.println(">> Rotação Simples para a Esquerda (S.E.) aplicada no nó 15.");
        System.out.println("Aplicação das fórmulas de recálculo:");
        System.out.println("  FB'(15) = FB(15) + 1 - min(FB(22), 0) = -2 + 1 - (-1) = 0");
        System.out.println("  FB'(22) = FB(22) + 1 + max(FB'(15), 0) = -1 + 1 + 0 = 0");

        arvoreExemplo.inserir(25);
        System.out.println("\nÁrvore após rotação S.E. em 15:");
        arvoreExemplo.mostrar();

        System.out.println("\n[PASSO 3] Removendo chave 5...");
        System.out.println("Detalhe da implementação:");
        System.out.println(" 1. O nó 5 possui dois filhos (2 e 8).");
        System.out.println(" 2. Localiza o sucessor in-order: o menor nó da subárvore direita, que é o 8.");
        System.out.println(" 3. Copia a chave 8 para a posição do 5.");
        System.out.println(" 4. Remove o nó folha original 8 da subárvore direita.");
        System.out.println(" 5. A subárvore direita de 8 diminuiu de altura. Recalcula FB(8):");
        System.out.println("    FB(8) = altura(esq=2) - altura(dir=vazio) = 1 - 0 = +1.");
        System.out.println(" 6. Como a altura total da subárvore enraizada em 8 permaneceu 2,");
        System.out.println("    a alteração de altura NÃO se propaga para a raiz 10 (FB(10) continua 0).");

        arvoreExemplo.remover(5);
        System.out.println("\nÁrvore resultante após remover 5:");
        arvoreExemplo.mostrar();
        System.out.println("\n======================================================================");
        System.out.println(">> Demonstração concluída. A árvore atual do menu agora reflete este estado.");
    }
}
