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
    static final float ARC_BTN_X = 20f;
    static final float ARC_BTN_Y = 350f;
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
static final float GLITCH_BTN_Y = H - 460f - GLITCH_BTN_SIZE * 2 - 40f;
static final float LUNE_COOLDOWN = 4f;
static final float LUNE_SPEED = 480f;
static final float LUNE_BTN_SIZE = 110f;
static final float LUNE_BTN_X = W - LUNE_BTN_SIZE - 30f;
static final float LUNE_BTN_Y = H - 460f - LUNE_BTN_SIZE * 3 - 60f;

private final Game game;
GameRenderer renderer;

ZoomController zoom;
XpSystem xpSystem;
final DungeonManager dungeon = new DungeonManager();
final Vector2 playerPos = new Vector2(W / 2f, H / 2f);
final Array<Fleche> fleches = new Array<Fleche>();
final Array<Ennemi> ennemis = new Array<Ennemi>();
final Array<WeaponDrop> weaponDrops = new Array<WeaponDrop>();
final Array<FlecheDrop> flecheDrops = new Array<FlecheDrop>();
final DamageNumbers damage = new DamageNumbers();
final ParticleSystem particles = new ParticleSystem();
final Stick moveStick = new Stick();
final Stick aimStick = new Stick();
Weapon equippedWeapon;
Chest chest;
int nbFleches = 50;
boolean arcBtnActive = false;
float arcBtnTimer = 0f;
int arcBtnNiveau = 0;
int arcBtnPointer = -1;
boolean arcTendu = false;
float arcAngle = 0f;
static final int NB_FLECHES_MAX = 50;

final Array<FlecheSol> flechesSol = new Array<FlecheSol>();
float regenFlechesTimer = 0f;
final Objectifs objectifs = new Objectifs();
float facing = 0f;
boolean arcTendu = false;
float arcAngle = 0f;
boolean aimStickWasActive = false;
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

final Vector2 tmp = new Vector2();
final int[] owner = new int[MAX_POINTERS];
boolean newTouch;

int lives;
int score;
float fireTimer;
float endTimer;

final VengeanceManager vengeance = VengeanceManager.get();
Elevator elevator;
ElevatorButton elevatorButton;
ShapeRenderer uiShapes;
SpriteBatch uiBatch;
BitmapFont uiFont;
boolean floorTransitionPending = false;
float glitchCooldown = 0f;
float luneCooldown = 0f;
int luneLevel = 1;
int luneKills = 0;
float glitchEffectTimer = 0f;
float transitionTimer = 0f;

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
    flecheDrops.clear();
    ennemis.clear();
    weaponDrops.clear();
    damage.clear();
    particles.clear();
    moveStick.end();
    aimStick.end();
    for (int i = 0; i < MAX_POINTERS; i++) owner[i] = OWNER_NONE;
    facing = 0f;
arcTendu = false;
arcAngle = 0f;
aimStickWasActive = false;
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
    Vector2 chestPos = GameFloor.findRandomFloorPos(this, 40f, 200f);
    chest = new Chest(chestPos.x, chestPos.y);
    nbFleches = 50;
    vengeance.clear();
    floorTransitionPending = false;
    transitionTimer = 0f;
    if (elevatorButton != null) elevatorButton.hide();
    Vector2 elevPos = GameFloor.findRandomFloorPos(this, 60f, 250f);
    elevator = new Elevator(elevPos.x - 40f, elevPos.y - 60f, 80f, 120f);
    Room cur = dungeon.getCurrent();
    if (cur != null && !cur.cleared) GameEnemies.spawnRoomEnemies(this, cur);
}

void setScore(int s) { score = s; scoreText = "Score : " + s; }
void setLives(int l) { lives = l; livesText = "Vies : " + l; }
int getLives() { return lives; }
int getScore() { return score; }

void triggerShake() { shakeTime = SHAKE_DURATION; }
void triggerStun() { stunTimer = STUN_DURATION; }
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

void openChest() {
    if (chest == null || chest.opened) return;
    chest.open();
    objectifs.incrCoffre();
    Weapon loot = WeaponDatabase.randomChestLoot();
    weaponDrops.add(new WeaponDrop(chest.pos.x, chest.pos.y + 50f, loot));
}
@Override
public void render(float delta) {
    if (renderer == null || zoom == null) return;
    if (!(delta > 0f) || delta > 0.05f) delta = Math.min(Math.max(delta, 0f), 0.05f);
    update(delta);
    renderer.draw(this);
    GameFloor.drawBossArrow(this);
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
            GameFloor.nextFloor(this);
        }
        return;
    }
    GameInput.pollTouches(this);
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
    if (luneCooldown > 0f) luneCooldown -= delta;
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
    
    for (int i = flechesSol.size - 1; i >= 0; i--) {
    FlecheSol fs = flechesSol.get(i);
    if (fs.pos.dst2(playerPos) < 60f * 60f) {
        if (nbFleches < 50) {
            nbFleches++;
            particles.spawnLightning(fs.pos.x, fs.pos.y, 0f, 2);
        }
        flechesSol.removeIndex(i);
    }
    }
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
        if (next != null) GameFloor.enterRoom(this, next);
    }
    fireTimer -= delta;
    boolean aimActiveNow = aimStick.active();
if (aimActiveNow && !aimStickWasActive) {
    arcTendu = true;
    arcAngle = facing;
}
if (arcTendu && aimStick.dir.len() > DEAD_ZONE) {
    arcAngle = aimStick.dir.angleDeg();
    facing = arcAngle;
}
if (!aimActiveNow && aimStickWasActive && arcTendu) {
    if (nbFleches > 0) {
        GameCombat.fireAt(this, arcAngle, GameCombat.getChargeDamage(this));
        playerRecoil = PLAYER_RECOIL_DURATION;
    }
    arcTendu = false;
}
aimStickWasActive = aimActiveNow;
    autoTarget = GameCombat.findNearestEnemy(this);
    if (autoAimActive && autoTarget != null && autoTarget != lastAutoTarget)
        targetAcquiredTimer = 1.5f;
    lastAutoTarget = autoTarget;
    boolean useManual = aimStick.dir.len() > DEAD_ZONE;
    boolean autoAimUsable = autoAimActive && !useManual && attention > 0f
            && autoTarget != null && !stunned;
    if (!stunned && fireTimer <= 0f && !arcTendu) {
        if (useManual) {
            GameCombat.fireAt(this, aimStick.dir.angleDeg(), CHARGE_DMG_LVL1);
            fireTimer = FIRE_DELAY;
            playerRecoil = PLAYER_RECOIL_DURATION;
        } else if (autoAimUsable) {
            tmp.set(autoTarget.pos).sub(playerPos);
            if (tmp.len2() > 0.001f) {
                float fireAngle = tmp.angleDeg();
                facing = fireAngle;
                GameCombat.fireAt(this, fireAngle, GameCombat.getChargeDamage(this));
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
    GameCombat.updateArrows(this, delta);
    GameEnemies.updateEnemies(this, delta);
    updateWeaponDrops(delta);
    updateChest(delta);
    if (chest != null && !chest.opened) {
        particles.spawnHalo(chest.pos.x, chest.pos.y, 45f, new Color(1f, 0.85f, 0.2f, 0.3f));
    }
    for (int i = 0; i < weaponDrops.size; i++) {
        WeaponDrop wd = weaponDrops.get(i);
        Color dropColor = wd.weapon.rarity.colorWithAlpha(0.4f);
        particles.spawnHalo(wd.pos.x, wd.pos.y, 25f, dropColor);
    }
    boolean unlocked = bossFledThisRun || lives <= 0;
    if (elevator != null) {
        elevator.update(delta, unlocked);
        GameFloor.updateElevatorButton(this, delta);
    }
    Room cur = dungeon.getCurrent();
    if (!victory && !gameOver && cur != null && !cur.cleared && ennemis.size == 0) {
        dungeon.markCleared();
        xpSystem.addXp(XP_PER_ROOM);
    }
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
void tryUseElevator() { GameFloor.tryUseElevator(this); }
void activateStunAbility() { GameAbilities.activateStunAbility(this); }
void activateGlitchDash() { GameAbilities.activateGlitchDash(this); }
void activateAutoAimPower() { GameAbilities.activateAutoAimPower(this); }
void deactivateAutoAimPower() { GameAbilities.deactivateAutoAimPower(this); }
boolean isAutoAimReady() { return GameAbilities.isAutoAimReady(this); }
float getAutoAimDurationRatio() { return GameAbilities.getAutoAimDurationRatio(this); }
float getAutoAimCooldownRatio() { return GameAbilities.getAutoAimCooldownRatio(this); }
}
