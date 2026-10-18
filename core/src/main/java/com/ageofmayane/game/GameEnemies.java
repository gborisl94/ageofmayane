package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

class GameEnemies {

    static void updateEnemies(GameScreen gs, float delta) {
        float normalSpeed = Math.min(80f + gs.score * 1.5f, 200f);
        for (int i = gs.ennemis.size - 1; i >= 0; i--) {
            Ennemi e = gs.ennemis.get(i);
            boolean frozen = e.stunned > 0f;
            if (frozen) {
                e.stunned -= delta;
                if (Math.random() < 0.15f) gs.particles.spawnLightning(e.pos.x, e.pos.y, 0f, 1);
            }
            if (e.hitPulse > 0f) e.hitPulse -= delta;
            if (e.attackPulse > 0f) e.attackPulse -= delta;

            if (!frozen && e.boss && e.fleeing) {
                e.fleeTimer += delta;
                e.pos.x -= 400f * delta;
                e.pos.y += 250f * delta;
                e.angle = 135f;
                if (Math.random() < 0.4f) gs.particles.spawnSmoke(e.pos.x, e.pos.y, 1);
                if (e.fleeTimer > 2.5f || e.pos.x < -150f || e.pos.y > GameScreen.H + 150f) {
                    gs.ennemis.removeIndex(i);
                    gs.bossFledThisRun = true;
                    continue;
                }
                continue;
            }

            if (!frozen && e.boss && !e.fleeing && e.hp > 0 && e.hp <= e.maxHp * 0.2f) {
                if (Math.random() < 0.5f * delta) {
                    e.fleeing = true;
                    e.fleeTimer = 0f;
                    gs.bossFleeMessageTimer = 3.5f;
                    gs.triggerShake();
                    gs.triggerStun();
                    gs.particles.spawnSmoke(e.pos.x, e.pos.y, 8);
                }
            }

            if (!frozen && e.boss && !e.fleeing) {
                if (e.vengeanceBonuses != null
                        && e.vengeanceBonuses.contains(VengeanceBonus.REGENERATOR, true)) {
                    e.hp = (int) Math.min(e.maxHp, e.hp + e.maxHp * 0.01f * delta);
                }
                if (e.vengeanceBonuses != null
                        && e.vengeanceBonuses.contains(VengeanceBonus.TELEPORTER, true)) {
                    e.teleportTimer += delta;
                    if (e.teleportTimer >= 5f) {
                        float angle = MathUtils.random(0f, 360f);
                        e.pos.set(
                            MathUtils.clamp(gs.playerPos.x + MathUtils.cosDeg(angle) * 150f, 80f, GameScreen.W - 80f),
                            MathUtils.clamp(gs.playerPos.y + MathUtils.sinDeg(angle) * 150f, 80f, GameScreen.H - 80f)
                        );
                        e.teleportTimer = 0f;
                        gs.particles.spawnLightning(e.pos.x, e.pos.y, 0f, 4);
                    }
                }
                if (e.vengeanceBonuses != null
                        && e.vengeanceBonuses.contains(VengeanceBonus.SUMMONER, true)) {
                    e.summonTimer += delta;
                    if (e.summonTimer >= 10f) {
                        for (int k = 0; k < 2; k++) {
                            Ennemi sbire = new Ennemi();
                            sbire.hp = getEnemyHp(gs);
                            sbire.maxHp = getEnemyHp(gs);
                            float ang = MathUtils.random(0f, 360f);
                            float dist = MathUtils.random(80f, 150f);
                            sbire.pos.set(
                                MathUtils.clamp(e.pos.x + MathUtils.cosDeg(ang) * dist, 40f, GameScreen.W - 40f),
                                MathUtils.clamp(e.pos.y + MathUtils.sinDeg(ang) * dist, 40f, GameScreen.H - 40f)
                            );
                            gs.ennemis.add(sbire);
                            gs.particles.spawnPoison(sbire.pos.x, sbire.pos.y, 3);
                        }
                        e.summonTimer = 0f;
                    }
                }

                float ratio = e.hp / (float) e.maxHp;
                int newPhase = 1;
                if (ratio > 0.66f) newPhase = 1;
                else if (ratio > 0.33f) newPhase = 2;
                else newPhase = 3;

                if (newPhase > e.phase) {
                    e.phase = newPhase;
                    e.phaseChangeFlash = 1.0f;
                    gs.bossPhaseMessage = newPhase;
                    gs.bossPhaseMessageTimer = 2.5f;
                    gs.triggerShake();
                    gs.particles.spawnSmoke(e.pos.x, e.pos.y, 15);
                    gs.particles.spawnBossExplosion(e.pos.x, e.pos.y, new Color(1f, 0.5f, 0.1f, 1f));
                }
                if (e.phaseChangeFlash > 0f) e.phaseChangeFlash -= delta;

                if (e.phase >= 2) {
                    e.attackCooldown -= delta;
                    if (e.attackCooldown <= 0f) {
                        float cd = (e.phase == 2) ? GameScreen.BOSS_ATTACK_COOLDOWN_PHASE_2
                                                  : GameScreen.BOSS_ATTACK_COOLDOWN_PHASE_3;
                        e.attackCooldown = cd;
                        Vector2 dir = gs.playerPos.cpy().sub(e.pos).nor();
                        float baseAngle = dir.angleDeg();
                        if (e.phase == 2) {
                            GameCombat.spawnBossProjectile(gs, e.pos.x, e.pos.y, baseAngle);
                        } else {
                            GameCombat.spawnBossProjectile(gs, e.pos.x, e.pos.y, baseAngle - 20f);
                            GameCombat.spawnBossProjectile(gs, e.pos.x, e.pos.y, baseAngle);
                            GameCombat.spawnBossProjectile(gs, e.pos.x, e.pos.y, baseAngle + 20f);
                        }
                    }
                }
                if (e.phase >= 3) {
                    e.spawnCooldown -= delta;
                    if (e.spawnCooldown <= 0f) {
                        e.spawnCooldown = GameScreen.BOSS_SPAWN_COOLDOWN;
                        for (int k = 0; k < 2; k++) {
                            Ennemi sbire = new Ennemi();
                            sbire.hp = getEnemyHp(gs);
                            sbire.maxHp = getEnemyHp(gs);
                            float ang = MathUtils.random(0f, 360f);
                            float dist = MathUtils.random(80f, 150f);
                            sbire.pos.set(
                                MathUtils.clamp(e.pos.x + MathUtils.cosDeg(ang) * dist, 40f, GameScreen.W - 40f),
                                MathUtils.clamp(e.pos.y + MathUtils.sinDeg(ang) * dist, 40f, GameScreen.H - 40f)
                            );
                            gs.ennemis.add(sbire);
                            gs.particles.spawnPoison(sbire.pos.x, sbire.pos.y, 3);
                        }
                    }
                }
            }

            if (!frozen) {
                float ddx = gs.playerPos.x - e.pos.x;
                float ddy = gs.playerPos.y - e.pos.y;
                float ddist = (float) Math.sqrt(ddx * ddx + ddy * ddy);
                float detectRange = e.boss ? 600f : 350f;
                if (ddist <= detectRange) {
                    if (e.hp > 0 && e.hp <= e.maxHp * 0.3f && !e.boss) e.aiState = 2;
                    else e.aiState = 1;
                } else {
                    e.aiState = 0;
                }

                if (e.aiState == 2) {
                    float fx = -ddx / ddist;
                    float fy = -ddy / ddist;
                    float fsp = normalSpeed * 0.8f;
                    float sx = fx * fsp * delta;
                    float sy = fy * fsp * delta;
                    if (!gs.dungeon.circleHitsWall(e.pos.x + sx, e.pos.y + sy, e.radius)) {
                        e.pos.x += sx;
                        e.pos.y += sy;
                    }
                    e.angle = (float) Math.toDegrees(Math.atan2(-ddy, -ddx));
                } else if (e.aiState == 1) {
                    float nx = ddx / ddist;
                    float ny = ddy / ddist;
                    e.angle = (float) Math.toDegrees(Math.atan2(ddy, ddx));
                    float spd;
                    if (e.boss) {
                        if (e.phase == 1) spd = GameScreen.BOSS_SPEED_PHASE_1;
                        else if (e.phase == 2) spd = GameScreen.BOSS_SPEED_PHASE_2;
                        else spd = GameScreen.BOSS_SPEED_PHASE_3;
                        if (e.berserker && e.hp <= e.maxHp * 0.5f) spd *= 1.3f;
                    } else spd = normalSpeed;
                    float sx = nx * spd * delta;
                    float sy = ny * spd * delta;
                    boolean moved = false;
                    if (!gs.dungeon.circleHitsWall(e.pos.x + sx, e.pos.y, e.radius)) {
                        e.pos.x += sx;
                        moved = true;
                    }
                    if (!gs.dungeon.circleHitsWall(e.pos.x, e.pos.y + sy, e.radius)) {
                        e.pos.y += sy;
                        moved = true;
                    }
                    if (!moved) {
                        float px = -ny * spd * delta;
                        float py = nx * spd * delta;
                        if (!gs.dungeon.circleHitsWall(e.pos.x + px, e.pos.y + py, e.radius)) {
                            e.pos.x += px;
                            e.pos.y += py;
                        } else if (!gs.dungeon.circleHitsWall(e.pos.x - px, e.pos.y - py, e.radius)) {
                            e.pos.x -= px;
                            e.pos.y -= py;
                        }
                    }
                    gs.dungeon.collide(e.pos, e.radius);
                } else {
                    e.patrolTimer -= delta;
                    if (e.patrolTimer <= 0f) {
                        e.patrolTimer = MathUtils.random(1.5f, 3f);
                        float a = MathUtils.random(0f, 360f);
                        e.patrolDirX = MathUtils.cosDeg(a);
                        e.patrolDirY = MathUtils.sinDeg(a);
                    }
                    float psp = normalSpeed * 0.4f;
                    float sx = e.patrolDirX * psp * delta;
                    float sy = e.patrolDirY * psp * delta;
                    if (!gs.dungeon.circleHitsWall(e.pos.x + sx, e.pos.y + sy, e.radius)) {
                        e.pos.x += sx;
                        e.pos.y += sy;
                    } else {
                        e.patrolTimer = 0f;
                    }
                    e.angle = (float) Math.toDegrees(Math.atan2(e.patrolDirY, e.patrolDirX));
                }
            }

            boolean mort = false;
            float rr = e.radius + GameScreen.ARROW_RADIUS;
            for (int j = gs.fleches.size - 1; j >= 0; j--) {
                if (gs.fleches.get(j).fromEnemy) continue;
                if (gs.fleches.get(j).pos.dst2(e.pos) < rr * rr) {
                    Fleche hitF = gs.fleches.get(j);
                    int arrowDmg = hitF.damage;
                    gs.fleches.removeIndex(j);
                    if (e.shieldCharges > 0) {
                        e.shieldCharges--;
                        gs.damage.add(e.pos.x, e.pos.y + e.size / 2f, "BLOQUE", false);
                        gs.particles.spawnLightning(e.pos.x, e.pos.y, 0f, 3);
                        continue;
                    }
                    if (hitF.croissant && hitF.generation == 0) {
                        GameAbilities.splitCrescent(gs, hitF);
                        gs.luneKills++;
                        if (gs.luneKills >= 5) {
                            gs.luneKills = 0;
                            gs.luneLevel = Math.min(gs.luneLevel + 1, 4);
                            gs.particles.spawnLightning(e.pos.x, e.pos.y, 0f, 8);
                        }
                    }
                    e.hp -= arrowDmg;
                    e.hitPulse = GameScreen.ENEMY_HIT_PULSE_DURATION;
                    gs.damage.add(e.pos.x, e.pos.y + e.size / 2f, "-" + arrowDmg, false);
                    if (e.hp <= 0) { mort = true; break; }
                }
            }

            if (mort) {
                if (e.boss && e.vengeanceBonuses != null
                        && e.vengeanceBonuses.contains(VengeanceBonus.EXPLOSIVE, true)) {
                    for (Ennemi other : gs.ennemis) {
                        if (other == e) continue;
                        if (other.pos.dst2(e.pos) < 200f * 200f) {
                            other.hp -= (int)(e.maxHp * 0.5f);
                        }
                    }
                    gs.particles.spawnBossExplosion(e.pos.x, e.pos.y, new Color(1f, 0.3f, 0.1f, 1f));
                }
                Color pcolor = e.boss ? new Color(0.9f, 0.2f, 0.2f, 1f)
                                      : new Color(0.2f, 0.9f, 0.9f, 1f);
                if (e.boss) {
                    gs.particles.spawnBossExplosion(e.pos.x, e.pos.y, pcolor);
                    gs.particles.spawnBlood(e.pos.x, e.pos.y, 30);
                    gs.particles.spawnSmoke(e.pos.x, e.pos.y, 20);
                    gs.triggerShake();
                } else {
                    gs.particles.spawnEnemyExplosion(e.pos.x, e.pos.y, pcolor);
                    gs.particles.spawnBlood(e.pos.x, e.pos.y, 12);
                    gs.particles.spawnSmoke(e.pos.x, e.pos.y, 5);
                }
                gs.ennemis.removeIndex(i);
                gs.setScore(gs.getScore() + (e.boss ? GameScreen.BOSS_SCORE : 1));
                gs.xpSystem.addXp(e.boss ? GameScreen.XP_PER_BOSS : GameScreen.XP_PER_ENEMY);
                {
    int dropCount = e.boss ? 5 + (int)(Math.random() * 4) : 2 + (int)(Math.random() * 2);
    for (int k = 0; k < dropCount; k++) {
        float dx = MathUtils.random(-40f, 40f);
        float dy = MathUtils.random(-40f, 40f);
        float fx = MathUtils.clamp(e.pos.x + dx, 20f, GameScreen.W - 20f);
        float fy = MathUtils.clamp(e.pos.y + dy, 20f, GameScreen.H - 20f);
        if (!gs.dungeon.circleHitsWall(fx, fy, 5f)) {
            gs.flechesSol.add(new FlecheSol(fx, fy, MathUtils.random(0f, 360f)));
        }
    }
    gs.particles.spawnLightning(e.pos.x, e.pos.y, 0f, e.boss ? 8 : 3);
    gs.particles.spawnSmoke(e.pos.x, e.pos.y, e.boss ? 10 : 3);
                }
                {
    int dropCount = e.boss ? 5 + (int)(Math.random() * 4) : 2 + (int)(Math.random() * 2);
    for (int k = 0; k < dropCount; k++) {
        float dx = MathUtils.random(-40f, 40f);
        float dy = MathUtils.random(-40f, 40f);
        float fx = MathUtils.clamp(e.pos.x + dx, 20f, GameScreen.W - 20f);
        float fy = MathUtils.clamp(e.pos.y + dy, 20f, GameScreen.H - 20f);
        if (!gs.dungeon.circleHitsWall(fx, fy, 5f)) {
            gs.flechesSol.add(new FlecheSol(fx, fy, MathUtils.random(0f, 360f)));
        }
    }
    gs.particles.spawnLightning(e.pos.x, e.pos.y, 0f, e.boss ? 8 : 3);
    gs.particles.spawnSmoke(e.pos.x, e.pos.y, e.boss ? 10 : 3);
                }
                if (e.boss) {
                    gs.vengeance.clear();
                    if (gs.currentFloor < GameScreen.TOTAL_FLOORS) {
                        GameFloor.nextFloor(gs);
                        return;
                    } else {
                        gs.victory = true;
                        gs.endTimer = 0f;
                    }
                }
                continue;
            }

            float prr = e.radius + GameScreen.PLAYER_RADIUS;
            if (!frozen && e.pos.dst2(gs.playerPos) < prr * prr) {
                e.attackPulse = GameScreen.ENEMY_ATTACK_PULSE_DURATION;
                if (e.boss) {
                    if (gs.invulnerable <= 0f) GameCombat.hurtPlayer(gs);
                    if (e.vengeanceBonuses != null
                            && e.vengeanceBonuses.contains(VengeanceBonus.VAMPIRIC, true)) {
                        e.hp = (int) Math.min(e.maxHp, e.hp + 3f);
                    }
                    if (e.vengeanceBonuses != null
                            && e.vengeanceBonuses.contains(VengeanceBonus.FROZEN, true)) {
                        gs.stunTimer = Math.max(gs.stunTimer, 1.0f);
                    }
                    gs.tmp.set(e.pos).sub(gs.playerPos);
                    if (gs.tmp.len2() < 0.001f) gs.tmp.set(1f, 0f);
                    gs.tmp.nor().scl(GameScreen.BOSS_KNOCKBACK);
                    e.pos.add(gs.tmp);
                    e.pos.x = MathUtils.clamp(e.pos.x, Room.WALL_T + e.radius, GameScreen.W - Room.WALL_T - e.radius);
                    e.pos.y = MathUtils.clamp(e.pos.y, Room.WALL_T + e.radius, GameScreen.H - Room.WALL_T - e.radius);
                } else {
                    gs.ennemis.removeIndex(i);
                    if (gs.invulnerable <= 0f) GameCombat.hurtPlayer(gs);
                }
            }
        }
    }

    static void spawnRoomEnemies(GameScreen gs, Room room) {
        if (room.boss) {
            Ennemi b = new Ennemi();
            b.boss = true;
            float mult = gs.vengeance.getStatMultiplier();
            b.hp    = (int)(getBossHp(gs) * mult);
            b.maxHp = b.hp;
            b.radius = GameScreen.BOSS_RADIUS;
            b.size = GameScreen.BOSS_SIZE;
            b.phase = 1;
            b.attackCooldown = GameScreen.BOSS_ATTACK_COOLDOWN_PHASE_2;
            b.spawnCooldown = GameScreen.BOSS_SPAWN_COOLDOWN;
            b.pos.set(GameScreen.W / 2f, GameScreen.H / 2f);
            if (gs.vengeance.getLevel() > 0) {
                b.vengeanceLevel = gs.vengeance.getLevel();
                b.vengeanceBonuses = new Array<VengeanceBonus>(gs.vengeance.getBonuses());
                if (gs.vengeance.has(VengeanceBonus.SHIELDED)) b.shieldCharges = 3;
                if (gs.vengeance.has(VengeanceBonus.BERSERKER)) b.berserker = true;
            }
            gs.ennemis.add(b);
        }
        for (int i = 0; i < room.enemyCount; i++) {
            Ennemi e = new Ennemi();
            e.hp = getEnemyHp(gs);
            e.maxHp = getEnemyHp(gs);
            findSpawnPoint(gs, e.pos, GameScreen.ENEMY_RADIUS);
            gs.ennemis.add(e);
        }
        if (gs.ennemis.size == 0) gs.dungeon.markCleared();
    }

    static void findSpawnPoint(GameScreen gs, Vector2 out, float radius) {
        for (int i = 0; i < 40; i++) {
            float x = MathUtils.random(Room.WALL_T + 50f, GameScreen.W - Room.WALL_T - 50f);
            float y = MathUtils.random(Room.WALL_T + 50f, GameScreen.H - Room.WALL_T - 50f);
            out.set(x, y);
            if (out.dst2(gs.playerPos) < GameScreen.SPAWN_MIN_DIST * GameScreen.SPAWN_MIN_DIST) continue;
            if (gs.dungeon.circleHitsWall(x, y, radius + 10f)) continue;
            return;
        }
        out.set(MathUtils.clamp(GameScreen.W - gs.playerPos.x, 80f, GameScreen.W - 80f),
                MathUtils.clamp(GameScreen.H - gs.playerPos.y, 80f, GameScreen.H - 80f));
    }

    static int getEnemyHp(GameScreen gs) {
    // Les ennemis gagnent 1 PV tous les 5 étages
    return GameScreen.ENEMY_HP + (gs.currentFloor / 5);
}

static int getBossHp(GameScreen gs) {
    // Le boss gagne 20 PV par étage
    return GameScreen.BOSS_HP + (gs.currentFloor * 20);
}
}
