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

    // Boss
    static final int BOSS_HP = 20;
    static final float BOSS_SPEED = 60f;
    static final float BOSS_SIZE = ENEMY_SIZE * 2f;
    static final float BOSS_RADIUS = ENEMY_RADIUS * 2f;
    static final float BOSS_KNOCKBACK = 140f;
    static final int BOSS_SCORE = 5;

    // Apparition des ennemis
    static final float SPAWN_MIN_DIST = 280f;

    // Mini-carte (espace UI) : sous le score, en haut a gauche
    static final float MINIMAP_X = 20f;
    static final float MINIMAP_Y = H - 170f;

    // Proprietaires d'un toucher
    static final int MAX_POINTERS = 5;
    static final int OWNER_NONE = 0;
    static final int OWNER_MOVE = 1;
    static final int OWNER_AIM = 2;
    static final int OWNER_ZOOM = 3;
    static final int OWNER_IGNORED = 4;

    private final Game game;

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

    private final DungeonManager dungeon = new DungeonManager();

    private final Vector2 playerPos = new Vector2(W / 2f, H / 2f);
    private final Array<Fleche> fleches = new Array<Fleche>();
    private final Array<Ennemi> ennemis = new Array<Ennemi>();
    private final Stick moveStick = new Stick();
    private final Stick aimStick = new Stick();
    private final Vector2 tmp = new Vector2();
    private final Color bossTint = new Color(1f, 0.55f, 0.55f, 1f);
    private final int[] owner = new int[MAX_POINTERS];
    private boolean newTouch;

    private float facing = 0f;

    private int lives;
    private int score;
    private String scoreText = "";
    private String livesText = "";
    private float fireTimer;
    private float invulnerable;
    private float endTimer;
    private boolean gameOver;
    private boolean victory;

    public GameScreen() { this(null); }
    public GameScreen(Game game) { this.game = game; }

    // ---------------------------------------------------------------
    // Cycle de vie
    // ---------------------------------------------------------------
    @Override
    public void show() {
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

    /** Nouvelle partie : nouveau donjon, joueur au centre de la salle de depart. */
    private void restart() {
        dungeon.generate();

        playerPos.set(W / 2f, H / 2f);
        fleches.clear();
        ennemis.clear();
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

        // Securite : la salle de depart devrait etre deja nettoyee
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

        // --- Deplacement + collisions avec les murs ---
        if (moveStick.dir.len() > DEAD_ZONE) {
            playerPos.x += moveStick.dir.x * PLAYER_SPEED * delta;
            playerPos.y += moveStick.dir.y * PLAYER_SPEED * delta;
        }
        dungeon.collide(playerPos, PLAYER_RADIUS);
        playerPos.x = MathUtils.clamp(playerPos.x, 0f, W);
        playerPos.y = MathUtils.clamp(playerPos.y, 0f, H);

        // --- Passage d'une porte ---
        int exitDir = dungeon.checkDoorExit(playerPos);
        if (exitDir >= 0) {
            Room next = dungeon.moveThrough(exitDir, playerPos);
            if (next != null) enterRoom(next);
        }

        // --- Tir automatique ---
        fireTimer -= delta;
        if (aimStick.dir.len() > DEAD_ZONE && fireTimer <= 0f) {
            fireTimer = FIRE_DELAY;
            Fleche f = new Fleche();
            f.pos.set(playerPos);
            f.vel.set(aimStick.dir).nor().scl(ARROW_SPEED);
            f.angle = f.vel.angleDeg();
            fleches.add(f);
        }

        // --- Fleches (par petits pas pour ne pas traverser les murs) ---
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

        // --- Ennemis ---
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

            // Touche par une fleche ?
            boolean mort = false;
            float rr = e.radius + ARROW_RADIUS;
            for (int j = fleches.size - 1; j >= 0; j--) {
                if (fleches.get(j).pos.dst2(e.pos) < rr * rr) {
                    fleches.removeIndex(j);
                    e.hp--;
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

            // Contact avec le joueur
            float pr = e.radius + PLAYER_RADIUS;
            if (e.pos.dst2(playerPos) < pr * pr) {
                if (e.boss) {
                    if (invulnerable <= 0f) hurtPlayer();
                    // Le boss est repousse (il ne disparait pas)
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

        // --- Salle nettoyee : les portes s'ouvrent ---
        Room cur = dungeon.getCurrent();
        if (!victory && !gameOver && cur != null && !cur.cleared && ennemis.size == 0) {
            dungeon.markCleared();
        }
    }

    private void hurtPlayer() {
        setLives(lives - 1);
        invulnerable = 1f;
        if (lives <= 0) {
            gameOver = true;
            endTimer = 0f;
        }
    }

    // ---------------------------------------------------------------
    // Salles et ennemis
    // ---------------------------------------------------------------

    /** Appele quand le joueur vient d'entrer dans 'room'. */
    private void enterRoom(Room room) {
        fleches.clear();
        ennemis.clear();
        if (!room.cleared) spawnRoomEnemies(room);
    }

    /** Fait apparaitre les ennemis de la salle ; les portes restent fermees. */
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
            findSpawnPoint(e.pos, ENEMY_RADIUS);
            ennemis.add(e);
        }
        if (ennemis.size == 0) dungeon.markCleared();
    }

    /** Point libre (hors murs/obstacles) et assez loin du joueur. */
    private void findSpawnPoint(Vector2 out, float radius) {
        for (int i = 0; i < 40; i++) {
            float x = MathUtils.random(Room.WALL_T + 50f, W - Room.WALL_T - 50f);
            float y = MathUtils.random(Room.WALL_T + 50f, H - Room.WALL_T - 50f);
            out.set(x, y);
            if (out.dst2(playerPos) < SPAWN_MIN_DIST * SPAWN_MIN_DIST) continue;
            if (dungeon.circleHitsWall(x, y, radius + 10f)) continue;
            return;
        }
        // Repli : point oppose au joueur
        out.set(MathUtils.clamp(W - playerPos.x, 80f, W - 80f),
                MathUtils.clamp(H - playerPos.y, 80f, H - 80f));
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

        // ===== Camera du monde (zoomee, suit le joueur) =====
        zoom.applyToCamera(camera, playerPos.x, playerPos.y, W, H);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        boolean playerVisible = !gameOver
                && (invulnerable <= 0f || ((int) (invulnerable * 10f)) % 2 == 0);

        // --- 1) Salle : sol, murs, obstacles, portes ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        dungeon.drawRoom(shapes);
        shapes.end();

        // --- 2) Sprites ---
        batch.begin();
        batch.setColor(Color.WHITE);
        if (texEnemy != null) {
            for (int i = 0; i < ennemis.size; i++) {
                Ennemi e = ennemis.get(i);
                batch.setColor(e.boss ? bossTint : Color.WHITE);
                drawSprite(texEnemy, e.pos.x, e.pos.y, e.size, e.angle + ENEMY_ROT);
            }
            batch.setColor(Color.WHITE);
        }
        if (texArrow != null) {
            for (int i = 0; i < fleches.size; i++) {
                Fleche f = fleches.get(i);
                drawSprite(texArrow, f.pos.x, f.pos.y, ARROW_SIZE, f.angle + ARROW_ROT);
            }
        }
        if (texPlayer != null && playerVisible) {
            drawSprite(texPlayer, playerPos.x, playerPos.y, PLAYER_SIZE, facing + PLAYER_ROT);
        }
        batch.end();

        // --- 3) Cercles de secours + barre de vie du boss ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < ennemis.size; i++) {
            Ennemi e = ennemis.get(i);
            if (texEnemy == null) {
                if (e.boss) shapes.setColor(0.9f, 0.3f, 0.3f, 1f);
                else shapes.setColor(0.2f, 0.9f, 0.9f, 1f);
                shapes.circle(e.pos.x, e.pos.y, e.radius, 24);
            }
            if (e.boss) {
                float bw = 120f;
                float bx = e.pos.x - bw / 2f;
                float by = e.pos.y + e.size / 2f + 8f;
                float frac = MathUtils.clamp(e.hp / (float) e.maxHp, 0f, 1f);
                shapes.setColor(0.05f, 0.05f, 0.05f, 0.9f);
                shapes.rect(bx - 2f, by - 2f, bw + 4f, 12f);
                shapes.setColor(0.85f, 0.1f, 0.1f, 1f);
                shapes.rect(bx, by, bw * frac, 8f);
            }
        }
        if (texArrow == null) {
            shapes.setColor(1f, 0.9f, 0.3f, 1f);
            for (int i = 0; i < fleches.size; i++) {
                Fleche f = fleches.get(i);
                shapes.circle(f.pos.x, f.pos.y, ARROW_RADIUS, 10);
            }
        }
        if (texPlayer == null && playerVisible) {
            shapes.setColor(1f, 0.4f, 0.7f, 1f);
            shapes.circle(playerPos.x, playerPos.y, PLAYER_RADIUS, 24);
        }
        shapes.end();

        // ===== A partir d'ici : camera UI fixe =====
        uiViewport.apply();
        shapes.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        // --- 4) Mini-carte + joysticks ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        dungeon.drawMiniMap(shapes, MINIMAP_X, MINIMAP_Y);
        drawStick(moveStick, 1f, 1f, 1f);
        drawStick(aimStick, 1f, 0.5f, 0.5f);
        shapes.end();

        // --- 5) Boutons de zoom ---
        zoom.draw(shapes, batch, font);

        // --- 6) HUD texte ---
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, scoreText, 20, H - 20);
        font.draw(batch, livesText, W - 160, H - 20);
        if (victory) {
            font.setColor(1f, 0.85f, 0.2f, 1f);
            font.draw(batch, "VICTOIRE !", W / 2f - 110, H / 2f + 30);
            font.setColor(Color.WHITE);
            font.draw(batch, "Touche l'ecran pour rejouer", W / 2f - 220, H / 2f - 20);
        } else if (gameOver) {
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
        shapes.ci