package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/** Gere toutes les particules du jeu (feu, foudre, sang, eau, fumee, poison). */
public class ParticleSystem {

    private static final int MAX_PARTICLES = 800;

    private static class Particle {
        float x, y;
        float vx, vy;
        float life;
        float maxLife;
        float size;
        float sizeGrowth;
        float r, g, b;
        int type;
    }

    private final Array<Particle> particles = new Array<Particle>();

    public void clear() {
        particles.clear();
    }

    // ==================== EXPLOSIONS CLASSIQUES ====================

    public void spawnEnemyExplosion(float x, float y, Color baseColor) {
        for (int i = 0; i < 12; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(80f, 220f);
            addParticle(x, y,
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed,
                    MathUtils.random(0.4f, 0.8f),
                    MathUtils.random(4f, 8f), 0f,
                    baseColor, 0);
        }
    }

    public void spawnBossExplosion(float x, float y, Color baseColor) {
        for (int i = 0; i < 40; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(120f, 380f);
            Color c = (MathUtils.randomBoolean(0.4f))
                    ? new Color(1f, 0.85f, 0.2f, 1f) : baseColor;
            addParticle(x, y,
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed,
                    MathUtils.random(0.6f, 1.2f),
                    MathUtils.random(6f, 12f), 0f,
                    c, 0);
        }
    }

    public void spawnHitSpark(float x, float y, Color baseColor) {
        for (int i = 0; i < 4; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(40f, 120f);
            addParticle(x, y,
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed,
                    MathUtils.random(0.15f, 0.35f),
                    MathUtils.random(3f, 5f), 0f,
                    baseColor, 0);
        }
    }

    // ==================== FEU ====================

    public void spawnFire(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(-30f, 30f) + 90f;
            float speed = MathUtils.random(60f, 140f);
            Color c;
            float r = MathUtils.random(0f, 1f);
            if (r < 0.33f) c = new Color(1f, 0.9f, 0.3f, 1f);
            else if (r < 0.66f) c = new Color(1f, 0.6f, 0.1f, 1f);
            else c = new Color(1f, 0.3f, 0.05f, 1f);

            addParticle(x + MathUtils.random(-8f, 8f),
                    y + MathUtils.random(-4f, 4f),
                    MathUtils.cosDeg(angle) * speed * MathUtils.random(0.3f, 1f),
                    MathUtils.sinDeg(angle) * speed,
                    MathUtils.random(0.5f, 1f),
                    MathUtils.random(5f, 10f), 0f,
                    c, 1);
        }
    }

    // ==================== FOUDRE ====================

    public void spawnLightning(float x, float y, float angleDeg, int count) {
        for (int i = 0; i < count; i++) {
            float a = angleDeg + MathUtils.random(-25f, 25f);
            float speed = MathUtils.random(300f, 600f);
            Color c;
            float r = MathUtils.random(0f, 1f);
            if (r < 0.4f) c = new Color(1f, 1f, 1f, 1f);
            else if (r < 0.7f) c = new Color(0.5f, 0.8f, 1f, 1f);
            else c = new Color(0.2f, 0.4f, 1f, 1f);

            addParticle(x, y,
                    MathUtils.cosDeg(a) * speed,
                    MathUtils.sinDeg(a) * speed,
                    MathUtils.random(0.15f, 0.35f),
                    MathUtils.random(3f, 6f), 0f,
                    c, 2);
        }
    }

    // ==================== SANG ====================

    public void spawnBlood(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(0f, 360f);
            float speed = MathUtils.random(100f, 280f);
            Color c;
            float r = MathUtils.random(0f, 1f);
            if (r < 0.4f) c = new Color(0.6f, 0.05f, 0.05f, 1f);
            else if (r < 0.75f) c = new Color(0.8f, 0.1f, 0.1f, 1f);
            else c = new Color(0.4f, 0.02f, 0.02f, 1f);

            addParticle(x, y,
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed,
                    MathUtils.random(0.4f, 0.8f),
                    MathUtils.random(3f, 7f), 0f,
                    c, 3);
        }
    }

    // ==================== EAU ====================

    public void spawnWater(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(20f, 160f);
            float speed = MathUtils.random(120f, 260f);
            Color c;
            float r = MathUtils.random(0f, 1f);
            if (r < 0.4f) c = new Color(0.4f, 0.8f, 1f, 1f);
            else if (r < 0.7f) c = new Color(0.1f, 0.5f, 0.9f, 1f);
            else c = new Color(0.5f, 0.95f, 1f, 1f);

            addParticle(x + MathUtils.random(-6f, 6f), y,
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed,
                    MathUtils.random(0.5f, 0.9f),
                    MathUtils.random(4f, 8f), 0f,
                    c, 4);
        }
    }

    public void spawnWaterJet(float x, float y, float angleDeg, int count) {
        for (int i = 0; i < count; i++) {
            float a = angleDeg + MathUtils.random(-30f, 30f);
            float speed = MathUtils.random(150f, 300f);
            Color c;
            float r = MathUtils.random(0f, 1f);
            if (r < 0.5f) c = new Color(0.3f, 0.7f, 1f, 1f);
            else c = new Color(0.6f, 0.9f, 1f, 1f);

            addParticle(x, y,
                    MathUtils.cosDeg(a) * speed,
                    MathUtils.sinDeg(a) * speed,
                    MathUtils.random(0.3f, 0.6f),
                    MathUtils.random(3f, 6f), 0f,
                    c, 4);
        }
    }

    // ==================== FUMEE ====================

    public void spawnSmoke(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(-40f, 40f) + 90f;
            float speed = MathUtils.random(20f, 60f);
            float g = MathUtils.random(0.3f, 0.55f);
            Color c = new Color(g, g * 0.95f, g * 0.9f, 0.85f);

            addParticle(x + MathUtils.random(-10f, 10f),
                    y + MathUtils.random(-6f, 6f),
                    MathUtils.cosDeg(angle) * speed,
                    MathUtils.sinDeg(angle) * speed,
                    MathUtils.random(1.0f, 1.8f),
                    MathUtils.random(6f, 12f), 20f,
                    c, 5);
        }
    }

    // ==================== POISON ====================

    public void spawnPoison(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(-50f, 50f) + 90f;
            float speed = MathUtils.random(15f, 50f);
            Color c;
            float r = MathUtils.random(0f, 1f);
            if (r < 0.33f) c = new Color(0.3f, 0.9f, 0.2f, 0.9f);
            else if (r < 0.66f) c = new Color(0.5f, 1f, 0.3f, 0.85f);
            else c = new Color(0.15f, 0.6f, 0.1f, 0.9f);

            addParticle(x + MathUtils.random(-12f, 12f),
                    y + MathUtils.random(-8f, 8f),
                    MathUtils.cosDeg(angle) * speed * MathUtils.random(0.3f, 1f),
                    MathUtils.sinDeg(angle) * speed,
                    MathUtils.random(1.2f, 2.0f),
                    MathUtils.random(5f, 10f),
                    25f,
                    c, 6);
        }
    }

    public void spawnPoisonJet(float x, float y, float angleDeg, int count) {
        for (int i = 0; i < count; i++) {
            float a = angleDeg + MathUtils.random(-20f, 20f);
            float speed = MathUtils.random(120f, 240f);
            Color c;
            float r = MathUtils.random(0f, 1f);
            if (r < 0.5f) c = new Color(0.4f, 0.95f, 0.2f, 0.9f);
            else c = new Color(0.2f, 0.7f, 0.15f, 0.9f);

            addParticle(x, y,
                    MathUtils.cosDeg(a) * speed,
                    MathUtils.sinDeg(a) * speed,
                    MathUtils.random(0.5f, 0.9f),
                    MathUtils.random(4f, 8f), 5f,
                    c, 6);
        }
    }

    // ==================== GESTION ====================

    private void addParticle(float x, float y, float vx, float vy,
                             float life, float size, float sizeGrowth,
                             Color color, int type) {
        if (particles.size >= MAX_PARTICLES) particles.removeIndex(0);
        Particle p = new Particle();
        p.x = x; p.y = y;
        p.vx = vx; p.vy = vy;
        p.life = life; p.maxLife = life;
        p.size = size;
        p.sizeGrowth = sizeGrowth;
        p.r = color.r; p.g = color.g; p.b = color.b;
        p.type = type;
        particles.add(p);
    }

    public void update(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.x += p.vx * delta;
            p.y += p.vy * delta;

            if (p.type == 1) {
                p.vx *= 0.95f;
                p.vy *= 0.96f;
                p.vy += 30f * delta;
            } else if (p.type == 2) {
                p.vx *= 0.85f;
                p.vy *= 0.85f;
            } else if (p.type == 3) {
                p.vx *= 0.94f;
                p.vy *= 0.94f;
                p.vy -= 200f * delta;
            } else if (p.type == 4) {
                p.vx *= 0.96f;
                p.vy *= 0.96f;
                p.vy -= 300f * delta;
            } else if (p.type == 5) {
                p.vx *= 0.98f;
                p.vy *= 0.98f;
                p.vy += 15f * delta;
            } else if (p.type == 6) {
                p.vx *= 0.97f;
                p.vy *= 0.97f;
                p.vy += 8f * delta;
            } else {
                p.vx *= 0.92f;
                p.vy *= 0.92f;
            }

            if (p.sizeGrowth > 0f) {
                p.size += p.sizeGrowth * delta;
            }

            p.life -= delta;
            if (p.life <= 0f) particles.removeIndex(i);
        }
    }

    public void draw(ShapeRenderer shapes) {
        for (int i = 0; i < particles.size; i++) {
            Particle p = particles.get(i);
            float alpha = Math.max(0f, p.life / p.maxLife);
            float size = p.size * (0.4f + 0.6f * alpha);

            if (p.type == 1) {
                size *= 0.85f + 0.15f * (float) Math.sin(p.life * 40f);
            } else if (p.type == 4) {
                size *= 0.9f + 0.1f * (float) Math.sin(p.life * 30f);
            } else if (p.type == 5) {
                alpha *= 0.6f;
            } else if (p.type == 6) {
                size *= 0.9f + 0.15f * (float) Math.sin(p.life * 12f);
                alpha *= 0.85f;
            }

            shapes.setColor(p.r, p.g, p.b, alpha);
            shapes.circle(p.x, p.y, size, 8);
        }
    }

    public int getCount() { return particles.size; }
}