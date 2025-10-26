public class Resposta extends Comunicado {

    private static final long serialVersionUID = 1L;

    private int contagem;

    public Resposta(int contagem) {
        this.contagem = contagem;
    }

    public int getContagem() {
        return this.contagem;
    }
}