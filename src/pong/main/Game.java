package pong.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;

import pong.entities.Ball;
import pong.entities.Enemy;
import pong.entities.Player;
import pong.ui.SoundManager;
import pong.ui.UI;
import pong.ui.VisualEffects;

public class Game extends Canvas implements Runnable, KeyListener {

    private static final long serialVersionUID = 1L;
    private static final Random RANDOM = new Random();
    private static JFrame frame;

    public static final int W = 160;
    public static final int H = 120;
    public static final int SCALE = 3;
    public static final int MAX_SCORE = 7;

    public static final String STATE_MENU = "MENU";
    public static final String STATE_PLAYING = "NORMAL";
    public static final String STATE_PAUSED = "PAUSED";
    public static final String STATE_GAME_OVER = "GAMEOVER";
    public static final String STATE_WIN = "WIN";
    public static final String STATE_STATS = "STATS";
    public static final String STATE_EDITOR = "EDITOR";
    public static final String STATE_COSMETICS = "COSMETICS";

    public final BufferedImage layer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

    public static String gameState = STATE_MENU;
    public static GameMode gameMode = GameMode.CLASSIC;
    public static int nivel = 1;
    public static boolean nextNivel;
    public static int playerScore;
    public static int enemyScore;
    public static int highScore;
    public static int survivalLives = 3;
    public static int survivalScore;
    public static int rallyCombo;
    public static int maxCombo;
    public static int totalPoints;
    public static int gameTicks;
    public static int menuSelection;
    public static int difficultyLevel = 1;
    public static boolean soundEnabled = true;
    public static int volumePercent = 70;
    public static boolean fullscreen;
    public static AbilityType selectedAbility = AbilityType.OVERDRIVE;
    public static AbilityType playerTwoAbility = AbilityType.SHIELD;
    public static int playerPointMultiplier = 1;
    public static int topPointMultiplier = 1;

    public static Player player;
    public static Player playerTwo;
    public static Enemy enemy;
    public static Ball ball;
    public static UI ui;
    public static VisualEffects effects;
    public static Arena arena;
    public static PowerUp powerUp;
    public static Stats stats;
    public static SoundManager sound;
    public static ArenaEditor editor;
    public static Inventory inventory;
    public static BossCampaign campaign;
    public static ArenaBlueprint activeBlueprint;

    private volatile boolean isRunning;
    private Thread thread;
    private static int stateAnimationTicks;
    private static boolean showStateMessage = true;
    private static int challengeProgress;
    private static int challengeTarget = 12;
    private static int cosmeticSlotSelection;

    public Game() {
        setPreferredSize(new Dimension(W * SCALE, H * SCALE));
        setIgnoreRepaint(true);
        setFocusable(true);
        addKeyListener(this);
        effects = new VisualEffects();
        ui = new UI();
        arena = new Arena();
        sound = new SoundManager();
        sound.setVolumePercent(volumePercent);
        stats = new Stats();
        stats.load();
        inventory = new Inventory(stats);
        editor = new ArenaEditor();
        campaign = new BossCampaign();
        highScore = stats.bestScore;
        loadSavedArena();
        restartToMenu();
    }

    public static void main(String[] args) {
        Game game = new Game();
        frame = new JFrame("Neon Ping Pong");
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

    public static void restartToMenu() {
        gameState = STATE_MENU;
        menuSelection = 0;
        stateAnimationTicks = 0;
        showStateMessage = true;
        resetMatchValues();
        resetRoundEntities(true);
    }

    private static void loadSavedArena() {
        activeBlueprint = ArenaBlueprint.fromShareCode(stats == null ? "" : stats.customArenaCode);
        if (activeBlueprint == null || !activeBlueprint.isValid()) {
            activeBlueprint = new ArenaBlueprint();
            activeBlueprint.setName("Arena Inicial");
        }
        if (editor != null) {
            editor.load(activeBlueprint);
        }
    }

    public static void openEditor() {
        gameState = STATE_EDITOR;
        if (editor == null) {
            editor = new ArenaEditor();
        }
    }

    public static void copyArenaCode() {
        if (editor != null && editor.isValid()) {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(editor.getShareCode()), null);
                if (effects != null) {
                    effects.announce("CODIGO COPIADO", new Color(110, 255, 180), 55);
                }
            } catch (Exception exception) {
                if (effects != null) {
                    effects.announce("CODIGO: " + editor.getShareCode(), new Color(255, 220, 100), 55);
                }
            }
        }
    }

    public static void saveEditorArena() {
        if (editor != null && editor.isValid()) {
            activeBlueprint = editor.getBlueprint();
            if (stats != null) {
                stats.setCustomArenaCode(activeBlueprint.toShareCode());
                stats.xp += 15;
                stats.save();
            }
            if (effects != null) {
                effects.announce("ARENA SALVA", new Color(110, 255, 180), 55);
            }
            gameState = STATE_MENU;
        }
    }

    public static CosmeticItem.Slot getCosmeticSlot() {
        CosmeticItem.Slot[] slots = CosmeticItem.Slot.values();
        return slots[Math.max(0, Math.min(slots.length - 1, cosmeticSlotSelection))];
    }

    public static void cycleCosmeticSlot(int direction) {
        int length = CosmeticItem.Slot.values().length;
        cosmeticSlotSelection = (cosmeticSlotSelection + direction + length) % length;
    }

    public static void cycleCosmeticItem(int direction) {
        if (inventory != null) {
            inventory.cycle(getCosmeticSlot(), direction);
        }
    }

    public static void equipCosmetic() {
        if (inventory != null) {
            cycleCosmeticItem(1);
        }
    }

    public static String getCosmeticLabel(CosmeticItem.Slot slot) {
        return inventory == null ? "-" : inventory.equipped(slot).getName();
    }

    public static Color getPaddlePrimary() {
        return inventory == null ? new Color(70, 220, 255) : inventory.equipped(CosmeticItem.Slot.PADDLE).getPrimary();
    }

    public static Color getPaddleSecondary() {
        return inventory == null ? new Color(35, 120, 220) : inventory.equipped(CosmeticItem.Slot.PADDLE).getSecondary();
    }

    public static Color getBallPrimary() {
        return inventory == null ? Color.white : inventory.equipped(CosmeticItem.Slot.BALL).getPrimary();
    }

    public static Color getBallSecondary() {
        return inventory == null ? new Color(100, 220, 255) : inventory.equipped(CosmeticItem.Slot.BALL).getSecondary();
    }

    public static Color getArenaPrimary() {
        return inventory == null ? new Color(65, 140, 190) : inventory.equipped(CosmeticItem.Slot.ARENA).getPrimary();
    }

    public static Color getArenaSecondary() {
        return inventory == null ? new Color(7, 13, 35) : inventory.equipped(CosmeticItem.Slot.ARENA).getSecondary();
    }

    public static String getBossLabel() {
        return campaign == null ? "-" : campaign.getBossType().getLabel();
    }

    public static int getBossHealth() {
        return campaign == null ? 0 : campaign.getBossHealth();
    }

    public static int getCampaignLives() {
        return campaign == null ? 0 : campaign.getPlayerLives();
    }

    public static void startMatch(GameMode mode) {
        gameMode = mode;
        resetMatchValues();
        gameState = STATE_PLAYING;
        stateAnimationTicks = 0;
        showStateMessage = true;
        challengeTarget = mode == GameMode.SURVIVAL ? 25 : 12;
        if (mode == GameMode.CAMPAIGN && campaign != null) {
            campaign.start();
        }
        if (mode == GameMode.MUTANT && activeBlueprint == null) {
            activeBlueprint = new ArenaBlueprint("Arena Mutante");
        }
        arena.setCustomBlueprint(mode == GameMode.MUTANT ? activeBlueprint : null);
        arena.reset();
        resetRoundEntities(true);
        if (sound != null) {
            sound.setEnabled(soundEnabled);
            sound.menu();
        }
    }

    public static String getDifficultyLabel() {
        return difficultyLevel == 1 ? "FACIL" : difficultyLevel == 2 ? "NORMAL" : "DIFICIL";
    }

    public static void cycleDifficulty() {
        difficultyLevel = difficultyLevel == 3 ? 1 : difficultyLevel + 1;
        if (sound != null) {
            sound.menu();
        }
    }

    public static String getVolumeLabel() {
        return volumePercent + "%";
    }

    public static void cycleVolume() {
        volumePercent += 25;
        if (volumePercent > 100) {
            volumePercent = 0;
        }
        if (sound != null) {
            sound.setVolumePercent(volumePercent);
            if (soundEnabled && volumePercent > 0) {
                sound.menu();
            }
        }
    }

    public static void toggleFullscreen() {
        if (frame == null) {
            return;
        }
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        fullscreen = !fullscreen;
        frame.dispose();
        frame.setUndecorated(fullscreen);
        if (fullscreen) {
            device.setFullScreenWindow(frame);
        } else {
            device.setFullScreenWindow(null);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }

    public static void toggleSound() {
        soundEnabled = !soundEnabled;
        if (sound != null) {
            sound.setEnabled(soundEnabled);
            sound.setVolumePercent(volumePercent);
            if (soundEnabled) {
                sound.menu();
            }
        }
    }

    public static void restartMatch() {
        startMatch(gameMode);
    }

    private static void resetMatchValues() {
        playerScore = 0;
        enemyScore = 0;
        survivalScore = 0;
        survivalLives = 3;
        rallyCombo = 0;
        maxCombo = 0;
        totalPoints = 0;
        nivel = 1;
        gameTicks = 0;
        nextNivel = false;
        playerPointMultiplier = 1;
        topPointMultiplier = 1;
        Ball.resetSpeed();
        Enemy.resetDifficulty();
        if (arena != null) {
            arena.reset();
        }
        powerUp = null;
    }

    private static void resetRoundEntities(boolean clearEffects) {
        player = new Player((W - 34) / 2.0, H - 14, getPaddlePrimary(), false);
        playerTwo = new Player((W - 34) / 2.0, 28, new Color(255, 185, 90), true);
        enemy = new Enemy((W - 34) / 2.0, 28);
        enemy.setBoss((gameMode == GameMode.SURVIVAL && survivalScore >= 3) || gameMode == GameMode.CAMPAIGN);
        ball = new Ball(W / 2.0 - 2, H / 2.0 - 2);
        if (clearEffects && effects != null) {
            effects.reset();
        }
    }

    public static Player getTopPlayer() {
        return gameMode.isVersus() ? playerTwo : null;
    }

    public static double getTopPaddleX() {
        return gameMode.isVersus() ? playerTwo.x : enemy.x;
    }

    public static double getTopPaddleY() {
        return gameMode.isVersus() ? playerTwo.y : enemy.y;
    }

    public static int getTopPaddleWidth() {
        return gameMode.isVersus() ? playerTwo.w : enemy.w;
    }

    public static int getTopPaddleHeight() {
        return gameMode.isVersus() ? playerTwo.h : enemy.h;
    }

    public static int getTargetScore() {
        return gameMode.getTargetScore() > 0 ? gameMode.getTargetScore() : Integer.MAX_VALUE;
    }

    public static String getAbilityLabel(boolean bottomPlayer) {
        return (bottomPlayer ? selectedAbility : playerTwoAbility).getLabel();
    }

    public void update(double deltaSeconds) {
        double delta = Math.min(0.05, Math.max(0.001, deltaSeconds));
        gameTicks++;

        if (STATE_PLAYING.equals(gameState)) {
            player.update(delta);
            if (gameMode.isVersus()) {
                playerTwo.update(delta);
            } else {
                enemy.update(delta);
            }
            if (arena != null) {
                arena.update(delta, gameMode);
            }
            if (gameMode == GameMode.CAMPAIGN && campaign != null) {
                campaign.update(delta);
            }
            ball.update(delta);
            updatePowerUp(delta);
            maybeSpawnPowerUp();
            updateChallenge();
            if (effects != null) {
                effects.update(ball.x, ball.y);
            }
        } else if (STATE_PAUSED.equals(gameState)) {
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

    private static void updatePowerUp(double deltaSeconds) {
        if (powerUp == null) {
            return;
        }
        powerUp.update(deltaSeconds);
        if (powerUp.intersects(player.x, player.y, player.w, player.h)) {
            collectPowerUp(powerUp, player, true);
            powerUp = null;
        } else if (gameMode.isVersus() && powerUp.intersects(playerTwo.x, playerTwo.y, playerTwo.w, playerTwo.h)) {
            collectPowerUp(powerUp, playerTwo, false);
            powerUp = null;
        } else if (!powerUp.active) {
            powerUp = null;
        }
    }

    private static void collectPowerUp(PowerUp collected, Player receiver, boolean bottomPlayer) {
        switch (collected.type) {
        case ENERGY:
            receiver.addEnergy(42);
            break;
        case SLOW:
            ball.slowTicks = 330;
            break;
        case SPLIT:
            ball.splitTicks = 330;
            break;
        case MULTI:
            if (bottomPlayer) {
                playerPointMultiplier = 2;
            } else {
                topPointMultiplier = 2;
            }
            break;
        default:
            break;
        }
        if (effects != null) {
            effects.powerUpCollected(collected.x, collected.y, collected.colorForType(), collected.type.getLabel());
        }
        if (sound != null) {
            sound.power();
        }
    }

    private static void maybeSpawnPowerUp() {
        if (powerUp != null || gameTicks < 180 || gameTicks % 360 != 0) {
            return;
        }
        if (gameMode == GameMode.CLASSIC && RANDOM.nextDouble() > 0.55) {
            return;
        }
        PowerUpType[] types = PowerUpType.values();
        PowerUpType type = types[RANDOM.nextInt(types.length)];
        powerUp = new PowerUp(24 + RANDOM.nextInt(112), 46 + RANDOM.nextInt(25), type);
        if (effects != null) {
            effects.powerUpSpawned(powerUp.x, powerUp.y, powerUp.colorForType());
        }
    }

    private static void updateChallenge() {
        if (rallyCombo >= challengeTarget && stats != null) {
            stats.completeChallenge();
            challengeTarget += gameMode == GameMode.SURVIVAL ? 10 : 6;
            if (effects != null) {
                effects.challengeComplete(challengeTarget);
            }
        }
    }

    public static void goalReached(boolean topBoundary, double impactX, double impactY) {
        if (!STATE_PLAYING.equals(gameState)) {
            return;
        }

        Player concedingPlayer = topBoundary ? getTopPlayer() : player;
        if (concedingPlayer != null && concedingPlayer.consumeShield()) {
            if (effects != null) {
                effects.shieldBreak(impactX, impactY);
            }
            if (sound != null) {
                sound.power();
            }
            resetRoundEntities(false);
            return;
        }

        boolean playerScored = topBoundary;
        rallyCombo = playerScored ? rallyCombo + 1 : 0;
        maxCombo = Math.max(maxCombo, rallyCombo);

        if (gameMode == GameMode.CAMPAIGN) {
            if (playerScored) {
                totalPoints += 150 + rallyCombo * 30;
                boolean finalBossDefeated = campaign.hitBoss();
                if (campaign.wasBossDefeated()) {
                    if (stats != null) {
                        stats.recordBossDefeat();
                    }
                }
                if (finalBossDefeated) {
                    endMatch(true);
                    return;
                }
            } else if (campaign.loseLife()) {
                endMatch(false);
                return;
            }
            if (effects != null) {
                effects.announce(campaign.getMessage(), campaign.getBossColor(), 45);
            }
            resetRoundEntities(false);
        } else if (gameMode == GameMode.SURVIVAL) {
            if (playerScored) {
                survivalScore += playerPointMultiplier;
                playerPointMultiplier = 1;
                player.addEnergy(20);
                totalPoints += 100 + rallyCombo * 20;
            } else {
                survivalLives--;
                if (survivalLives <= 0) {
                    endMatch(false);
                    return;
                }
            }
            if (survivalScore >= 3) {
                enemy.setBoss(true);
            }
            nivel = 1 + survivalScore / 3;
            resetRoundEntities(false);
        } else {
            if (playerScored) {
                playerScore += playerPointMultiplier;
                playerPointMultiplier = 1;
                highScore = Math.max(highScore, playerScore);
                player.addEnergy(20);
            } else {
                enemyScore += topPointMultiplier;
                topPointMultiplier = 1;
                if (gameMode.isVersus()) {
                    playerTwo.addEnergy(20);
                }
            }
            totalPoints += 100 + rallyCombo * 25;
            if (effects != null) {
                effects.pointScored(playerScored, impactX, impactY);
            }
            if (sound != null) {
                sound.point(playerScored);
            }
            if (playerScore >= getTargetScore() || enemyScore >= getTargetScore()) {
                endMatch(playerScore >= getTargetScore());
                return;
            }
            nivel = 1 + (playerScore + enemyScore) / 3;
            Ball.speed = Math.min(gameMode == GameMode.TURBO ? 5.1 : 4.4,
                    (gameMode == GameMode.TURBO ? 2.75 : 2.0) + (nivel - 1) * 0.22);
            Enemy.difficulty = Math.min(0.11, 0.055 + (nivel - 1) * 0.007 + difficultyLevel * 0.008);
            resetRoundEntities(false);
        }
    }

    public static void registerPoint(boolean playerScored, double impactX, double impactY) {
        goalReached(playerScored, impactX, impactY);
    }

    private static void endMatch(boolean won) {
        gameState = won ? STATE_WIN : STATE_GAME_OVER;
        stateAnimationTicks = 0;
        showStateMessage = true;
        int score = gameMode == GameMode.SURVIVAL ? survivalScore : gameMode == GameMode.CAMPAIGN ? campaign.getBossIndex() : playerScore;
        if (stats != null) {
            stats.recordMatch(won, score, maxCombo, totalPoints);
            highScore = Math.max(highScore, stats.bestScore);
        }
        if (effects != null) {
            effects.matchEnded(won);
        }
        if (sound != null) {
            sound.point(won);
        }
    }

    public static void onPaddleHit(boolean bottomPlayer, double impactX, double impactY) {
        rallyCombo++;
        maxCombo = Math.max(maxCombo, rallyCombo);
        Player receiver = bottomPlayer ? player : getTopPlayer();
        if (receiver != null) {
            receiver.addEnergy(7);
        }
        if (effects != null) {
            effects.paddleHit(impactX, impactY, bottomPlayer ? new Color(70, 220, 255) : new Color(255, 150, 80));
            if (rallyCombo >= 4) {
                effects.combo(rallyCombo);
            }
        }
        if (sound != null) {
            sound.paddle();
        }
    }

    public static void onWallHit(double x, double y) {
        if (effects != null) {
            effects.wallHit(x, y);
        }
        if (sound != null) {
            sound.wall();
        }
    }

    public static void onArenaHit(double x, double y) {
        if (effects != null) {
            effects.arenaHit(x, y);
        }
        if (sound != null) {
            sound.event();
        }
    }

    public static void activateAbility(boolean bottomPlayer) {
        Player receiver = bottomPlayer ? player : playerTwo;
        if (receiver == null || !gameMode.isVersus() && !bottomPlayer) {
            return;
        }
        AbilityType ability = bottomPlayer ? selectedAbility : playerTwoAbility;
        if (!receiver.spendEnergy()) {
            if (effects != null) {
                effects.abilityDenied(receiver.x, receiver.y);
            }
            return;
        }
        switch (ability) {
        case OVERDRIVE:
            receiver.overdriveTicks = 210;
            Ball.speed = Math.min(5.1, Ball.speed + 0.55);
            break;
        case SHIELD:
            receiver.shieldTicks = 600;
            break;
        case WIDE:
            receiver.wideTicks = 360;
            break;
        default:
            break;
        }
        if (effects != null) {
            effects.abilityActivated(receiver.x + receiver.w / 2.0, receiver.y, ability.getLabel());
        }
        if (sound != null) {
            sound.power();
        }
    }

    public static void cycleAbility(boolean bottomPlayer) {
        if (!bottomPlayer && !gameMode.isVersus()) {
            return;
        }
        if (bottomPlayer) {
            selectedAbility = AbilityType.values()[(selectedAbility.ordinal() + 1) % AbilityType.values().length];
        } else {
            playerTwoAbility = AbilityType.values()[(playerTwoAbility.ordinal() + 1) % AbilityType.values().length];
        }
        if (sound != null) {
            sound.menu();
        }
    }

    public static void togglePause() {
        if (STATE_PLAYING.equals(gameState)) {
            gameState = STATE_PAUSED;
        } else if (STATE_PAUSED.equals(gameState)) {
            gameState = STATE_PLAYING;
        }
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics2D layerGraphics = layer.createGraphics();
        if (STATE_EDITOR.equals(gameState)) {
            if (editor != null) {
                editor.render(layerGraphics);
            }
        } else {
            if (effects != null) {
                effects.renderBackground(layerGraphics);
                effects.renderTrail(layerGraphics);
            }
            if (arena != null) {
                arena.render(layerGraphics);
            }
            if (powerUp != null) {
                powerUp.render(layerGraphics);
            }
            player.render(layerGraphics);
            if (gameMode.isVersus()) {
                playerTwo.render(layerGraphics);
            } else {
                enemy.render(layerGraphics);
            }
            ball.render(layerGraphics);
            if (effects != null) {
                effects.renderForeground(layerGraphics);
            }
        }
        layerGraphics.dispose();

        Graphics g = bs.getDrawGraphics();
        try {
            Graphics2D output = (Graphics2D) g;
            int shakeX = effects == null ? 0 : effects.getShakeX() * SCALE;
            int shakeY = effects == null ? 0 : effects.getShakeY() * SCALE;
            output.translate(shakeX, shakeY);
            output.drawImage(layer, 0, 0, W * SCALE, H * SCALE, null);
            output.translate(-shakeX, -shakeY);
            if (STATE_PLAYING.equals(gameState) || STATE_PAUSED.equals(gameState)) {
                ui.render(g);
            }
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
        if (STATE_EDITOR.equals(gameState)) {
            g2.setColor(new Color(2, 5, 18, 115));
            g2.fillRect(0, 0, W * SCALE, 30);
            g2.fillRect(0, Game.H * SCALE - 42, W * SCALE, 42);
            drawCentered(g2, "ENTER salva  |  BACKSPACE limpa  |  ESC menu  |  TAB troca elemento", 116,
                    new Font("Dialog", Font.BOLD, 12), new Color(255, 220, 100));
            return;
        }
        if (STATE_COSMETICS.equals(gameState)) {
            g2.setColor(new Color(3, 6, 20, 238));
            g2.fillRect(0, 0, W * SCALE, H * SCALE);
            renderCosmeticsOverlay(g2);
            return;
        }
        g2.setColor(new Color(3, 6, 20, 228));
        g2.fillRect(0, 0, W * SCALE, H * SCALE);

        if (STATE_MENU.equals(gameState)) {
            drawCentered(g2, "NEON PING PONG", 25, new Font("Dialog", Font.BOLD, 31), new Color(120, 240, 255));
            drawCentered(g2, "DUEL OF ENERGY", 39, new Font("Dialog", Font.PLAIN, 13), Color.white);
            String[] menuItems = { "JOGAR CLASSICO", "SOBREVIVENCIA", "MODO TURBO", "VERSUS LOCAL",
                    "ARENA MUTANTE", "CAMPANHA BOSS", "SKINS E ITENS", "EDITOR DE ARENA", "ESTATISTICAS", "SAIR" };
            for (int i = 0; i < menuItems.length; i++) {
                Color color = i == menuSelection ? new Color(255, 215, 90) : new Color(180, 210, 230);
                drawCentered(g2, (i == menuSelection ? "> " : "  ") + menuItems[i], 43 + i * 6,
                        new Font("Dialog", Font.BOLD, i == menuSelection ? 10 : 9), color);
            }
            drawCentered(g2, "SETAS + ENTER | F2 dificuldade | F3 som | F4 volume | F11 tela cheia", 116,
                    new Font("Dialog", Font.PLAIN, 9), new Color(170, 200, 220));
        } else if (STATE_PAUSED.equals(gameState)) {
            drawCentered(g2, "PAUSADO", 45, new Font("Dialog", Font.BOLD, 30), Color.white);
            drawCentered(g2, "ESC ou ENTER para continuar", 68, new Font("Dialog", Font.BOLD, 15), new Color(255, 215, 90));
            drawCentered(g2, "M volta ao menu", 87, new Font("Dialog", Font.PLAIN, 13), new Color(180, 220, 240));
        } else if (STATE_STATS.equals(gameState)) {
            drawCentered(g2, "ESTATISTICAS", 24, new Font("Dialog", Font.BOLD, 27), new Color(120, 240, 255));
            drawCentered(g2, "Rank " + stats.getRank() + "  |  XP " + stats.xp, 42, new Font("Dialog", Font.BOLD, 14), Color.white);
            drawCentered(g2, "Partidas " + stats.matches + "   Vitorias " + stats.wins + "   Derrotas " + stats.losses, 60,
                    new Font("Dialog", Font.PLAIN, 13), new Color(180, 220, 240));
            drawCentered(g2, "Melhor combo " + stats.bestCombo + "   Melhor score " + stats.bestScore, 76,
                    new Font("Dialog", Font.PLAIN, 13), new Color(180, 220, 240));
            drawCentered(g2, "Arenas desbloqueadas " + stats.unlockedArena + "   Desafios " + stats.completedChallenges, 92,
                    new Font("Dialog", Font.PLAIN, 13), new Color(180, 220, 240));
            drawCentered(g2, "ENTER ou ESC para voltar", 114, new Font("Dialog", Font.BOLD, 14), new Color(255, 215, 90));
        } else {
            String title = STATE_WIN.equals(gameState) ? "VOCE VENCEU" : "GAME OVER";
            String subtitle = gameMode == GameMode.SURVIVAL
                    ? "Sobreviveu com " + survivalScore + " pontos e " + maxCombo + " combo"
                    : "Placar final  " + playerScore + "  x  " + enemyScore;
            drawCentered(g2, title, 35, new Font("Dialog", Font.BOLD, 29), Color.white);
            drawCentered(g2, subtitle, 59, new Font("Dialog", Font.BOLD, 15), new Color(160, 230, 245));
            drawCentered(g2, "SCORE " + totalPoints + "  |  RECORDE " + highScore, 78,
                    new Font("Dialog", Font.PLAIN, 14), new Color(180, 220, 240));
            if (showStateMessage) {
                drawCentered(g2, "ENTER joga novamente  |  ESC volta ao menu", 108,
                        new Font("Dialog", Font.BOLD, 13), new Color(255, 215, 90));
            }
        }
    }

    private void renderCosmeticsOverlay(Graphics2D g2) {
        CosmeticItem.Slot slot = getCosmeticSlot();
        drawCentered(g2, "SKINS E ITENS", 22, new Font("Dialog", Font.BOLD, 27), new Color(120, 240, 255));
        drawCentered(g2, "TAB muda categoria  |  SETAS trocam  |  ENTER equipa  |  ESC volta", 35,
                new Font("Dialog", Font.PLAIN, 11), new Color(180, 220, 240));
        drawCentered(g2, "CATEGORIA: " + slot.name(), 51, new Font("Dialog", Font.BOLD, 15), new Color(255, 215, 90));
        CosmeticItem[] items = CosmeticCatalog.forSlot(slot);
        for (int i = 0; i < items.length; i++) {
            CosmeticItem item = items[i];
            boolean equipped = inventory != null && inventory.equipped(slot).getId().equals(item.getId());
            boolean unlocked = inventory != null && inventory.isUnlocked(item);
            Color color = unlocked ? item.getPrimary() : new Color(110, 125, 145);
            String marker = equipped ? "> " : "  ";
            String status = unlocked ? (equipped ? "EQUIPADO" : "OK") : "XP " + item.getUnlockXp();
            drawCentered(g2, marker + item.getName() + "  [" + status + "]", 67 + i * 13,
                    new Font("Dialog", Font.BOLD, equipped ? 14 : 12), color);
        }
        CosmeticItem equipped = inventory == null ? null : inventory.equipped(slot);
        if (equipped != null) {
            drawCentered(g2, equipped.getDescription(), 109, new Font("Dialog", Font.PLAIN, 11), new Color(190, 220, 235));
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

        if (STATE_MENU.equals(gameState)) {
            if (keyCode == KeyEvent.VK_F11) {
                toggleFullscreen();
            } else if (keyCode == KeyEvent.VK_F2) {
                cycleDifficulty();
            } else if (keyCode == KeyEvent.VK_F3) {
                toggleSound();
            } else if (keyCode == KeyEvent.VK_F4) {
                cycleVolume();
            } else if (keyCode == KeyEvent.VK_UP) {
                menuSelection = (menuSelection + 9) % 10;
                sound.menu();
            } else if (keyCode == KeyEvent.VK_DOWN) {
                menuSelection = (menuSelection + 1) % 10;
                sound.menu();
            } else if (keyCode == KeyEvent.VK_ENTER) {
                selectMenuItem();
            }
            return;
        }

        if (STATE_STATS.equals(gameState)) {
            if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE) {
                gameState = STATE_MENU;
            }
            return;
        }

        if (STATE_COSMETICS.equals(gameState)) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                gameState = STATE_MENU;
            } else if (keyCode == KeyEvent.VK_TAB) {
                cycleCosmeticSlot(1);
            } else if (keyCode == KeyEvent.VK_LEFT) {
                cycleCosmeticItem(-1);
            } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_ENTER) {
                cycleCosmeticItem(1);
            }
            return;
        }

        if (STATE_EDITOR.equals(gameState)) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                gameState = STATE_MENU;
            } else if (keyCode == KeyEvent.VK_UP) {
                editor.moveCursor(0, -4);
            } else if (keyCode == KeyEvent.VK_DOWN) {
                editor.moveCursor(0, 4);
            } else if (keyCode == KeyEvent.VK_LEFT) {
                editor.moveCursor(-4, 0);
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                editor.moveCursor(4, 0);
            } else if (keyCode == KeyEvent.VK_TAB) {
                editor.cycleType(1);
            } else if (keyCode == KeyEvent.VK_SPACE) {
                editor.place();
            } else if (keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_DELETE) {
                editor.remove();
            } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                editor.clear();
            } else if (keyCode == KeyEvent.VK_C) {
                copyArenaCode();
            } else if (keyCode == KeyEvent.VK_ENTER) {
                saveEditorArena();
            }
            return;
        }

        if (STATE_PAUSED.equals(gameState)) {
            if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER) {
                togglePause();
            } else if (keyCode == KeyEvent.VK_M) {
                restartToMenu();
            }
            return;
        }

        if (STATE_GAME_OVER.equals(gameState) || STATE_WIN.equals(gameState)) {
            if (keyCode == KeyEvent.VK_ENTER) {
                restartMatch();
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                restartToMenu();
            }
            return;
        }

        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            player.right = true;
        }
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            player.left = true;
        }
        if (keyCode == KeyEvent.VK_F11) {
            toggleFullscreen();
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            activateAbility(true);
        }
        if (keyCode == KeyEvent.VK_Q) {
            cycleAbility(true);
        }
        if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_P) {
            togglePause();
        }
        if (keyCode == KeyEvent.VK_M) {
            restartToMenu();
        }

        if (gameMode.isVersus()) {
            if (keyCode == KeyEvent.VK_L) {
                playerTwo.right = true;
            }
            if (keyCode == KeyEvent.VK_J) {
                playerTwo.left = true;
            }
            if (keyCode == KeyEvent.VK_I) {
                activateAbility(false);
            }
            if (keyCode == KeyEvent.VK_O) {
                cycleAbility(false);
            }
        }
    }

    private void selectMenuItem() {
        switch (menuSelection) {
        case 0:
            startMatch(GameMode.CLASSIC);
            break;
        case 1:
            startMatch(GameMode.SURVIVAL);
            break;
        case 2:
            startMatch(GameMode.TURBO);
            break;
        case 3:
            startMatch(GameMode.VERSUS);
            break;
        case 4:
            startMatch(GameMode.MUTANT);
            break;
        case 5:
            startMatch(GameMode.CAMPAIGN);
            break;
        case 6:
            gameState = STATE_COSMETICS;
            break;
        case 7:
            openEditor();
            break;
        case 8:
            gameState = STATE_STATS;
            break;
        case 9:
            stop();
            System.exit(0);
            break;
        default:
            break;
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
        if (gameMode.isVersus()) {
            if (keyCode == KeyEvent.VK_L) {
                playerTwo.right = false;
            }
            if (keyCode == KeyEvent.VK_J) {
                playerTwo.left = false;
            }
        }
    }
}
