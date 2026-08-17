package pong.main;

public enum AbilityType {
    OVERDRIVE("OVERDRIVE", "A bola acelera por alguns segundos"),
    SHIELD("ESCUDO", "Bloqueia um ponto perdido"),
    WIDE("RAQUETE XL", "Aumenta temporariamente a raquete");

    private final String label;
    private final String description;

    AbilityType(String label, String description) {
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
