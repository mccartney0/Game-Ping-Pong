package pong.main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Random;

public class Arena {

    private static final Random RANDOM = new Random();
    private static final int EVENT_INTERVAL = 420;
    private static final int EVENT_DURATION = 360;

    private int cooldown = EVENT_INTERVAL;
    private int eventTicks;
    private int eventType;
    private int ticks;
    private String eventLabel = "";

    public void reset() {
        cooldown = EVENT_INTERVAL;
        eventTicks = 0;
        eventType = 0;
        ticks = 0;
        eventLabel = "";
    }

    public void update(double deltaSeconds, GameMode mode) {
        ticks++;
        if (mode == GameMode.CLASSIC || mode == GameMode.VERSUS) {
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
        return eventLabel;
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
        Rectangle obstacle = getObstacleBounds();
        return obstacle != null && obstacle.intersects(new Rectangle((int) Math.round(x), (int) Math.round(y), width, height));
    }

    public boolean isTurboZone(double x, double y) {
        return isActive() && eventType == 1 && x > 52 && x < 108 && y > 42 && y < 80;
    }

    public void render(Graphics g) {
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
