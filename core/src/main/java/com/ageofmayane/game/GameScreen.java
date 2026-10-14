package com.ageofmayane.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends ScreenAdapter {

    static final float W = 1280f;
    static final float H = 720f;
    static final float STICK_RADIUS = 110f;
    static final float DEAD_ZONE = 0.2f;
    static final float PLAYER_RADIUS = 20f;
    static final float PLAYER_SPEED = 280f;
    static final float ENEMY_RADIUS = 18f;
    static final float ARROW_RADIUS = 5f;
    static final float ARROW_SPEED = 650f;
    static final float FIRE_DELAY = 0.18f;
    static final float PLAYER_SIZE = 64f;
    static final float ENEMY_SIZE = 56f;
    static final float ARROW_SIZE = 20f;
    static final float PLAYER_ROT = 0f;
    static final float ENEMY_ROT = 0f;
    static final float ARROW_ROT = 0f;
    static final int ENEMY_HP = 2;
    static final int BOSS_HP = 20;
    static final float BOSS_SIZE = ENEMY_SIZE * 2f;
    static final float BOSS_RADIUS = ENEMY_RADIUS * 2f;
    static final float BOSS_KNOCKBACK = 140f;
    static final int BOSS_SCORE = 5;
    static final float BOSS_SPEED_PHASE_1 = 60f;
    static final float BOSS_SPEED_PHASE_2 = 100f;
    static final float BOSS_SPEED_PHASE_3 = 160f;
    static final float BOSS_ATTACK_COOLDOWN_PHASE_2 = 1.5f;
    static final float BOSS_ATTACK_COOLDOWN_PHASE_3 = 1.2f;
    static final float BOSS_SPAWN_COOLDOWN = 6f;
    static final float BOSS_PROJECTILE_SPEED = 300f;
    static final int XP_PER_ENEMY = 5;
    static final int XP_PER_BOSS = 100;
    static final int XP_PER_ROOM = 20;
    static final float SPAWN_MIN_DIST = 280f;
    static final float CHEST_INTERACT_DIST = 120f;
    static final float OPEN_BTN_W = 260f;
    static final float OPEN_BTN_H = 80f;
    static final float OPEN_BTN_X = W - OPEN_BTN_W - 30f;
    static final float OPEN_BTN_Y = H - 320f;
    static final float STUN_ABILITY_RADIUS = 280f;
    static final float STUN_ABILITY_DURATION = 3.0f;
    static final float STUN_ABILITY_COOLDOWN = 8.0f;
    static final float STUN_BTN_SIZE = 110f;
    static final float STUN_BTN_X = W - STUN_BTN_SIZE - 30f;
    static final float STUN_BTN_Y = H - 460f;
    static final float AIM_BTN_SIZE = 110f;
    static final float AIM_BTN_X = W - AIM_BTN_SIZE - 30f;
    static final float AIM_BTN_Y = H - 460f - AIM_BTN_SIZE - 20f;
    static final float AUTO_AIM_DURATION = 8f;
    static final float AUTO_AIM_COOLDOWN = 12f;
    static final int MAX_POINTERS = 5;
    static final int OWNER_NONE = 0;
    static final int OWNER_MOVE = 1;
    static final int OWNER_AIM = 2;
    static final int OWNER_ZOOM = 3;
    static final int OWNER_IGNORED = 4;
    static final float HIT_FLASH_DURATION = 0.35f;
    static final float SHAKE_DURATION = 0.7f;
    static final float SHAKE_INTENSITY = 25f;
    static final float STUN_DURATION = 1.5f;
    static final float STUN_SPEED_FACTOR = 0.5f;
    static final float PLAYER_RECOIL_DURATION = 0.15f;
    static final float ENEMY_HIT_PULSE_DURATION = 0.25f;
    static final float ENEMY_ATTACK_PULSE_DURATION = 0.3f;
    static final float ATTENTION_MAX = 100f;
    static final float ATTENTION_DRAIN = 30f;
    static final float ATTENTION_REGEN = 15f;
    static final float AUTO_AIM_RANGE = 600f;
    static final float AUTO_AIM_RANGE_SQ = 600f * 600f;
    static final float AIM_STABILITY_TIME = 2.0f;
    static final float AIM_TREMBLE_MAX = 22f;
    static final int CHARGE_LEVEL_1 = 1;
    static final int CHARGE_LEVEL_2 = 2;
    static final int CHARGE_LEVEL_3 = 3;
    static final int CHARGE_DMG_LVL1 = 1;
    static final int CHARGE_DMG_LVL2 = 2;
    static final int CHARGE_DMG_LVL3 = 3;
    static final int TOTAL_FLOORS = 3;
    static final float ELEVATOR_INTERACT_DIST = 120f;
    static final float TRANSITION_TIME = 1.2f;
    static final float GLITCH_COOLDOWN = 5f;
    static final float GLITCH_DISTANCE = 220f;
    static final float GLITCH_INVULN = 0.5f;
    static final float GLITCH_BTN_SIZE = 110f;
    static final float GLITCH_BTN_X = W - GLITCH_BTN_SIZE - 30f;
    static final float GLITCH_BTN_Y =static final float LUNE_COOLDOWN = 4f;
static final float LUNE_SPEED = 480f;
static final float LUNE_BTN_SIZE = 110f;
static final float LUNE_BTN_X = W - LUNE_BTN_SIZE - 30f;
static final float LUNE_BTN_Y = H - 460f - LUNE_BTN_SIZE * 3 - 60f; H - 460f - GLITCH_BTN_SIZE * 2 - 40f;

    private final Game game;
    private GameRenderer renderer;

    ZoomController zoom;
    XpSystem xpSystem;
    final DungeonManager dungeon = new DungeonManager();
    final Vector2 playerPos = new Vector2(W / 2f, H / 2f);
    final Array<Fleche> fleches = new Array<Fleche>();
    final Array<Ennemi> ennemis = new Array<Ennemi>();
    final Array<WeaponDrop> weaponDrops = new Array<WeaponDrop>();
    final Array<FlecheSol> flechesSol = new Array<FlecheSol>();
int nbFleches = 50;
static final int NB_FLECHES_MAX = 50;
static final float RAMASSAGE_DIST = 60f;
final Objectifs objectifs = new Objectifs();
    final DamageNumbers damage = new DamageNumbers();
    final ParticleSystem particles = new ParticleSystem();
    final Stick moveStick = new Stick();
    final Stick aimStick = new Stick();
    Weapon equippedWeapon;
    Chest chest;
    float facing = 0f;
    float invulnerable;
    float hitFlash = 0f;
    float playerRecoil = 0f;
    float bossFleeMessageTimer = 0f;
    float bossPhaseMessageTimer = 0f;
    int bossPhaseMessage = 0;
    float shakeTime = 0f;
    float shakeOffsetX = 0f;
    float shakeOffsetY = 0f;
    float stunTimer = 0f;
    float stunCooldown = 0f;
    float dizzyTime = 0f;
    boolean bossFledThisRun = false;
    boolean gameOver;
    boolean victory;
    String scoreText = "";
    String livesText = "";
    float attention = ATTENTION_MAX;
    Ennemi autoTarget = null;
    float aimStability = 0f;
    float aimTrembleSeed = 0f;
    float aimTrembleX = 0f;
    float aimTrembleY = 0f;
    int chargeLevel = 1;
    boolean autoAimActive = false;
    float autoAimTimer = 0f;
    float autoAimCooldown = 0f;
    float targetAcquiredTimer = 0f;
    Ennemi lastAutoTarget = null;
    int currentFloor = 1;
    float floorBannerTimer = 0f;

    private final Vector2 tmp = new Vector2();
    private final int[] owner = new int[MAX_POINTERS];
    private boolean newTouch;

    private int lives;
    private int score;
    private float fireTimer;
    private float endTimer;

    private final VengeanceManager vengeance = VengeanceManager.get();
    Elevator elevator;
    ElevatorButton elevatorButton;
    private ShapeRenderer uiShapes;
    private SpriteBatch uiBatch;
    private BitmapFont uiFont;
    private boolean floorTransitionPending = false;
    float glitchCooldown = 0f;
    float luneCooldown = 0f;
int luneLevel = 1;
int luneKills = 0;
    float glitchEffectTimer = 0f;
    private float transitionTimer = 0f;

    public GameScreen() { this(null); }
    public GameScreen(Game game) { this.game = game; }
@Override
public void show() {
    disposeResources();
    renderer = new GameRenderer();
    renderer.create();
    zoom = new ZoomController(W, H);
    xpSystem = new XpSystem();
    uiShapes = new ShapeRenderer();
    uiBatch = new SpriteBatch();
    uiFont = new BitmapFont();
    uiFont.setColor(Color.WHITE);
    uiFont.getData().setScale(1.4f);
    elevatorButton = new ElevatorButton(W - 280f, 40f, 240f, 80f);
    restart();
}

private void restart() {
    dungeon.generate();
    playerPos.set(W / 2f, H / 2f);
    fleches.clear();
    ennemis.clear();
    weaponDrops.clear();
    damage.clear();
    particles.clear();
    moveStick.end();
    aimStick.end();
    for (int i = 0; i < MAX_POINTERS; i++) owner[i] = OWNER_NONE;
    facing = 0f;
    setLives(5);
    setScore(0);
    fireTimer = 0f;
    invulnerable = 0f;
    hitFlash = 0f;
    playerRecoil = 0f;
    bossFleeMessageTimer = 0f;
    bossPhaseMessageTimer = 0f;
    bossPhaseMessage = 0;
    shakeTime = 0f;
    shakeOffsetX = 0f;
    shakeOffsetY = 0f;
    stunTimer = 0f;
    stunCooldown = 0f;
    dizzyTime = 0f;
    bossFledThisRun = false;
    endTimer = 0f;
    gameOver = false;
    victory = false;
    equippedWeapon = WeaponDatabase.woodBow();
    attention = ATTENTION_MAX;
    autoTarget = null;
    aimStability = 0f;
    aimTrembleSeed = 0f;
    aimTrembleX = 0f;
    aimTrembleY = 0f;
    chargeLevel = CHARGE_LEVEL_1;
    autoAimActive = false;
    autoAimTimer = 0f;
    autoAimCooldown = 0f;
    targetAcquiredTimer = 0f;
    lastAutoTarget = null;
    currentFloor = 1;
    floorBannerTimer = 0f;
    Vector2 chestPos = findRandomFloorPos(40f, 200f);
    chest = new Chest(chestPos.x, chestPos.y);
    flechesSol.clear();
nbFleches = 50;
    vengeance.clear();
    floorTransitionPending = false;
    transitionTimer = 0f;
    if (elevatorButton != null) elevatorButton.hide();
    Vector2 elevPos = findRandomFloorPos(60f, 250f);
    elevator = new Elevator(elevPos.x - 40f, elevPos.y - 60f, 80f, 120f);
    Room cur = dungeon.getCurrent();
    if (cur != null && !cur.cleared) spawnRoomEnemies(cur);
}

private void setScore(int s) { score = s; scoreText = "Score : " + s; }
private void setLives(int l) { lives = l; livesText = "Vies : " + l; }

private void triggerShake() { shakeTime = SHAKE_DURATION; }
private void triggerStun() { stunTimer = STUN_DURATION; }
boolean isStunned() { return stunTimer > 0f; }
boolean isStunReady() { return stunCooldown <= 0f; }

float getStunCooldownRatio() {
    if (STUN_ABILITY_COOLDOWN <= 0f) return 0f;
    return Math.max(0f, Math.min(1f, stunCooldown / STUN_ABILITY_COOLDOWN));
}

boolean canInteractWithChest() {
    if (chest == null || chest.opened) return false;
    return chest.pos.dst2(playerPos) < CHEST_INTERACT_DIST * CHEST_INTERACT_DIST;
}

private void openChest() {
    if (chest == null || chest.opened) return;
    chest.open();
    objectifs.incrCoffre();
    Weapon loot = WeaponDatabase.randomChestLoot();
    weaponDrops.add(new WeaponDrop(chest.pos.x, chest.pos.y + 50f, loot));
}

private int getEnemyHp() { return ENEMY_HP + (currentFloor - 1); }
private int getBossHp() { return BOSS_HP + (currentFloor - 1) * 10; }
private void nextFloor() {
    if (bossFledThisRun) vengeance.registerFlee();
    vengeance.consumePending();
    currentFloor++;
    floorBannerTimer = 3f;
    if (currentFloor > TOTAL_FLOORS) {
        victory = true;
        endTimer = 0f;
        return;
    }
    dungeon.generate();
    playerPos.set(W / 2f, H / 2f);
    fleches.clear();
    ennemis.clear();
    weaponDrops.clear();
    damage.clear();
    particles.clear();
    moveStick.end();
    aimStick.end();
    for (int i = 0; i < MAX_POINTERS; i++) owner[i] = OWNER_NONE;
    facing = 0f;
    fireTimer = 0f;
    invulnerable = 2.5f;
    hitFlash = 0f;
    playerRecoil = 0f;
    bossFleeMessageTimer = 0f;
    bossPhaseMessageTimer = 0f;
    bossPhaseMessage = 0;
    shakeTime = 0f;
    stunTimer = 0f;
    stunCooldown = 0f;
    dizzyTime = 0f;
    bossFledThisRun = false;
    endTimer = 0f;
    gameOver = false;
    victory = false;
    attention = ATTENTION_MAX;
    autoTarget = null;
    aimStability = 0f;
    targetAcquiredTimer = 0f;
    lastAutoTarget = null;
    chargeLevel = CHARGE_LEVEL_1;
    autoAimActive = false;
    autoAimTimer = 0f;
    autoAimCooldown = 0f;
    Vector2 chestPos = findRandomFloorPos(40f, 200f);
    chest = new Chest(chestPos.x, chestPos.y);
    Vector2 elevPos = findRandomFloorPos(60f, 250f);
    elevator = new Elevator(elevPos.x - 40f, elevPos.y - 60f, 80f, 120f);
    if (elevatorButton != null) elevatorButton.hide();
    floorTransitionPending = false;
    transitionTimer = 0f;
    Room cur = dungeon.getCurrent();
    if (cur != null && !cur.cleared) spawnRoomEnemies(cur);
    triggerShake();
    particles.spawnLightning(playerPos.x, playerPos.y, 0f, 12);
}

private void activateStunAbility() {
    if (stunCooldown > 0f) return;
    stunCooldown = STUN_ABILITY_COOLDOWN;
    for (int i = 0; i < ennemis.size; i++) {
        Ennemi e = ennemis.get(i);
        if (e.pos.dst2(playerPos) < STUN_ABILITY_RADIUS * STUN_ABILITY_RADIUS) {
            e.stunned = STUN_ABILITY_DURATION;
            e.stunBase = STUN_ABILITY_DURATION;
            particles.spawnLightning(e.pos.x, e.pos.y, 0f, 3);
            particles.spawnSmoke(e.pos.x, e.pos.y, 2);
        }
    }
    for (int i = 0; i < 24; i++) {
        float ang = i * (360f / 24f);
        float x = playerPos.x + MathUtils.cosDeg(ang) * STUN_ABILITY_RADIUS;
        float y = playerPos.y + MathUtils.sinDeg(ang) * STUN_ABILITY_RADIUS;
        particles.spawnLightning(x, y, ang, 1);
    }
    triggerShake();
}

private void activateGlitchDash() {
    if (glitchCooldown > 0f) return;
    glitchCooldown = GLITCH_COOLDOWN;
    float rad = facing * MathUtils.degRad;
    float dx = MathUtils.cos(rad) * GLITCH_DISTANCE;
    float dy = MathUtils.sin(rad) * GLITCH_DISTANCE;
    particles.spawnLightning(playerPos.x, playerPos.y, facing, 10);
    playerPos.x = MathUtils.clamp(playerPos.x + dx, PLAYER_RADIUS, W - PLAYER_RADIUS);
    playerPos.y = MathUtils.clamp(playerPos.y + dy, PLAYER_RADIUS, H - PLAYER_RADIUS);
    dungeon.collide(playerPos, PLAYER_RADIUS);
    invulnerable = GLITCH_INVULN;
    triggerShake();
    glitchEffectTimer = 0.4f;
    particles.spawnLightning(playerPos.x, playerPos.y, facing + 180f, 10);
}

private void activateAutoAimPower() {
    if (autoAimCooldown > 0f) return;
    if (autoAimActive) return;
    autoAimActive = true;
    autoAimTimer = AUTO_AIM_DURATION;
    aimStability = 0f;
    particles.spawnLightning(playerPos.x, playerPos.y, 0f, 6);
}

private void deactivateAutoAimPower() {
    if (!autoAimActive) return;
    autoAimActive = false;
    autoAimCooldown = AUTO_AIM_COOLDOWN * 0.5f;
    autoAimTimer = 0f;
}

boolean isAutoAimReady() { return !autoAimActive && autoAimCooldown <= 0f; }

float getAutoAimDurationRatio() {
    if (AUTO_AIM_DURATION <= 0f) return 0f;
    return Math.max(0f, Math.min(1f, autoAimTimer / AUTO_AIM_DURATION));
}

float getAutoAimCooldownRatio() {
    if (AUTO_AIM_COOLDOWN <= 0f) return 0f;
    return Math.max(0f, Math.min(1f, autoAimCooldown / AUTO_AIM_COOLDOWN));
}

private Ennemi findNearestEnemy() {
    Ennemi nearest = null;
    float bestDist = AUTO_AIM_RANGE_SQ;
    for (int i = 0; i < ennemis.size; i++) {
        Ennemi e = ennemis.get(i);
        if (e.hp <= 0) continue;
        float d = e.pos.dst2(playerPos);
        if (d < bestDist) { bestDist = d; nearest = e; }
    }
    return nearest;
}

private void fireAt(float angleDeg, int dmg) {
    if (nbFleches <= 0) return;
nbFleches--;
    
    Fleche f = new Fleche();
    f.pos.set(playerPos);
    f.vel.set(MathUtils.cosDeg(angleDeg) * ARROW_SPEED,
              MathUtils.sinDeg(angleDeg) * ARROW_SPEED);
    f.angle = angleDeg;
    f.fromEnemy = false;
    f.damage = dmg;
    fleches.add(f);
    if (equippedWeapon != null) {
        String s = equippedWeapon.spriteName;
        if (s != null) {
            if (s.equals("bow_fire.png")) particles.spawnFire(playerPos.x, playerPos.y, 3);
            else if (s.equals("bow_ice.png")) particles.spawnWaterJet(playerPos.x, playerPos.y, angleDeg, 3);
            else if (s.equals("bow_void.png")) particles.spawnPoisonJet(playerPos.x, playerPos.y, angleDeg, 3);
            else if (s.equals("bow_legendary.png")) {
                particles.spawnFire(playerPos.x, playerPos.y, 2);
                particles.spawnLightning(playerPos.x, playerPos.y, angleDeg, 2);
            }
            else if (s.equals("bow_fast.png") || s.equals("bow_triple.png"))
                particles.spawnLightning(playerPos.x, playerPos.y, angleDeg, 2);
        }
    }
}

private int getChargeDamage() {
    if (chargeLevel == CHARGE_LEVEL_3) return CHARGE_DMG_LVL3;
    if (chargeLevel == CHARGE_LEVEL_2) return CHARGE_DMG_LVL2;
    return CHARGE_DMG_LVL1;
}
@Override
public void render(float delta) {
    if (renderer == null || zoom == null) return;
    if (!(delta > 0f) || delta > 0.05f) delta = Math.min(Math.max(delta, 0f), 0.05f);
    update(delta);
    renderer.draw(this);
    drawBossArrow();
    if (elevator != null && !floorTransitionPending && elevator.isUnlocked()) {
        uiShapes.setProjectionMatrix(renderer.uiCamera.combined);
        uiShapes.begin(ShapeRenderer.ShapeType.Filled);
        elevator.render(uiShapes);
        if (elevatorButton != null && elevatorButton.isVisible()) {
            elevatorButton.renderShape(uiShapes);
        }
        uiShapes.end();
        if (elevatorButton != null && elevatorButton.isVisible()) {
            uiBatch.setProjectionMatrix(renderer.uiCamera.combined);
            uiBatch.begin();
            uiFont.setColor(Color.WHITE);
            uiFont.draw(uiBatch, elevatorButton.getLabel(),
                elevatorButton.getBounds().x + 60f,
                elevatorButton.getBounds().y + 50f);
            uiBatch.end();
        }
    }
    if (floorTransitionPending) {
        float t = transitionTimer / TRANSITION_TIME;
        float alpha = (t < 0.5f) ? (t * 2f) : (1f - (t - 0.5f) * 2f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        uiShapes.setProjectionMatrix(renderer.uiCamera.combined);
        uiShapes.begin(ShapeRenderer.ShapeType.Filled);
        uiShapes.setColor(0f, 0f, 0f, alpha);
        uiShapes.rect(0, 0, W, H);
        uiShapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}

private void update(float delta) {
    if (floorTransitionPending) {
        transitionTimer += delta;
        if (transitionTimer >= TRANSITION_TIME) {
            floorTransitionPending = false;
            transitionTimer = 0f;
            nextFloor();
        }
        return;
    }
    pollTouches();
    zoom.update(delta);
    damage.update(delta);
    particles.update(delta);
    if (shakeTime > 0f) {
        shakeTime -= delta;
        float intensity = SHAKE_INTENSITY * (shakeTime / SHAKE_DURATION);
        shakeOffsetX = MathUtils.random(-intensity, intensity);
        shakeOffsetY = MathUtils.random(-intensity, intensity);
    } else { shakeOffsetX = 0f; shakeOffsetY = 0f; }
    if (stunTimer > 0f) { stunTimer -= delta; dizzyTime += delta; }
    if (stunCooldown > 0f) stunCooldown -= delta;
    if (glitchCooldown > 0f) glitchCooldown -= delta;
    if (glitchEffectTimer > 0f) glitchEffectTimer -= delta;
    if (autoAimActive) {
        autoAimTimer -= delta;
        if (autoAimTimer <= 0f) {
            autoAimActive = false;
            autoAimTimer = 0f;
            autoAimCooldown = AUTO_AIM_COOLDOWN;
        }
    } else if (autoAimCooldown > 0f) {
        autoAimCooldown -= delta;
        if (autoAimCooldown < 0f) autoAimCooldown = 0f;
    }
    if (gameOver || victory) {
        endTimer += delta;
        if (endTimer > 0.6f && newTouch) restart();
        return;
    }
    if (invulnerable > 0f) invulnerable -= delta;
    if (hitFlash > 0f) hitFlash -= delta;
    if (playerRecoil > 0f) playerRecoil -= delta;
    if (bossFleeMessageTimer > 0f) bossFleeMessageTimer -= delta;
    if (bossPhaseMessageTimer > 0f) bossPhaseMessageTimer -= delta;
    if (targetAcquiredTimer > 0f) targetAcquiredTimer -= delta;
    if (floorBannerTimer > 0f) floorBannerTimer -= delta;
    boolean stunned = isStunned();
    if (aimStick.dir.len() > DEAD_ZONE) facing = aimStick.dir.angleDeg();
    else if (moveStick.dir.len() > DEAD_ZONE) facing = moveStick.dir.angleDeg();
    if (moveStick.dir.len() > DEAD_ZONE) {
        float speedFactor = stunned ? STUN_SPEED_FACTOR : 1f;
        playerPos.x += moveStick.dir.x * PLAYER_SPEED * speedFactor * delta;
        playerPos.y += moveStick.dir.y * PLAYER_SPEED * speedFactor * delta;
    }
    dungeon.collide(playerPos, PLAYER_RADIUS);
    playerPos.x = MathUtils.clamp(playerPos.x, 0f, W);
    playerPos.y = MathUtils.clamp(playerPos.y, 0f, H);
    int exitDir = dungeon.checkDoorExit(playerPos);
    if (exitDir >= 0) {
        Room next = dungeon.moveThrough(exitDir, playerPos);
        if (next != null) enterRoom(next);
    }
    fireTimer -= delta;
    autoTarget = findNearestEnemy();
    if (autoAimActive && autoTarget != null && autoTarget != lastAutoTarget)
        targetAcquiredTimer = 1.5f;
    lastAutoTarget = autoTarget;
    boolean useManual = aimStick.dir.len() > DEAD_ZONE;
    boolean autoAimUsable = autoAimActive && !useManual && attention > 0f
            && autoTarget != null && !stunned;
    if (!stunned && fireTimer <= 0f) {
        if (useManual) {
            fireAt(aimStick.dir.angleDeg(), CHARGE_DMG_LVL1);
            fireTimer = FIRE_DELAY;
            playerRecoil = PLAYER_RECOIL_DURATION;
        } else if (autoAimUsable) {
            tmp.set(autoTarget.pos).sub(playerPos);
            if (tmp.len2() > 0.001f) {
                float fireAngle = tmp.angleDeg();
                facing = fireAngle;
                fireAt(fireAngle, getChargeDamage());
                fireTimer = FIRE_DELAY;
                playerRecoil = PLAYER_RECOIL_DURATION;
            }
        }
    }
    if (autoAimUsable && fireTimer > 0f) {
        attention -= ATTENTION_DRAIN * delta;
        if (attention < 0f) attention = 0f;
    } else if (autoAimActive && !useManual && moveStick.dir.len() < DEAD_ZONE) {
        attention += ATTENTION_REGEN * delta;
        if (attention > ATTENTION_MAX) attention = ATTENTION_MAX;
    }
    boolean moving = moveStick.dir.len() > DEAD_ZONE;
    if (moving || !autoAimActive) aimStability = 0f;
    else if (autoTarget != null && !stunned) {
        aimStability += delta / AIM_STABILITY_TIME;
        if (aimStability > 1f) aimStability = 1f;
    } else {
        aimStability -= delta * 0.5f;
        if (aimStability < 0f) aimStability = 0f;
    }
    if (aimStability >= 0.66f) chargeLevel = CHARGE_LEVEL_3;
    else if (aimStability >= 0.33f) chargeLevel = CHARGE_LEVEL_2;
    else chargeLevel = CHARGE_LEVEL_1;
    aimTrembleSeed += delta * 25f;
    float trembleAmt = (1f - aimStability) * AIM_TREMBLE_MAX;
    if (trembleAmt > 0.5f && autoAimActive) {
        aimTrembleX = MathUtils.sin(aimTrembleSeed * 1.3f) * trembleAmt
                    + MathUtils.sin(aimTrembleSeed * 2.7f) * trembleAmt * 0.5f;
        aimTrembleY = MathUtils.cos(aimTrembleSeed * 1.7f) * trembleAmt
                    + MathUtils.cos(aimTrembleSeed * 3.1f) * trembleAmt * 0.5f;
    } else { aimTrembleX = 0f; aimTrembleY = 0f; }
    updateArrows(delta);
    updateEnemies(delta);
    updateWeaponDrops(delta);
    updateChest(delta);
    if (chest != null && !chest.opened) {
        particles.spawnHalo(chest.pos.x, chest.pos.y, 45f, new Color(1f, 0.85f, 0.2f, 1f));
    }
    for (int i = 0; i < weaponDrops.size; i++) {
        WeaponDrop wd = weaponDrops.get(i);
        Color dropColor = wd.weapon.rarity.colorWithAlpha(1f);
        particles.spawnHalo(wd.pos.x, wd.pos.y, 25f, dropColor);
    }
    boolean unlocked = bossFledThisRun || lives <= 0;
    if (elevator != null) {
        elevator.update(delta, unlocked);
        updateElevatorButton(delta);
    }
    Room cur = dungeon.getCurrent();
    if (!victory && !gameOver && cur != null && !cur.cleared && ennemis.size == 0) {
        dungeon.markCleared();
        xpSystem.addXp(XP_PER_ROOM);
    }
}

private Vector2 findRandomFloorPos(float radius, float minDistFromPlayer) {
    Vector2 out = new Vector2();
    for (int i = 0; i < 60; i++) {
        float x = MathUtils.random(Room.WALL_T + 60f, W - Room.WALL_T - 60f);
        float y = MathUtils.random(Room.WALL_T + 60f, H - Room.WALL_T - 60f);
        if (dungeon.circleHitsWall(x, y, radius + 10f)) continue;
        if (out.dst2(playerPos) < minDistFromPlayer * minDistFromPlayer) continue;
        out.set(x, y);
        return out;
    }
    out.set(W / 2f, H / 2f);
    return out;
}

private void drawBossArrow() {
    Vector2 target = null;
    boolean isBoss = false;
    for (int i = 0; i < ennemis.size; i++) {
        Ennemi e = ennemis.get(i);
        if (e.boss && e.hp > 0 && !e.fleeing) {
            target = e.pos;
            isBoss = true;
            break;
        }
    }
    if (target == null) {
        Room cur = dungeon.getCurrent();
        if (cur != null) {
            float bestD = Float.MAX_VALUE;
            for (int dir = 0; dir < 4; dir++) {
                if (!cur.hasDoor(dir)) continue;
                com.badlogic.gdx.math.Rectangle r = new com.badlogic.gdx.math.Rectangle();
                cur.getDoorRect(dir, r);
                if (r == null) continue;
                float cx = r.x + r.width / 2f;
                float cy = r.y + r.height / 2f;
                float d = Vector2.dst2(playerPos.x, playerPos.y, cx, cy);
                if (d < bestD) { bestD = d; target = new Vector2(cx, cy); }
            }
        }
    }
    if (target == null) return;
    float cx = W / 2f;
    float cy = 110f;
    float size = 42f;
    float angle = MathUtils.atan2(target.y - playerPos.y, target.x - playerPos.x);
    float tipX = cx + MathUtils.cos(angle) * size;
    float tipY = cy + MathUtils.sin(angle) * size;
    float lx = cx + MathUtils.cos(angle + 2.4f) * size * 0.75f;
    float ly = cy + MathUtils.sin(angle + 2.4f) * size * 0.75f;
    float rx = cx + MathUtils.cos(angle - 2.4f) * size * 0.75f;
    float ry = cy + MathUtils.sin(angle - 2.4f) * size * 0.75f;
    uiShapes.setProjectionMatrix(renderer.uiCamera.combined);
    uiShapes.begin(ShapeRenderer.ShapeType.Filled);
    uiShapes.setColor(0f, 0f, 0f, 0.55f);
    uiShapes.circle(cx, cy, size + 6f, 24);
    if (isBoss) uiShapes.setColor(1f, 0.15f, 0.15f, 1f);
    else uiShapes.setColor(1f, 0.9f, 0.2f, 1f);
    uiShapes.triangle(tipX, tipY, lx, ly, rx, ry);
    uiShapes.end();
}
private void updateElevatorButton(float delta) {
    if (elevator == null || elevatorButton == null) return;
    elevatorButton.update(delta);
    if (!elevator.isUnlocked()) { elevatorButton.hide(); return; }
    float cx = elevator.getBounds().x + elevator.getBounds().width / 2f;
    float cy = elevator.getBounds().y + elevator.getBounds().height / 2f;
    if (playerPos.dst2(cx, cy) < ELEVATOR_INTERACT_DIST * ELEVATOR_INTERACT_DIST) {
        elevatorButton.show();
        elevatorButton.setLabel("MONTER");
    } else {
        elevatorButton.hide();
    }
}

private void tryUseElevator() {
    if (elevator == null || !elevator.isUnlocked()) return;
    if (elevator.tryActivate(playerPos, PLAYER_RADIUS)) {
        floorTransitionPending = true;
        transitionTimer = 0f;
        if (elevatorButton != null) elevatorButton.hide();
    }
}

private void updateArrows(float delta) {
    for (int i = fleches.size - 1; i >= 0; i--) {
        Fleche f = fleches.get(i);
        int steps = MathUtils.clamp((int) Math.ceil(ARROW_SPEED * delta / 8f), 1, 8);
        float sdt = delta / steps;
        boolean remove = false;
        for (int s = 0; s < steps; s++) {
            f.pos.x += f.vel.x * sdt;
            f.pos.y += f.vel.y * sdt;
            if (dungeon.circleHitsWall(f.pos.x, f.pos.y, ARROW_RADIUS)) {
                remove = true;
                break;
            }
        }
        f.life -= delta;
        if (f.fromEnemy && !remove) {
            float rr = PLAYER_RADIUS + ARROW_RADIUS;
            if (f.pos.dst2(playerPos) < rr * rr) {
                remove = true;
                if (invulnerable <= 0f) hurtPlayer();
            }
        }
        if (remove || f.life <= 0f || f.pos.x < -20 || f.pos.x > W + 20
                || f.pos.y < -20 || f.pos.y > H + 20) {
            fleches.removeIndex(i);
        }
    }
}

private void updateEnemies(float delta) {
    float normalSpeed = Math.min(80f + score * 1.5f, 200f);
    for (int i = ennemis.size - 1; i >= 0; i--) {
        Ennemi e = ennemis.get(i);
        boolean frozen = e.stunned > 0f;
        if (frozen) {
            e.stunned -= delta;
            if (Math.random() < 0.15f) particles.spawnLightning(e.pos.x, e.pos.y, 0f, 1);
        }
        if (e.hitPulse > 0f) e.hitPulse -= delta;
        if (e.attackPulse > 0f) e.attackPulse -= delta;

        if (!frozen && e.boss && e.fleeing) {
            e.fleeTimer += delta;
            e.pos.x -= 400f * delta;
            e.pos.y += 250f * delta;
            e.angle = 135f;
            if (Math.random() < 0.4f) particles.spawnSmoke(e.pos.x, e.pos.y, 1);
            if (e.fleeTimer > 2.5f || e.pos.x < -150f || e.pos.y > H + 150f) {
                ennemis.removeIndex(i);
                bossFledThisRun = true;
                continue;
            }
            continue;
        }

        if (!frozen && e.boss && !e.fleeing && e.hp > 0 && e.hp <= e.maxHp * 0.2f) {
            if (Math.random() < 0.5f * delta) {
                e.fleeing = true;
                e.fleeTimer = 0f;
                bossFleeMessageTimer = 3.5f;
                triggerShake();
                triggerStun();
                particles.spawnSmoke(e.pos.x, e.pos.y, 8);
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
                        MathUtils.clamp(playerPos.x + MathUtils.cosDeg(angle) * 150f, 80f, W - 80f),
                        MathUtils.clamp(playerPos.y + MathUtils.sinDeg(angle) * 150f, 80f, H - 80f)
                    );
                    e.teleportTimer = 0f;
                    particles.spawnLightning(e.pos.x, e.pos.y, 0f, 4);
                }
            }
            if (e.vengeanceBonuses != null
                    && e.vengeanceBonuses.contains(VengeanceBonus.SUMMONER, true)) {
                e.summonTimer += delta;
                if (e.summonTimer >= 10f) {
                    for (int k = 0; k < 2; k++) {
                        Ennemi sbire = new Ennemi();
                        sbire.hp = getEnemyHp();
                        sbire.maxHp = getEnemyHp();
                        float ang = MathUtils.random(0f, 360f);
                        float dist = MathUtils.random(80f, 150f);
                        sbire.pos.set(
                            MathUtils.clamp(e.pos.x + MathUtils.cosDeg(ang) * dist, 40f, W - 40f),
                            MathUtils.clamp(e.pos.y + MathUtils.sinDeg(ang) * dist, 40f, H - 40f)
                        );
                        ennemis.add(sbire);
                        particles.spawnPoison(sbire.pos.x, sbire.pos.y, 3);
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
                bossPhaseMessage = newPhase;
                bossPhaseMessageTimer = 2.5f;
                triggerShake();
                particles.spawnSmoke(e.pos.x, e.pos.y, 15);
                particles.spawnBossExplosion(e.pos.x, e.pos.y, new Color(1f, 0.5f, 0.1f, 1f));
            }
            if (e.phaseChangeFlash > 0f) e.phaseChangeFlash -= delta;

            if (e.phase >= 2) {
                e.attackCooldown -= delta;
                if (e.attackCooldown <= 0f) {
                    float cd = (e.phase == 2) ? BOSS_ATTACK_COOLDOWN_PHASE_2
                                              : BOSS_ATTACK_COOLDOWN_PHASE_3;
                    e.attackCooldown = cd;
                    Vector2 dir = playerPos.cpy().sub(e.pos).nor();
                    float baseAngle = dir.angleDeg();
                    if (e.phase == 2) {
                        spawnBossProjectile(e.pos.x, e.pos.y, baseAngle);
                    } else {
                        spawnBossProjectile(e.pos.x, e.pos.y, baseAngle - 20f);
                        spawnBossProjectile(e.pos.x, e.pos.y, baseAngle);
                        spawnBossProjectile(e.pos.x, e.pos.y, baseAngle + 20f);
                    }
                }
            }
            if (e.phase >= 3) {
                e.spawnCooldown -= delta;
                if (e.spawnCooldown <= 0f) {
                    e.spawnCooldown = BOSS_SPAWN_COOLDOWN;
                    for (int k = 0; k < 2; k++) {
                        Ennemi sbire = new Ennemi();
                        sbire.hp = getEnemyHp();
                        sbire.maxHp = getEnemyHp();
                        float ang = MathUtils.random(0f, 360f);
                        float dist = MathUtils.random(80f, 150f);
                        sbire.pos.set(
                            MathUtils.clamp(e.pos.x + MathUtils.cosDeg(ang) * dist, 40f, W - 40f),
                            MathUtils.clamp(e.pos.y + MathUtils.sinDeg(ang) * dist, 40f, H - 40f)
                        );
                        ennemis.add(sbire);
                        particles.spawnPoison(sbire.pos.x, sbire.pos.y, 3);
                    }
                }
            }
        }

        if (!frozen) {
            float ddx = playerPos.x - e.pos.x;
            float ddy = playerPos.y - e.pos.y;
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
                if (!dungeon.circleHitsWall(e.pos.x + sx, e.pos.y + sy, e.radius)) {
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
                    if (e.phase == 1) spd = BOSS_SPEED_PHASE_1;
                    else if (e.phase == 2) spd = BOSS_SPEED_PHASE_2;
                    else spd = BOSS_SPEED_PHASE_3;
                    if (e.berserker && e.hp <= e.maxHp * 0.5f) spd *= 1.3f;
                } else spd = normalSpeed;
                float sx = nx * spd * delta;
                float sy = ny * spd * delta;
                boolean moved = false;
                if (!dungeon.circleHitsWall(e.pos.x + sx, e.pos.y, e.radius)) {
                    e.pos.x += sx;
                    moved = true;
                }
                if (!dungeon.circleHitsWall(e.pos.x, e.pos.y + sy, e.radius)) {
                    e.pos.y += sy;
                    moved = true;
                }
                if (!moved) {
                    float px = -ny * spd * delta;
                    float py = nx * spd * delta;
                    if (!dungeon.circleHitsWall(e.pos.x + px, e.pos.y + py, e.radius)) {
                        e.pos.x += px;
                        e.pos.y += py;
                    } else if (!dungeon.circleHitsWall(e.pos.x - px, e.pos.y - py, e.radius)) {
                        e.pos.x -= px;
                        e.pos.y -= py;
                    }
                }
                dungeon.collide(e.pos, e.radius);
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
                if (!dungeon.circleHitsWall(e.pos.x + sx, e.pos.y + sy, e.radius)) {
                    e.pos.x += sx;
                    e.pos.y += sy;
                } else {
                    e.patrolTimer = 0f;
                }
                e.angle = (float) Math.toDegrees(Math.atan2(e.patrolDirY, e.patrolDirX));
            }
        }

        boolean mort = false;
        float rr = e.radius + ARROW_RADIUS;
        for (int j = fleches.size - 1; j >= 0; j--) {
            if (fleches.get(j).fromEnemy) continue;
            if (fleches.get(j).pos.dst2(e.pos) < rr * rr) {
                int arrowDmg = fleches.get(j).damage;
                fleches.removeIndex(j);
                if (e.shieldCharges > 0) {
                    e.shieldCharges--;
                    damage.add(e.pos.x, e.pos.y + e.size / 2f, "BLOQUE", false);
                    particles.spawnLightning(e.pos.x, e.pos.y, 0f, 3);
                    continue;
                }
                e.hp -= arrowDmg;
                e.hitPulse = ENEMY_HIT_PULSE_DURATION;
                damage.add(e.pos.x, e.pos.y + e.size / 2f, "-" + arrowDmg, false);
                if (e.hp <= 0) { mort = true; break; }
            }
        }

        if (mort) {
            if (e.boss && e.vengeanceBonuses != null
                    && e.vengeanceBonuses.contains(VengeanceBonus.EXPLOSIVE, true)) {
                for (Ennemi other : ennemis) {
                    if (other == e) continue;
                    if (other.pos.dst2(e.pos) < 200f * 200f) {
                        other.hp -= (int)(e.maxHp * 0.5f);
                    }
                }
                particles.spawnBossExplosion(e.pos.x, e.pos.y, new Color(1f, 0.3f, 0.1f, 1f));
            }
            Color pcolor = e.boss ? new Color(0.9f, 0.2f, 0.2f, 1f)
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
            }
            ennemis.removeIndex(i);
            setScore(score + (e.boss ? BOSS_SCORE : 1));
            currentFloor++;
        floorBannerTimer = 3f;
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
        }

        float prr = e.radius + PLAYER_RADIUS;
        if (!frozen && e.pos.dst2(playerPos) < prr * prr) {
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
                }
                tmp.set(e.pos).sub(playerPos);
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
}

private void spawnRoomEnemies(Room room) {
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
    }
    for (int i = 0; i < room.enemyCount; i++) {
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
}

private void hurtPlayer() {
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
}

private void updateWeaponDrops(float delta) {
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
}

private void pollTouches() {
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
            }
            if (x >= AIM_BTN_X && x <= AIM_BTN_X + AIM_BTN_SIZE
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
            } else if (x >= GLITCH_BTN_X && x <= GLITCH_BTN_X + GLITCH_BTN_SIZE
                    && y >= GLITCH_BTN_Y && y <= GLITCH_BTN_Y + GLITCH_BTN_SIZE) {
                if (glitchCooldown <= 0f) activateGlitchDash();
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
            } else {
                newTouch = true;
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
}
    private void disposeResources() {
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
