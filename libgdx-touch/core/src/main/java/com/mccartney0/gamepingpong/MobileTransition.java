package com.mccartney0.gamepingpong;

public final class MobileTransition {
    public enum Type {
        START_MATCH,
        RETURN_TO_MENU,
        SHOW_RESULTS
    }

    private Type type;
    private float duration;
    private float elapsed;
    private boolean active;

    public void begin(Type type, float durationSeconds) {
        this.type = type == null ? Type.START_MATCH : type;
        this.duration = Math.max(0.01f, durationSeconds);
        this.elapsed = 0f;
        this.active = true;
    }

    public void update(float deltaSeconds) {
        if (!active) {
            return;
        }
        elapsed += Math.max(0f, deltaSeconds);
        if (elapsed >= duration) {
            elapsed = duration;
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }

    public float getProgress() {
        if (duration <= 0f) {
            return 1f;
        }
        return Math.min(1f, elapsed / duration);
    }

    public Type getType() {
        return type;
    }

    public String getCountdownText() {
        if (!active) {
            return "GO!";
        }
        int count = Math.max(1, (int) Math.ceil((duration - elapsed) / 0.4f));
        return Integer.toString(count);
    }
}
