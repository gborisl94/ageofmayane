package com.ageofmayane.game;

import com.badlogic.gdx.math.MathUtils;

class GameAbilities {

    static void activateStunAbility(GameScreen gs) {
        if (gs.stunCooldown > 0f) return;
        gs.stunCooldown = GameScreen.STUN_ABILITY_COOLDOWN;
        for (int i = 0; i < gs.ennemis.size; i++) {
            Ennemi e = gs.ennemis.get(i);
            if (e.pos.dst2(gs.playerPos) < GameScreen.STUN_ABILITY_RADIUS * GameScreen.STUN_ABILITY_RADIUS) {
                e.stunned = GameScreen.STUN_ABILITY_DURATION;
                e.stunBase = GameScreen.STUN_ABILITY_DURATION;
                gs.particles.spawnLightning(e.pos.x, e.pos.y, 0f, 3);
                gs.particles.spawnSmoke(e.pos.x, e.pos.y, 2);
            }
        }
        for (int i = 0; i < 24; i++) {
            float ang = i * (360f / 24f);
            float x = gs.playerPos.x + MathUtils.cosDeg(ang) * GameScreen.STUN_ABILITY_RADIUS;
            float y = gs.playerPos.y + MathUtils.sinDeg(ang) * GameScreen.STUN_ABILITY_RADIUS;
            gs.particles.spawnLightning(x, y, ang, 1);
        }
        gs.triggerShake();
    }

    static void activateGlitchDash(GameScreen gs) {
        if (gs.glitchCooldown > 0f) return;
        gs.glitchCooldown = GameScreen.GLITCH_COOLDOWN;
        float rad = gs.facing * MathUtils.degRad;
        float dx = MathUtils.cos(rad) * GameScreen.GLITCH_DISTANCE;
        float dy = MathUtils.sin(rad) * GameScreen.GLITCH_DISTANCE;
        gs.particles.spawnLightning(gs.playerPos.x, gs.playerPos.y, gs.facing, 10);
        gs.playerPos.x = MathUtils.clamp(gs.playerPos.x + dx, GameScreen.PLAYER_RADIUS, GameScreen.W - GameScreen.PLAYER_RADIUS);
        gs.playerPos.y = MathUtils.clamp(gs.playerPos.y + dy, GameScreen.PLAYER_RADIUS, GameScreen.H - GameScreen.PLAYER_RADIUS);
        gs.dungeon.collide(gs.playerPos, GameScreen.PLAYER_RADIUS);
        gs.invulnerable = GameScreen.GLITCH_INVULN;
        gs.triggerShake();
        gs.glitchEffectTimer = 0.4f;
        gs.particles.spawnLightning(gs.playerPos.x, gs.playerPos.y, gs.facing + 180f, 10);
    }

    static void fireCrescent(GameScreen gs) {
        if (gs.luneCooldown > 0f) return;
        Ennemi target = GameCombat.findNearestEnemy(gs);
        if (target == null) return;
        gs.luneCooldown = GameScreen.LUNE_COOLDOWN;
        gs.tmp.set(target.pos).sub(gs.playerPos);
        float angle = gs.tmp.angleDeg();
        Fleche f = new Fleche();
        f.pos.set(gs.playerPos);
        f.vel.set(MathUtils.cosDeg(angle) * GameScreen.LUNE_SPEED,
                  MathUtils.sinDeg(angle) * GameScreen.LUNE_SPEED);
        f.angle = angle;
        f.fromEnemy = false;
        f.damage = 3;
        f.croissant = true;
        f.generation = 0;
        f.life = 3f;
        gs.fleches.add(f);
        gs.particles.spawnWaterJet(gs.playerPos.x, gs.playerPos.y, angle, 4);
    }

    static void splitCrescent(GameScreen gs, Fleche parent) {
        int nbFill = Math.min(2 + gs.luneLevel, 6);
        float baseAngle = parent.angle;
        for (int i = 0; i < nbFill; i++) {
            float spread = -30f + (60f / Math.max(1, nbFill - 1)) * i;
            float a = baseAngle + spread;
            Fleche f = new Fleche();
            f.pos.set(parent.pos);
            f.vel.set(MathUtils.cosDeg(a) * GameScreen.LUNE_SPEED * 0.85f,
                      MathUtils.sinDeg(a) * GameScreen.LUNE_SPEED * 0.85f);
            f.angle = a;
            f.fromEnemy = false;
            f.damage = Math.max(1, parent.damage - 1);
            f.croissant = true;
            f.generation = parent.generation + 1;
            f.life = 1.5f;
            gs.fleches.add(f);
        }
        gs.particles.spawnWater(parent.pos.x, parent.pos.y, 8);
    }

    static void activateAutoAimPower(GameScreen gs) {
        if (gs.autoAimCooldown > 0f) return;
        if (gs.autoAimActive) return;
        gs.autoAimActive = true;
        gs.autoAimTimer = GameScreen.AUTO_AIM_DURATION;
        gs.aimStability = 0f;
        gs.particles.spawnLightning(gs.playerPos.x, gs.playerPos.y, 0f, 6);
    }

    static void deactivateAutoAimPower(GameScreen gs) {
        if (!gs.autoAimActive) return;
        gs.autoAimActive = false;
        gs.autoAimCooldown = GameScreen.AUTO_AIM_COOLDOWN * 0.5f;
        gs.autoAimTimer = 0f;
    }

    static boolean isAutoAimReady(GameScreen gs) {
        return !gs.autoAimActive && gs.autoAimCooldown <= 0f;
    }

    static float getAutoAimDurationRatio(GameScreen gs) {
        if (GameScreen.AUTO_AIM_DURATION <= 0f) return 0f;
        return Math.max(0f, Math.min(1f, gs.autoAimTimer / GameScreen.AUTO_AIM_DURATION));
    }

    static float getAutoAimCooldownRatio(GameScreen gs) {
        if (GameScreen.AUTO_AIM_COOLDOWN <= 0f) return 0f;
        return Math.max(0f, Math.min(1f, gs.autoAimCooldown / GameScreen.AUTO_AIM_COOLDOWN));
    }
}