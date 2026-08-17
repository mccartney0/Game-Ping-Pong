package pong.main;

import java.awt.Color;

import pong.entities.Ball;

public class BossCampaign {

    private final BossType[] bosses = BossType.values();
    private int bossIndex;
    private int bossHealth;
    private int playerLives;
    private int ticks;
    private String message = "";
    private int messageTicks;
    private boolean bossDefeatedThisHit;

    public BossCampaign() {
        reset();
    }

    public void reset() {
        bossIndex = 0;
        bossHealth = 3;
        playerLives = 3;
        ticks = 0;
        message = "";
        messageTicks = 0;
        bossDefeatedThisHit = false;
    }

    public void start() {
        reset();
        announce("BOSS " + getBossType().getLabel());
    }

    public void update(double deltaSeconds) {
        ticks++;
        if (messageTicks > 0) {
            messageTicks--;
        }
    }

    public BossType getBossType() {
        return bosses[Math.min(bossIndex, bosses.length - 1)];
    }

    public int getBossIndex() {
        return bossIndex;
    }

    public int getBossHealth() {
        return bossHealth;
    }

    public int getPlayerLives() {
        return playerLives;
    }

    public String getMessage() {
        return messageTicks > 0 ? message : "";
    }

    public boolean hitBoss() {
        bossHealth--;
        bossDefeatedThisHit = false;
        announce("IMPACTO NO " + getBossType().getLabel());
        if (bossHealth <= 0) {
            bossDefeatedThisHit = true;
            if (bossIndex >= bosses.length - 1) {
                return true;
            }
            bossIndex++;
            bossHealth = 3 + bossIndex;
            announce("PROXIMO BOSS: " + getBossType().getLabel());
        }
        return false;
    }

    public boolean wasBossDefeated() {
        return bossDefeatedThisHit;
    }

    public boolean loseLife() {
        playerLives--;
        announce(playerLives > 0 ? "VIDA PERDIDA" : "CAMPANHA ENCERRADA");
        return playerLives <= 0;
    }

    public double adjustTarget(double targetX, double ballX, double ballDx) {
        switch (getBossType()) {
        case VOLT:
            return targetX + Math.sin(ticks * 0.09) * 18.0;
        case MIRROR:
            return Game.W - targetX - 34.0 + Math.signum(ballDx) * 8.0;
        case TWIN:
            return targetX + ((ticks / 90) % 2 == 0 ? -20.0 : 20.0);
        case GRAVITY:
            return targetX + Math.sin((ballX + ticks) * 0.07) * 10.0;
        default:
            return targetX;
        }
    }

    public void applyBallForces(Ball ball) {
        switch (getBossType()) {
        case VOLT:
            if (ticks % 120 < 20) {
                ball.dx += Math.sin(ticks * 0.15) * 0.008;
            }
            break;
        case MIRROR:
            if (ticks % 180 == 0) {
                ball.dx *= -1;
            }
            break;
        case TWIN:
            if (ticks % 150 < 2) {
                ball.dy *= 0.96;
            }
            break;
        case GRAVITY:
            ball.dx += (Game.W / 2.0 - ball.x) * 0.00035;
            ball.dy += (Game.H / 2.0 - ball.y) * 0.00012;
            normalize(ball);
            break;
        default:
            break;
        }
    }

    public Color getBossColor() {
        switch (getBossType()) {
        case VOLT:
            return new Color(255, 220, 80);
        case MIRROR:
            return new Color(210, 120, 255);
        case TWIN:
            return new Color(100, 255, 190);
        case GRAVITY:
            return new Color(100, 180, 255);
        default:
            return Color.white;
        }
    }

    private void normalize(Ball ball) {
        double length = Math.sqrt(ball.dx * ball.dx + ball.dy * ball.dy);
        if (length > 0.01) {
            ball.dx /= length;
            ball.dy /= length;
        }
    }

    private void announce(String value) {
        message = value;
        messageTicks = 120;
    }
}
