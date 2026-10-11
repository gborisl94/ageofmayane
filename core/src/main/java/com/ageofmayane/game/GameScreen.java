package com.ageofmayane.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen extends ScreenAdapter {

    static final float W = Room.ROOM_W;
    static final float H = Room.ROOM_H;

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

    static final float BOSS_RADIUS = 40f;
    static final float BOSS_SIZE = 96f;
    static final int   BOSS_HP = 20;

    private final Game game;

    private FitViewport viewport;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;

    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture texPlayer;
    private Texture texEnemy;
    private Texture texArrow;
    private Texture texBoss;

    private ZoomController zoomController;
    private DungeonManager dungeonManager;
    private DungeonRenderer dungeonRenderer;

    private final Vector2 playerPos = new Vector2(W / 2f, H / 2f);
    private float facing = 0f;
    private int lives;
    private int score;
    private float fireTimer;
    private float invulnerable;
    private float gameOverTimer;
    private boolean gameOver;
    private boolean victory;

    private final Array<Fleche> fleches = new Array<Fleche>();
    private final Array<Ennemi> ennemis = new Array<Ennemi>();

    private final Stick moveStick = new Stick();
    private final Stick aimStick = new Stick();

    private final Vector2 tmp = new Vector2();
    private final Vector3 tmp3 = new Vector3();
    private final Rectangle doorRectTmp = new Rectangle();

    private float spawnTimer = 0f;
    private boolean roomSpawned = false;

    public GameScreen() { this(null); }
    public GameScreen(Game game) { this.game = game; }

    @Override
    public void show() {
        viewport = new FitViewport(W, H);
        camera = (OrthographicCamera) viewport.getCamera();

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, W, H);

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        texPlayer = loadTexture("player.png");
        texEnemy  = loadTexture("enemy.png");
        texArrow  = loadTexture("arrow.png");
        texBoss   = loadTexture("boss.png");

        zoomController   = new ZoomController(W, H);
        dungeonManager   = new DungeonManager();
        dungeonRenderer  = new DungeonRenderer();

        newGame();
    }

    private Texture loadTexture(String name) {
        try {
            Texture t = new Texture(Gdx.files.internal(name));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        } catch (Throwable e) {
            Gdx.app.error("Mayane", "Texture introuvable : " + name, e);
            return null;
        }
    }

    private void newGame() {
        dungeonManager.generate();
        resetRoom();
        lives = 5;
        score = 0;
        gameOver = false;
        victory = false;
        gameOverTimer = 0;
        zoomController.reset();
    }

    private void resetRoom() {
        playerPos.set(W / 2f, H / 2f);
        facing = 0f;
        fireTimer = 0;
        invulnerable = 0;
        fleches.clear();
        ennemis.clear();
        moveStick.end();
        aimStick.end();
        roomSpawned = false;
        spawnTimer = 0f;
    }@Override
public void render(float delta) {
    if (delta > 0.05f) delta = 0.05f;
    update(delta);
    draw();
}

private void update(float delta) {
    pollTouches(delta);

    if (gameOver || victory) {
        gameOverTimer += delta;
        if (gameOverTimer > 0.6f && Gdx.input.justTouched()) {
            newGame();
        }
        return;
    }

    if (invulnerable > 0) invulnerable -= delta;

    Room current = dungeonManager.getCurrent();
    if (current == null) return;

    if (!current.cleared && !current.boss && !roomSpawned) {
        spawnRoomEnemies(current);
        roomSpawned = true;
    }
    if (current.boss && !current.cleared && !roomSpawned) {
        spawnBoss(current);
        roomSpawned = true;
    }

    if (aimStick.dir.len() > DEAD_ZONE) {
        facing = aimStick.dir.angleDeg();
    } else if (moveStick.dir.len() > DEAD_ZONE) {
        facing = moveStick.dir.angleDeg();
    }

    if (moveStick.dir.len() > DEAD_ZONE) {
        playerPos.x += moveStick.dir.x * PLAYER_SPEED * delta;
        playerPos.y += moveStick.dir.y * PLAYER_SPEED * delta;
    }
    dungeonManager.collide(playerPos, PLAYER_RADIUS);

    fireTimer -= delta;
    if (aimStick.dir.len() > DEAD_ZONE && fireTimer <= 0f) {
        fireTimer = FIRE_DELAY;
        Fleche f = new Fleche();
        f.pos.set(playerPos);
        f.vel.set(aimStick.dir).nor().scl(ARROW_SPEED);
        f.angle = f.vel.angleDeg();
        fleches.add(f);
    }

    for (int i = fleches.size - 1; i >= 0; i--) {
        Fleche f = fleches.get(i);
        f.pos.x += f.vel.x * delta;
        f.pos.y += f.vel.y * delta;
        f.life -= delta;

        if (f.life <= 0f
                || f.pos.x < -20 || f.pos.x > W + 20
                || f.pos.y < -20 || f.pos.y > H + 20
                || dungeonManager.circleHitsWall(f.pos.x, f.pos.y, ARROW_RADIUS)) {
            fleches.removeIndex(i);
        }
    }

    float enemySpeed = MathUtils.clamp(80f + score * 1.5f, 80f, 200f);
    for (int i = ennemis.size - 1; i >= 0; i--) {
        Ennemi e = ennemis.get(i);
        tmp.set(playerPos).sub(e.pos);
        if (tmp.len2() > 0.001f) {
            e.angle = tmp.angleDeg();
            tmp.nor().scl(e.speed > 0 ? e.speed : enemySpeed).scl(delta);
            e.pos.add(tmp);
        }

        float rr = (ENEMY_RADIUS + ARROW_RADIUS);
        for (int j = fleches.size - 1; j >= 0; j--) {
            if (fleches.get(j).pos.dst2(e.pos) < rr * rr) {
                fleches.removeIndex(j);
                e.hp--;
                if (e.hp <= 0) {
                    ennemis.removeIndex(i);
                    score++;
                    if (e.isBoss && dungeonManager.isCurrentBossRoom()) {
                        victory = true;
                        gameOverTimer = 0;
                    }
                }
                break;
            }
        }
        if (i >= ennemis.size) continue;
        if (e.hp <= 0) continue;

        float pr = (ENEMY_RADIUS + PLAYER_RADIUS);
        if (e.pos.dst2(playerPos) < pr * pr) {
            if (invulnerable <= 0f) {
                lives--;
                invulnerable = 1f;
                if (lives <= 0) {
                    gameOver = true;
                    gameOverTimer = 0f;
                }
            }
        }
    }

    if (!current.cleared && ennemis.size == 0 && roomSpawned) {
        dungeonManager.markCleared();
    }

    int exit = dungeonManager.checkDoorExit(playerPos);
    if (exit >= 0) {
        dungeonManager.moveThrough(exit, playerPos);
        fleches.clear();
        ennemis.clear();
        moveStick.end();
        aimStick.end();
        roomSpawned = false;
    }
}

private void spawnRoomEnemies(Room r) {
    int count = Math.max(3, Math.min(7, r.distance + 3));
    for (int i = 0; i < count; i++) {
        Ennemi e = new Ennemi();
        e.hp = 1;
        e.speed = 0;
        float x, y;
        int tries = 0;
        do {
            x = MathUtils.random(Room.WALL_T + ENEMY_RADIUS + 10f, W - Room.WALL_T - ENEMY_RADIUS - 10f);
            y = MathUtils.random(Room.WALL_T + ENEMY_RADIUS + 10f, H - Room.WALL_T - ENEMY_RADIUS - 10f);
            tries++;
        } while (tries < 30 && (tmp.set(x, y).dst(playerPos) < 250f));
        e.pos.set(x, y);
        ennemis.add(e);
    }
}

private void spawnBoss(Room r) {
    Ennemi boss = new Ennemi();
    boss.isBoss = true;
    boss.hp = BOSS_HP;
    boss.speed = 60f;
    boss.pos.set(W / 2f, H / 2f + 200f);
    ennemis.add(boss);
}

private void pollTouches(float delta) {
    if (moveStick.active() && !Gdx.input.isTouched(moveStick.pointer)) moveStick.end();
    if (aimStick.active() && !Gdx.input.isTouched(aimStick.pointer)) aimStick.end();

    zoomController.resetFrame();

    for (int i = 0; i < 5; i++) {
        if (!Gdx.input.isTouched(i)) continue;

        tmp3.set(Gdx.input.getX(i), Gdx.input.getY(i), 0f);
        uiCamera.unproject(tmp3);
        if (zoomController.handleTouch(tmp3.x, tmp3.y, delta)) {
            continue;
        }

        tmp.set(Gdx.input.getX(i), Gdx.input.getY(i));
        viewport.unproject(tmp);
        float x = tmp.x;
        float y = tmp.y;

        if (i == moveStick.pointer) {
            moveStick.move(x, y, STICK_RADIUS);
        } else if (i == aimStick.pointer) {
            aimStick.move(x, y, STICK_RADIUS);
        } else if (x < W / 2f && !moveStick.active()) {
            moveStick.begin(i, x, y);
        } else if (x >= W / 2f && !aimStick.active()) {
            aimStick.begin(i, x, y);
        }
    }
}    private void drawSprite(Texture t, float cx, float cy, float h, float rotation) {
        if (t == null) return;
        float w = h * t.getWidth() / (float) t.getHeight();
        batch.draw(t,
                cx - w / 2f, cy - h / 2f,
                w / 2f, h / 2f,
                w, h,
                1f, 1f,
                rotation,
                0, 0, t.getWidth(), t.getHeight(),
                false, false);
    }

    private void draw() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        zoomController.applyToCamera(camera);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Room current = dungeonManager.getCurrent();
        boolean playerVisible = !gameOver && !victory
                && (invulnerable <= 0f || ((int) (invulnerable * 10f)) % 2 == 0);

        Rectangle doorRectLocal = doorRectTmp;
        batch.begin();
        boolean solOk   = current != null && dungeonRenderer.renderSol(batch, current);
        boolean murOk   = current != null && dungeonRenderer.renderMurs(batch, current, !current.cleared, doorRectLocal);
        boolean porteOk = current != null && dungeonRenderer.renderPortes(batch, current, doorRectLocal);
        batch.end();

        if (!solOk || !murOk || !porteOk) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            if (current != null) dungeonManager.drawRoom(shapes);
            shapes.end();
        }

        batch.begin();
        batch.setColor(Color.WHITE);

        for (Ennemi e : ennemis) {
            if (!e.isBoss && texEnemy != null) {
                drawSprite(texEnemy, e.pos.x, e.pos.y, ENEMY_SIZE, e.angle + ENEMY_ROT);
            }
        }
        for (Ennemi e : ennemis) {
            if (e.isBoss && texBoss != null) {
                drawSprite(texBoss, e.pos.x, e.pos.y, BOSS_SIZE, e.angle);
            }
        }
        if (texArrow != null) {
            for (Fleche f : fleches) {
                drawSprite(texArrow, f.pos.x, f.pos.y, ARROW_SIZE, f.angle + ARROW_ROT);
            }
        }
        if (playerVisible && texPlayer != null) {
            drawSprite(texPlayer, playerPos.x, playerPos.y, PLAYER_SIZE, facing + PLAYER_ROT);
        }
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (texEnemy == null) {
            shapes.setColor(0.2f, 0.9f, 0.9f, 1f);
            for (Ennemi e : ennemis) if (!e.isBoss) shapes.circle(e.pos.x, e.pos.y, ENEMY_RADIUS, 20);
        }
        if (texBoss == null) {
            shapes.setColor(1f, 0.2f, 0.2f, 1f);
            for (Ennemi e : ennemis) if (e.isBoss) shapes.circle(e.pos.x, e.pos.y, BOSS_RADIUS, 32);
        }
        if (texArrow == null) {
            shapes.setColor(1f, 0.9f, 0.3f, 1f);
            for (Fleche f : fleches) shapes.circle(f.pos.x, f.pos.y, ARROW_RADIUS, 10);
        }
        if (playerVisible && texPlayer == null) {
            shapes.setColor(1f, 0.4f, 0.7f, 1f);
            shapes.circle(playerPos.x, playerPos.y, PLAYER_RADIUS, 24);
        }

        drawStick(moveStick, new Color(1f, 1f, 1f, 0.15f), new Color(1f, 1f, 1f, 0.45f));
        drawStick(aimStick, new Color(1f, 0.5f, 0.5f, 0.15f), new Color(1f, 0.5f, 0.5f, 0.45f));
        shapes.end();

        uiCamera.update();
        shapes.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        dungeonManager.drawMiniMap(shapes, 20f, H - 20f - 5 * 20f);
        shapes.end();

        zoomController.draw(shapes, batch, font);

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Score : " + score, 20, H - 20);
        font.draw(batch, "Vies : " + lives, W - 160, H - 20);

        if (gameOver) {
            font.setColor(1f, 0.4f, 0.4f, 1f);
            font.draw(batch, "GAME OVER", W / 2f - 100, H / 2f + 30);
            font.setColor(Color.WHITE);
            font.draw(batch, "Touche l'ecran pour rejouer", W / 2f - 220, H / 2f - 20);
        } else if (victory) {
            font.setColor(0.4f, 1f, 0.4f, 1f);
            font.draw(batch, "VICTOIRE !", W / 2f - 90, H / 2f + 30);
            font.setColor(Color.WHITE);
            font.draw(batch, "Touche l'ecran pour un nouveau donjon", W / 2f - 300, H / 2f - 20);
        }
        batch.end();
    }

    private void drawStick(Stick s, Color base, Color knob) {
        if (!s.active()) return;
        shapes.setColor(base);
        shapes.circle(s.origin.x, s.origin.y, STICK_RADIUS, 32);
        shapes.setColor(knob);
        shapes.circle(s.current.x, s.current.y, 45f, 24);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiCamera.setToOrtho(false, W, H);
    }

    @Override
    public void dispose() {
        if (shapes != null) shapes.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (texPlayer != null) texPlayer.dispose();
        if (texEnemy != null) texEnemy.dispose();
        if (texArrow != null) texArrow.dispose();
        if (texBoss != null) texBoss.dispose();
        if (dungeonRenderer != null) dungeonRenderer.dispose();
        shapes = null;
        batch = null;
        font = null;
        texPlayer = null;
        texEnemy = null;
        texArrow = null;
        texBoss = null;
    }

    @Override
    public void hide() {
        dispose();
    }

    static class Fleche {
        final Vector2 pos = new Vector2();
        final Vector2 vel = new Vector2();
        float angle = 0f;
        float life = 1.5f;
    }

    static class Ennemi {
        final Vector2 pos = new Vector2();
        float angle = 0f;
        int hp = 1;
        float speed = 0f;
        boolean isBoss = false;
    }
}