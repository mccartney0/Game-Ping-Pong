package pong.main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Random;

import pong.entities.Ball;

public class Arena {

    private static final Random RANDOM = new Random();
    private static final int EVENT_INTERVAL = 420;
    private static final int EVENT_DURATION = 360;

    private int cooldown = EVENT_INTERVAL;
    private int eventTicks;
    private int eventType;
    private int ticks;
    private String eventLabel = "";
    private ArenaBlueprint customBlueprint;

    public void reset() {
        cooldown = EVENT_INTERVAL;
        eventTicks = 0;
        eventType = 0;
        ticks = 0;
        eventLabel = "";
    }

    public void setCustomBlueprint(ArenaBlueprint blueprint) {
        customBlueprint = blueprint;
    }

    public ArenaBlueprint getCustomBlueprint() {
        return customBlueprint;
    }

    public boolean isCustom() {
        return customBlueprint != null && customBlueprint.isValid();
    }

    public void update(double deltaSeconds, GameMode mode) {
        ticks++;
        if (mode == GameMode.CLASSIC || mode == GameMode.VERSUS || mode == GameMode.CAMPAIGN) {
            return;
        }
        if (mode == GameMode.MUTANT && isCustom()) {
            if (eventTicks > 0) {
                eventTicks--;
                if (eventTicks == 0) {
                    eventLabel = "";
                    cooldown = EVENT_INTERVAL / 2;
                }
                return;
            }
            cooldown--;
            if (cooldown <= 0) {
                eventType = RANDOM.nextInt(3);
                eventTicks = EVENT_DURATION;
                eventLabel = eventType == 0 ? "REGRAS MUTANTES" : eventType == 1 ? "FLUXO INSTAVEL" : "PULSO DA ARENA";
            }
            return;
        }

        if (eventTicks > 0) {
            eventTicks--;
            if (eventTicks == 0) {
                eventLabel = "";
                cooldown = EVENT_INTERVAL;
            }
            return;
        }
        cooldown--;
        if (cooldown <= 0) {
            int unlockedEvents = Game.stats == null ? 1 : Math.min(3, Math.max(1, Game.stats.unlockedArena));
            eventType = RANDOM.nextInt(unlockedEvents);
            eventTicks = EVENT_DURATION;
            eventLabel = eventType == 0 ? "BLOQUEIO NEON" : eventType == 1 ? "ZONA TURBO" : "GRAVIDADE ZERO";
        }
    }

    public boolean isActive() {
        return eventTicks > 0;
    }

    public int getEventType() {
        return eventType;
    }

    public String getEventLabel() {
        return isCustom() && !eventLabel.isEmpty() ? eventLabel : eventLabel;
    }

    public double getEventProgress() {
        return eventTicks / (double) EVENT_DURATION;
    }

    public Rectangle getObstacleBounds() {
        if (!isActive() || eventType != 0) {
            return null;
        }
        int obstacleWidth = 45;
        int obstacleX = 57 + (int) Math.round(Math.sin(ticks * 0.035) * 28);
        return new Rectangle(obstacleX, Game.H / 2 - 2, obstacleWidth, 4);
    }

    public boolean collides(double x, double y, int width, int height) {
        Rectangle ballBounds = new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
        if (isCustom()) {
            for (ArenaElement element : customBlueprint.getElements()) {
                if (element.type == ArenaElementType.BLOCK && element.bounds().intersects(ballBounds)) {
                    return true;
                }
            }
            return false;
        }
        Rectangle obstacle = getObstacleBounds();
        return obstacle != null && obstacle.intersects(ballBounds);
    }

    public double speedMultiplier(double x, double y) {
        if (isCustom()) {
            for (ArenaElement element : customBlueprint.getElements()) {
                if (element.bounds().contains(x, y)) {
                    if (element.type == ArenaElementType.TURBO) {
                        return 1.36;
                    }
                    if (element.type == ArenaElementType.SLOW) {
                        return 0.66;
                    }
                }
            }
            if (eventTicks > 0 && eventType == 1) {
                return 1.18;
            }
            return 1.0;
        }
        return isTurboZone(x, y) ? 1.24 : 1.0;
    }

    public void applyForces(Ball ball) {
        if (!isCustom()) {
            return;
        }
        for (ArenaElement element : customBlueprint.getElements()) {
            Rectangle bounds = element.bounds();
            if (element.type == ArenaElementType.PORTAL && bounds.intersects(ballBounds(ball))) {
                ball.x = Game.W - ball.x - ball.w;
                if (Game.effects != null) {
                    Game.effects.arenaHit(ball.x, ball.y);
                }
                break;
            }
            if (element.type == ArenaElementType.GRAVITY && bounds.contains(ball.x, ball.y)) {
                ball.dx += (Game.W / 2.0 - ball.x) * 0.0008;
                ball.dy += (Game.H / 2.0 - ball.y) * 0.00035;
                normalize(ball);
            }
        }
        if (eventTicks > 0 && eventType == 2) {
            ball.dx += Math.sin(ticks * 0.1) * 0.008;
            normalize(ball);
        }
    }

    private Rectangle ballBounds(Ball ball) {
        return new Rectangle((int) Math.round(ball.x), (int) Math.round(ball.y), ball.w, ball.h);
    }

    private void normalize(Ball ball) {
        double length = Math.sqrt(ball.dx * ball.dx + ball.dy * ball.dy);
        if (length > 0.01) {
            ball.dx /= length;
            ball.dy /= length;
        }
    }

    public boolean isTurboZone(double x, double y) {
        return isActive() && eventType == 1 && x > 52 && x < 108 && y > 42 && y < 80;
    }

    public void render(Graphics g) {
        if (isCustom()) {
            customBlueprint.renderElements(g);
            if (isActive()) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(255, 255, 255, 80));
                g2.drawOval(45, 38, 70, 44);
                g2.setFont(new Font("Dialog", Font.BOLD, 7));
                g2.setColor(Color.white);
                g2.drawString(eventLabel, 6, 27);
            }
            return;
        }
        if (!isActive()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        float alpha = 0.45f + (float) (Math.sin(ticks * 0.12) * 0.15);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        if (eventType == 0) {
            Rectangle obstacle = getObstacleBounds();
            g2.setColor(new Color(255, 75, 190));
            g2.fillRoundRect(obstacle.x - 2, obstacle.y - 2, obstacle.width + 4, obstacle.height + 4, 6, 6);
            g2.setColor(new Color(255, 225, 250));
            g2.fillRoundRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height, 4, 4);
        } else if (eventType == 1) {
            g2.setColor(new Color(255, 200, 80));
            g2.fillOval(54, 43, 52, 34);
        } else {
            g2.setColor(new Color(110, 190, 255));
            g2.drawOval(42, 33, 76, 54);
        }
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setFont(new Font("Dialog", Font.BOLD, 8));
        g2.setColor(Color.white);
        int labelWidth = g2.getFontMetrics().stringWidth(eventLabel);
        g2.drawString(eventLabel, (Game.W - labelWidth) / 2, 25);
    }
}
