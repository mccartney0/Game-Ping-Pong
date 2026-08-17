package pong.entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import pong.main.Game;

public class Player {

    public boolean right;
    public boolean left;
    public double x;
    public final double y;
    public int w;
    public final int h;

    private final Color baseColor;
    private final boolean topPaddle;
    private double velocityX;

    public int energy = 25;
    public int overdriveTicks;
    public int shieldTicks;
    public int wideTicks;

    public Player(double x, double y) {
        this(x, y, new Color(70, 220, 255), false);
    }

    public Player(double x, double y, Color color, boolean topPaddle) {
        this.x = x;
        this.y = y;
        this.baseColor = color;
        this.topPaddle = topPaddle;
        this.w = 34;
        this.h = 7;
    }

    public void update(double deltaSeconds) {
        double frameScale = Math.min(2.5, Math.max(0.35, deltaSeconds * 60.0));
        if (overdriveTicks > 0) {
            overdriveTicks--;
        }
        if (shieldTicks > 0) {
            shieldTicks--;
        }
        if (wideTicks > 0) {
            wideTicks--;
        }
        w = wideTicks > 0 ? 47 : 34;

        double input = 0;
        if (right) {
            input += 1;
        }
        if (left) {
            input -= 1;
        }

        double maxSpeed = overdriveTicks > 0 ? 4.5 : 3.4;
        double targetVelocity = input * maxSpeed;
        velocityX += (targetVelocity - velocityX) * Math.min(1.0, 0.28 * frameScale);
        x += velocityX * frameScale;

        if (x + w > Game.W - 2) {
            x = Game.W - w - 2;
            velocityX = Math.min(0, velocityX);
        } else if (x < 2) {
            x = 2;
            velocityX = Math.max(0, velocityX);
        }
    }

    public void addEnergy(int amount) {
        energy = Math.min(100, energy + Math.max(0, amount));
    }

    public boolean spendEnergy() {
        if (energy < 100) {
            return false;
        }
        energy = 0;
        return true;
    }

    public boolean hasShield() {
        return shieldTicks > 0;
    }

    public boolean consumeShield() {
        if (!hasShield()) {
            return false;
        }
        shieldTicks = 0;
        return true;
    }

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Color glowColor = overdriveTicks > 0 ? new Color(255, 220, 90) : baseColor;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.23f));
        g2.setColor(glowColor);
        g2.fillRoundRect((int) x - 3, (int) y - 3, w + 6, h + 6, 6, 6);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setPaint(new GradientPaint(0, (int) y, glowColor.brighter(), 0, (int) y + h, glowColor.darker()));
        g2.fillRoundRect((int) x, (int) y, w, h, 4, 4);
        g2.setColor(new Color(240, 255, 255));
        g2.drawRoundRect((int) x, (int) y, w - 1, h - 1, 4, 4);

        if (hasShield()) {
            g2.setColor(new Color(255, 230, 100, 190));
            g2.drawRoundRect((int) x - 4, (int) y - 4, w + 7, h + 7, 8, 8);
        }
        if (topPaddle) {
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawLine((int) x + 3, (int) y + h + 2, (int) x + w - 3, (int) y + h + 2);
        }
    }
}
