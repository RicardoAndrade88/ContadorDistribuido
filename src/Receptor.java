import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Receptor {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        try {
            System.out.print("[R] Digite a porta para este servidor: ");
            int porta = teclado.nextInt();

            ServerSocket servidor = new ServerSocket(porta);
            System.out.println("[R] Servidor Receptor iniciado na porta " + porta);

            while (true) {
                Socket conexao = servidor.accept();
                System.out.println("[R] Cliente conectado: " + conexao.getInetAddress().getHostAddress());

                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());

                try {
                    while (true) {
                        Object obj = receptor.readObject();

                        if (obj instanceof Pedido) {
                            Pedido pedido = (Pedido) obj;
                            System.out.println("[R] Pedido recebido. Contando...");
                            int resultado = pedido.contar();

                            Resposta resposta = new Resposta(resultado);
                            transmissor.writeObject(resposta);
                            transmissor.flush();
                            System.out.println("[R] Resposta enviada: " + resultado);
                        } 
                        else if (obj instanceof ComunicadoEncerramento) {
                            System.out.println("[R] Recebido Comunicado de Encerramento. Fechando conexão...");
                            transmissor.close();
                            receptor.close();
                            conexao.close();
                            break;
                        }
                    }
                } 
                catch (Exception e) {
                    System.err.println("[R] Erro na conexão com cliente: " + e.getMessage());
                }
            }
        } 
        catch (Exception e) {
            System.err.println("[R] Erro no servidor: " + e.getMessage());
        }

        teclado.close();
    }
}
