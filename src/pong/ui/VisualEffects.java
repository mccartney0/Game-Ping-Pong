package pong.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import pong.main.Game;

/**
 * Efeitos visuais procedurais para reforcar colisões e eventos sem assets externos.
 */
public class VisualEffects {

    private static final int MAX_TRAIL_POINTS = 14;
    private final Deque<TrailPoint> trail = new ArrayDeque<TrailPoint>();
    private final List<Particle> particles = new ArrayList<Particle>();
    private final Random random = new Random();

    private int flashTicks;
    private int shakeTicks;
    private Color flashColor = Color.white;
    private String message = "";
    private Color messageColor = Color.white;
    private int messageTicks;

    public void update(double ballX, double ballY) {
        trail.addFirst(new TrailPoint(ballX, ballY));
        while (trail.size() > MAX_TRAIL_POINTS) {
            trail.removeLast();
        }
        if (flashTicks > 0) {
            flashTicks--;
        }
        if (shakeTicks > 0) {
            shakeTicks--;
        }
        if (messageTicks > 0) {
            messageTicks--;
        }
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.update();
            if (particle.life <= 0) {
                iterator.remove();
            }
        }
    }

    public void reset() {
        trail.clear();
        particles.clear();
        flashTicks = 0;
        shakeTicks = 0;
        messageTicks = 0;
        message = "";
    }

    public void paddleHit(double x, double y, Color color) {
        triggerFlash(color);
        emit(x, y, color, 10, 0.6);
    }

    public void wallHit(double x, double y) {
        emit(x, y, new Color(120, 220, 255), 5, 0.35);
    }

    public void arenaHit(double x, double y) {
        triggerFlash(new Color(255, 90, 210));
        emit(x, y, new Color(255, 100, 220), 16, 0.75);
        announce("ARENA!", new Color(255, 180, 240), 32);
    }

    public void pointScored(boolean playerScored, double x, double y) {
        Color color = playerScored ? new Color(70, 220, 255) : new Color(255, 100, 120);
        triggerFlash(color);
        emit(x, y, color, 26, 1.15);
        announce(playerScored ? "PONTO PARA VOCE" : "PONTO DO OPONENTE", color, 60);
    }

    public void combo(int value) {
        announce("COMBO x" + value, new Color(255, 220, 90), 34);
        emit(Game.W / 2.0, Game.H / 2.0, new Color(255, 215, 80), 8, 0.75);
    }

    public void abilityActivated(double x, double y, String label) {
        triggerFlash(new Color(255, 220, 90));
        emit(x, y, new Color(255, 220, 90), 22, 0.9);
        announce(label + " ATIVADO", new Color(255, 230, 120), 55);
    }

    public void abilityDenied(double x, double y) {
        announce("ENERGIA INSUFICIENTE", new Color(255, 120, 140), 45);
        emit(x, y, new Color(255, 100, 120), 5, 0.3);
    }

    public void powerUpSpawned(double x, double y, Color color) {
        emit(x, y, color, 12, 0.4);
        announce("POWER-UP", color, 28);
    }

    public void powerUpCollected(double x, double y, Color color, String label) {
        triggerFlash(color);
        emit(x, y, color, 20, 0.9);
        announce(label + " COLETADO", color, 52);
    }

    public void shieldBreak(double x, double y) {
        triggerFlash(new Color(255, 220, 90));
        emit(x, y, new Color(255, 220, 90), 28, 0.95);
        announce("ESCUDO SALVOU O PONTO", new Color(255, 230, 120), 55);
    }

    public void challengeComplete(int nextTarget) {
        triggerFlash(new Color(110, 255, 175));
        emit(Game.W / 2.0, Game.H / 2.0, new Color(110, 255, 175), 32, 1.1);
        announce("DESAFIO COMPLETO", new Color(120, 255, 180), 65);
    }

    public void matchEnded(boolean won) {
        triggerFlash(won ? new Color(100, 255, 190) : new Color(255, 90, 120));
        emit(Game.W / 2.0, Game.H / 2.0, won ? new Color(100, 255, 190) : new Color(255, 90, 120), 36, 1.0);
    }

    private void announce(String text, Color color, int ticks) {
        message = text;
        messageColor = color;
        messageTicks = ticks;
    }

    private void triggerFlash(Color color) {
        flashColor = color;
        flashTicks = 5;
        shakeTicks = Math.max(shakeTicks, 4);
    }

    public int getShakeX() {
        return shakeTicks > 0 ? random.nextInt(3) - 1 : 0;
    }

    public int getShakeY() {
        return shakeTicks > 0 ? random.nextInt(3) - 1 : 0;
    }

    private void emit(double x, double y, Color color, int amount, double velocity) {
        for (int i = 0; i < amount; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double speed = (0.35 + random.nextDouble() * 0.65) * velocity;
            particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, color));
        }
    }

    public void renderBackground(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();
        g2.setPaint(new GradientPaint(0, 0, new Color(7, 13, 35), 0, Game.H, new Color(2, 3, 12)));
        g2.fillRect(0, 0, Game.W, Game.H);
        g2.setColor(new Color(65, 140, 190, 55));
        g2.setStroke(new BasicStroke(1f));
        for (int x = 18; x < Game.W; x += 24) {
            g2.drawLine(x, 0, x, Game.H);
        }
        for (int y = 18; y < Game.H; y += 24) {
            g2.drawLine(0, y, Game.W, y);
        }
        g2.setColor(new Color(145, 225, 255, 130));
        g2.setStroke(new BasicStroke(1.1f));
        g2.drawRect(1, 1, Game.W - 3, Game.H - 3);
        for (int y = 4; y < Game.H - 4; y += 8) {
            g2.drawLine(Game.W / 2, y, Game.W / 2, Math.min(y + 4, Game.H - 4));
        }
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
    }

    public void renderTrail(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int index = 0;
        for (TrailPoint point : trail) {
            float alpha = Math.max(0.03f, 0.28f * (1f - (index / (float) MAX_TRAIL_POINTS)));
            int size = Math.max(2, 5 - index / 3);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(new Color(130, 235, 255));
            g2.fillOval((int) point.x - size / 2, (int) point.y - size / 2, size, size);
            index++;
        }
        g2.setComposite(AlphaComposite.SrcOver);
    }

    public void renderForeground(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (flashTicks > 0) {
            int alpha = Math.min(95, flashTicks * 18);
            g2.setColor(new Color(flashColor.getRed(), flashColor.getGreen(), flashColor.getBlue(), alpha));
            g2.fillRect(0, 0, Game.W, Game.H);
        }
        for (Particle particle : particles) {
            particle.render(g2);
        }
        if (messageTicks > 0) {
            int alpha = Math.min(255, messageTicks * 8);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));
            g2.setColor(messageColor);
            g2.setFont(new Font("Dialog", Font.BOLD, 8));
            int width = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (Game.W - width) / 2, Game.H / 2 + 13);
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }

    private static class TrailPoint {
        private final double x;
        private final double y;

        private TrailPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Particle {
        private double x;
        private double y;
        private double dx;
        private double dy;
        private final Color color;
        private int life = 24;

        private Particle(double x, double y, double dx, double dy, Color color) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
        }

        private void update() {
            x += dx;
            y += dy;
            dy += 0.018;
            dx *= 0.985;
            dy *= 0.985;
            life--;
        }

        private void render(Graphics2D g2) {
            float alpha = Math.max(0.05f, life / 24f);
            int size = life > 12 ? 2 : 1;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(color);
            g2.fillRect((int) x, (int) y, size, size);
            g2.setComposite(AlphaComposite.SrcOver);
        }
    }
}
