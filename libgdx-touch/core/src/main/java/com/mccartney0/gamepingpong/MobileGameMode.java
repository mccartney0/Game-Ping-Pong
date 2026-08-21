package com.mccartney0.gamepingpong;

public enum MobileGameMode {
    CLASSIC("CLASSIC", "Partida tradicional ate 7 pontos"),
    SURVIVAL("SURVIVAL", "Sobreviva com 3 vidas e acumule pontos"),
    TURBO("TURBO", "A bola acelera e exige reflexos"),
    VERSUS("VERSUS", "Dois jogadores no mesmo aparelho"),
    MUTANT("MUTANT ARENA", "Power-ups aparecem com mais frequencia"),
    CAMPAIGN("BOSS CAMPAIGN", "Derrote quatro chefes em sequencia");

    private final String label;
    private final String description;

    MobileGameMode(String label, String description) {
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
