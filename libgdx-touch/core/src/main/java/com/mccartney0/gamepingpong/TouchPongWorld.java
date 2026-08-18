package com.mccartney0.gamepingpong;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mccartney0.gamepingpong.input.PaddleSide;
import com.mccartney0.gamepingpong.visual.BallEffects;
import com.mccartney0.gamepingpong.input.PaddleTouchTarget;

public class TouchPongWorld implements PaddleTouchTarget {

    public static final float WIDTH = 16f;
    public static final float HEIGHT = 12f;

    public float playerX = WIDTH / 2f;
    public float enemyX = WIDTH / 2f;
    public final float playerY = 0.7f;
    public final float enemyY = HEIGHT - 1.0f;
    public final float paddleWidth = 3.4f;
    public final float paddleHeight = 0.5f;
    public float ballX = WIDTH / 2f;
    public float ballY = HEIGHT / 2f;
    public float ballDx = 5.7f;
    public float ballDy = 4.1f;
    public int playerScore;
    public int enemyScore;
    public int abilityActivations;
    public int pauseToggles;
    public int playerDragEvents;
    public int enemyDragEvents;

    private boolean paused;
    private float abilityTicks;
    private String lastEvent = "READY";
    private final BallEffects ballEffects = new BallEffects();

    @Override
    public void movePaddleTo(PaddleSide side, float worldX) {
        float clamped = Math.max(paddleWidth / 2f,
                Math.min(WIDTH - paddleWidth / 2f, worldX));
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
        abilityActivations++;
        abilityTicks = 0.75f;
        if (side == PaddleSide.BOTTOM) {
            ballDx *= 1.12f;
            lastEvent = "PLAYER OVERDRIVE";
        } else {
            ballDx *= 0.90f;
            lastEvent = "TOP SHIELD";
        }
        ballEffects.burst(ballX, ballY, ballDx, ballDy, 16, 1.5f);
    }

    @Override
    public void cycleAbility(PaddleSide side) {
        lastEvent = side == PaddleSide.BOTTOM ? "PLAYER ABILITY CHANGED" : "TOP ABILITY CHANGED";
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
        if (abilityTicks > 0f) {
            abilityTicks -= safeDelta;
        }

        ballEffects.update(safeDelta, ballX, ballY, ballDx, ballDy);
        ballX += ballDx * safeDelta;
        ballY += ballDy * safeDelta;
        if (ballX < 0.2f || ballX > WIDTH - 0.2f) {
            ballX = Math.max(0.2f, Math.min(WIDTH - 0.2f, ballX));
            ballDx *= -1f;
            ballEffects.burst(ballX, ballY, ballDx, ballDy, 8, 1.0f);
            lastEvent = "WALL HIT";
        }

        if (ballDy < 0f && intersectsPaddle(playerX, playerY)) {
            ballY = playerY + paddleHeight + 0.2f;
            bounceFrom(playerX, playerY, false);
            ballEffects.burst(ballX, ballY, ballDx, ballDy, 12, 1.25f);
            lastEvent = "PLAYER HIT";
        } else if (ballDy > 0f && intersectsPaddle(enemyX, enemyY)) {
            ballY = enemyY - 0.2f;
            bounceFrom(enemyX, enemyY, true);
            ballEffects.burst(ballX, ballY, ballDx, ballDy, 12, 1.25f);
            lastEvent = "TOP HIT";
        }

        if (ballY < -0.5f) {
            enemyScore++;
            ballEffects.burst(ballX, 0.1f, ballDx, ballDy, 24, 1.65f);
            resetBall(-1f);
            lastEvent = "ENEMY POINT";
        } else if (ballY > HEIGHT + 0.5f) {
            playerScore++;
            ballEffects.burst(ballX, HEIGHT - 0.1f, ballDx, ballDy, 24, 1.65f);
            resetBall(1f);
            lastEvent = "PLAYER POINT";
        }
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

    public void setEffectsQuality(BallEffects.Quality quality) {
        ballEffects.setQuality(quality);
    }

    public void render(ShapeRenderer renderer) {
        renderer.setColor(new Color(0.02f, 0.06f, 0.14f, 1f));
        renderer.rect(0f, 0f, WIDTH, HEIGHT);
        renderer.setColor(new Color(0.14f, 0.35f, 0.55f, 1f));
        renderer.line(WIDTH / 2f, 0f, WIDTH / 2f, HEIGHT);
        ballEffects.render(renderer);
        renderer.setColor(new Color(0.2f, 0.85f, 1f, 1f));
        renderer.rect(playerX - paddleWidth / 2f, playerY, paddleWidth, paddleHeight);
        renderer.setColor(new Color(1f, 0.55f, 0.3f, 1f));
        renderer.rect(enemyX - paddleWidth / 2f, enemyY, paddleWidth, paddleHeight);
        renderer.setColor(Color.WHITE);
        renderer.circle(ballX, ballY, 0.2f, 16);
    }

    private boolean intersectsPaddle(float paddleX, float paddleY) {
        return ballX >= paddleX - paddleWidth / 2f
                && ballX <= paddleX + paddleWidth / 2f
                && Math.abs(ballY - paddleY) <= 0.35f;
    }

    private void bounceFrom(float paddleX, float paddleY, boolean top) {
        float normalized = (ballX - paddleX) / (paddleWidth / 2f);
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
}
