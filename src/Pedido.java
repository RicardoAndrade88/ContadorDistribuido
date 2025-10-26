import java.io.Serializable;

public class Pedido extends Comunicado implements Serializable
{
    private byte[] numeros;
    private byte procurado;

    public Pedido(byte[] numeros, byte procurado) {
        this.numeros = numeros;
        this.procurado = procurado;
    }

    public int contar() {
        int qtdProcessadores = Runtime.getRuntime().availableProcessors();
        ThreadContadora[] threads = new ThreadContadora[qtdProcessadores];
        int tamanho = numeros.length;
        int parte = tamanho / qtdProcessadores;
        int resto = tamanho % qtdProcessadores;

        int inicio = 0;

        for (int i = 0; i < qtdProcessadores; i++)
        {
            int fim = inicio + parte;
            if (i == qtdProcessadores - 1) fim += resto;

            threads[i] = new ThreadContadora(numeros, procurado, inicio, fim);
            threads[i].start();

            inicio = fim;
        }

        int total = 0;
        try
        {
            for (ThreadContadora t : threads)
            {
                t.join();
                total += t.getContagem();
            }
        }
        catch (InterruptedException e)
        {
            System.out.println("[R] Erro ao aguardar threads: " + e.getMessage());
        }

        return total;
    }
}

class ThreadContadora extends Thread
{
    private byte[] vetor;
    private byte procurado;
    private int inicio, fim;
    private int contagem = 0;

    public ThreadContadora(byte[] vetor, byte procurado, int inicio, int fim)
    {
        this.vetor = vetor;
        this.procurado = procurado;
        this.inicio = inicio;
        this.fim = fim;
    }

    @Override
    public void run()
    {
        for (int i = inicio; i < fim; i++)
        {
            if (vetor[i] == procurado) contagem++;
        }
    }

    public int getContagem()
    {
        return contagem;
    }
}