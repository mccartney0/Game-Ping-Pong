package com.mccartney0.gamepingpong;

public enum PowerUpType {
    ENERGY("ENERGY", "Recupera energia", 0.25f, 0.85f, 1f),
    SLOW("SLOW", "Desacelera a bola", 0.55f, 0.72f, 1f),
    SPLIT("SPLIT", "Cria um eco visual", 0.78f, 0.42f, 1f),
    MULTI("MULTI", "Dobra o próximo ponto", 1f, 0.62f, 0.15f);

    private final String label;
    private final String description;
    private final float red;
    private final float green;
    private final float blue;

    PowerUpType(String label, String description, float red, float green, float blue) {
        this.label = label;
        this.description = description;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }
}
