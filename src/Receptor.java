import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Receptor {

    public static void main(String[] args) {
        int porta = 12345;

        try (ServerSocket servidor = new ServerSocket(porta)) {
            System.out.println("[R] Servidor Receptor iniciado na porta " + porta);

            while (true) {
                Socket conexao = servidor.accept();
                System.out.println("[R] Nova conexão recebida de " + conexao.getInetAddress().getHostAddress());

                Thread t = new Thread(new Atendente(conexao));
                t.start();
            }
        } catch (Exception erro) {
            System.err.println("[R] Erro no servidor: " + erro.getMessage());
            erro.printStackTrace();
        }
    }
}

class Atendente implements Runnable {

    private Socket conexao;
    private ObjectInputStream receptor;
    private ObjectOutputStream transmissor;

    public Atendente(Socket conexao) {
        this.conexao = conexao;
    }

    @Override
    public void run() {
        try {
            transmissor = new ObjectOutputStream(conexao.getOutputStream());
            receptor = new ObjectInputStream(conexao.getInputStream());

            while (true) {
                Object obj = receptor.readObject();

                if (obj instanceof Pedido) {
                    Pedido pedido = (Pedido) obj;
                    int contagem = pedido.contar();

                    transmissor.writeObject(new Resposta(contagem));
                    transmissor.flush();

                    System.out.println("[R] Contagem enviada: " + contagem);
                } else if (obj instanceof ComunicadoEncerramento) {
                    System.out.println("[R] Encerrando conexão com cliente...");
                    break;
                } else {
                    System.out.println("[R] Objeto desconhecido recebido: " + obj.getClass().getName());
                }
            }
        } catch (java.io.EOFException eof) {
            System.out.println("[R] Cliente fechou a conexão.");
        } catch (Exception erro) {
            System.err.println("[R] Erro na conexão: " + erro.getMessage());
            erro.printStackTrace();
        } finally {
            try {
                if (receptor != null) receptor.close();
                if (transmissor != null) transmissor.close();
                if (conexao != null) conexao.close();
            } catch (Exception e) {
                // ignorar
            }
            System.out.println("[R] Conexão finalizada.");
        }
    }
}
