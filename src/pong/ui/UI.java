package pong.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import pong.main.Game;

public class UI {

    private final Font tinyFont = new Font("Dialog", Font.BOLD, 11);
    private final Font smallFont = new Font("Dialog", Font.BOLD, 13);
    private final Font scoreFont = new Font("Dialog", Font.BOLD, 34);
    private final Font hintFont = new Font("Dialog", Font.PLAIN, 11);

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int width = Game.W * Game.SCALE;

        g2.setColor(new Color(2, 6, 20, 218));
        g2.fillRoundRect(12, 10, width - 24, 70, 14, 14);
        g2.setColor(new Color(100, 220, 255, 120));
        g2.drawRoundRect(12, 10, width - 25, 70, 14, 14);

        g2.setFont(tinyFont);
        g2.setColor(new Color(180, 220, 240));
        drawCentered(g2, Game.gameMode.getTitle() + "  |  NIVEL " + Game.nivel, 25, width);

        g2.setFont(smallFont);
        g2.setColor(new Color(150, 230, 250));
        g2.drawString(Game.gameMode == pong.main.GameMode.SURVIVAL ? "PILOTO" : "VOCE", 30, 43);
        g2.setColor(new Color(255, 185, 150));
        g2.drawString(Game.gameMode.isVersus() ? "JOGADOR 2" : (Game.enemy.isBoss() ? "BOSS" : "MAQUINA"), width - 105, 43);

        g2.setFont(scoreFont);
        g2.setColor(new Color(110, 240, 255));
        g2.drawString(String.valueOf(Game.gameMode == pong.main.GameMode.SURVIVAL ? Game.survivalScore : Game.playerScore), 36, 73);
        g2.setColor(new Color(245, 250, 255));
        g2.drawString(":", width / 2 - 7, 73);
        g2.setColor(new Color(255, 150, 105));
        g2.drawString(String.valueOf(Game.gameMode == pong.main.GameMode.SURVIVAL ? Game.survivalLives : Game.enemyScore), width - 56, 73);

        g2.setFont(tinyFont);
        g2.setColor(new Color(255, 215, 100));
        String leftMeta = Game.gameMode == pong.main.GameMode.SURVIVAL
                ? "VIDAS " + Game.survivalLives
                : "META " + Game.getTargetScore();
        g2.drawString(leftMeta, 20, 103);
        g2.setColor(new Color(180, 205, 225));
        String centerMeta = "COMBO x" + Game.rallyCombo + "  REC " + Game.highScore;
        int centerX = (width - g2.getFontMetrics().stringWidth(centerMeta)) / 2;
        g2.drawString(centerMeta, centerX, 103);
        g2.setColor(new Color(255, 215, 100));
        g2.drawString("RANK " + (Game.stats == null ? 1 : Game.stats.getRank()), width - 68, 103);

        renderEnergy(g2, 18, 116, Game.player.energy, new Color(70, 220, 255),
                "PODER Q/SPACE " + Game.getAbilityLabel(true));
        if (Game.gameMode.isVersus()) {
            renderEnergy(g2, width - 127, 116, Game.playerTwo.energy, new Color(255, 180, 90),
                    "J2 O/I " + Game.getAbilityLabel(false));
        }

        g2.setColor(new Color(2, 6, 20, 170));
        g2.fillRoundRect(24, Game.H * Game.SCALE - 27, width - 48, 22, 10, 10);
        g2.setFont(hintFont);
        g2.setColor(new Color(190, 220, 235, 205));
        String hint = Game.gameMode.isVersus()
                ? "J/L movem J2  |  ESC pausa  |  M menu"
                : "A/D ou SETAS  |  Q troca poder  |  ESC pausa";
        int hintX = (width - g2.getFontMetrics().stringWidth(hint)) / 2;
        g2.drawString(hint, hintX, Game.H * Game.SCALE - 10);
    }

    private void renderEnergy(Graphics2D g2, int x, int y, int energy, Color color, String label) {
        int barWidth = 105;
        g2.setFont(tinyFont);
        g2.setColor(new Color(200, 220, 235));
        g2.drawString(label, x, y);
        g2.setColor(new Color(10, 16, 30, 220));
        g2.fillRoundRect(x, y + 5, barWidth, 7, 5, 5);
        g2.setColor(color);
        g2.fillRoundRect(x, y + 5, Math.max(2, (barWidth * energy) / 100), 7, 5, 5);
        g2.setColor(new Color(230, 250, 255, 180));
        g2.drawRoundRect(x, y + 5, barWidth, 7, 5, 5);
    }

    private void drawCentered(Graphics2D g2, String text, int y, int width) {
        int x = (width - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }
}
