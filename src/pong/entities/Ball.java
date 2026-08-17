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
    private static final double MAX_SPEED = 5.1;
    private static final Random RANDOM = new Random();

    public double x;
    public double y;
    public double dx;
    public double dy;
    public final int w;
    public final int h;
    public static double speed = INITIAL_SPEED;
    public int splitTicks;
    public int slowTicks;

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

    public void resetPowerStates() {
        splitTicks = 0;
        slowTicks = 0;
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
        if (slowTicks > 0) {
            slowTicks--;
        }
        if (splitTicks > 0) {
            splitTicks--;
        }

        if (Game.arena != null) {
            Game.arena.applyForces(this);
        }
        double currentSpeed = speed * (slowTicks > 0 ? 0.62 : 1.0);
        if (Game.arena != null) {
            currentSpeed *= Game.arena.speedMultiplier(x, y);
        }
        double stepX = dx * currentSpeed * frameScale;
        double stepY = dy * currentSpeed * frameScale;
        double nextX = x + stepX;
        double nextY = y + stepY;

        if (nextX <= 0) {
            nextX = 0;
            dx = Math.abs(dx);
            Game.onWallHit(nextX, y + h / 2.0);
        } else if (nextX + w >= Game.W) {
            nextX = Game.W - w;
            dx = -Math.abs(dx);
            Game.onWallHit(nextX + w, y + h / 2.0);
        }

        if (Game.arena != null && Game.arena.collides(nextX, nextY, w, h)) {
            dy *= -1;
            nextY += dy * 2.0;
            Game.onArenaHit(nextX + w / 2.0, nextY + h / 2.0);
        }

        if (nextY + h >= Game.H) {
            Game.goalReached(false, nextX + w / 2.0, Game.H - 2);
            return;
        }
        if (nextY <= 0) {
            Game.goalReached(true, nextX + w / 2.0, 2);
            return;
        }

        Rectangle nextBounds = new Rectangle((int) Math.round(nextX), (int) Math.round(nextY), w, h);
        Player bottomPaddle = Game.player;
        double topX = Game.getTopPaddleX();
        double topY = Game.getTopPaddleY();
        int topWidth = Game.getTopPaddleWidth();
        int topHeight = Game.getTopPaddleHeight();

        Rectangle bottomBounds = new Rectangle((int) Math.round(bottomPaddle.x), (int) Math.round(bottomPaddle.y),
                bottomPaddle.w, bottomPaddle.h);
        Rectangle topBounds = new Rectangle((int) Math.round(topX), (int) Math.round(topY), topWidth, topHeight);

        if (dy > 0 && nextBounds.intersects(bottomBounds)) {
            double relativeHit = ((nextX + w / 2.0) - (bottomPaddle.x + bottomPaddle.w / 2.0))
                    / (bottomPaddle.w / 2.0);
            bounceFromPaddle(relativeHit, -1.0);
            nextY = bottomPaddle.y - h - 0.1;
            speed = Math.min(MAX_SPEED, speed + 0.045);
            Game.onPaddleHit(true, nextX + w / 2.0, bottomPaddle.y);
        } else if (dy < 0 && nextBounds.intersects(topBounds)) {
            double relativeHit = ((nextX + w / 2.0) - (topX + topWidth / 2.0)) / (topWidth / 2.0);
            bounceFromPaddle(relativeHit, 1.0);
            nextY = topY + topHeight + 0.1;
            speed = Math.min(MAX_SPEED, speed + 0.045);
            Game.onPaddleHit(false, nextX + w / 2.0, topY + topHeight);
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
        if (splitTicks > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.34f));
            g2.setColor(new Color(210, 115, 255));
            g2.fillOval((int) x + 9, (int) y + 2, w, h);
            g2.setComposite(AlphaComposite.SrcOver);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
        Color ballGlow = slowTicks > 0 ? new Color(120, 190, 255) : Game.getBallSecondary();
        g2.setColor(ballGlow);
        g2.fillOval((int) x - 3, (int) y - 3, w + 6, h + 6);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Game.getBallPrimary());
        g2.fillRoundRect((int) Math.round(x), (int) Math.round(y), w, h, 2, 2);
    }
}
