package com.ageofmayane.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
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
    static final float BOSS_SPEED = 60f;
    static final float BOSS_SIZE = ENEMY_SIZE * 2f;
    static final float BOSS_RADIUS = ENEMY_RADIUS * 2f;
    static final float BOSS_KNOCKBACK = 140f;
    static final int BOSS_SCORE = 5;

    static final float SPAWN_MIN_DIST = 280f;

    static final int MAX_POINTERS = 5;
    static final int OWNER_NONE = 0;
    static final int OWNER_MOVE = 1;
    static final int OWNER_AIM = 2;
    static final int OWNER_ZOOM = 3;
    static final int OWNER_IGNORED = 4;

    private final Game game;
    private GameRenderer renderer;

    ZoomController zoom;
    final DungeonManager dungeon = new DungeonManager();
    final Vector2 playerPos = new Vector2(W / 2f, H / 2f);
    final Array<Fleche> fleches = new Array<Fleche>();
    final Array<Ennemi> ennemis = new Array<Ennemi>();
    final DamageNumbers damage = new DamageNumbers();
    final Stick moveStick = new Stick();
    final Stick aimStick = new Stick();
    float facing = 0f;
    float invulnerable;
    boolean gameOver;
    boolean victory;
    String scoreText = "";
    String livesText = "";

    private final Vector2 tmp = new Vector2();
    private final int[] owner = new int[MAX_POINTERS];
    private boolean newTouch;

    private int lives;
    private int score;
    private float fireTimer;
    private float endTimer;

    public GameScreen() { this(null); }
    public GameScreen(Game game) { this.game = game; }

    @Override
    public void show() {
        disposeResources();
        renderer = new GameRenderer();
        renderer.create();
        zoom = new ZoomController(W, H);
        restart();
    }

    private void restart() {
        dungeon.generate();

        playerPos.set(W / 2f, H / 2f);
        fleches.clear();
        ennemis.clear();
        damage.clear();
        moveStick.end();
        aimStick.end();
        for (int i = 0; i < MAX_POINTERS; i++) owner[i] = OWNER_NONE;
        facing = 0f;
        setLives(5);
        setScore(0);
        fireTimer = 0f;
        invulnerable = 0f;
        endTimer = 0f;
        gameOver = false;
        victory = false;

        Room cur = dungeon.getCurrent();
        if (cur != null && !cur.cleared) spawnRoomEnemies(cur);
    }

    private void setScore(int s) {
        score = s;
        scoreText = "Score : " + s;
    }

    private void setLives(int l) {
        lives = l;
        livesText = "Vies : " + l;
    }

    @Override
    public void render(float delta) {
        if (renderer == null || zoom == null) return;
        if (!(delta > 0f) || delta > 0.05f) delta = Math.min(Math.max(delta, 0f), 0.05f);
        update(delta);
        renderer.draw(this);
    }private void update(float delta) {
    pollTouches();
    zoom.update(delta);
    damage.update(delta);

    if (gameOver || victory) {
        endTimer += delta;
        if (endTimer > 0.6f && newTouch) restart();
        return;
    }

    if (invulnerable > 0f) invulnerable -= delta;

    if (aimStick.dir.len() > DEAD_ZONE) {
        facing = aimStick.dir.angleDeg();
    } else if (moveStick.dir.len() > DEAD_ZONE) {
        facing = moveStick.dir.angleDeg();
    }

    if (moveStick.dir.len() > DEAD_ZONE) {
        playerPos.x += moveStick.dir.x * PLAYER_SPEED * delta;
        playerPos.y += moveStick.dir.y * PLAYER_SPEED * delta;
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
    if (aimStick.dir.len() > DEAD_ZONE && fireTimer <= 0f) {
        fireTimer = FIRE_DELAY;
        Fleche f = new Fleche();
        f.pos.set(playerPos);
        f.vel.set(aimStick.dir).nor().scl(ARROW_SPEED);
        f.angle = f.vel.angleDeg();
        fleches.add(f);
    }

    updateArrows(delta);
    updateEnemies(delta);

    Room cur = dungeon.getCurrent();
    if (!victory && !gameOver && cur != null && !cur.cleared && ennemis.size == 0) {
        dungeon.markCleared();
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

        tmp.set(playerPos).sub(e.pos);
        if (tmp.len2() > 0.001f) {
            e.angle = tmp.angleDeg();
            float spd = e.boss ? BOSS_SPEED : normalSpeed;
            tmp.nor().scl(spd * delta);
            e.pos.add(tmp);
        }

        boolean mort = false;
        float rr = e.radius + ARROW_RADIUS;
        for (int j = fleches.size - 1; j >= 0; j--) {
            if (fleches.get(j).pos.dst2(e.pos) < rr * rr) {
                fleches.removeIndex(j);
                e.hp--;
                damage.add(e.pos.x, e.pos.y + e.size / 2f, "-1", false);
                if (e.hp <= 0) {
                    mort = true;
                    break;
                }
            }
        }
        if (mort) {
            ennemis.removeIndex(i);
            setScore(score + (e.boss ? BOSS_SCORE : 1));
            if (e.boss) {
                victory = true;
                endTimer = 0f;
            }
            continue;
        }

        float pr = e.radius + PLAYER_RADIUS;
        if (e.pos.dst2(playerPos) < pr * pr) {
            if (e.boss) {
                if (invulnerable <= 0f) hurtPlayer();
                tmp.set(e.pos).sub(playerPos);
                if (tmp.len2() < 0.001f) tmp.set(1f, 0f);
                tmp.nor().scl(BOSS_KNOCKBACK);
                e.pos.add(tmp);
                e.pos.x = MathUtils.clamp(e.pos.x,
                        Room.WALL_T + e.radius, W - Room.WALL_T - e.radius);
                e.pos.y = MathUtils.clamp(e.pos.y,
                        Room.WALL_T + e.radius, H - Room.WALL_T - e.radius);
            } else {
                ennemis.removeIndex(i);
                if (invulnerable <= 0f) hurtPlayer();
            }
        }
    }
}

private void hurtPlayer() {
    setLives(lives - 1);
    invulnerable = 1f;
    damage.add(playerPos.x, playerPos.y + 30f, "-1", true);
    if (lives <= 0) {
        gameOver = true;
        endTimer = 0f;
    }
}private void enterRoom(Room room) {
    fleches.clear();
    ennemis.clear();
    damage.clear();
    if (!room.cleared) spawnRoomEnemies(room);
}

private void spawnRoomEnemies(Room room) {
    if (room.boss) {
        Ennemi b = new Ennemi();
        b.boss = true;
        b.hp = BOSS_HP;
        b.maxHp = BOSS_HP;
        b.radius = BOSS_RADIUS;
        b.size = BOSS_SIZE;
        b.pos.set(W / 2f, H / 2f);
        ennemis.add(b);
    }
    for (int i = 0; i < room.enemyCount; i++) {
        Ennemi e = new Ennemi();
        e.hp = ENEMY_HP;
        e.maxHp = ENEMY_HP;
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
            if (zoom.handleTouch(x, y)) {
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
}    @Override
    public void resize(int width, int height) {
        if (renderer != null) renderer.resize(width, height);
    }

    private void disposeResources() {
        if (renderer != null) renderer.dispose();
        renderer = null;
        zoom = null;
    }

    @Override
    public void dispose() {
        disposeResources();
    }

    @Override
    public void hide() {
        disposeResources();
    }
}