package com.ageofmayane.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

    // Taille d'affichage (hauteur en pixels du monde virtuel)
    static final float PLAYER_SIZE = 64f;
    static final float ENEMY_SIZE = 56f;
    static final float ARROW_SIZE = 20f;

    // Correction d'orientation des images (0 = l'image regarde vers la droite,
    // -90 = elle regarde vers le haut, 90 = vers le bas, 180 = vers la gauche)
    static final float PLAYER_ROT = 0f;
    static final float ENEMY_ROT = 0f;
    static final float ARROW_ROT = 0f;

    private final Game game;

    private FitViewport viewport;
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

    private float facing = 0f; // orientation du joueur en degres

    private int lives;
    private int score;
    private float fireTimer;
    private float spawnTimer;
    private float invulnerable;
    private float gameOverTimer;
    private boolean gameOver;

    public GameScreen() { this(null); }
    public GameScreen(Game game) { this.game = game; }

    @Override
    public void show() {
        viewport = new FitViewport(W, H);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        texPlayer = loadTexture("player.png");
        texEnemy = loadTexture("enemy.png");
        texArrow = loadTexture("arrow.png");

        restart();
    }

    // Charge une image ; renvoie null si elle est introuvable (le jeu
    // dessinera alors un cercle de couleur a la place).
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
        facing = 0f;
        lives = 5;
        score = 0;
        fireTimer = 0;
        spawnTimer = 1f;
        invulnerable = 0;
        gameOverTimer = 0;
        gameOver = false;
    }

    @Override
    public void render(float delta) {
        if (delta > 0.05f) delta = 0.05f;
        update(delta);
        draw();
    }

    private void update(float delta) {
        pollTouches();

        if (gameOver) {
            gameOverTimer += delta;
            if (gameOverTimer > 0.6f && Gdx.input.justTouched()) restart();
            return;
        }

        if (invulnerable > 0) invulnerable -= delta;

        // Orientation : visee en priorite, sinon deplacement
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
                score++;
                continue;
            }

            float pr = (ENEMY_RADIUS + PLAYER_RADIUS);
            if (e.pos.dst2(playerPos) < pr * pr) {
                ennemis.removeIndex(i);
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

    private void pollTouches() {
        if (moveStick.active() && !Gdx.input.isTouched(moveStick.pointer)) moveStick.end();
        if (aimStick.active() && !Gdx.input.isTouched(aimStick.pointer)) aimStick.end();

        for (int i = 0; i < 5; i++) {
            if (!Gdx.input.isTouched(i)) continue;
            tmp.set(Gdx.input.getX(i), Gdx.input.getY(i));
            viewport.unproject(tmp);
            float x = tmp.x;
            float y = tmp.y;

            if (i == moveStick.pointer) {
                moveStick.move(x, y);
            } else if (i == aimStick.pointer) {
                aimStick.move(x, y);
            } else if (x < W / 2f && !moveStick.active()) {
                moveStick.begin(i, x, y);
            } else if (x >= W / 2f && !aimStick.active()) {
                aimStick.begin(i, x, y);
            }
        }
    }

    // Dessine une texture centree en (cx, cy), de hauteur h, avec rotation.
    // La largeur suit les proportions de l'image.
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

        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        boolean playerVisible = !gameOver
                && (invulnerable <= 0f || ((int) (invulnerable * 10f)) % 2 == 0);

        // --- 1) Sprites (images PNG) ---
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

        // --- 2) Formes : cercles de secours (si image absente) + joysticks ---
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

        drawStick(moveStick, new Color(1f, 1f, 1f, 0.15f), new Color(1f, 1f, 1f, 0.45f));
        drawStick(aimStick, new Color(1f, 0.5f, 0.5f, 0.15f), new Color(1f, 0.5f, 0.5f, 0.45f));

        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.3f, 0.3f, 0.45f, 1f);
        shapes.rect(1, 1, W - 2, H - 2);
        shapes.end();

        // --- 3) Textes ---
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Score : " + score, 20, H - 20);
        font.draw(batch, "Vies : " + lives, W - 160, H - 20);
        if (gameOver) {
            font.setColor(1f, 0.4f, 0.4f, 1f);
            font.draw(batch, "GAME OVER", W / 2f - 100, H / 2f + 30);
            font.setColor(Color.WHITE);
            font.draw(batch, "Touche l'ecran pour rejouer", W / 2f - 220, H / 2f - 20);
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
    }

    @Override
    public void dispose() {
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