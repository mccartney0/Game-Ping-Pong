package pong.main;

public enum BossType {
    VOLT("VOLT", "Oscila e acelera nas laterais", 1),
    MIRROR("MIRROR", "Copia a trajetória ao contrário", 2),
    TWIN("TWIN", "Alterna entre duas posições", 3),
    GRAVITY("GRAVITY", "Distorce a leitura do centro", 4);

    private final String label;
    private final String description;
    private final int stage;

    BossType(String label, String description, int stage) {
        this.label = label;
        this.description = description;
        this.stage = stage;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public int getStage() {
        return stage;
    }
}
