package com.mccartney0.gamepingpong.visual;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class BallEffects {

    public enum Quality {
        LOW(18, 36, 0.018f),
        MEDIUM(32, 72, 0.014f),
        HIGH(48, 120, 0.010f);

        private final int trailCapacity;
        private final int particleCapacity;
        private final float trailInterval;

        Quality(int trailCapacity, int particleCapacity, float trailInterval) {
            this.trailCapacity = trailCapacity;
            this.particleCapacity = particleCapacity;
            this.trailInterval = trailInterval;
        }
    }

    private static final class TrailPoint {
        float x;
        float y;
        float age;
        float life;
        float size;
        boolean active;
    }

    private static final class Particle {
        float x;
        float y;
        float dx;
        float dy;
        float age;
        float life;
        float size;
        boolean active;
    }

    private final TrailPoint[] trail = new TrailPoint[48];
    private final Particle[] particles = new Particle[120];
    private Quality quality = Quality.MEDIUM;
    private float trailAccumulator;
    private int trailCursor;
    private int particleCursor;
    private Color primary = new Color(0.78f, 0.95f, 1f, 1f);
    private Color secondary = new Color(0.15f, 0.75f, 1f, 1f);
    private boolean enabled = true;
    private int emittedParticles;

    public BallEffects() {
        for (int i = 0; i < trail.length; i++) {
            trail[i] = new TrailPoint();
        }
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle();
        }
    }

    public void setQuality(Quality quality) {
        this.quality = quality == null ? Quality.MEDIUM : quality;
        clearInactiveBeyondCapacity();
    }

    public Quality getQuality() {
        return quality;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            clear();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setColors(Color primary, Color secondary) {
        if (primary != null) {
            this.primary = primary;
        }
        if (secondary != null) {
            this.secondary = secondary;
        }
    }

    public void update(float delta, float x, float y, float dx, float dy) {
        if (!enabled) {
            return;
        }
        float safeDelta = Math.min(0.05f, Math.max(0f, delta));
        trailAccumulator += safeDelta;
        while (trailAccumulator >= quality.trailInterval) {
            trailAccumulator -= quality.trailInterval;
            addTrailPoint(x, y, dx, dy);
        }
        for (TrailPoint point : trail) {
            if (point.active) {
                point.age += safeDelta;
                if (point.age >= point.life) {
                    point.active = false;
                }
            }
        }
        int activeParticleLimit = quality.particleCapacity;
        for (int i = 0; i < particles.length; i++) {
            Particle particle = particles[i];
            if (i >= activeParticleLimit) {
                particle.active = false;
            } else if (particle.active) {
                particle.age += safeDelta;
                particle.x += particle.dx * safeDelta;
                particle.y += particle.dy * safeDelta;
                particle.dx *= 0.96f;
                particle.dy *= 0.96f;
                if (particle.age >= particle.life) {
                    particle.active = false;
                }
            }
        }
    }

    public void burst(float x, float y, float dx, float dy, int count, float intensity) {
        if (!enabled) {
            return;
        }
        int amount = Math.min(count, quality.particleCapacity);
        emittedParticles += amount;
        for (int i = 0; i < amount; i++) {
            Particle particle = particles[particleCursor++ % quality.particleCapacity];
            double angle = ((i * 2.3999632) + particleCursor * 0.17) % (Math.PI * 2.0);
            float speed = intensity * (0.45f + (i % 5) * 0.12f);
            particle.x = x;
            particle.y = y;
            particle.dx = (float) Math.cos(angle) * speed - dx * 0.10f;
            particle.dy = (float) Math.sin(angle) * speed - dy * 0.10f;
            particle.age = 0f;
            particle.life = 0.18f + (i % 4) * 0.045f;
            particle.size = 0.035f + (i % 3) * 0.018f;
            particle.active = true;
        }
    }

    public void render(ShapeRenderer renderer) {
        if (!enabled) {
            return;
        }
        for (TrailPoint point : trail) {
            if (!point.active) {
                continue;
            }
            float alpha = 1f - point.age / point.life;
            renderer.setColor(secondary.r, secondary.g, secondary.b, alpha * 0.42f);
            renderer.rect(point.x - point.size / 2f, point.y - point.size / 2f,
                    point.size, point.size);
        }
        for (Particle particle : particles) {
            if (!particle.active) {
                continue;
            }
            float alpha = 1f - particle.age / particle.life;
            renderer.setColor(primary.r, primary.g, primary.b, alpha * 0.90f);
            renderer.circle(particle.x, particle.y, particle.size, 6);
        }
    }

    public int getEmittedParticles() {
        return emittedParticles;
    }

    public int getActiveTrailCount() {
        int count = 0;
        for (TrailPoint point : trail) {
            if (point.active) {
                count++;
            }
        }
        return count;
    }

    public int getActiveParticleCount() {
        int count = 0;
        for (Particle particle : particles) {
            if (particle.active) {
                count++;
            }
        }
        return count;
    }

    public void clear() {
        trailAccumulator = 0f;
        emittedParticles = 0;
        for (TrailPoint point : trail) {
            point.active = false;
        }
        for (Particle particle : particles) {
            particle.active = false;
        }
    }

    private void addTrailPoint(float x, float y, float dx, float dy) {
        TrailPoint point = trail[trailCursor++ % quality.trailCapacity];
        point.x = x - dx * 0.005f;
        point.y = y - dy * 0.005f;
        point.age = 0f;
        point.life = 0.18f;
        point.size = 0.10f + Math.min(0.10f, (Math.abs(dx) + Math.abs(dy)) * 0.004f);
        point.active = true;
    }

    private void clearInactiveBeyondCapacity() {
        for (int i = quality.trailCapacity; i < trail.length; i++) {
            trail[i].active = false;
        }
        for (int i = quality.particleCapacity; i < particles.length; i++) {
            particles[i].active = false;
        }
    }
}
