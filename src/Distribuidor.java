import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Distribuidor {

    // Lista de IPs dos servidores (mude para os IPs reais da sua rede)
    private static final String[] RECEPTORS = {
        "192.168.1.160", // localhost para teste local
        "127.0.0.1", // pode duplicar se quiser várias threads em máquina local
        "127.0.0.1"
    };

    private static final int PORTA = 12345;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Digite o tamanho do vetor grande: ");
        int tamanho = sc.nextInt();

        byte[] vetor = new byte[tamanho];
        for (int i = 0; i < tamanho; i++) {
            vetor[i] = (byte) ((Math.random() * 201) - 100); // -100 a 100
        }

        System.out.print("Deseja imprimir o vetor na tela? (s/n): ");
        String imprimir = sc.next();

        if (imprimir.equalsIgnoreCase("s")) {
            for (byte b : vetor) {
                System.out.print(b + " ");
            }
            System.out.println();
        }

        System.out.print("Deseja informar um número a contar ou sortear? (digite 'informar' ou 'sortear'): ");
        String escolha = sc.next();

        byte procurado;
        if (escolha.equalsIgnoreCase("informar")) {
            System.out.print("Digite o número a contar (-100 a 100, ou 111 se não existe no vetor): ");
            procurado = sc.nextByte();
        } else {
            int indice = (int) (Math.random() * tamanho);
            procurado = vetor[indice];
            System.out.println("[D] Número sorteado a ser contado: " + procurado);
        }

        long inicio = System.currentTimeMillis();

        // Criar threads para cada servidor
        Thread[] threads = new Thread[RECEPTORS.length];
        int parte = tamanho / RECEPTORS.length;
        int[] resultados = new int[RECEPTORS.length];

        for (int i = 0; i < RECEPTORS.length; i++) {
            final int idx = i;
            final int inicioParte = idx * parte;
            final int fimParte = (idx == RECEPTORS.length - 1) ? tamanho : inicioParte + parte;

            byte[] subVetor = new byte[fimParte - inicioParte];
            System.arraycopy(vetor, inicioParte, subVetor, 0, fimParte - inicioParte);

            threads[i] = new Thread(() -> {
                try (Socket conexao = new Socket(RECEPTORS[idx], PORTA);
                     ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                     ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream())) {

                    System.out.println("[D] Enviando Pedido para " + RECEPTORS[idx]);
                    transmissor.writeObject(new Pedido(subVetor, procurado));
                    transmissor.flush();

                    Object resposta = receptor.readObject();
                    if (resposta instanceof Resposta) {
                        resultados[idx] = ((Resposta) resposta).getContagem();
                        System.out.println("[D] Resposta recebida de " + RECEPTORS[idx] + ": " + resultados[idx]);
                    }

                    // Envia comunicado de encerramento
                    transmissor.writeObject(new ComunicadoEncerramento());
                    transmissor.flush();
                } catch (Exception e) {
                    System.err.println("[D] Erro na thread " + idx + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });

            threads[i].start();
        }

        // Espera todas threads terminarem
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Soma resultados
        int total = 0;
        for (int r : resultados) {
            total += r;
        }

        long fim = System.currentTimeMillis();

        System.out.println("\n[D] Contagem final do número " + procurado + ": " + total);
        System.out.printf("[D] Tempo total de execução: %.2f segundos%n", (fim - inicio) / 1000.0);

        sc.close();
    }
}
