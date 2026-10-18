package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class GameFloor {

    static void nextFloor(GameScreen gs) {
        if (gs.bossFledThisRun) gs.vengeance.registerFlee();
        gs.vengeance.consumePending();
        gs.currentFloor++;
        gs.floorBannerTimer = 3f;
        if (gs.currentFloor > GameScreen.TOTAL_FLOORS) {
            gs.victory = true;
            gs.endTimer = 0f;
            return;
        }
        gs.dungeon.generate();
        gs.playerPos.set(GameScreen.W / 2f, GameScreen.H / 2f);
        gs.fleches.clear();
        gs.ennemis.clear();
        gs.weaponDrops.clear();
        gs.damage.clear();
        gs.particles.clear();
        gs.moveStick.end();
        gs.aimStick.end();
        for (int i = 0; i < GameScreen.MAX_POINTERS; i++) gs.owner[i] = GameScreen.OWNER_NONE;
        gs.facing = 0f;
        gs.fireTimer = 0f;
        gs.invulnerable = 2.5f;
        gs.hitFlash = 0f;
        gs.playerRecoil = 0f;
        gs.bossFleeMessageTimer = 0f;
        gs.bossPhaseMessageTimer = 0f;
        gs.bossPhaseMessage = 0;
        gs.shakeTime = 0f;
        gs.stunTimer = 0f;
        gs.stunCooldown = 0f;
        gs.dizzyTime = 0f;
        gs.bossFledThisRun = false;
        gs.endTimer = 0f;
        gs.gameOver = false;
        gs.victory = false;
        gs.attention = GameScreen.ATTENTION_MAX;
        gs.autoTarget = null;
        gs.aimStability = 0f;
        gs.targetAcquiredTimer = 0f;
        gs.lastAutoTarget = null;
        gs.chargeLevel = 1;
        gs.autoAimActive = false;
        gs.autoAimTimer = 0f;
        gs.autoAimCooldown = 0f;
        Vector2 chestPos = findRandomFloorPos(gs, 40f, 200f);
        gs.chest = new Chest(chestPos.x, chestPos.y);
        gs.elevator = null;
        if (gs.elevatorButton != null) gs.elevatorButton.hide();
        gs.floorTransitionPending = false;
        gs.transitionTimer = 0f;
        Room cur = gs.dungeon.getCurrent();
        if (cur != null && !cur.cleared) GameEnemies.spawnRoomEnemies(gs, cur);
        gs.triggerShake();
        gs.particles.spawnLightning(gs.playerPos.x, gs.playerPos.y, 0f, 12);
    }
if (gs.currentFloor == 100) {
    // C'est l'étage final ! On met un boss géant.
    gs.triggerShake();
    // Tu peux ajouter une musique spéciale ou un message
}
    static Vector2 findRandomFloorPos(GameScreen gs, float radius, float minDistFromPlayer) {
        Vector2 out = new Vector2();
        for (int i = 0; i < 60; i++) {
            float x = MathUtils.random(Room.WALL_T + 60f, GameScreen.W - Room.WALL_T - 60f);
            float y = MathUtils.random(Room.WALL_T + 60f, GameScreen.H - Room.WALL_T - 60f);
            if (gs.dungeon.circleHitsWall(x, y, radius + 10f)) continue;
            if (out.dst2(gs.playerPos) < minDistFromPlayer * minDistFromPlayer) continue;
            out.set(x, y);
            return out;
        }
        out.set(GameScreen.W / 2f, GameScreen.H / 2f);
        return out;
    }

    static void enterRoom(GameScreen gs, Room room) {
        gs.fleches.clear();
        gs.ennemis.clear();
        gs.damage.clear();
        if (!room.cleared) GameEnemies.spawnRoomEnemies(gs, room);
    }

    static void updateElevatorButton(GameScreen gs, float delta) {
        if (gs.elevator == null || gs.elevatorButton == null) return;
        gs.elevatorButton.update(delta);
        if (!gs.elevator.isUnlocked()) { gs.elevatorButton.hide(); return; }
        float cx = gs.elevator.getBounds().x + gs.elevator.getBounds().width / 2f;
        float cy = gs.elevator.getBounds().y + gs.elevator.getBounds().height / 2f;
        if (gs.playerPos.dst2(cx, cy) < GameScreen.ELEVATOR_INTERACT_DIST * GameScreen.ELEVATOR_INTERACT_DIST) {
            gs.elevatorButton.show();
            gs.elevatorButton.setLabel("MONTER");
        } else {
            gs.elevatorButton.hide();
        }
    }

    static void tryUseElevator(GameScreen gs) {
        if (gs.elevator == null || !gs.elevator.isUnlocked()) return;
        if (gs.elevator.tryActivate(gs.playerPos, GameScreen.PLAYER_RADIUS)) {
            gs.floorTransitionPending = true;
            gs.transitionTimer = 0f;
            if (gs.elevatorButton != null) gs.elevatorButton.hide();
        }
    }

    static void drawBossArrow(GameScreen gs) {
        Vector2 target = null;
        boolean isBoss = false;
        for (int i = 0; i < gs.ennemis.size; i++) {
            Ennemi e = gs.ennemis.get(i);
            if (e.boss && e.hp > 0 && !e.fleeing) {
                target = e.pos;
                isBoss = true;
                break;
            }
        }
        if (target == null) {
            Room cur = gs.dungeon.getCurrent();
            if (cur != null) {
                float bestD = Float.MAX_VALUE;
                for (int dir = 0; dir < 4; dir++) {
                    if (!cur.hasDoor(dir)) continue;
                    Rectangle r = new Rectangle();
                    cur.getDoorRect(dir, r);
                    if (r == null) continue;
                    float cx = r.x + r.width / 2f;
                    float cy = r.y + r.height / 2f;
                    float d = Vector2.dst2(gs.playerPos.x, gs.playerPos.y, cx, cy);
                    if (d < bestD) { bestD = d; target = new Vector2(cx, cy); }
                }
            }
        }
        if (target == null) return;
        float cx = GameScreen.W / 2f;
        float cy = 110f;
        float size = 42f;
        float angle = MathUtils.atan2(target.y - gs.playerPos.y, target.x - gs.playerPos.x);
        float tipX = cx + MathUtils.cos(angle) * size;
        float tipY = cy + MathUtils.sin(angle) * size;
        float lx = cx + MathUtils.cos(angle + 2.4f) * size * 0.75f;
        float ly = cy + MathUtils.sin(angle + 2.4f) * size * 0.75f;
        float rx = cx + MathUtils.cos(angle - 2.4f) * size * 0.75f;
        float ry = cy + MathUtils.sin(angle - 2.4f) * size * 0.75f;
        gs.uiShapes.setProjectionMatrix(gs.renderer.uiCamera.combined);
        gs.uiShapes.begin(ShapeRenderer.ShapeType.Filled);
        gs.uiShapes.setColor(0f, 0f, 0f, 0.55f);
        gs.uiShapes.circle(cx, cy, size + 6f, 24);
        if (isBoss) gs.uiShapes.setColor(1f, 0.15f, 0.15f, 1f);
        else gs.uiShapes.setColor(1f, 0.9f, 0.2f, 1f);
        gs.uiShapes.triangle(tipX, tipY, lx, ly, rx, ry);
        gs.uiShapes.end();
    }
}