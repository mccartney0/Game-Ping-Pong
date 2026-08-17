package pong.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import pong.entities.Ball;
import pong.entities.Enemy;
import pong.entities.Player;
import pong.ui.UI;
import pong.ui.VisualEffects;

public class Game extends Canvas implements Runnable, KeyListener {

    private static final long serialVersionUID = 1L;

    public static final int W = 160;
    public static final int H = 120;
    public static final int SCALE = 3;
    public static final int MAX_SCORE = 7;

    public static final String STATE_PLAYING = "NORMAL";
    public static final String STATE_GAME_OVER = "GAMEOVER";
    public static final String STATE_WIN = "WIN";

    public final BufferedImage layer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

    public static String gameState = STATE_PLAYING;
    public static int nivel = 1;
    public static boolean nextNivel;
    public static int playerScore;
    public static int enemyScore;
    public static int highScore;
    public static long gameTicks;

    public static Player player;
    public static Enemy enemy;
    public static Ball ball;
    public static UI ui;
    public static VisualEffects effects;

    private volatile boolean isRunning;
    private Thread thread;
    private static int stateAnimationTicks;
    private static boolean showStateMessage = true;

    public Game() {
        setPreferredSize(new Dimension(W * SCALE, H * SCALE));
        setIgnoreRepaint(true);
        setFocusable(true);
        addKeyListener(this);
        effects = new VisualEffects();
        ui = new UI();
        restartMatch();
    }

    public static void main(String[] args) {
        Game game = new Game();
        JFrame frame = new JFrame("Neon Ping Pong");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        game.start();
    }

    public synchronized void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        thread = new Thread(this, "ping-pong-game-loop");
        thread.start();
        requestFocusInWindow();
    }

    public synchronized void stop() {
        isRunning = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public static void registerPoint(boolean playerScored, double impactX, double impactY) {
        if (!STATE_PLAYING.equals(gameState)) {
            return;
        }

        if (playerScored) {
            playerScore++;
            highScore = Math.max(highScore, playerScore);
        } else {
            enemyScore++;
        }

        if (effects != null) {
            effects.pointScored(playerScored, impactX, impactY);
        }

        if (playerScore >= MAX_SCORE || enemyScore >= MAX_SCORE) {
            gameState = playerScore >= MAX_SCORE ? STATE_WIN : STATE_GAME_OVER;
            stateAnimationTicks = 0;
            showStateMessage = true;
            return;
        }

        nivel = 1 + (playerScore + enemyScore) / 3;
        Ball.speed = Math.min(4.2, 2.0 + (nivel - 1) * 0.18);
        Enemy.difficulty = Math.min(0.09, 0.055 + (nivel - 1) * 0.006);
        nextNivel = true;
        resetRoundEntities(false);
    }

    public static void restartMatch() {
        playerScore = 0;
        enemyScore = 0;
        nivel = 1;
        nextNivel = false;
        gameState = STATE_PLAYING;
        stateAnimationTicks = 0;
        showStateMessage = true;
        Ball.resetSpeed();
        Enemy.resetDifficulty();
        resetRoundEntities(true);
    }

    private static void resetRoundEntities(boolean clearEffects) {
        player = new Player((W - 34) / 2.0, H - 14);
        enemy = new Enemy((W - 34) / 2.0, 28);
        ball = new Ball(W / 2.0 - 2, H / 2.0 - 2);
        if (clearEffects && effects != null) {
            effects.reset();
        }
    }

    public void update(double deltaSeconds) {
        double delta = Math.min(0.05, Math.max(0.001, deltaSeconds));
        gameTicks++;

        if (STATE_PLAYING.equals(gameState)) {
            player.update(delta);
            enemy.update(delta);
            ball.update(delta);
            if (effects != null) {
                effects.update(ball.x, ball.y);
            }
        } else {
            stateAnimationTicks++;
            if (stateAnimationTicks >= 30) {
                stateAnimationTicks = 0;
                showStateMessage = !showStateMessage;
            }
            if (effects != null) {
                effects.update(ball.x, ball.y);
            }
        }
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics2D layerGraphics = layer.createGraphics();
        if (effects != null) {
            effects.renderBackground(layerGraphics);
            effects.renderTrail(layerGraphics);
        }
        player.render(layerGraphics);
        enemy.render(layerGraphics);
        ball.render(layerGraphics);
        if (effects != null) {
            effects.renderForeground(layerGraphics);
        }
        layerGraphics.dispose();

        Graphics g = bs.getDrawGraphics();
        try {
            g.drawImage(layer, 0, 0, W * SCALE, H * SCALE, null);
            ui.render(g);
            renderStateOverlay(g);
            bs.show();
        } finally {
            g.dispose();
        }
    }

    private void renderStateOverlay(Graphics g) {
        if (STATE_PLAYING.equals(gameState)) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(3, 6, 20, 225));
        g2.fillRect(0, 0, W * SCALE, H * SCALE);

        String title = STATE_WIN.equals(gameState) ? "VOCE VENCEU" : "GAME OVER";
        String subtitle = STATE_WIN.equals(gameState)
                ? "Pontuacao maxima alcancada!"
                : "A maquina chegou a pontuacao maxima.";
        drawCentered(g2, title, 39, new Font("Dialog", Font.BOLD, 28), Color.white);
        drawCentered(g2, subtitle, 66, new Font("Dialog", Font.PLAIN, 14), new Color(180, 220, 240));
        drawCentered(g2, "Placar final  " + playerScore + "  x  " + enemyScore, 91,
                new Font("Dialog", Font.BOLD, 18), new Color(110, 240, 255));

        if (showStateMessage) {
            drawCentered(g2, "Pressione ENTER para jogar novamente", 116,
                    new Font("Dialog", Font.BOLD, 15), new Color(255, 215, 100));
        }
    }

    private void drawCentered(Graphics2D g, String text, int logicalY, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        int x = (W * SCALE - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, logicalY * SCALE);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        requestFocusInWindow();

        while (isRunning) {
            long now = System.nanoTime();
            double deltaSeconds = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            update(deltaSeconds);
            render();

            try {
                Thread.sleep(2L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Entrada tratada em keyPressed/keyReleased.
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            player.right = true;
        }
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            player.left = true;
        }
        if (keyCode == KeyEvent.VK_ENTER && !STATE_PLAYING.equals(gameState)) {
            restartMatch();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            player.right = false;
        }
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            player.left = false;
        }
    }
}
