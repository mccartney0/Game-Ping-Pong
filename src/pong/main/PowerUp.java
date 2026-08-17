package pong.main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class PowerUp {

    public static final int SIZE = 8;

    public double x;
    public double y;
    public final PowerUpType type;
    public boolean active = true;

    private int ticks;

    public PowerUp(double x, double y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update(double deltaSeconds) {
        ticks++;
        y += 0.12 * Math.min(2.5, Math.max(0.35, deltaSeconds * 60.0));
        if (y > Game.H + SIZE) {
            active = false;
        }
    }

    public boolean intersects(double paddleX, double paddleY, int paddleWidth, int paddleHeight) {
        return active && x + SIZE >= paddleX && x <= paddleX + paddleWidth
                && y + SIZE >= paddleY && y <= paddleY + paddleHeight;
    }

    public void render(Graphics g) {
        if (!active) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        Color color = colorForType();
        float pulse = 0.55f + (float) (Math.sin(ticks * 0.18) * 0.15);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
        g2.setColor(color);
        g2.fillOval((int) x - 3, (int) y - 3, SIZE + 6, SIZE + 6);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Color.white);
        g2.fillRoundRect((int) x, (int) y, SIZE, SIZE, 3, 3);
        g2.setColor(color);
        g2.setFont(new Font("Dialog", Font.BOLD, 7));
        g2.drawString(type.getLabel(), (int) x - 3, (int) y - 7);
    }

    public Color colorForType() {
        switch (type) {
        case ENERGY:
            return new Color(255, 215, 70);
        case SLOW:
            return new Color(120, 190, 255);
        case SPLIT:
            return new Color(210, 120, 255);
        case MULTI:
            return new Color(100, 255, 175);
        default:
            return Color.white;
        }
    }
}
