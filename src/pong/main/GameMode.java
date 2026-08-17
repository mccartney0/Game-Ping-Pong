package pong.main;

public enum GameMode {
    CLASSIC("CLASSICO", "Primeiro a chegar na meta", 7),
    SURVIVAL("SOBREVIVENCIA", "Proteja suas 3 vidas", 0),
    TURBO("TURBO", "Partida curta e veloz", 5),
    VERSUS("VERSUS LOCAL", "J2 usa J/L e I", 7),
    MUTANT("ARENA MUTANTE", "Crie e jogue sua arena", 5),
    CAMPAIGN("CAMPANHA BOSS", "Derrote quatro chefes", 0);

    private final String title;
    private final String description;
    private final int targetScore;

    GameMode(String title, String description, int targetScore) {
        this.title = title;
        this.description = description;
        this.targetScore = targetScore;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public boolean isVersus() {
        return this == VERSUS;
    }

    public boolean usesCustomArena() {
        return this == MUTANT;
    }

    public boolean isCampaign() {
        return this == CAMPAIGN;
    }
}
