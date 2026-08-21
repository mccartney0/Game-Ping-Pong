package com.mccartney0.gamepingpong;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mccartney0.gamepingpong.input.PaddleSide;
import com.mccartney0.gamepingpong.input.PaddleTouchTarget;
import com.mccartney0.gamepingpong.visual.BallEffects;

public class TouchPongWorld implements PaddleTouchTarget {

    public static final float WIDTH = 16f;
    public static final float HEIGHT = 12f;
    public static final int MATCH_SCORE = 7;
    public static final float BASE_PADDLE_WIDTH = 3.4f;
    public static final float PADDLE_HEIGHT = 0.5f;

    public float playerX = WIDTH / 2f;
    public float enemyX = WIDTH / 2f;
    public final float playerY = 0.7f;
    public final float enemyY = HEIGHT - 1.0f;
    public final float paddleWidth = BASE_PADDLE_WIDTH;
    public final float paddleHeight = PADDLE_HEIGHT;
    public float ballX = WIDTH / 2f;
    public float ballY = HEIGHT / 2f;
    public float ballDx = 5.7f;
    public float ballDy = 4.1f;
    public int playerScore;
    public int enemyScore;
    public int abilityActivations;
    public int playerAbilityActivations;
    public int enemyAbilityActivations;
    public int pauseToggles;
    public int playerDragEvents;
    public int enemyDragEvents;
    public int powerUpsSpawned;
    public int powerUpsCollected;
    public int survivalLives = 3;
    public int bossIndex;
    public int bossHealth = 3;
    public int playerPointMultiplier = 1;
    public int enemyPointMultiplier = 1;
    public float playerEnergy = 70f;
    public float enemyEnergy = 70f;

    private boolean paused;
    private MobileGameMode mode = MobileGameMode.CLASSIC;
    private float overdriveTicks;
    private float enemyOverdriveTicks;
    private float playerShieldTicks;
    private float enemyShieldTicks;
    private float playerWideTicks;
    private float enemyWideTicks;
    private float slowTicks;
    private float splitTicks;
    private float powerUpClock;
    private int nextPowerUpOrdinal;
    private PowerUp activePowerUp;
    private static final String[] BOSS_NAMES = {"VOLT", "MIRROR", "TWIN", "GRAVITY"};
    private AbilityType playerAbility = AbilityType.OVERDRIVE;
    private AbilityType enemyAbility = AbilityType.SHIELD;
    private String lastEvent = "READY";
    private final BallEffects ballEffects = new BallEffects();

    public TouchPongWorld() {
        resetMatch();
    }

    public void resetMatch() {
        playerScore = 0;
        enemyScore = 0;
        abilityActivations = 0;
        playerAbilityActivations = 0;
        enemyAbilityActivations = 0;
        pauseToggles = 0;
        playerDragEvents = 0;
        enemyDragEvents = 0;
        powerUpsSpawned = 0;
        powerUpsCollected = 0;
        survivalLives = 3;
        bossIndex = 0;
        bossHealth = 3;
        playerPointMultiplier = 1;
        enemyPointMultiplier = 1;
        playerEnergy = 70f;
        enemyEnergy = 70f;
        playerAbility = AbilityType.OVERDRIVE;
        enemyAbility = AbilityType.SHIELD;
        paused = false;
        overdriveTicks = 0f;
        enemyOverdriveTicks = 0f;
        playerShieldTicks = 0f;
        enemyShieldTicks = 0f;
        playerWideTicks = 0f;
        enemyWideTicks = 0f;
        slowTicks = 0f;
        splitTicks = 0f;
        powerUpClock = 0f;
        nextPowerUpOrdinal = 0;
        activePowerUp = null;
        ballEffects.clear();
        playerX = WIDTH / 2f;
        enemyX = WIDTH / 2f;
        resetBall(1f);
        lastEvent = "READY";
    }

    public void setMode(MobileGameMode mode) {
        this.mode = mode == null ? MobileGameMode.CLASSIC : mode;
        resetMatch();
        lastEvent = "MODE " + this.mode.getLabel();
    }

    public MobileGameMode getMode() {
        return mode;
    }

    @Override
    public void movePaddleTo(PaddleSide side, float worldX) {
        float width = getPaddleWidth(side);
        float clamped = Math.max(width / 2f, Math.min(WIDTH - width / 2f, worldX));
        if (side == PaddleSide.BOTTOM) {
            playerX = clamped;
            playerDragEvents++;
        } else {
            enemyX = clamped;
            enemyDragEvents++;
        }
    }

    @Override
    public void activateAbility(PaddleSide side) {
        boolean bottom = side == PaddleSide.BOTTOM;
        AbilityType ability = bottom ? playerAbility : enemyAbility;
        float energy = bottom ? playerEnergy : enemyEnergy;
        if (energy < ability.getEnergyCost()) {
            lastEvent = bottom ? "PLAYER ENERGY LOW" : "TOP ENERGY LOW";
            ballEffects.burst(bottom ? playerX : enemyX, bottom ? playerY : enemyY,
                    ballDx, ballDy, 6, 0.65f);
            return;
        }
        if (bottom) {
            playerEnergy -= ability.getEnergyCost();
            playerAbilityActivations++;
        } else {
            enemyEnergy -= ability.getEnergyCost();
            enemyAbilityActivations++;
        }
        abilityActivations++;
        switch (ability) {
        case OVERDRIVE:
            if (bottom) {
                overdriveTicks = 3.5f;
            } else {
                enemyOverdriveTicks = 3.5f;
            }
            lastEvent = bottom ? "PLAYER OVERDRIVE" : "TOP OVERDRIVE";
            break;
        case SHIELD:
            if (bottom) {
                playerShieldTicks = 8f;
            } else {
                enemyShieldTicks = 8f;
            }
            lastEvent = bottom ? "PLAYER SHIELD" : "TOP SHIELD";
            break;
        case WIDE:
            if (bottom) {
                playerWideTicks = 6f;
            } else {
                enemyWideTicks = 6f;
            }
            lastEvent = bottom ? "PLAYER WIDE" : "TOP WIDE";
            break;
        default:
            break;
        }
        ballEffects.burst(bottom ? playerX : enemyX, bottom ? playerY : enemyY,
                ballDx, ballDy, 18, 1.5f);
    }

    @Override
    public void cycleAbility(PaddleSide side) {
        if (side == PaddleSide.BOTTOM) {
            playerAbility = playerAbility.next();
            lastEvent = "PLAYER " + playerAbility.getLabel();
        } else {
            enemyAbility = enemyAbility.next();
            lastEvent = "TOP " + enemyAbility.getLabel();
        }
    }

    @Override
    public void togglePause() {
        paused = !paused;
        pauseToggles++;
        lastEvent = paused ? "PAUSED" : "RESUMED";
    }

    public void update(float delta) {
        float safeDelta = Math.min(0.05f, Math.max(0f, delta));
        if (paused) {
            return;
        }
        overdriveTicks = decrease(overdriveTicks, safeDelta);
        enemyOverdriveTicks = decrease(enemyOverdriveTicks, safeDelta);
        playerShieldTicks = decrease(playerShieldTicks, safeDelta);
        enemyShieldTicks = decrease(enemyShieldTicks, safeDelta);
        playerWideTicks = decrease(playerWideTicks, safeDelta);
        enemyWideTicks = decrease(enemyWideTicks, safeDelta);
        slowTicks = decrease(slowTicks, safeDelta);
        splitTicks = decrease(splitTicks, safeDelta);
        powerUpClock += safeDelta;

        updatePowerUp(safeDelta);
        float spawnInterval = mode == MobileGameMode.MUTANT ? 1.8f
                : mode == MobileGameMode.TURBO ? 4.5f : 3.5f;
        if (activePowerUp == null && powerUpClock >= spawnInterval) {
            powerUpClock = 0f;
            spawnNextPowerUp();
        }

        ballEffects.update(safeDelta, ballX, ballY, ballDx, ballDy);
        float speedFactor = slowTicks > 0f ? 0.56f : 1f;
        if (overdriveTicks > 0f) {
            speedFactor *= 1.18f;
        }
        if (enemyOverdriveTicks > 0f) {
            speedFactor *= 1.10f;
        }
        if (mode == MobileGameMode.TURBO) {
            speedFactor *= 1.22f;
        }
        if (mode == MobileGameMode.CAMPAIGN) {
            speedFactor *= 1f + bossIndex * 0.06f;
        }
        ballX += ballDx * speedFactor * safeDelta;
        ballY += ballDy * speedFactor * safeDelta;
        if (ballX < 0.2f || ballX > WIDTH - 0.2f) {
            ballX = Math.max(0.2f, Math.min(WIDTH - 0.2f, ballX));
            ballDx *= -1f;
            ballEffects.burst(ballX, ballY, ballDx, ballDy, 8, 1.0f);
            lastEvent = "WALL HIT";
        }

        if (ballDy < 0f && intersectsPaddle(playerX, playerY, PaddleSide.BOTTOM)) {
            ballY = playerY + PADDLE_HEIGHT + 0.2f;
            bounceFrom(playerX, playerY, false);
            addEnergy(PaddleSide.BOTTOM, 7f);
            ballEffects.burst(ballX, ballY, ballDx, ballDy, 12, 1.25f);
            lastEvent = "PLAYER HIT";
        } else if (ballDy > 0f && intersectsPaddle(enemyX, enemyY, PaddleSide.TOP)) {
            ballY = enemyY - 0.2f;
            bounceFrom(enemyX, enemyY, true);
            addEnergy(PaddleSide.TOP, 7f);
            ballEffects.burst(ballX, ballY, ballDx, ballDy, 12, 1.25f);
            lastEvent = "TOP HIT";
        }

        if (ballY < -0.5f) {
            if (playerShieldTicks > 0f) {
                playerShieldTicks = 0f;
                resetBall(-1f);
                ballEffects.burst(playerX, playerY, ballDx, ballDy, 24, 1.65f);
                lastEvent = "PLAYER SHIELD BREAK";
            } else {
                if (mode == MobileGameMode.SURVIVAL) {
                    survivalLives--;
                }
                enemyScore += enemyPointMultiplier;
                enemyPointMultiplier = 1;
                ballEffects.burst(ballX, 0.1f, ballDx, ballDy, 24, 1.65f);
                resetBall(-1f);
                lastEvent = mode == MobileGameMode.SURVIVAL
                        ? "LIFE LOST " + survivalLives : "ENEMY POINT";
            }
        } else if (ballY > HEIGHT + 0.5f) {
            if (enemyShieldTicks > 0f) {
                enemyShieldTicks = 0f;
                resetBall(1f);
                ballEffects.burst(enemyX, enemyY, ballDx, ballDy, 24, 1.65f);
                lastEvent = "TOP SHIELD BREAK";
            } else {
                playerScore += playerPointMultiplier;
                playerPointMultiplier = 1;
                addEnergy(PaddleSide.BOTTOM, 20f);
                if (mode == MobileGameMode.CAMPAIGN) {
                    bossHealth--;
                    if (bossHealth <= 0) {
                        bossIndex++;
                        bossHealth = 3;
                        lastEvent = bossIndex >= BOSS_NAMES.length
                                ? "CAMPAIGN COMPLETE" : "BOSS DEFEATED " + BOSS_NAMES[bossIndex - 1];
                    } else {
                        lastEvent = "BOSS HIT " + BOSS_NAMES[bossIndex];
                    }
                }
                ballEffects.burst(ballX, HEIGHT - 0.1f, ballDx, ballDy, 24, 1.65f);
                resetBall(1f);
                if (mode != MobileGameMode.CAMPAIGN) {
                    lastEvent = "PLAYER POINT";
                }
            }
        }
        if (isMatchOver()) {
            paused = true;
            lastEvent = "MATCH OVER";
        }
    }

    private float decrease(float value, float delta) {
        return Math.max(0f, value - delta);
    }

    private void updatePowerUp(float delta) {
        if (activePowerUp == null) {
            return;
        }
        activePowerUp.update(delta);
        if (activePowerUp.intersects(playerX, playerY, getPaddleWidth(PaddleSide.BOTTOM), PADDLE_HEIGHT)) {
            collectPowerUp(PaddleSide.BOTTOM);
        } else if (activePowerUp.intersects(enemyX, enemyY, getPaddleWidth(PaddleSide.TOP), PADDLE_HEIGHT)) {
            collectPowerUp(PaddleSide.TOP);
        } else if (!activePowerUp.isActive()) {
            activePowerUp = null;
        }
    }

    private void spawnNextPowerUp() {
        PowerUpType[] values = PowerUpType.values();
        PowerUpType type = values[nextPowerUpOrdinal++ % values.length];
        float spawnX = 2.0f + (nextPowerUpOrdinal * 3.7f) % (WIDTH - 4f);
        activePowerUp = new PowerUp(spawnX, HEIGHT / 2f, type);
        powerUpsSpawned++;
        ballEffects.burst(spawnX, HEIGHT / 2f, 0f, 0f, 10, 0.8f);
        lastEvent = "POWER " + type.getLabel();
    }

    private void collectPowerUp(PaddleSide side) {
        PowerUpType type = activePowerUp.getType();
        boolean bottom = side == PaddleSide.BOTTOM;
        switch (type) {
        case ENERGY:
            addEnergy(side, 42f);
            break;
        case SLOW:
            slowTicks = 4.5f;
            break;
        case SPLIT:
            splitTicks = 4.5f;
            break;
        case MULTI:
            if (bottom) {
                playerPointMultiplier = 2;
            } else {
                enemyPointMultiplier = 2;
            }
            break;
        default:
            break;
        }
        powerUpsCollected++;
        ballEffects.burst(activePowerUp.getX(), activePowerUp.getY(), ballDx, ballDy, 20, 1.4f);
        lastEvent = (bottom ? "PLAYER " : "TOP ") + type.getLabel();
        activePowerUp = null;
    }

    private void addEnergy(PaddleSide side, float amount) {
        if (side == PaddleSide.BOTTOM) {
            playerEnergy = Math.min(100f, playerEnergy + amount);
        } else {
            enemyEnergy = Math.min(100f, enemyEnergy + amount);
        }
    }

    private boolean intersectsPaddle(float paddleX, float paddleY, PaddleSide side) {
        float width = getPaddleWidth(side);
        return ballX >= paddleX - width / 2f
                && ballX <= paddleX + width / 2f
                && Math.abs(ballY - paddleY) <= 0.35f;
    }

    private void bounceFrom(float paddleX, float paddleY, boolean top) {
        float width = top ? getPaddleWidth(PaddleSide.TOP) : getPaddleWidth(PaddleSide.BOTTOM);
        float normalized = (ballX - paddleX) / (width / 2f);
        normalized = Math.max(-1f, Math.min(1f, normalized));
        float speed = Math.min(8.5f, Math.max(5.5f,
                (float) Math.sqrt(ballDx * ballDx + ballDy * ballDy) * 1.015f));
        ballDx = normalized * speed * 0.9f;
        ballDy = (top ? -1f : 1f) * speed;
    }

    private void resetBall(float direction) {
        ballX = WIDTH / 2f;
        ballY = HEIGHT / 2f;
        ballDx = direction * 4.3f;
        ballDy = direction * 3.5f;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        lastEvent = paused ? "PAUSED" : "RESUMED";
    }

    public boolean isPaused() {
        return paused;
    }

    public String getLastEvent() {
        return lastEvent;
    }

    public BallEffects getBallEffects() {
        return ballEffects;
    }

    public boolean isMatchOver() {
        if (mode == MobileGameMode.SURVIVAL) {
            return survivalLives <= 0 || playerScore >= MATCH_SCORE * 2;
        }
        if (mode == MobileGameMode.CAMPAIGN) {
            return bossIndex >= BOSS_NAMES.length || survivalLives <= 0;
        }
        return playerScore >= MATCH_SCORE || enemyScore >= MATCH_SCORE;
    }

    public String getModeLabel() {
        return mode.getLabel();
    }

    public String getModeDescription() {
        return mode.getDescription();
    }

    public int getBossIndex() {
        return bossIndex;
    }

    public int getBossHealth() {
        return bossHealth;
    }

    public String getBossName() {
        return BOSS_NAMES[Math.min(bossIndex, BOSS_NAMES.length - 1)];
    }

    public int getSurvivalLives() {
        return survivalLives;
    }

    public String getProgressLabel() {
        if (mode == MobileGameMode.SURVIVAL) {
            return "LIVES " + survivalLives;
        }
        if (mode == MobileGameMode.CAMPAIGN) {
            return "BOSS " + (bossIndex + 1) + "/" + BOSS_NAMES.length
                    + " HP " + bossHealth;
        }
        return "TARGET " + MATCH_SCORE;
    }

    public long getLeaderboardScore() {
        return playerScore;
    }

    public void setEffectsQuality(BallEffects.Quality quality) {
        ballEffects.setQuality(quality);
    }

    public AbilityType getAbility(PaddleSide side) {
        return side == PaddleSide.BOTTOM ? playerAbility : enemyAbility;
    }

    public String getAbilityLabel(PaddleSide side) {
        return getAbility(side).getLabel();
    }

    public float getEnergy(PaddleSide side) {
        return side == PaddleSide.BOTTOM ? playerEnergy : enemyEnergy;
    }

    public void grantRewardEnergy() {
        grantRewardEnergy(35);
    }

    public void grantRewardEnergy(int amount) {
        addEnergy(PaddleSide.BOTTOM, Math.max(1, amount));
        lastEvent = "REWARDED ENERGY +" + Math.max(1, amount);
        ballEffects.burst(playerX, playerY, ballDx, ballDy, 18, 1.2f);
    }

    public float getPaddleWidth(PaddleSide side) {
        if (side == PaddleSide.BOTTOM) {
            return playerWideTicks > 0f ? BASE_PADDLE_WIDTH * 1.55f : BASE_PADDLE_WIDTH;
        }
        return enemyWideTicks > 0f ? BASE_PADDLE_WIDTH * 1.55f : BASE_PADDLE_WIDTH;
    }

    public boolean isShieldActive(PaddleSide side) {
        return side == PaddleSide.BOTTOM ? playerShieldTicks > 0f : enemyShieldTicks > 0f;
    }

    public boolean isOverdriveActive(PaddleSide side) {
        return side == PaddleSide.BOTTOM ? overdriveTicks > 0f : enemyOverdriveTicks > 0f;
    }

    public boolean isWideActive(PaddleSide side) {
        return side == PaddleSide.BOTTOM ? playerWideTicks > 0f : enemyWideTicks > 0f;
    }

    public float getSlowTicks() {
        return slowTicks;
    }

    public float getSplitTicks() {
        return splitTicks;
    }

    public PowerUp getActivePowerUp() {
        return activePowerUp;
    }

    public void spawnPowerUpAt(float x, float y, PowerUpType type) {
        activePowerUp = new PowerUp(
                Math.max(0.45f, Math.min(WIDTH - 0.45f, x)),
                Math.max(0.45f, Math.min(HEIGHT - 0.45f, y)),
                type);
        powerUpsSpawned++;
        lastEvent = "POWER " + type.getLabel();
    }

    public void render(ShapeRenderer renderer) {
        renderer.setColor(new Color(0.02f, 0.06f, 0.14f, 1f));
        renderer.rect(0f, 0f, WIDTH, HEIGHT);
        renderer.setColor(new Color(0.14f, 0.35f, 0.55f, 1f));
        renderer.line(WIDTH / 2f, 0f, WIDTH / 2f, HEIGHT);
        ballEffects.render(renderer);
        if (activePowerUp != null) {
            activePowerUp.render(renderer);
        }
        renderPaddle(renderer, playerX, playerY, PaddleSide.BOTTOM, new Color(0.2f, 0.85f, 1f, 1f));
        renderPaddle(renderer, enemyX, enemyY, PaddleSide.TOP, new Color(1f, 0.55f, 0.3f, 1f));
        renderer.setColor(Color.WHITE);
        renderer.circle(ballX, ballY, 0.2f, 16);
        if (splitTicks > 0f) {
            renderer.setColor(0.55f, 0.88f, 1f, 0.35f);
            renderer.circle(ballX - ballDx * 0.08f, ballY - ballDy * 0.08f, 0.14f, 12);
        }
    }

    private void renderPaddle(ShapeRenderer renderer, float x, float y,
            PaddleSide side, Color color) {
        float width = getPaddleWidth(side);
        renderer.setColor(color);
        renderer.rect(x - width / 2f, y, width, PADDLE_HEIGHT);
        if (isShieldActive(side)) {
            renderer.setColor(color.r, color.g, color.b, 0.28f);
            renderer.circle(x, y + PADDLE_HEIGHT / 2f, width * 0.72f, 24);
        }
    }
}
