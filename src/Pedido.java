public class Pedido extends Comunicado {

    private static final long serialVersionUID = 1L;

    private byte[] numeros;
    private byte procurado;

    public Pedido(byte[] numeros, byte procurado) {
        this.numeros = numeros;
        this.procurado = procurado;
    }

    public int contar() {
        int cont = 0; // <--- ESSA LINHA É IMPORTANTE
        for (byte n : numeros) {
            if (n == procurado) {
                cont++;
            }
        }
        return cont;
    }

    public byte[] getNumeros() {
        return this.numeros;
    }

    public byte getProcurado() {
        return this.procurado;
    }
}
