package serial.utils;

public class Biomarcador {
    public final String id;
    public final double expressao;
    public final int conservacao;
    public final int similaridadeHumana;
    public final Localizacao localizacao;

    public Biomarcador(String id, double expressao, int conservacao, int similaridadeHumana, Localizacao localizacao) {
        this.id = id;
        this.expressao = expressao;
        this.conservacao = conservacao;
        this.similaridadeHumana = similaridadeHumana;
        this.localizacao = localizacao;
    }
}
