package com.ageofmayane.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

class GameCombat {

    static Ennemi findNearestEnemy(GameScreen gs) {
        Ennemi nearest = null;
        float bestDist = GameScreen.AUTO_AIM_RANGE_SQ;
        for (int i = 0; i < gs.ennemis.size; i++) {
            Ennemi e = gs.ennemis.get(i);
            if (e.hp <= 0) continue;
            float d = e.pos.dst2(gs.playerPos);
            if (d < bestDist) { bestDist = d; nearest = e; }
        }
        return nearest;
    }

    static void fireAt(GameScreen gs, float angleDeg, int dmg) {
        if (gs.nbFleches <= 0) return;
        gs.nbFleches--;

        Fleche f = new Fleche();
        f.pos.set(gs.playerPos);
        f.vel.set(MathUtils.cosDeg(angleDeg) * GameScreen.ARROW_SPEED,
                  MathUtils.sinDeg(angleDeg) * GameScreen.ARROW_SPEED);
        f.angle = angleDeg;
        f.fromEnemy = false;
        f.damage = dmg;
        gs.fleches.add(f);
        if (gs.equippedWeapon != null) {
            String s = gs.equippedWeapon.spriteName;
            if (s != null) {
                if (s.equals("bow_fire.png")) gs.particles.spawnFire(gs.playerPos.x, gs.playerPos.y, 3);
                else if (s.equals("bow_ice.png")) gs.particles.spawnWaterJet(gs.playerPos.x, gs.playerPos.y, angleDeg, 3);
                else if (s.equals("bow_void.png")) gs.particles.spawnPoisonJet(gs.playerPos.x, gs.playerPos.y, angleDeg, 3);
                else if (s.equals("bow_legendary.png")) {
                    gs.particles.spawnFire(gs.playerPos.x, gs.playerPos.y, 2);
                    gs.particles.spawnLightning(gs.playerPos.x, gs.playerPos.y, angleDeg, 2);
                }
                else if (s.equals("bow_fast.png") || s.equals("bow_triple.png"))
                    gs.particles.spawnLightning(gs.playerPos.x, gs.playerPos.y, angleDeg, 2);
            }
        }
    }

    static int getChargeDamage(GameScreen gs) {
        if (gs.chargeLevel == GameScreen.CHARGE_LEVEL_3) return GameScreen.CHARGE_DMG_LVL3;
        if (gs.chargeLevel == GameScreen.CHARGE_LEVEL_2) return GameScreen.CHARGE_DMG_LVL2;
        return GameScreen.CHARGE_DMG_LVL1;
    }

    static void updateArrows(GameScreen gs, float delta) {
        for (int i = gs.fleches.size - 1; i >= 0; i--) {
            Fleche f = gs.fleches.get(i);
            int steps = MathUtils.clamp((int) Math.ceil(GameScreen.ARROW_SPEED * delta / 8f), 1, 8);
            float sdt = delta / steps;
            boolean remove = false;
            for (int s = 0; s < steps; s++) {
                f.pos.x += f.vel.x * sdt;
                f.pos.y += f.vel.y * sdt;
                if (gs.dungeon.circleHitsWall(f.pos.x, f.pos.y, GameScreen.ARROW_RADIUS)) {
                    remove = true;
                    break;
                }
            }
            f.life -= delta;
            if (f.fromEnemy && !remove) {
                float rr = GameScreen.PLAYER_RADIUS + GameScreen.ARROW_RADIUS;
                if (f.pos.dst2(gs.playerPos) < rr * rr) {
                    remove = true;
                    if (gs.invulnerable <= 0f) hurtPlayer(gs);
                }
            }
            if (remove || f.life <= 0f || f.pos.x < -20 || f.pos.x > GameScreen.W + 20
                    || f.pos.y < -20 || f.pos.y > GameScreen.H + 20) {
                gs.fleches.removeIndex(i);
            }
        }
    }
    static void tirerCharge(GameScreen gs, float angleDeg, int niveau) {
    if (niveau <= 0) return;
    int nbFlechesATirer = Math.min(niveau, gs.nbFleches);
    if (nbFlechesATirer <= 0) return;
    float spread = (niveau == 3) ? 15f : (niveau == 2) ? 8f : 0f;
    for (int i = 0; i < nbFlechesATirer; i++) {
        float offset = 0f;
        if (nbFlechesATirer > 1) {
            offset = -spread + (2f * spread * i / (nbFlechesATirer - 1));
        }
        fireAt(gs, angleDeg + offset, 2);
    }
}

    static void spawnBossProjectile(GameScreen gs, float x, float y, float angleDeg) {
        Fleche p = new Fleche();
        p.pos.set(x, y);
        p.vel.set(MathUtils.cosDeg(angleDeg) * GameScreen.BOSS_PROJECTILE_SPEED,
                  MathUtils.sinDeg(angleDeg) * GameScreen.BOSS_PROJECTILE_SPEED);
        p.angle = angleDeg;
        p.fromEnemy = true;
        p.life = 3f;
        p.damage = 1;
        gs.fleches.add(p);
        gs.particles.spawnFire(x, y, 2);
    }

    static void hurtPlayer(GameScreen gs) {
        gs.setLives(gs.getLives() - 1);
        gs.invulnerable = 1f;
        gs.hitFlash = GameScreen.HIT_FLASH_DURATION;
        gs.triggerShake();
        gs.triggerStun();
        gs.damage.add(gs.playerPos.x, gs.playerPos.y + 30f, "-1", true);
        gs.particles.spawnHitSpark(gs.playerPos.x, gs.playerPos.y,
                new com.badlogic.gdx.graphics.Color(1f, 0.3f, 0.3f, 1f));
        if (gs.getLives() <= 0) {
            gs.gameOver = true;
            gs.endTimer = 0f;
            if (gs.elevator != null) gs.elevator.forceUnlock();
        }
    }
}