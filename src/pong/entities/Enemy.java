package pong.entities;

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
    public final int w;
    public final int h;
    public static double difficulty = 0.055;

    private static final double INITIAL_DIFFICULTY = 0.055;
    private double velocityX;

    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
        this.w = 34;
        this.h = 7;
    }

    public static void resetDifficulty() {
        difficulty = INITIAL_DIFFICULTY;
    }

    public void update(double deltaSeconds) {
        double frameScale = Math.min(2.5, Math.max(0.35, deltaSeconds * 60.0));
        double targetX;

        if (Game.ball != null && Game.ball.dy < 0) {
            double prediction = Game.ball.x + Game.ball.dx * Ball.speed * 6.0;
            targetX = prediction + Game.ball.w / 2.0 - w / 2.0;
        } else {
            targetX = Game.W / 2.0 - w / 2.0;
        }

        double distance = targetX - x;
        double steering = Math.max(-1.0, Math.min(1.0, distance * difficulty));
        double maxSpeed = Math.min(3.0, 1.45 + (Game.nivel - 1) * 0.14);
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
        g2.setColor(new Color(255, 80, 110, 50));
        g2.fillRoundRect((int) x - 2, (int) y - 2, w + 4, h + 4, 5, 5);
        g2.setPaint(new GradientPaint(0, (int) y, new Color(255, 200, 210), 0, (int) y + h,
                new Color(210, 45, 90)));
        g2.fillRoundRect((int) x, (int) y, w, h, 4, 4);
        g2.setColor(new Color(255, 240, 245));
        g2.drawRoundRect((int) x, (int) y, w - 1, h - 1, 4, 4);
    }
}
