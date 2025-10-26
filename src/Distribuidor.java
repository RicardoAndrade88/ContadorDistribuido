import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Distribuidor {

    // Endereços dos servidores Receptores
    private static final String[] IPS = { 
        "127.0.0.1",
        "127.0.0.1",
        "127.0.0.1"
    };

    private static final int[] PORTAS = { 
        12345,
        12346,
        12347
    };

    public static void main(String[] args) {

        int numServidores = IPS.length;
        Scanner sc = new Scanner(System.in);

        System.out.print("Digite o tamanho do vetor grande: ");
        int tamanho = sc.nextInt();

        byte[] vetor = new byte[tamanho];
        for (int i = 0; i < tamanho; i++) {
            vetor[i] = (byte) ((Math.random() * 201) - 100);
        }

        System.out.print("Deseja imprimir o vetor na tela? (s/n): ");
        String imprimir = sc.next();

        if (imprimir.equalsIgnoreCase("s")) {
            for (byte b : vetor) System.out.print(b + " ");
            System.out.println();
        }

        System.out.print("Deseja informar um número a contar ou sortear? (digite 'informar' ou 'sortear'): ");
        String escolha = sc.next();

        byte procurado;
        if (escolha.equalsIgnoreCase("informar")) {
            System.out.print("Digite o número a contar (-100 a 100, ou 111 se não existe no vetor): ");
            procurado = sc.nextByte();
        } else {
            procurado = vetor[(int) (Math.random() * tamanho)];
            System.out.println("[D] Número sorteado a ser contado: " + procurado);
        }

        long inicio = System.currentTimeMillis();

        Thread[] threads = new Thread[numServidores];
        int parte = tamanho / numServidores;
        int[] resultados = new int[numServidores];

        // Cria threads
        for (int i = 0; i < numServidores; i++) {
            final int idx = i;
            final int inicioParte = idx * parte;
            final int fimParte = (idx == numServidores - 1) ? tamanho : inicioParte + parte;

            byte[] subVetor = new byte[fimParte - inicioParte];
            System.arraycopy(vetor, inicioParte, subVetor, 0, fimParte - inicioParte);

            threads[i] = new Thread(() -> {
                try (Socket conexao = new Socket(IPS[idx], PORTAS[idx]);
                     ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                     ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream())) {

                    System.out.println("[D] Enviando para " + IPS[idx] + ":" + PORTAS[idx]);
                    transmissor.writeObject(new Pedido(subVetor, procurado));
                    transmissor.flush();

                    Object resposta = receptor.readObject();
                    if (resposta instanceof Resposta) {
                        resultados[idx] = ((Resposta) resposta).getContagem();
                        System.out.println("[D] Resposta recebida de " + IPS[idx] + ":" + PORTAS[idx] + " -> " + resultados[idx]);
                    }

                    transmissor.writeObject(new ComunicadoEncerramento());
                    transmissor.flush();

                } catch (Exception e) {
                    System.err.println("[D] Servidor " + IPS[idx] + ":" + PORTAS[idx] + " indisponível ou erro: " + e.getMessage());
                    resultados[idx] = 0; // contabiliza como zero, mas continua a execução
                }
            });

            threads[i].start();
        }

        for (Thread t : threads) {
            try { t.join(); } catch (Exception ignored) {}
        }

        int total = 0;
        for (int r : resultados) total += r;

        long fim = System.currentTimeMillis();

        System.out.println("\n[D] Contagem final do número " + procurado + ": " + total);
        System.out.printf("[D] Tempo total de execução: %.2f segundos%n", (fim - inicio) / 1000.0);

        sc.close();
    }
}
