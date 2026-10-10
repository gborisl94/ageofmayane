package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends ScreenAdapter {
    private static final float WORLD_W = 1280f;
    private static final float WORLD_H = 720f;

    private final AgeOfMayaneGame game;
    private OrthographicCamera camera;
    private ShapeRenderer sr;

    private Player player;
    private final Array<Enemy> enemies = new Array<>();
    private final Array<Arrow> arrows  = new Array<>();

    private float spawnTimer = 0f;
    private float spawnInterval = 1.1f;
    private float difficultyTimer = 0f;
    private int score = 0;
    private int wave = 1;
    private boolean gameOver = false;

    private final Vector2 leftStickOrigin  = new Vector2();
    private final Vector2 rightStickOrigin = new Vector2();
    private final Vector2 leftStickCur     = new Vector2();
    private final Vector2 rightStickCur    = new Vector2();
    private boolean leftActive = false, rightActive = false;

    public GameScreen(AgeOfMayaneGame game) { this.game = game; }

    @Override
    public void show() {
        sr = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_W, WORLD_H);
        reset();
    }

    private void reset() {
        player = new Player();
        player.position.set(WORLD_W / 2f, WORLD_H / 2f);
        enemies.clear();
        arrows.clear();
        spawnTimer = 0;
        spawnInterval = 1.1f;
        difficultyTimer = 0;
        score = 0;
        wave = 1;
        gameOver = false;
    }

    @Override
    public void render(float delta) {
        if (delta > 0.05f) delta = 0.05f;

        if (!gameOver) {
            handleInput(delta);
            update(delta);
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) reset();
        }

        Gdx.gl.glClearColor(Assets.BG.r, Assets.BG.g, Assets.BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        sr.setProjectionMatrix(camera.combined);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        renderGrid();
        for (Enemy e : enemies) e.render(sr);
        for (Arrow a : arrows)  a.render(sr);
        if (!gameOver) player.render(sr);
        sr.end();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.setColor(Assets.HUD);
        game.font.draw(game.batch, "SCORE  " + score, 20, WORLD_H - 20);
        game.font.draw(game.batch, "WAVE   " + wave,  20, WORLD_H - 60);
        game.font.draw(game.batch, "HP     " + player.hp, 20, WORLD_H - 100);
        if (gameOver) {
            game.font.draw(game.batch, "GAME OVER", WORLD_W / 2f - 90, WORLD_H / 2f + 20);
            game.font.draw(game.batch, "TAP TO RESTART", WORLD_W / 2f - 150, WORLD_H / 2f - 20);
        }
        game.batch.end();
    }

    private void renderGrid() {
        sr.setColor(Assets.GRID);
        for (float x = 0; x <= WORLD_W; x += 80f) sr.rectLine(x, 0, x, WORLD_H, 1f);
        for (float y = 0; y <= WORLD_H; y += 80f) sr.rectLine(0, y, WORLD_W, y, 1f);
    }

    private void handleInput(float delta) {
        Vector2 move = new Vector2();
        Vector2 aim  = new Vector2();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) move.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) move.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) move.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) move.x += 1;

        if (Gdx.input.isKeyPressed(Input.Keys.UP))    aim.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  aim.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  aim.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) aim.x += 1;

        int w = Gdx.graphics.getWidth();
        for (int i = 0; i < 2; i++) {
            if (!Gdx.input.isTouched(i)) {
                if (i == 0) leftActive = false; else rightActive = false;
                continue;
            }
            int tx = Gdx.input.getX(i);
            int ty = Gdx.input.getY(i);
            boolean leftSide = tx < w / 2;
            Vector3 world = camera.unproject(new Vector3(tx, ty, 0));

            if (leftSide && !rightActive) {
                if (!leftActive) { leftActive = true; leftStickOrigin.set(world.x, world.y); }
                leftStickCur.set(world.x, world.y);
                Vector2 d = leftStickCur.cpy().sub(leftStickOrigin);
                if (d.len() > 12f) move.set(d.nor());
            } else if (!leftSide && !leftActive) {
                if (!rightActive) { rightActive = true; rightStickOrigin.set(world.x, world.y); }
                rightStickCur.set(world.x, world.y);
                Vector2 d = rightStickCur.cpy().sub(rightStickOrigin);
                if (d.len() > 12f) aim.set(d.nor());
            }
        }

        if (move.len2() > 0) player.velocity.set(move.nor().scl(player.speed));
        else                player.velocity.setZero();

        if (aim.len2() > 0) {
            player.aim.set(aim.nor());
            if (player.canFire()) {
                arrows.add(new Arrow(player.position, player.aim));
                player.onFire();
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && player.canFire()) {
            arrows.add(new Arrow(player.position, player.aim));
            player.onFire();
        }
    }

    private void update(float delta) {
        player.update(delta);
        player.position.x = MathUtils.clamp(player.position.x, player.radius, WORLD_W - player.radius);
        player.position.y = MathUtils.clamp(player.position.y, player.radius, WORLD_H - player.radius);

        difficultyTimer += delta;
        if (difficultyTimer > 12f) {
            difficultyTimer = 0;
            wave++;
            spawnInterval = Math.max(0.35f, spawnInterval - 0.12f);
        }

        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) { spawnTimer = 0; spawnEnemy(); }

        for (Enemy e : enemies) e.update(delta, player.position);

        for (Arrow a : arrows) {
            a.update(delta);
            if (a.position.x < -20 || a.position.x > WORLD_W + 20
             || a.position.y < -20 || a.position.y > WORLD_H + 20) {
                a.alive = false;
                continue;
            }
            for (Enemy e : enemies) {
                if (e.hp > 0 && e.position.dst2(a.position) < (e.radius + a.radius) * (e.radius + a.radius)) {
                    e.hp = 0;
                    a.alive = false;
                    score += (e.type == Enemy.Type.FAST) ? 20 : 10;
                    break;
                }
            }
        }

        if (!player.isInvulnerable()) {
            for (Enemy e : enemies) {
                if (e.hp > 0 && e.position.dst2(player.position) < (e.radius + player.radius) * (e.radius + player.radius)) {
                    player.hp--;
                    player.invulnTimer = 1.5f;
                    if (player.hp <= 0) { gameOver = true; break; }
                }
            }
        }

        // --- Suppression des objets morts (compatible LibGDX Array) ---
        for (int i = arrows.size - 1; i >= 0; i--) {
            if (!arrows.get(i).alive) arrows.removeIndex(i);
        }
        for (int i = enemies.size - 1; i >= 0; i--) {
            if (enemies.get(i).hp <= 0) enemies.removeIndex(i);
        }
    }

    private void spawnEnemy() {
        int side = MathUtils.random(3);
        float x, y;
        switch (side) {
            case 0: x = MathUtils.random(WORLD_W); y = WORLD_H + 40; break;
            case 1: x = MathUtils.random(WORLD_W); y = -40;          break;
            case 2: x = -40;                       y = MathUtils.random(WORLD_H); break;
            default:x = WORLD_W + 40;              y = MathUtils.random(WORLD_H); break;
        }
        Enemy.Type type = (wave >= 2 && MathUtils.randomBoolean(0.25f))
                ? Enemy.Type.FAST : Enemy.Type.SLOW;
        enemies.add(new Enemy(x, y, type));
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, WORLD_W, WORLD_H);
    }

    @Override
    public void dispose() {
        sr.dispose();
    }
}