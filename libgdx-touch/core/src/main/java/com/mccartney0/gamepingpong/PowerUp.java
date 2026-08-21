package com.mccartney0.gamepingpong;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class PowerUp {
    private static final float LIFETIME_SECONDS = 8f;
    private static final float DRIFT_SPEED = 0.38f;

    private final PowerUpType type;
    private float x;
    private float y;
    private float age;
    private boolean active = true;

    public PowerUp(float x, float y, PowerUpType type) {
        if (type == null) {
            throw new IllegalArgumentException("type obrigatorio");
        }
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update(float delta) {
        if (!active) {
            return;
        }
        float safeDelta = Math.min(0.05f, Math.max(0f, delta));
        age += safeDelta;
        y -= DRIFT_SPEED * safeDelta;
        if (age >= LIFETIME_SECONDS || y < 0.35f) {
            active = false;
        }
    }

    public boolean intersects(float paddleX, float paddleY, float paddleWidth, float paddleHeight) {
        return active
                && Math.abs(x - paddleX) <= paddleWidth / 2f + 0.28f
                && Math.abs(y - paddleY) <= paddleHeight / 2f + 0.28f;
    }

    public void render(ShapeRenderer renderer) {
        if (!active) {
            return;
        }
        renderer.setColor(type.getRed(), type.getGreen(), type.getBlue(), 0.95f);
        renderer.circle(x, y, 0.30f, 12);
        renderer.setColor(1f, 1f, 1f, 0.75f);
        renderer.circle(x, y, 0.12f, 8);
    }

    public PowerUpType getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAge() {
        return age;
    }

    public boolean isActive() {
        return active;
    }
}
