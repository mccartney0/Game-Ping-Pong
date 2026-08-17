package pong.entities;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import pong.main.Game;

public class Player {

    public boolean right;
    public boolean left;
    public double x;
    public double y;
    public final int w;
    public final int h;

    private double velocityX;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.w = 34;
        this.h = 7;
    }

    public void update(double deltaSeconds) {
        double frameScale = Math.min(2.5, Math.max(0.35, deltaSeconds * 60.0));
        double input = 0;
        if (right) {
            input += 1;
        }
        if (left) {
            input -= 1;
        }

        double targetVelocity = input * 3.4;
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

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(70, 220, 255, 50));
        g2.fillRoundRect((int) x - 2, (int) y - 2, w + 4, h + 4, 5, 5);
        g2.setPaint(new GradientPaint(0, (int) y, new Color(190, 250, 255), 0, (int) y + h,
                new Color(35, 150, 220)));
        g2.fillRoundRect((int) x, (int) y, w, h, 4, 4);
        g2.setColor(new Color(235, 255, 255));
        g2.drawRoundRect((int) x, (int) y, w - 1, h - 1, 4, 4);
    }
}
