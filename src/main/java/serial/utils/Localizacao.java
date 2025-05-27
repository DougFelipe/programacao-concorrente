package serial.utils;

public enum Localizacao {
    MEMBRANA(0), CITOSOL(1), SECRECAO(2), DESCONHECIDA(3);

    private final int codigo;

    Localizacao(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static Localizacao from(int codigo) {
        return switch (codigo) {
            case 0 -> MEMBRANA;
            case 1 -> CITOSOL;
            case 2 -> SECRECAO;
            default -> DESCONHECIDA;
        };
    }
}
