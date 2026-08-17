package pong.main;

public enum ArenaElementType {
    BLOCK("BLOCO", "Reflete a bola"),
    TURBO("TURBO", "Acelera a bola"),
    SLOW("SLOW", "Reduz a bola"),
    PORTAL("PORTAL", "Teleporta para o lado oposto"),
    GRAVITY("GRAVIDADE", "Puxa a bola para o centro");

    private final String label;
    private final String description;

    ArenaElementType(String label, String description) {
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
