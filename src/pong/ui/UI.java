package pong.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import pong.main.Game;

public class UI {

    private final Font smallFont = new Font("Dialog", Font.BOLD, 13);
    private final Font scoreFont = new Font("Dialog", Font.BOLD, 34);
    private final Font hintFont = new Font("Dialog", Font.PLAIN, 12);

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int width = Game.W * Game.SCALE;

        g2.setColor(new Color(2, 6, 20, 215));
        g2.fillRoundRect(12, 10, width - 24, 66, 14, 14);
        g2.setColor(new Color(100, 220, 255, 120));
        g2.drawRoundRect(12, 10, width - 25, 66, 14, 14);

        g2.setFont(smallFont);
        g2.setColor(new Color(150, 230, 250));
        g2.drawString("VOCE", 30, 29);
        g2.setColor(new Color(255, 170, 190));
        g2.drawString("MAQUINA", width - 88, 29);

        g2.setFont(scoreFont);
        g2.setColor(new Color(110, 240, 255));
        g2.drawString(String.valueOf(Game.playerScore), 36, 61);
        g2.setColor(new Color(245, 250, 255));
        g2.drawString(":", width / 2 - 7, 61);
        g2.setColor(new Color(255, 110, 145));
        g2.drawString(String.valueOf(Game.enemyScore), width - 56, 61);

        g2.setFont(smallFont);
        g2.setColor(new Color(255, 215, 100));
        g2.drawString("NIVEL " + Game.nivel, 20, 101);
        g2.drawString("META " + Game.MAX_SCORE, width - 79, 101);
        g2.setColor(new Color(180, 205, 225));
        g2.drawString("RECORDE " + Game.highScore, width / 2 - 40, 101);

        g2.setFont(hintFont);
        g2.setColor(new Color(190, 220, 235, 190));
        String hint = "A/D ou SETAS  |  ENTER reinicia ao fim";
        int hintX = (width - g2.getFontMetrics().stringWidth(hint)) / 2;
        g2.drawString(hint, hintX, Game.H * Game.SCALE - 10);
    }
}
