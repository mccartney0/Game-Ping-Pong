package pong.entities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Random;

import pong.main.Game;

public class Ball {

    private static final double INITIAL_SPEED = 2.0;
    private static final double MAX_SPEED = 4.2;
    private static final Random RANDOM = new Random();

    public double x;
    public double y;
    public double dx;
    public double dy;
    public final int w;
    public final int h;
    public static double speed = INITIAL_SPEED;

    public Ball(double x, double y) {
        this.x = x;
        this.y = y;
        this.w = 4;
        this.h = 4;
        resetDirection();
    }

    public static void resetSpeed() {
        speed = INITIAL_SPEED;
    }

    private void resetDirection() {
        double horizontal = RANDOM.nextBoolean() ? 1.0 : -1.0;
        double angle = 0.25 + RANDOM.nextDouble() * 0.42;
        setDirection(horizontal * angle, RANDOM.nextBoolean() ? 1.0 : -1.0);
    }

    private void setDirection(double horizontal, double vertical) {
        double length = Math.sqrt(horizontal * horizontal + vertical * vertical);
        dx = horizontal / length;
        dy = vertical / length;
    }

    public void update(double deltaSeconds) {
        double frameScale = Math.min(2.5, Math.max(0.35, deltaSeconds * 60.0));
        double stepX = dx * speed * frameScale;
        double stepY = dy * speed * frameScale;
        double nextX = x + stepX;
        double nextY = y + stepY;

        if (nextX <= 0) {
            nextX = 0;
            dx = Math.abs(dx);
            if (Game.effects != null) {
                Game.effects.wallHit(nextX, y + h / 2.0);
            }
        } else if (nextX + w >= Game.W) {
            nextX = Game.W - w;
            dx = -Math.abs(dx);
            if (Game.effects != null) {
                Game.effects.wallHit(nextX + w, y + h / 2.0);
            }
        }

        if (nextY + h >= Game.H) {
            Game.registerPoint(false, nextX + w / 2.0, Game.H - 2);
            return;
        }
        if (nextY <= 0) {
            Game.registerPoint(true, nextX + w / 2.0, 2);
            return;
        }

        Rectangle nextBounds = new Rectangle((int) Math.round(nextX), (int) Math.round(nextY), w, h);
        Rectangle playerBounds = new Rectangle((int) Math.round(Game.player.x), (int) Math.round(Game.player.y),
                Game.player.w, Game.player.h);
        Rectangle enemyBounds = new Rectangle((int) Math.round(Game.enemy.x), (int) Math.round(Game.enemy.y),
                Game.enemy.w, Game.enemy.h);

        if (dy > 0 && nextBounds.intersects(playerBounds)) {
            double relativeHit = ((nextX + w / 2.0) - (Game.player.x + Game.player.w / 2.0))
                    / (Game.player.w / 2.0);
            bounceFromPaddle(relativeHit, -1.0);
            nextY = Game.player.y - h - 0.1;
            speed = Math.min(MAX_SPEED, speed + 0.045);
            if (Game.effects != null) {
                Game.effects.paddleHit(nextX + w / 2.0, Game.player.y, new Color(70, 220, 255));
            }
        } else if (dy < 0 && nextBounds.intersects(enemyBounds)) {
            double relativeHit = ((nextX + w / 2.0) - (Game.enemy.x + Game.enemy.w / 2.0))
                    / (Game.enemy.w / 2.0);
            bounceFromPaddle(relativeHit, 1.0);
            nextY = Game.enemy.y + Game.enemy.h + 0.1;
            speed = Math.min(MAX_SPEED, speed + 0.045);
            if (Game.effects != null) {
                Game.effects.paddleHit(nextX + w / 2.0, Game.enemy.y + Game.enemy.h, new Color(255, 100, 120));
            }
        }

        x = nextX;
        y = nextY;
    }

    private void bounceFromPaddle(double relativeHit, double verticalDirection) {
        double clampedHit = Math.max(-1.0, Math.min(1.0, relativeHit));
        double horizontal = clampedHit * 1.15;
        setDirection(horizontal, verticalDirection);
    }

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
        g2.setColor(new Color(100, 230, 255));
        g2.fillOval((int) x - 3, (int) y - 3, w + 6, h + 6);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Color.white);
        g2.fillRoundRect((int) Math.round(x), (int) Math.round(y), w, h, 2, 2);
    }
}
