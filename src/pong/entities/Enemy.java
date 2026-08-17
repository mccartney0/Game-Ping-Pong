package pong.entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import pong.main.Game;

public class Enemy {

    public boolean right;
    public boolean left;
    public double x;
    public final double y;
    public int w;
    public final int h;
    public static double difficulty = 0.055;

    private static final double INITIAL_DIFFICULTY = 0.055;
    private double velocityX;
    private boolean boss;
    private int bossPulse;

    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
        this.w = 34;
        this.h = 7;
    }

    public static void resetDifficulty() {
        difficulty = INITIAL_DIFFICULTY;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
        this.w = boss ? 48 : 34;
    }

    public boolean isBoss() {
        return boss;
    }

    public void update(double deltaSeconds) {
        double frameScale = Math.min(2.5, Math.max(0.35, deltaSeconds * 60.0));
        bossPulse++;
        double targetX;

        if (Game.ball != null && Game.ball.dy < 0) {
            double prediction = Game.ball.x + Game.ball.dx * Ball.speed * (boss ? 8.0 : 6.0);
            targetX = prediction + Game.ball.w / 2.0 - w / 2.0;
        } else {
            targetX = Game.W / 2.0 - w / 2.0;
        }

        double distance = targetX - x;
        double steering = Math.max(-1.0, Math.min(1.0, distance * difficulty * (boss ? 1.25 : 1.0)));
        double maxSpeed = Math.min(boss ? 3.5 : 3.0, (boss ? 1.8 : 1.45) + (Game.nivel - 1) * 0.14);
        double targetVelocity = steering * maxSpeed;
        velocityX += (targetVelocity - velocityX) * Math.min(1.0, 0.22 * frameScale);
        x += velocityX * frameScale;

        if (x + w > Game.W - 2) {
            x = Game.W - w - 2;
            velocityX = Math.min(0, velocityX);
        } else if (x < 2) {
            x = 2;
            velocityX = Math.max(0, velocityX);
        }
    }

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Color baseColor = boss ? new Color(255, 170, 70) : new Color(255, 80, 110);
        float pulseAlpha = boss ? 0.24f + (float) (Math.sin(bossPulse * 0.12) * 0.08) : 0.2f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha));
        g2.setColor(baseColor);
        g2.fillRoundRect((int) x - 3, (int) y - 3, w + 6, h + 6, 6, 6);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setPaint(new GradientPaint(0, (int) y, baseColor.brighter(), 0, (int) y + h, baseColor.darker()));
        g2.fillRoundRect((int) x, (int) y, w, h, 4, 4);
        g2.setColor(new Color(255, 245, 245));
        g2.drawRoundRect((int) x, (int) y, w - 1, h - 1, 4, 4);
        if (boss) {
            g2.setColor(new Color(255, 220, 110, 180));
            g2.drawRoundRect((int) x - 4, (int) y - 4, w + 7, h + 7, 8, 8);
        }
    }
}
