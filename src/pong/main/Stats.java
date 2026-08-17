package pong.main;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Stats {

    private final Path savePath = Paths.get("pong-stats.properties");

    public int matches;
    public int wins;
    public int losses;
    public int bestCombo;
    public int bestScore;
    public int totalPoints;
    public int xp;
    public int unlockedArena = 1;
    public int completedChallenges;

    public void load() {
        if (!Files.exists(savePath)) {
            return;
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(savePath)) {
            properties.load(input);
            matches = read(properties, "matches", matches);
            wins = read(properties, "wins", wins);
            losses = read(properties, "losses", losses);
            bestCombo = read(properties, "bestCombo", bestCombo);
            bestScore = read(properties, "bestScore", bestScore);
            totalPoints = read(properties, "totalPoints", totalPoints);
            xp = read(properties, "xp", xp);
            unlockedArena = Math.max(1, read(properties, "unlockedArena", unlockedArena));
            completedChallenges = read(properties, "completedChallenges", completedChallenges);
        } catch (Exception ignored) {
            // Um arquivo de save inválido não pode impedir uma nova partida.
        }
    }

    public void recordMatch(boolean won, int score, int combo, int points) {
        matches++;
        if (won) {
            wins++;
            xp += 35;
        } else {
            losses++;
            xp += 10;
        }
        bestScore = Math.max(bestScore, score);
        bestCombo = Math.max(bestCombo, combo);
        totalPoints += points;
        unlockedArena = Math.min(5, 1 + xp / 100);
        save();
    }

    public void completeChallenge() {
        completedChallenges++;
        xp += 25;
        unlockedArena = Math.min(5, 1 + xp / 100);
        save();
    }

    public int getRank() {
        return 1 + xp / 100;
    }

    public void save() {
        Properties properties = new Properties();
        properties.setProperty("matches", String.valueOf(matches));
        properties.setProperty("wins", String.valueOf(wins));
        properties.setProperty("losses", String.valueOf(losses));
        properties.setProperty("bestCombo", String.valueOf(bestCombo));
        properties.setProperty("bestScore", String.valueOf(bestScore));
        properties.setProperty("totalPoints", String.valueOf(totalPoints));
        properties.setProperty("xp", String.valueOf(xp));
        properties.setProperty("unlockedArena", String.valueOf(unlockedArena));
        properties.setProperty("completedChallenges", String.valueOf(completedChallenges));
        try (OutputStream output = Files.newOutputStream(savePath)) {
            properties.store(output, "Neon Ping Pong statistics");
        } catch (Exception ignored) {
            // Estatísticas são um extra; falha de disco não interrompe o jogo.
        }
    }

    private int read(Properties properties, String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
