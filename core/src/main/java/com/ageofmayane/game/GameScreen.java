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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

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

    // Proprietaires d'un toucher
    static final int MAX_POINTERS = 5;
    static final int OWNER_NONE = 0;
    static final int OWNER_MOVE = 1;
    static final int OWNER_AIM = 2;
    static final int OWNER_ZOOM = 3;
    static final int OWNER_IGNORED = 4;

    private final Game game;

    // Camera du MONDE (zoomee) et camera UI (fixe)
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private FitViewport viewport;
    private FitViewport uiViewport;

    private ZoomController zoom;

    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture texPlayer;
    private Texture texEnemy;
    private Texture texArrow;

    private final Vector2 playerPos = new Vector2(W / 2f, H / 2f);
    private final Array<Fleche> fleches = new Array<Fleche>();
    private final Array<Ennemi> ennemis = new Array<Ennemi>();
    private final Stick moveStick = new Stick();
    private final Stick aimStick = new Stick();
    private final Vector2 tmp = new Vector2();
    private final int[] owner = new int[MAX_POINTERS];
    private boolean newTouch;

    private float facing = 0f;

    private int lives;
    private int score;
    private String scoreText = "";
    private String livesText = "";
    private float fireTimer;
    private float spawnTimer;
    private float invulnerable;
    private float gameOverTimer;
    private boolean gameOver;

    public GameScreen() { this(null); }
    public GameScreen(Game game) { this.game = game; }

    // ---------------------------------------------------------------
    // Cycle de vie
    // ---------------------------------------------------------------
    @Override
    public void show() {
        // Securite : si show() est rappele, on libere d'abord l'ancien contenu
        disposeResources();

        camera = new OrthographicCamera();
        uiCamera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        uiCamera.setToOrtho(false, W, H);
        viewport = new FitViewport(W, H, camera);
        uiViewport = new FitViewport(W, H, uiCamera);

        int sw = Math.max(1, Gdx.graphics.getWidth());
        int sh = Math.max(1, Gdx.graphics.getHeight());
        viewport.update(sw, sh, true);
        uiViewport.update(sw, sh, true);

        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
        font.getRegion().getTexture().setFilter(
                Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        zoom = new ZoomController(W, H);

        texPlayer = loadTexture("player.png");
        texEnemy = loadTexture("enemy.png");
        texArrow = loadTexture("arrow.png");

        restart();
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

    private void restart() {
        playerPos.set(W / 2f, H / 2f);
        fleches.clear();
        ennemis.clear();
        moveStick.end();
        aimStick.end();
        for (int i = 0; i < MAX_POINTERS; i++) owner[i] = OWNER_NONE;
        facing = 0f;
        setLives(5);
        setScore(0);
        fireTimer = 0;
        spawnTimer = 1f;
        invulnerable = 0;
        gameOverTimer = 0;
        gameOver = false;
        // Le niveau de zoom est volontairement conserve entre les parties
    }

    private void setScore(int s) {
        score = s;
        scoreText = "Score : " + s;
    }

    private void setLives(int l) {
        lives = l;
        livesText = "Vies : " + l;
    }

    // ---------------------------------------------------------------
    // Boucle principale
    // ---------------------------------------------------------------
    @Override
    public void render(float delta) {
        if (viewport == null || shapes == null) return;
        if (!(delta > 0f) || delta > 0.05f) delta = Math.min(Math.max(delta, 0f), 0.05f);
        update(delta);
        draw();
    }

    private void update(float delta) {
        pollTouches();
        zoom.update(delta);

        if (gameOver) {
            gameOverTimer += delta;
            if (gameOverTimer > 0.6f && newTouch) restart();
            return;
        }

        if (invulnerable > 0) invulnerable -= delta;

        if (aimStick.dir.len() > DEAD_ZONE) {
            facing = aimStick.dir.angleDeg();
        } else if (moveStick.dir.len() > DEAD_ZONE) {
            facing = moveStick.dir.angleDeg();
        }

        if (moveStick.dir.len() > DEAD_ZONE) {
            playerPos.x += moveStick.dir.x * PLAYER_SPEED * delta;
            playerPos.y += moveStick.dir.y * PLAYER_SPEED * delta;
            playerPos.x = MathUtils.clamp(playerPos.x, PLAYER_RADIUS, W - PLAYER_RADIUS);
            playerPos.y = MathUtils.clamp(playerPos.y, PLAYER_RADIUS, H - PLAYER_RADIUS);
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

        spawnTimer -= delta;
        if (spawnTimer <= 0f) {
            spawnEnemy();
            spawnTimer = Math.max(0.35f, 1.3f - score * 0.02f);
        }

        for (int i = fleches.size - 1; i >= 0; i--) {
            Fleche f = fleches.get(i);
            f.pos.x += f.vel.x * delta;
            f.pos.y += f.vel.y * delta;
            f.life -= delta;
            if (f.life <= 0f || f.pos.x < -20 || f.pos.x > W + 20
                    || f.pos.y < -20 || f.pos.y > H + 20) {
                fleches.removeIndex(i);
            }
        }

        float enemySpeed = Math.min(80f + score * 1.5f, 200f);
        for (int i = ennemis.size - 1; i >= 0; i--) {
            Ennemi e = ennemis.get(i);
            tmp.set(playerPos).sub(e.pos);
            if (tmp.len2() > 0.001f) {
                e.angle = tmp.angleDeg();
                tmp.nor().scl(enemySpeed * delta);
                e.pos.add(tmp);
            }

            boolean mort = false;
            float rr = (ENEMY_RADIUS + ARROW_RADIUS);
            for (int j = fleches.size - 1; j >= 0; j--) {
                if (fleches.get(j).pos.dst2(e.pos) < rr * rr) {
                    fleches.removeIndex(j);
                    mort = true;
                    break;
                }
            }
            if (mort) {
                ennemis.removeIndex(i);
                setScore(score + 1);
                continue;
            }

            float pr = (ENEMY_RADIUS + PLAYER_RADIUS);
            if (e.pos.dst2(playerPos) < pr * pr) {
                ennemis.removeIndex(i);
                if (invulnerable <= 0f) {
                    setLives(lives - 1);
                    invulnerable = 1f;
                    if (lives <= 0) {
                        gameOver = true;
                        gameOverTimer = 0f;
                    }
                }
            }
        }
    }

    private void spawnEnemy() {
        Ennemi e = new Ennemi();
        int side = MathUtils.random(3);
        switch (side) {
            case 0: e.pos.set(MathUtils.random(W), -ENEMY_RADIUS); break;
            case 1: e.pos.set(MathUtils.random(W), H + ENEMY_RADIUS); break;
            case 2: e.pos.set(-ENEMY_RADIUS, MathUtils.random(H)); break;
            default: e.pos.set(W + ENEMY_RADIUS, MathUtils.random(H)); break;
        }
        ennemis.add(e);
    }

    // ---------------------------------------------------------------
    // Entrees tactiles (tout en espace UI, independant du zoom)
    // ---------------------------------------------------------------
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
            uiViewport.unproject(tmp);
            float x = tmp.x;
            float y = tmp.y;

            if (owner[i] == OWNER_NONE) {
                // Nouveau toucher : 1) bouton de zoom ? 2) sinon joystick
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
    }

    // ---------------------------------------------------------------
    // Affichage
    // ---------------------------------------------------------------
    private void drawSprite(Texture t, float cx, float cy, float h, float rotation) {
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
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Camera du monde : zoom + suivi du joueur
        zoom.applyToCamera(camera, playerPos.x, playerPos.y, W, H);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        boolean playerVisible = !gameOver
                && (invulnerable <= 0f || ((int) (invulnerable * 10f)) % 2 == 0);

        // --- 1) Sprites (camera zoomee) ---
        batch.begin();
        batch.setColor(Color.WHITE);
        if (texEnemy != null) {
            for (Ennemi e : ennemis) {
                drawSprite(texEnemy, e.pos.x, e.pos.y, ENEMY_SIZE, e.angle + ENEMY_ROT);
            }
        }
        if (texArrow != null) {
            for (Fleche f : fleches) {
                drawSprite(texArrow, f.pos.x, f.pos.y, ARROW_SIZE, f.angle + ARROW_ROT);
            }
        }
        if (texPlayer != null && playerVisible) {
            drawSprite(texPlayer, playerPos.x, playerPos.y, PLAYER_SIZE, facing + PLAYER_ROT);
        }
        batch.end();

        // --- 2) Cercles de secours (camera zoomee) ---
        boolean needFallback = texEnemy == null || texArrow == null
                || (texPlayer == null && playerVisible);
        if (needFallback) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            if (texEnemy == null) {
                shapes.setColor(0.2f, 0.9f, 0.9f, 1f);
                for (Ennemi e : ennemis) shapes.circle(e.pos.x, e.pos.y, ENEMY_RADIUS, 20);
            }
            if (texArrow == null) {
                shapes.setColor(1f, 0.9f, 0.3f, 1f);
                for (Fleche f : fleches) shapes.circle(f.pos.x, f.pos.y, ARROW_RADIUS, 10);
            }
            if (texPlayer == null && playerVisible) {
                shapes.setColor(1f, 0.4f, 0.7f, 1f);
                shapes.circle(playerPos.x, playerPos.y, PLAYER_RADIUS, 24);
            }
            shapes.end();
        }

        // --- 3) Contour de l'arene (camera zoomee) ---
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.3f, 0.3f, 0.45f, 1f);
        shapes.rect(1, 1, W - 2, H - 2);
        shapes.end();

        // ===== A partir d'ici : camera UI fixe =====
        uiViewport.apply();
        shapes.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        // Joysticks
        if (moveStick.active() || aimStick.active()) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            drawStick(moveStick, 1f, 1f, 1f);
            drawStick(aimStick, 1f, 0.5f, 0.5f);
            shapes.end();
        }

        // --- 4) Boutons de zoom (camera UI) ---
        zoom.draw(shapes, batch, font);

        // --- 5) HUD texte (camera UI) ---
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, scoreText, 20, H - 20);
        font.draw(batch, livesText, W - 160, H - 20);
        if (gameOver) {
            font.setColor(1f, 0.4f, 0.4f, 1f);
            font.draw(batch, "GAME OVER", W / 2f - 100, H / 2f + 30);
            font.setColor(Color.WHITE);
            font.draw(batch, "Touche l'ecran pour rejouer", W / 2f - 220, H / 2f - 20);
        }
        batch.end();
    }

    private void drawStick(Stick s, float r, float g, float b) {
        if (!s.active()) return;
        shapes.setColor(r, g, b, 0.15f);
        shapes.circle(s.origin.x, s.origin.y, STICK_RADIUS, 32);
        shapes.setColor(r, g, b, 0.45f);
        shapes.circle(s.current.x, s.current.y, 45f, 24);
    }

    @Override
    public void resize(int width, int height) {
        if (viewport == null || uiViewport == null) return;
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        viewport.update(w, h, true);
        uiViewport.update(w, h, true);
    }

    // ---------------------------------------------------------------
    // Liberation des ressources
    // ---------------------------------------------------------------
    private void disposeResources() {
        if (shapes != null) shapes.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (texPlayer != null) texPlayer.dispose();
        if (texEnemy != null) texEnemy.dispose();
        if (texArrow != null) texArrow.dispose();
        shapes = null;
        batch = null;
        font = null;
        texPlayer = null;
        texEnemy = null;
        texArrow = null;
        viewport = null;
        uiViewport = null;
        camera = null;
        uiCamera = null;
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

    // ---------------------------------------------------------------
    // Classes internes
    // ---------------------------------------------------------------
    static class Fleche {
        final Vector2 pos = new Vector2();
        final Vector2 vel = new Vector2();
        float angle = 0f;
        float life = 1.5f;
    }

    static class Ennemi {
        final Vector2 pos = new Vector2();
        float angle = 0f;
    }

    static class Stick {
        int pointer = -1;
        final Vector2 origin = new Vector2();
        final Vector2 current = new Vector2();
        final Vector2 dir = new Vector2();

        boolean active() { return pointer != -1; }

        void begin(int p, float x, float y) {
            pointer = p;
            origin.set(x, y);
            current.set(x, y);
            dir.setZero();
        }

        void move(float x, float y) {
            dir.set(x - origin.x, y - origin.y);
            float len = dir.len();
            if (len > STICK_RADIUS) {
                dir.scl(1f / len);
                current.set(origin).add(dir.x * STICK_RADIUS, dir.y * STICK_RADIUS);
            } else {
                current.set(x, y);
                dir.scl(1f / STICK_RADIUS);
            }
        }

        void end() {
            pointer = -1;
            dir.setZero();
        }
    }
}