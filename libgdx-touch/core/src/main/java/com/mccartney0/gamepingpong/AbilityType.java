package com.mccartney0.gamepingpong;

public enum AbilityType {
    OVERDRIVE("OVERDRIVE", "Aumenta a velocidade da bola", 35f),
    SHIELD("SHIELD", "Bloqueia uma falha", 30f),
    WIDE("WIDE", "Amplia a raquete", 25f);

    private final String label;
    private final String description;
    private final float energyCost;

    AbilityType(String label, String description, float energyCost) {
        this.label = label;
        this.description = description;
        this.energyCost = energyCost;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public float getEnergyCost() {
        return energyCost;
    }

    public AbilityType next() {
        AbilityType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
