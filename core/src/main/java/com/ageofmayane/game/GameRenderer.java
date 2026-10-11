package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Tout l'affichage : cameras, viewports, textures, dessin du monde et de l'UI. */
class GameRenderer {

    static final float W = GameScreen.W;
    static final float H = GameScreen.H;

    static final float MINIMAP_X = 20f;
    static final float MINIMAP_Y = H - 170f;

    OrthographicCamera camera;
    OrthographicCamera uiCamera;
    FitViewport viewport;
    FitViewport uiViewport;

    ShapeRenderer shapes;
    SpriteBatch batch;
    BitmapFont font;

    Texture texPlayer;
    Texture texEnemy;
    Texture texArrow;

    private final Color bossTint = new Color(1f, 0.55f, 0.55f, 1f);

    void create() {
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

        texPlayer = loadTexture("player.png");
        texEnemy = loadTexture("enemy.png");
        texArrow = loadTexture("arrow.png");
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

    void resize(int width, int height) {
        if (viewport == null || uiViewport == null) return;
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        viewport.update(w, h, true);
        uiViewport.update(w, h, true);
    }

    void dispose() {
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
    }

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

    private void drawStick(Stick s, float r, float g, float b) {
        if (!s.active()) return;
        shapes.setColor(r, g, b, 0.15f);
        shapes.circle(s.origin.x, s.origin.y, GameScreen.STICK_RADIUS, 32);
        shapes.setColor(r, g, b, 0.45f);
        shapes.circle(s.current.x, s.current.y, 45f, 24);
    }

    void draw(GameScreen g) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // ===== Camera du monde (zoomee, suit le joueur) =====
        g.zoom.applyToCamera(camera, g.playerPos.x, g.playerPos.y, W, H);
        viewport.apply();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        boolean playerVisible = !g.gameOver
                && (g.invulnerable <= 0f || ((int) (g.invulnerable * 10f)) % 2 == 0);

        // 1) Salle
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        g.dungeon.drawRoom(shapes);
        shapes.end();

        // 2) Sprites
        batch.begin();
        batch.setColor(Color.WHITE);
        if (texEnemy != null) {
            for (int i = 0; i < g.ennemis.size; i++) {
                Ennemi e = g.ennemis.get(i);
                batch.setColor(e.boss ? bossTint : Color.WHITE);
                drawSprite(texEnemy, e.pos.x, e.pos.y, e.size,
                        e.angle + GameScreen.ENEMY_ROT);
            }
            batch.setColor(Color.WHITE);
        }
        if (texArrow != null) {
            for (int i = 0; i < g.fleches.size; i++) {
                Fleche f = g.fleches.get(i);
                drawSprite(texArrow, f.pos.x, f.pos.y, GameScreen.ARROW_SIZE,
                        f.angle + GameScreen.ARROW_ROT);
            }
        }
        if (texPlayer != null && playerVisible) {
            drawSprite(texPlayer, g.playerPos.x, g.playerPos.y, GameScreen.PLAYER_SIZE,
                    g.facing + GameScreen.PLAYER_ROT);
        }
        batch.end();

        // 3) Cercles de secours + barre de vie du boss
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < g.ennemis.size; i++) {
            Ennemi e = g.ennemis.get(i);
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
            for (int i = 0; i < g.fleches.size; i++) {
                Fleche f = g.fleches.get(i);
                shapes.circle(f.pos.x, f.pos.y, GameScreen.ARROW_RADIUS, 10);
            }
        }
        if (texPlayer == null && playerVisible) {
            shapes.setColor(1f, 0.4f, 0.7f, 1f);
            shapes.circle(g.playerPos.x, g.playerPos.y, GameScreen.PLAYER_RADIUS, 24);
        }
        shapes.end();

        // ===== Camera UI fixe =====
        uiViewport.apply();
        shapes.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        // 4) Mini-carte + joysticks
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        g.dungeon.drawMiniMap(shapes, MINIMAP_X, MINIMAP_Y);
        drawStick(g.moveStick, 1f, 1f, 1f);
        drawStick(g.aimStick, 1f, 0.5f, 0.5f);
        shapes.end();

        // 5) Boutons de zoom
        g.zoom.draw(shapes, batch, font);

        // 6) HUD texte
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, g.scoreText, 20, H - 20);
        font.draw(batch, g.livesText, W - 160, H - 20);
        if (g.victory) {
            font.setColor(1f, 0.85f, 0.2f, 1f);
            font.draw(batch, "VICTOIRE !", W / 2f - 110, H / 2f + 30);
            font.setColor(Color.WHITE);
            font.draw(batch, "Touche l'ecran pour rejouer", W / 2f - 220, H / 2f - 20);
        } else if (g.gameOver) {
            font.setColor(1f, 0.4f, 0.4f, 1f);
            font.draw(batch, "GAME OVER", W / 2f - 100, H / 2f + 30);
            font.setColor(Color.WHITE);
            font.draw(batch, "Touche l'ecran pour rejouer", W / 2f - 220, H / 2f - 20);
        }
        batch.end();
    }
}