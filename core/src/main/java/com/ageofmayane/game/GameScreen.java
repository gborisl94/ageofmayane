damage.add(e.pos.x, e.pos.y + e.size / 2f, "-" + arrowDmg, false);
                    if (e.hp <= 0) { mort = true; break; }
                }
            }if (mort) {
    if (e.boss && e.vengeanceBonuses != null
            && e.vengeanceBonuses.contains(VengeanceBonus.EXPLOSIVE, true)) {
        for (Ennemi other : ennemis) {
            if (other == e) continue;
            if (other.pos.dst2(e.pos) < 200f * 200f) {
                other.hp -= (int)(e.maxHp * 0.5f);
            }
        }
        particles.spawnBossExplosion(e.pos.x, e.pos.y, new Color(1f, 0.3f, 0.1f, 1f));
    }Color pcolor = e.boss ? new Color(0.9f, 0.2f, 0.2f, 1f)
                      : new Color(0.2f, 0.9f, 0.9f, 1f);
if (e.boss) {
    particles.spawnBossExplosion(e.pos.x, e.pos.y, pcolor);
    particles.spawnBlood(e.pos.x, e.pos.y, 30);
    particles.spawnSmoke(e.pos.x, e.pos.y, 20);
    triggerShake();
} else {
    particles.spawnEnemyExplosion(e.pos.x, e.pos.y, pcolor);
    particles.spawnBlood(e.pos.x, e.pos.y, 12);
    particles.spawnSmoke(e.pos.x, e.pos.y, 5);
}    ennemis.removeIndex(i);
    setScore(score + (e.boss ? BOSS_SCORE : 1));
    xpSystem.addXp(e.boss ? XP_PER_BOSS : XP_PER_ENEMY);

    if (e.boss) {
        vengeance.clear();
        if (currentFloor < TOTAL_FLOORS) {
            nextFloor();
            return;
        } else {
            victory = true;
            endTimer = 0f;
        }
    }
    continue;
}float pr = e.radius + PLAYER_RADIUS;
if (!frozen && e.pos.dst2(playerPos) < pr * pr) {
    e.attackPulse = ENEMY_ATTACK_PULSE_DURATION;
    if (e.boss) {
        if (invulnerable <= 0f) hurtPlayer();
        if (e.vengeanceBonuses != null
                && e.vengeanceBonuses.contains(VengeanceBonus.VAMPIRIC, true)) {
            e.hp = (int) Math.min(e.maxHp, e.hp + 3f);
        }
        if (e.vengeanceBonuses != null
                && e.vengeanceBonuses.contains(VengeanceBonus.FROZEN, true)) {
            stunTimer = Math.max(stunTimer, 1.0f);
        }                tmp.set(e.pos).sub(playerPos);
                if (tmp.len2() < 0.001f) tmp.set(1f, 0f);
                tmp.nor().scl(BOSS_KNOCKBACK);
                e.pos.add(tmp);
                e.pos.x = MathUtils.clamp(e.pos.x, Room.WALL_T + e.radius, W - Room.WALL_T - e.radius);
                e.pos.y = MathUtils.clamp(e.pos.y, Room.WALL_T + e.radius, H - Room.WALL_T - e.radius);
            } else {
                ennemis.removeIndex(i);
                if (invulnerable <= 0f) hurtPlayer();
            }
        }
    }
}

private void spawnBossProjectile(float x, float y, float angleDeg) {
    Fleche p = new Fleche();
    p.pos.set(x, y);
    p.vel.set(MathUtils.cosDeg(angleDeg) * BOSS_PROJECTILE_SPEED,
              MathUtils.sinDeg(angleDeg) * BOSS_PROJECTILE_SPEED);
    p.angle = angleDeg;
    p.fromEnemy = true;
    p.life = 3f;
    p.damage = 1;
    fleches.add(p);
    particles.spawnFire(x, y, 2);
}private void spawnRoomEnemies(Room room) {
    if (room.boss) {
        Ennemi b = new Ennemi();
        b.boss = true;

        float mult = vengeance.getStatMultiplier();
        b.hp    = (int)(getBossHp() * mult);
        b.maxHp = b.hp;
        b.radius = BOSS_RADIUS;
        b.size = BOSS_SIZE;
        b.phase = 1;
        b.attackCooldown = BOSS_ATTACK_COOLDOWN_PHASE_2;
        b.spawnCooldown = BOSS_SPAWN_COOLDOWN;
        b.pos.set(W / 2f, H / 2f);

        if (vengeance.getLevel() > 0) {
            b.vengeanceLevel = vengeance.getLevel();
            b.vengeanceBonuses = new Array<VengeanceBonus>(vengeance.getBonuses());
            if (vengeance.has(VengeanceBonus.SHIELDED)) b.shieldCharges = 3;
            if (vengeance.has(VengeanceBonus.BERSERKER)) b.berserker = true;
        }
        ennemis.add(b);
    }    for (int i = 0; i < room.enemyCount; i++) {
        Ennemi e = new Ennemi();
        e.hp = getEnemyHp();
        e.maxHp = getEnemyHp();
        findSpawnPoint(e.pos, ENEMY_RADIUS);
        ennemis.add(e);
    }
    if (ennemis.size == 0) dungeon.markCleared();
}

private void findSpawnPoint(Vector2 out, float radius) {
    for (int i = 0; i < 40; i++) {
        float x = MathUtils.random(Room.WALL_T + 50f, W - Room.WALL_T - 50f);
        float y = MathUtils.random(Room.WALL_T + 50f, H - Room.WALL_T - 50f);
        out.set(x, y);
        if (out.dst2(playerPos) < SPAWN_MIN_DIST * SPAWN_MIN_DIST) continue;
        if (dungeon.circleHitsWall(x, y, radius + 10f)) continue;
        return;
    }
    out.set(MathUtils.clamp(W - playerPos.x, 80f, W - 80f),
            MathUtils.clamp(H - playerPos.y, 80f, H - 80f));
}private void hurtPlayer() {
    setLives(lives - 1);
    invulnerable = 1f;
    hitFlash = HIT_FLASH_DURATION;
    triggerShake();
    triggerStun();
    damage.add(playerPos.x, playerPos.y + 30f, "-1", true);
    particles.spawnHitSpark(playerPos.x, playerPos.y, new Color(1f, 0.3f, 0.3f, 1f));
    if (lives <= 0) {
        gameOver = true;
        endTimer = 0f;
        if (elevator != null) elevator.forceUnlock();
    }
}

private void enterRoom(Room room) {
    fleches.clear();
    ennemis.clear();
    damage.clear();
    if (!room.cleared) spawnRoomEnemies(room);
}private void updateWeaponDrops(float delta) {
    for (int i = weaponDrops.size - 1; i >= 0; i--) {
        WeaponDrop wd = weaponDrops.get(i);
        wd.update(delta);
        if (wd.pos.dst2(playerPos) < 60f * 60f) {
            equippedWeapon = wd.weapon;
            weaponDrops.removeIndex(i);
            particles.spawnLightning(playerPos.x, playerPos.y, 0f, 4);
        }
    }
}

private void updateChest(float delta) {
    if (chest != null) chest.update(delta);
}private void pollTouches() {
    zoom.resetFrame();
    newTouch = false;
    for (int i = 0; i < MAX_POINTERS; i++) {
        if (!Gdx.input.isTouched(i)) {
            if (moveStick.pointer == i) moveStick.end();
            if (aimStick.pointer == i) aimStick.end();
            owner[i] = OWNER_NONE;
            continue;
        }
        tmp.set(Gdx.input.getX(i), Gdx.input.getY(i));
        renderer.uiViewport.unproject(tmp);
        float x = tmp.x;
        float y = tmp.y;

        if (owner[i] == OWNER_NONE) {
            if (elevatorButton != null && elevatorButton.isVisible()
                    && elevatorButton.getBounds().contains(x, y)) {
                if (elevatorButton.handleClick(x, y)) {
                    tryUseElevator();
                    newTouch = true;
                    owner[i] = OWNER_IGNORED;
                    continue;
                }
            }if (x >= AIM_BTN_X && x <= AIM_BTN_X + AIM_BTN_SIZE
        && y >= AIM_BTN_Y && y <= AIM_BTN_Y + AIM_BTN_SIZE) {
    if (autoAimActive) deactivateAutoAimPower();
    else if (isAutoAimReady()) activateAutoAimPower();
    newTouch = true;
    owner[i] = OWNER_IGNORED;
} else if (x >= STUN_BTN_X && x <= STUN_BTN_X + STUN_BTN_SIZE
        && y >= STUN_BTN_Y && y <= STUN_BTN_Y + STUN_BTN_SIZE) {
    if (isStunReady()) activateStunAbility();
    newTouch = true;
    owner[i] = OWNER_IGNORED;
} else if (canInteractWithChest() && x >= OPEN_BTN_X
        && x <= OPEN_BTN_X + OPEN_BTN_W
        && y >= OPEN_BTN_Y && y <= OPEN_BTN_Y + OPEN_BTN_H) {
    openChest();
    newTouch = true;
    owner[i] = OWNER_IGNORED;
} else if (zoom.handleTouch(x, y)) {
    owner[i] = OWNER_ZOOM;
} else {                newTouch = true;
                if (x < W / 2f && !moveStick.active()) {
                    owner[i] = OWNER_MOVE;
                    moveStick.begin(i, x, y);
                } else if (x >= W / 2f && !aimStick.active()) {
                    owner[i] = OWNER_AIM;
                    aimStick.begin(i, x, y);
                } else {
                    owner[i] = OWNER_IGNORED;
                }
            }
        } else if (owner[i] == OWNER_ZOOM) {
            zoom.handleTouch(x, y);
        } else if (owner[i] == OWNER_MOVE && moveStick.pointer == i) {
            moveStick.move(x, y);
        } else if (owner[i] == OWNER_AIM && aimStick.pointer == i) {
            aimStick.move(x, y);
        }
    }
}    private void disposeResources() {
        if (renderer != null) {
            renderer.dispose();
            renderer = null;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (renderer != null) renderer.resize(width, height);
    }

    @Override
    public void dispose() {
        disposeResources();
        if (uiShapes != null) { uiShapes.dispose(); uiShapes = null; }
        if (uiBatch != null) { uiBatch.dispose(); uiBatch = null; }
        if (uiFont != null) { uiFont.dispose(); uiFont = null; }
    }
}