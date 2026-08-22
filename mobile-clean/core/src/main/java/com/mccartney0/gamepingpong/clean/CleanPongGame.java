package com.mccartney0.gamepingpong.clean;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Mobile baseline deliberately independent from the historical libGDX module.
 * It uses only immediate procedural shapes so that the first Android APK has no
 * texture, shader, audio, ad or external service lifecycle to invalidate.
 */
public final class CleanPongGame extends ApplicationAdapter {

    public static final float WORLD_WIDTH = 16f;
    public static final float WORLD_HEIGHT = 9f;
    private static final float PADDLE_WIDTH = 2.2f;
    private static final float PADDLE_HEIGHT = 0.22f;
    private static final float BALL_RADIUS = 0.13f;
    private static final int TARGET_SCORE = 7;

    private enum Screen {
        MENU, PLAYING, PAUSED, GAME_OVER
    }

    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
    private final Vector3 touchWorld = new Vector3();
    private ShapeRenderer shapes;
    private Screen screen = Screen.MENU;
    private float playerX = WORLD_WIDTH / 2f;
    private float enemyX = WORLD_WIDTH / 2f;
    private float ballX;
    private float ballY;
    private float ballDx;
    private float ballDy;
    private int playerScore;
    private int enemyScore;
    private boolean previousTouch;
    private boolean previousBack;
    private float elapsed;

    @Override
    public void create() {
        shapes = new ShapeRenderer();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();
        resetMatch();
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);
        elapsed += delta;
        handleInput();
        if (screen == Screen.PLAYING) {
            updateGame(delta);
        }

        Gdx.gl.glClearColor(0.015f, 0.025f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawArena();
        if (screen == Screen.MENU) {
            drawMenu();
        } else {
            drawMatch();
            if (screen == Screen.PAUSED) {
                drawPauseOverlay();
            } else if (screen == Screen.GAME_OVER) {
                drawGameOver();
            }
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        previousTouch = Gdx.input.isTouched(0);
        previousBack = Gdx.input.isKeyPressed(Input.Keys.BACK);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();
    }

    @Override
    public void pause() {
        if (screen == Screen.PLAYING) {
            screen = Screen.PAUSED;
        }
    }

    @Override
    public void resume() {
        // Deliberately remain paused after an Android activity interruption.
        // The player must explicitly tap to continue.
    }

    @Override
    public void dispose() {
        if (shapes != null) {
            shapes.dispose();
            shapes = null;
        }
    }

    public String getScreenName() {
        return screen.name();
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getEnemyScore() {
        return enemyScore;
    }

    private void handleInput() {
        boolean touched = Gdx.input.isTouched(0);
        boolean tapped = touched && !previousTouch;
        boolean back = Gdx.input.isKeyPressed(Input.Keys.BACK)
                || Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
        boolean backPressed = back && !previousBack;

        if (backPressed) {
            if (screen == Screen.PLAYING) {
                screen = Screen.PAUSED;
            } else if (screen == Screen.PAUSED) {
                screen = Screen.PLAYING;
            } else if (screen == Screen.GAME_OVER) {
                screen = Screen.MENU;
            }
        }

        if (screen == Screen.MENU) {
            if (tapped) {
                resetMatch();
                screen = Screen.PLAYING;
            }
            return;
        }
        if (screen == Screen.PAUSED) {
            if (tapped) {
                screen = Screen.PLAYING;
            }
            return;
        }
        if (screen == Screen.GAME_OVER) {
            if (tapped) {
                screen = Screen.MENU;
            }
            return;
        }

        if (touched) {
            touchWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
            viewport.unproject(touchWorld);
            if (tapped && touchWorld.y > WORLD_HEIGHT - 1.0f
                    && touchWorld.x > WORLD_WIDTH - 2.2f) {
                screen = Screen.PAUSED;
            } else {
                playerX = MathUtils.clamp(touchWorld.x,
                        PADDLE_WIDTH / 2f, WORLD_WIDTH - PADDLE_WIDTH / 2f);
            }
        }
    }

    private void updateGame(float delta) {
        float enemyTarget = ballX;
        float enemyStep = 4.0f * delta;
        enemyX = moveTowards(enemyX, enemyTarget, enemyStep);

        ballX += ballDx * delta;
        ballY += ballDy * delta;
        if (ballX < BALL_RADIUS || ballX > WORLD_WIDTH - BALL_RADIUS) {
            ballX = MathUtils.clamp(ballX, BALL_RADIUS, WORLD_WIDTH - BALL_RADIUS);
            ballDx = -ballDx;
        }

        float playerY = 0.55f;
        if (ballDy < 0f && ballY - BALL_RADIUS <= playerY + PADDLE_HEIGHT
                && ballY + BALL_RADIUS >= playerY
                && Math.abs(ballX - playerX) <= PADDLE_WIDTH / 2f) {
            ballY = playerY + PADDLE_HEIGHT + BALL_RADIUS;
            ballDy = Math.abs(ballDy);
            ballDx += (ballX - playerX) * 1.15f;
            clampBallSpeed();
        }

        float enemyY = WORLD_HEIGHT - 0.77f;
        if (ballDy > 0f && ballY + BALL_RADIUS >= enemyY
                && ballY - BALL_RADIUS <= enemyY + PADDLE_HEIGHT
                && Math.abs(ballX - enemyX) <= PADDLE_WIDTH / 2f) {
            ballY = enemyY - BALL_RADIUS;
            ballDy = -Math.abs(ballDy);
            ballDx += (ballX - enemyX) * 1.0f;
            clampBallSpeed();
        }

        if (ballY < -BALL_RADIUS) {
            enemyScore++;
            resetBall(1f);
        } else if (ballY > WORLD_HEIGHT + BALL_RADIUS) {
            playerScore++;
            resetBall(-1f);
        }
        if (playerScore >= TARGET_SCORE || enemyScore >= TARGET_SCORE) {
            screen = Screen.GAME_OVER;
        }
    }

    private void resetMatch() {
        playerScore = 0;
        enemyScore = 0;
        playerX = WORLD_WIDTH / 2f;
        enemyX = WORLD_WIDTH / 2f;
        resetBall(1f);
    }

    private void resetBall(float direction) {
        ballX = WORLD_WIDTH / 2f;
        ballY = WORLD_HEIGHT / 2f;
        ballDx = 3.7f * (direction > 0f ? 1f : -1f);
        ballDy = 3.0f * direction;
    }

    private void clampBallSpeed() {
        ballDx = MathUtils.clamp(ballDx, -6.8f, 6.8f);
        ballDy = MathUtils.clamp(ballDy, 2.6f, 6.0f) * Math.signum(ballDy);
    }

    private float moveTowards(float current, float target, float amount) {
        if (Math.abs(target - current) <= amount) {
            return target;
        }
        return current + Math.signum(target - current) * amount;
    }

    private void drawArena() {
        shapes.setColor(new Color(0.015f, 0.045f, 0.12f, 1f));
        shapes.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        shapes.setColor(new Color(0.05f, 0.17f, 0.30f, 1f));
        shapes.rect(0f, 0f, WORLD_WIDTH, 0.06f);
        shapes.rect(0f, WORLD_HEIGHT - 0.06f, WORLD_WIDTH, 0.06f);
        shapes.setColor(new Color(0.10f, 0.32f, 0.48f, 0.55f));
        for (float y = 0.35f; y < WORLD_HEIGHT; y += 0.62f) {
            shapes.rect(WORLD_WIDTH / 2f - 0.025f, y, 0.05f, 0.30f);
        }
        shapes.setColor(new Color(0.03f, 0.12f, 0.22f, 0.75f));
        shapes.rect(0.35f, 0.30f, WORLD_WIDTH - 0.70f, 0.02f);
        shapes.rect(0.35f, WORLD_HEIGHT - 0.32f, WORLD_WIDTH - 0.70f, 0.02f);
    }

    private void drawMatch() {
        float pulse = 0.72f + 0.20f * MathUtils.sin(elapsed * 5f);
        shapes.setColor(0.20f, 0.86f, 1f, 1f);
        shapes.rect(playerX - PADDLE_WIDTH / 2f, 0.55f,
                PADDLE_WIDTH, PADDLE_HEIGHT);
        shapes.setColor(1f, 0.55f, 0.24f, 1f);
        shapes.rect(enemyX - PADDLE_WIDTH / 2f, WORLD_HEIGHT - 0.77f,
                PADDLE_WIDTH, PADDLE_HEIGHT);
        shapes.setColor(0.65f, 0.95f, 1f, pulse);
        shapes.circle(ballX, ballY, BALL_RADIUS, 16);
        shapes.setColor(1f, 0.90f, 0.35f, 0.28f);
        shapes.circle(ballX, ballY, BALL_RADIUS * 2.0f, 16);
        drawScore();
        drawText("PAUSA", WORLD_WIDTH - 1.82f, WORLD_HEIGHT - 0.18f,
                0.18f, new Color(0.50f, 0.84f, 1f, 0.9f));
    }

    private void drawScore() {
        drawText(Integer.toString(playerScore), WORLD_WIDTH / 2f - 0.55f,
                WORLD_HEIGHT - 0.50f, 0.34f, new Color(0.60f, 0.94f, 1f, 1f));
        drawText(":", WORLD_WIDTH / 2f - 0.05f,
                WORLD_HEIGHT - 0.50f, 0.25f, Color.WHITE);
        drawText(Integer.toString(enemyScore), WORLD_WIDTH / 2f + 0.38f,
                WORLD_HEIGHT - 0.50f, 0.34f, new Color(1f, 0.68f, 0.35f, 1f));
    }

    private void drawMenu() {
        shapes.setColor(new Color(0.03f, 0.15f, 0.25f, 0.92f));
        shapes.rect(2.05f, 2.20f, 11.90f, 4.60f);
        shapes.setColor(new Color(0.15f, 0.68f, 0.88f, 0.9f));
        shapes.rect(2.05f, 2.20f, 0.07f, 4.60f);
        shapes.rect(13.88f, 2.20f, 0.07f, 4.60f);
        drawText("PONG", 5.95f, 6.0f, 0.48f, new Color(0.48f, 0.93f, 1f, 1f));
        drawText("TOQUE PARA JOGAR", 4.10f, 4.85f, 0.22f,
                new Color(0.82f, 0.96f, 1f, 1f));
        float pulse = 0.68f + 0.25f * MathUtils.sin(elapsed * 3.0f);
        shapes.setColor(1f, 0.60f, 0.22f, pulse);
        shapes.rect(5.0f, 3.15f, 6.0f, 0.12f);
        shapes.setColor(0.22f, 0.78f, 1f, pulse);
        shapes.rect(5.0f, 3.15f, 6.0f, 0.03f);
    }

    private void drawPauseOverlay() {
        shapes.setColor(0.01f, 0.02f, 0.06f, 0.82f);
        shapes.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        drawText("PAUSADO", 5.15f, 5.2f, 0.40f,
                new Color(0.55f, 0.92f, 1f, 1f));
        drawText("TOQUE PARA VOLTAR", 4.05f, 3.75f, 0.20f, Color.WHITE);
    }

    private void drawGameOver() {
        shapes.setColor(0.01f, 0.02f, 0.06f, 0.86f);
        shapes.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        drawText("FIM", 6.75f, 5.5f, 0.48f,
                new Color(1f, 0.66f, 0.30f, 1f));
        drawScore();
        drawText("TOQUE PARA VOLTAR", 4.05f, 3.50f, 0.20f, Color.WHITE);
    }

    private void drawText(String value, float x, float y, float scale, Color color) {
        String[] lines = new String[value.length()];
        float cursor = x;
        shapes.setColor(color);
        for (int i = 0; i < value.length(); i++) {
            String[] glyph = glyph(value.charAt(i));
            for (int row = 0; row < glyph.length; row++) {
                for (int column = 0; column < glyph[row].length(); column++) {
                    if (glyph[row].charAt(column) == '1') {
                        shapes.rect(cursor + column * scale * 0.26f,
                                y - row * scale * 0.26f,
                                scale * 0.22f, scale * 0.22f);
                    }
                }
            }
            cursor += scale * 1.55f;
        }
    }

    private String[] glyph(char raw) {
        char c = Character.toUpperCase(raw);
        switch (c) {
            case 'A': return new String[]{"01110", "10001", "10001", "11111", "10001", "10001", "10001"};
            case 'D': return new String[]{"11110", "10001", "10001", "10001", "10001", "10001", "11110"};
            case 'E': return new String[]{"11111", "10000", "10000", "11110", "10000", "10000", "11111"};
            case 'F': return new String[]{"11111", "10000", "10000", "11110", "10000", "10000", "10000"};
            case 'G': return new String[]{"01110", "10001", "10000", "10111", "10001", "10001", "01110"};
            case 'I': return new String[]{"11111", "00100", "00100", "00100", "00100", "00100", "11111"};
            case 'J': return new String[]{"00111", "00010", "00010", "00010", "10010", "10010", "01100"};
            case 'M': return new String[]{"10001", "11011", "10101", "10101", "10001", "10001", "10001"};
            case 'N': return new String[]{"10001", "11001", "11001", "10101", "10011", "10011", "10001"};
            case 'O': return new String[]{"01110", "10001", "10001", "10001", "10001", "10001", "01110"};
            case 'P': return new String[]{"11110", "10001", "10001", "11110", "10000", "10000", "10000"};
            case 'Q': return new String[]{"01110", "10001", "10001", "10001", "10101", "10010", "01101"};
            case 'R': return new String[]{"11110", "10001", "10001", "11110", "10100", "10010", "10001"};
            case 'S': return new String[]{"01111", "10000", "10000", "01110", "00001", "00001", "11110"};
            case 'T': return new String[]{"11111", "00100", "00100", "00100", "00100", "00100", "00100"};
            case 'U': return new String[]{"10001", "10001", "10001", "10001", "10001", "10001", "01110"};
            case 'V': return new String[]{"10001", "10001", "10001", "10001", "10001", "01010", "00100"};
            case '0': return new String[]{"01110", "10001", "10011", "10101", "11001", "10001", "01110"};
            case '1': return new String[]{"00100", "01100", "00100", "00100", "00100", "00100", "01110"};
            case '2': return new String[]{"01110", "10001", "00001", "00010", "00100", "01000", "11111"};
            case '3': return new String[]{"11110", "00001", "00001", "01110", "00001", "00001", "11110"};
            case '4': return new String[]{"00010", "00110", "01010", "10010", "11111", "00010", "00010"};
            case '5': return new String[]{"11111", "10000", "10000", "11110", "00001", "00001", "11110"};
            case '6': return new String[]{"01110", "10000", "10000", "11110", "10001", "10001", "01110"};
            case '7': return new String[]{"11111", "00001", "00010", "00100", "01000", "01000", "01000"};
            case '8': return new String[]{"01110", "10001", "10001", "01110", "10001", "10001", "01110"};
            case '9': return new String[]{"01110", "10001", "10001", "01111", "00001", "00001", "01110"};
            case ':': return new String[]{"00000", "00100", "00100", "00000", "00100", "00100", "00000"};
            default: return new String[]{"00000", "00000", "00000", "00000", "00000", "00000", "00000"};
        }
    }
}
