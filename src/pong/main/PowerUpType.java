package pong.main;

public enum PowerUpType {
    ENERGY("ENERGIA", "Carrega a habilidade"),
    SLOW("SLOW", "Reduz a velocidade da bola"),
    SPLIT("SPLIT", "Cria uma duplicata visual da bola"),
    MULTI("2X", "Dobra o próximo ponto");

    private final String label;
    private final String description;

    PowerUpType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
