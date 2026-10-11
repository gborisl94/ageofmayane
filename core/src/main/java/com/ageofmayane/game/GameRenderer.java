package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.HashMap;

class GameRenderer {

    static final float W = GameScreen.W;
    static final float H = GameScreen.H;

    static final float MINIMAP_X = 20f;
    static final float MINIMAP_Y = H - 170f;

    static final float FLOOR_TILE = 80f;
    static final float WALL_TILE = 40f;

    static final float XP_BAR_X = 20f;
    static final float XP_BAR_Y = H - 100f;
    static final float XP_BAR_W = 260f;
    static final float XP_BAR_H = 20f;

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
    Texture texBoss;
    Texture texSol;
    Texture texMur;
    Texture texPorte;
    Texture texChestClosed;
    Texture texChestOpen;

    private final Color bossTint = new Color(1f, 0.55f, 0.55f, 1f);
    private final Rectangle tmpRect = new Rectangle();
    private final HashMap<String, Texture> dropCache = new HashMap<String, Texture>();
    private final GlyphLayout layout = new GlyphLayout();

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
        texBoss = loadTexture("boss.png");
        texSol = loadTexture("sol.png");
        texMur = loadTexture("mur.png");
        texPorte = loadTexture("porte.png");
        texChestClosed = loadTexture("chest_closed.png");
        texChestOpen = loadTexture("chest_open.png");
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

    private Texture loadDropTexture(String name) {
        if (name == null) return null;
        if (dropCache.containsKey(name)) return dropCache.get(name);
        Texture t = loadTexture(name);
        dropCache.put(name, t);
        return t;
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
        if (texBoss != null) texBoss.dispose();
        if (texSol != null) texSol.dispose();
        if (texMur != null) texMur.dispose();
        if (texPorte != null) texPorte.dispose();
        if (texChestClosed != null) texChestClosed.dispose();
        if (texChestOpen != null) texChestOpen.dispose();
        for (Texture t : dropCache.values()) {
            if (t != null) t.dispose();
        }
        dropCache.clear();
        shapes = null; batch = null; font = null;
        texPlayer = null; texEnemy = null; texArrow = null; texBoss = null;
        texSol = null; texMur = null; texPorte = null;
        texChestClosed = null; texChestOpen = null;
        viewport = null; uiViewport = null; camera = null; uiCamera = null;
    }private void drawSprite(Texture t, float cx, float cy, float h, float rotation) {
    float w = h * t.getWidth() / (float) t.getHeight();
    batch.draw(t, cx - w / 2f, cy - h / 2f, w / 2f, h / 2f, w, h,
            1f, 1f, rotation, 0, 0, t.getWidth(), t.getHeight(), false, false);
}

private void drawTiled(Texture t, float x, float y, float w, float h, float tile) {
    int tw = t.getWidth();
    int th = t.getHeight();
    int nx = (int) Math.ceil(w / tile - 0.001f);
    int ny = (int) Math.ceil(h / tile - 0.001f);
    for (int iy = 0; iy < ny; iy++) {
        float yy = iy * tile;
        float ch = Math.min(tile, h - yy);
        int srcH = Math.max(1, Math.round(th * ch / tile));
        for (int ix = 0; ix < nx; ix++) {
            float xx = ix * tile;
            float cw = Math.min(tile, w - xx);
            int srcW = Math.max(1, Math.round(tw * cw / tile));
            batch.draw(t, x + xx, y + yy, cw, ch, 0, 0, srcW, srcH, false, false);
        }
    }
}

private void drawDoorTexture(GameScreen g, int dir) {
    Rectangle r = g.dungeon.doorRectOrNull(dir, tmpRect);
    if (r == null) return;
    if (g.dungeon.doorsOpen()) batch.setColor(0.6f, 1f, 0.6f, 1f);
    else batch.setColor(1f, 0.55f, 0.55f, 1f);
    float cx = r.x + r.width / 2f;
    float cy = r.y + r.height / 2f;
    boolean vertical = r.height > r.width;
    float len = Math.max(r.width, r.height);
    float thick = Math.min(r.width, r.height);
    batch.draw(texPorte, cx - len / 2f, cy - thick / 2f, len / 2f, thick / 2f,
            len, thick, 1f, 1f, vertical ? 90f : 0f,
            0, 0, texPorte.getWidth(), texPorte.getHeight(), false, false);
    batch.setColor(Color.WHITE);
}

private void drawStick(Stick s, float r, float g, float b) {
    if (!s.active()) return;
    shapes.setColor(r, g, b, 0.15f);
    shapes.circle(s.origin.x, s.origin.y, GameScreen.STICK_RADIUS, 32);
    shapes.setColor(r, g, b, 0.45f);
    shapes.circle(s.current.x, s.current.y, 45f, 24);
}

private void drawChest(Chest c) {
    Texture t = c.opened ? texChestOpen : texChestClosed;
    float pulse = c.opened ? 0f : (0.5f + 0.5f * MathUtils.sin(c.pulseTime * 3f));

    if (!c.opened) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 0.85f, 0.2f, 0.15f + 0.25f * pulse);
        shapes.circle(c.pos.x, c.pos.y, 45f + 10f * pulse, 32);
        shapes.end();
    }

    if (t != null) {
        batch.begin();
        batch.setColor(Color.WHITE);
        float size = 64f;
        batch.draw(t, c.pos.x - size / 2f, c.pos.y - size / 2f, size, size);
        batch.end();
    } else {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.55f, 0.35f, 0.1f, 1f);
        shapes.rect(c.pos.x - 25f, c.pos.y - 25f, 50f, 50f);
        shapes.setColor(0.3f, 0.3f, 0.35f, 1f);
        shapes.rect(c.pos.x - 25f, c.pos.y + 17f, 50f, 4f);
        shapes.rect(c.pos.x - 25f, c.pos.y - 21f, 50f, 4f);
        if (!c.opened) {
            shapes.setColor(1f, 0.85f, 0.2f, 1f);
            shapes.circle(c.pos.x, c.pos.y, 6f, 16);
        }
        shapes.end();
    }
}

private void drawOpenButton(GameScreen g) {
    if (!g.canInteractWithChest()) return;

    float bx = GameScreen.OPEN_BTN_X;
    float by = GameScreen.OPEN_BTN_Y;
    float bw = GameScreen.OPEN_BTN_W;
    float bh = GameScreen.OPEN_BTN_H;

    shapes.begin(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(0.1f, 0.1f, 0.15f, 0.9f);
    shapes.rect(bx - 3f, by - 3f, bw + 6f, bh + 6f);
    shapes.setColor(0.85f, 0.65f, 0.15f, 0.95f);
    shapes.rect(bx, by, bw, bh);
    shapes.end();

    batch.begin();
    font.getData().setScale(1.5f);
    font.setColor(1f, 1f, 1f, 1f);
    String txt = "OUVRIR";
    layout.setText(font, txt);
    float tx = bx + (bw - layout.width) / 2f;
    float ty = by + bh * 0.7f;
    font.draw(batch, txt, tx, ty);

    String txt2 = "LE COFFRE";
    layout.setText(font, txt2);
    float tx2 = bx + (bw - layout.width) / 2f;
    float ty2 = by + bh * 0.35f;
    font.draw(batch, txt2, tx2, ty2);
    font.getData().setScale(2f);
    batch.end();
}

private void drawXpBar(GameScreen g) {
    if (g.xpSystem == null) return;

    float bx = XP_BAR_X;
    float by = XP_BAR_Y;
    float bw = XP_BAR_W;
    float bh = XP_BAR_H;
    float ratio = g.xpSystem.getProgress();

    shapes.begin(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(0.05f, 0.05f, 0.1f, 0.95f);
    shapes.rect(bx - 2f, by - 2f, bw + 4f, bh + 4f);

    if (ratio > 0.66f) shapes.setColor(0.2f, 0.9f, 0.3f, 1f);
    else if (ratio > 0.33f) shapes.setColor(1f, 0.85f, 0.2f, 1f);
    else shapes.setColor(0.9f, 0.4f, 0.2f, 1f);

    shapes.rect(bx, by, bw * ratio, bh);

    shapes.setColor(0.4f, 0.4f, 0.5f, 1f);
    shapes.rect(bx - 2f, by - 2f, bw + 4f, 2f);
    shapes.rect(bx - 2f, by + bh, bw + 4f, 2f);
    shapes.rect(bx - 2f, by, 2f, bh);
    shapes.rect(bx + bw, by, 2f, bh);
    shapes.end();

    batch.begin();
    font.getData().setScale(1.5f);
    font.setColor(1f, 0.9f, 0.3f, 1f);
    font.draw(batch, "Niv " + g.xpSystem.level, bx, by + bh + 26f);

    font.getData().setScale(1.2f);
    font.setColor(1f, 1f, 1f, 1f);
    String txt = g.xpSystem.xp + " / " + g.xpSystem.xpRequired;
    layout.setText(font, txt);
    font.draw(batch, txt, bx + bw - layout.width, by + bh + 22f);
    font.getData().setScale(2f);
    batch.end();
}

void draw(GameScreen g) {
    Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    g.zoom.applyToCamera(camera, g.playerPos.x, g.playerPos.y, W, H);
    viewport.apply();
    shapes.setProjectionMatrix(camera.combined);
    batch.setProjectionMatrix(camera.combined);

    boolean playerVisible = !g.gameOver
            && (g.invulnerable <= 0f || ((int) (g.invulnerable * 10f)) % 2 == 0);

    if (texSol != null) {
        batch.begin();
        batch.setColor(Color.WHITE);
        drawTiled(texSol, 0f, 0f, W, H, FLOOR_TILE);
        batch.end();
    }

    if (texSol == null || texMur == null) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (texSol == null) g.dungeon.drawFloorColor(shapes);
        if (texMur == null) g.dungeon.drawWallsColor(shapes);
        shapes.end();
    }

    if (texMur != null || texPorte != null) {
        batch.begin();
        batch.setColor(Color.WHITE);
        if (texMur != null) {
            Array<Rectangle> solids = g.dungeon.getSolids();
            for (int i = 0; i < solids.size; i++) {
                Rectangle s = solids.get(i);
                float tile = Math.min(WALL_TILE, Math.min(s.width, s.height));
                drawTiled(texMur, s.x, s.y, s.width, s.height, tile);
            }
        }
        if (texPorte != null) {
            for (int d = 0; d < 4; d++) drawDoorTexture(g, d);
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    if (texPorte == null) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        g.dungeon.drawDoorsColor(shapes);
        shapes.end();
    }

    if (g.chest != null) {
        drawChest(g.chest);
    }

    batch.begin();
    batch.setColor(Color.WHITE);
    for (int i = 0; i < g.ennemis.size; i++) {
        Ennemi e = g.ennemis.get(i);
        Texture t = (e.boss && texBoss != null) ? texBoss : texEnemy;
        if (t == null) continue;
        boolean tint = e.boss && texBoss == null;
        batch.setColor(tint ? bossTint : Color.WHITE);

        // Si le boss fuit, on applique un fondu
        if (e.fleeing) {
            float alpha = Math.max(0f, 1f - e.fleeTimer / 2.5f);
            batch.setColor(1f, 1f, 1f, alpha);
        }

        drawSprite(t, e.pos.x, e.pos.y, e.size, e.angle + GameScreen.ENEMY_ROT);
    }
    batch.setColor(Color.WHITE);
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

    g.damage.draw(batch, font);
    batch.end();        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < g.ennemis.size; i++) {
            Ennemi e = g.ennemis.get(i);
            boolean hasTex = e.boss ? (texBoss != null || texEnemy != null) : texEnemy != null;
            if (!hasTex) {
                if (e.boss) shapes.setColor(0.9f, 0.3f, 0.3f, 1f);
                else shapes.setColor(0.2f, 0.9f, 0.9f, 1f);
                shapes.circle(e.pos.x, e.pos.y, e.radius, 24);
            }
            if (e.boss && !e.fleeing) {
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

        // ===== PARTICULES (explosions, etincelles) =====
        g.particles.draw(shapes);

        shapes.end();

        // Arcs poses au sol
        for (int i = 0; i < g.weaponDrops.size; i++) {
            WeaponDrop d = g.weaponDrops.get(i);
            float pulse = 0.5f + 0.5f * MathUtils.sin(d.pulseTime * 6f);

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(d.weapon.rarity.colorWithAlpha(0.15f + 0.2f * pulse));
            shapes.circle(d.pos.x, d.pos.y, 18f + 4f * pulse, 20);
            shapes.end();

            Texture t = loadDropTexture(d.weapon.spriteName);
            if (t != null) {
                batch.begin();
                batch.setColor(Color.WHITE);
                float w = 32f;
                float h = 16f;
                batch.draw(t, d.pos.x - w / 2f, d.pos.y - h / 2f, w, h);
                batch.end();
            }
        }

        uiViewport.apply();
        shapes.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        g.dungeon.drawMiniMap(shapes, MINIMAP_X, MINIMAP_Y);
        drawStick(g.moveStick, 1f, 1f, 1f);
        drawStick(g.aimStick, 1f, 0.5f, 0.5f);
        shapes.end();

        g.zoom.draw(shapes, batch, font);

        drawOpenButton(g);
        drawXpBar(g);

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, g.scoreText, 20, H - 20);
        font.draw(batch, g.livesText, W - 160, H - 20);

        // Message "Le champion s'enfuit !"
        if (g.bossFleeMessageTimer > 0f) {
            float alpha = Math.min(1f, g.bossFleeMessageTimer);
            font.getData().setScale(2.2f);
            String fleeMsg = "LE CHAMPION S'ENFUIT !";
            layout.setText(font, fleeMsg);
            float msgX = W / 2f - layout.width / 2f;
            float msgY = H / 2f + 80f;

            font.setColor(0f, 0f, 0f, alpha * 0.8f);
            font.draw(batch, fleeMsg, msgX + 3f, msgY - 3f);
            font.setColor(1f, 0.3f, 0.3f, alpha);
            font.draw(batch, fleeMsg, msgX, msgY);

            font.getData().setScale(1.4f);
            font.setColor(1f, 1f, 1f, alpha);
            String subMsg = "Reviens le defier plus tard...";
            layout.setText(font, subMsg);
            font.draw(batch, subMsg, W / 2f - layout.width / 2f, msgY - 40f);

            font.getData().setScale(2f);
            font.setColor(Color.WHITE);
        }

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

        if (g.hitFlash > 0f) {
            float alpha = Math.min(0.55f, g.hitFlash / GameScreen.HIT_FLASH_DURATION * 0.55f);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            uiViewport.apply();
            shapes.setProjectionMatrix(uiCamera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1f, 0f, 0f, alpha);
            shapes.rect(0f, 0f, W, H);
            shapes.end();
            shapes.setColor(1f, 1f, 1f, 1f);
        }
    }
}