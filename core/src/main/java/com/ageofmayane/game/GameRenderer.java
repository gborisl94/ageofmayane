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
    private final Color bossTintPhase2 = new Color(1f, 0.4f, 0.2f, 1f);
    private final Color bossTintPhase3 = new Color(1f, 0.2f, 0.6f, 1f);
    private final Color enemyProjectileColor = new Color(1f, 0.4f, 0.1f, 1f);
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

private void drawDoorTexture(GameScreen gs, int dir) {
    Rectangle r = gs.dungeon.doorRectOrNull(dir, tmpRect);
    if (r == null) return;
    if (gs.dungeon.doorsOpen()) batch.setColor(0.6f, 1f, 0.6f, 1f);
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

private void drawOpenButton(GameScreen gs) {
    if (!gs.canInteractWithChest()) return;

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

private void drawStunButton(GameScreen gs) {
    float bx = GameScreen.STUN_BTN_X;
    float by = GameScreen.STUN_BTN_Y;
    float bs = GameScreen.STUN_BTN_SIZE;

    float ratio = gs.getStunCooldownRatio();
    boolean ready = gs.isStunReady();

    shapes.begin(ShapeRenderer.ShapeType.Filled);

    shapes.setColor(0.05f, 0.05f, 0.1f, 0.95f);
    shapes.rect(bx - 3f, by - 3f, bs + 6f, bs + 6f);

    if (ready) shapes.setColor(0.9f, 0.75f, 0.2f, 0.95f);
    else shapes.setColor(0.35f, 0.35f, 0.4f, 0.9f);
    shapes.rect(bx, by, bs, bs);

    if (!ready) {
        shapes.setColor(0.1f, 0.1f, 0.15f, 0.75f);
        float barH = bs * ratio;
        shapes.rect(bx, by + bs - barH, bs, barH);
    }

    if (ready) shapes.setColor(1f, 1f, 0.4f, 1f);
    else shapes.setColor(0.6f, 0.6f, 0.6f, 1f);

    float cx = bx + bs / 2f;
    float cy = by + bs / 2f + 5f;
    float sz = bs * 0.25f;
    shapes.rectLine(cx - sz * 0.5f, cy + sz, cx + sz * 0.3f, cy, 6f);
    shapes.rectLine(cx + sz * 0.3f, cy, cx - sz * 0.2f, cy - sz, 6f);

    shapes.end();

    batch.begin();
    font.getData().setScale(1.3f);
    String txt = "STUN";
    font.setColor(0.1f, 0.1f, 0.1f, 1f);
    layout.setText(font, txt);
    font.draw(batch, txt, bx + (bs - layout.width) / 2f + 1f,
            by + bs * 0.22f + 1f);
    font.setColor(1f, 1f, 1f, 1f);
    font.draw(batch, txt, bx + (bs - layout.width) / 2f, by + bs * 0.22f);

    if (!ready) {
        String cd = String.format("%.1f", gs.stunCooldown);
        layout.setText(font, cd);
        font.setColor(1f, 0.4f, 0.4f, 1f);
        font.draw(batch, cd, bx + (bs - layout.width) / 2f, by + bs - 8f);
    } else {
        String rdy = "PRET";
        layout.setText(font, rdy);
        font.setColor(0.2f, 0.6f, 0.2f, 1f);
        font.draw(batch, rdy, bx + (bs - layout.width) / 2f, by + bs - 8f);
    }

    font.getData().setScale(2f);
    font.setColor(Color.WHITE);
    batch.end();
}

private void drawXpBar(GameScreen gs) {
    if (gs.xpSystem == null) return;

    float bx = XP_BAR_X;
    float by = XP_BAR_Y;
    float bw = XP_BAR_W;
    float bh = XP_BAR_H;
    float ratio = gs.xpSystem.getProgress();

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
    font.draw(batch, "Niv " + gs.xpSystem.level, bx, by + bh + 26f);

    font.getData().setScale(1.2f);
    font.setColor(1f, 1f, 1f, 1f);
    String txt = gs.xpSystem.xp + " / " + gs.xpSystem.xpRequired;
    layout.setText(font, txt);
    font.draw(batch, txt, bx + bw - layout.width, by + bh + 22f);
    font.getData().setScale(2f);
    batch.end();
}

private void drawEnemyStunStars(Ennemi e) {
    if (e.stunned <= 0f) return;
    float cx = e.pos.x;
    float cy = e.pos.y + e.size / 2f + 18f;
    float radius = 18f;
    float baseAngle = e.stunned * 6f;
    for (int i = 0; i < 3; i++) {
        float a = baseAngle + i * (MathUtils.PI2 / 3f);
        float sx = cx + MathUtils.cos(a) * radius;
        float sy = cy + MathUtils.sin(a) * radius * 0.5f;
        shapes.setColor(1f, 0.9f, 0.2f, 0.9f);
        shapes.circle(sx, sy, 5f, 10);
        shapes.setColor(1f, 0.95f, 0.5f, 0.7f);
        shapes.circle(sx, sy, 2.5f, 8);
    }
}

private void drawDizzyStars(GameScreen gs) {
    if (!gs.isStunned()) return;

    float cx = gs.playerPos.x;
    float cy = gs.playerPos.y + 50f;
    float radius = 32f;

    for (int i = 0; i < 4; i++) {
        float angle = gs.dizzyTime * 3f + i * (MathUtils.PI2 / 4f);
        float sx = cx + MathUtils.cos(angle) * radius;
        float sy = cy + MathUtils.sin(angle) * radius * 0.5f;

        shapes.setColor(1f, 0.9f, 0.2f, 0.9f);
        shapes.circle(sx, sy, 6f, 12);
        shapes.setColor(1f, 0.95f, 0.4f, 0.8f);
        shapes.circle(sx, sy, 3f, 8);
    }
}

void draw(GameScreen gs) {
    Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    gs.zoom.applyToCamera(camera, gs.playerPos.x, gs.playerPos.y, W, H);

    camera.position.x += gs.shakeOffsetX;
    camera.position.y += gs.shakeOffsetY;
    camera.update();

    viewport.apply();
    shapes.setProjectionMatrix(camera.combined);
    batch.setProjectionMatrix(camera.combined);

    boolean playerVisible = !gs.gameOver
            && (gs.invulnerable <= 0f || ((int) (gs.invulnerable * 10f)) % 2 == 0);

    if (texSol != null) {
        batch.begin();
        batch.setColor(Color.WHITE);
        drawTiled(texSol, 0f, 0f, W, H, FLOOR_TILE);
        batch.end();
    }

    if (texSol == null || texMur == null) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (texSol == null) gs.dungeon.drawFloorColor(shapes);
        if (texMur == null) gs.dungeon.drawWallsColor(shapes);
        shapes.end();
    }

    if (texMur != null || texPorte != null) {
        batch.begin();
        batch.setColor(Color.WHITE);
        if (texMur != null) {
            Array<Rectangle> solids = gs.dungeon.getSolids();
            for (int i = 0; i < solids.size; i++) {
                Rectangle s = solids.get(i);
                float tile = Math.min(WALL_TILE, Math.min(s.width, s.height));
                drawTiled(texMur, s.x, s.y, s.width, s.height, tile);
            }
        }
        if (texPorte != null) {
            for (int d = 0; d < 4; d++) drawDoorTexture(gs, d);
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    if (texPorte == null) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        gs.dungeon.drawDoorsColor(shapes);
        shapes.end();
    }

    if (gs.chest != null) {
        drawChest(gs.chest);
    }

    // ==================== ENNEMIS ====================
    batch.begin();
    batch.setColor(Color.WHITE);
    for (int i = 0; i < gs.ennemis.size; i++) {
        Ennemi e = gs.ennemis.get(i);
        Texture t = (e.boss && texBoss != null) ? texBoss : texEnemy;
        if (t == null) continue;

        float scale = 1f;

        if (e.boss) {
            if (e.phase == 1) batch.setColor(bossTint);
            else if (e.phase == 2) batch.setColor(bossTintPhase2);
            else batch.setColor(bossTintPhase3);
        } else {
            batch.setColor(Color.WHITE);
        }

        if (e.fleeing) {
            float alpha = Math.max(0f, 1f - e.fleeTimer / 2.5f);
            batch.setColor(1f, 1f, 1f, alpha);
        }

        if (e.boss && e.phaseChangeFlash > 0f) {
            scale = 1f + 0.3f * (e.phaseChangeFlash);
            batch.setColor(1f, 1f, 1f, 1f);
        }

        if (e.stunned > 0f) {
            float alpha = 0.6f + 0.4f * (float) Math.sin(e.stunned * 20f);
            batch.setColor(1f, 1f, 1f, alpha);
        }

        // HIT PULSE : grossit + clignote
        if (e.hitPulse > 0f) {
            float pulsePct = e.hitPulse / GameScreen.ENEMY_HIT_PULSE_DURATION;

            // Grossissement de 50% max
            scale *= 1f + 0.5f * pulsePct;

            // Clignotement rapide
            float blink = (float) Math.sin(e.hitPulse * 60f);
            if (blink > 0f) {
                batch.setColor(1f, 1f, 1f, 1f);
            } else {
                batch.setColor(1f, 0.2f, 0.2f, 1f);
            }
        }

        // ATTACK PULSE
        if (e.attackPulse > 0f) {
            float atkPct = e.attackPulse / GameScreen.ENEMY_ATTACK_PULSE_DURATION;
            scale *= 1f + 0.35f * atkPct;
            float greenVal = 0.5f + 0.5f * (1f - atkPct);
            float blueVal = 0.5f + 0.5f * (1f - atkPct);
            batch.setColor(1f, greenVal, blueVal, 1f);
        }

        drawSprite(t, e.pos.x, e.pos.y, e.size * scale,
                e.angle + GameScreen.ENEMY_ROT);
    }
    batch.setColor(Color.WHITE);

    // Projectiles
    for (int i = 0; i < gs.fleches.size; i++) {
        Fleche f = gs.fleches.get(i);
        if (f.fromEnemy) {
            batch.end();
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(enemyProjectileColor);
            shapes.circle(f.pos.x, f.pos.y, 9f, 16);
            shapes.setColor(1f, 0.8f, 0.3f, 0.7f);
            shapes.circle(f.pos.x, f.pos.y, 5f, 12);
            shapes.end();
            batch.begin();
            batch.setColor(Color.WHITE);
        } else if (texArrow != null) {
            drawSprite(texArrow, f.pos.x, f.pos.y, GameScreen.ARROW_SIZE,
                    f.angle + GameScreen.ARROW_ROT);
        }
    }

    // JOUEUR
    if (texPlayer != null && playerVisible) {
        float playerScale = 1f;
        if (gs.playerRecoil > 0f) {
            float pct = gs.playerRecoil / GameScreen.PLAYER_RECOIL_DURATION;
            playerScale = 1f + 0.25f * pct;
            float recoilOffset = -12f * pct;
            float dx = -MathUtils.cosDeg(gs.facing) * recoilOffset;
            float dy = -MathUtils.sinDeg(gs.facing) * recoilOffset;
            drawSprite(texPlayer,
                    gs.playerPos.x + dx,
                    gs.playerPos.y + dy,
                    GameScreen.PLAYER_SIZE * playerScale,
                    gs.facing + GameScreen.PLAYER_ROT);
        } else {
            drawSprite(texPlayer, gs.playerPos.x, gs.playerPos.y,
                    GameScreen.PLAYER_SIZE, gs.facing + GameScreen.PLAYER_ROT);
        }
    }

    gs.damage.draw(batch, font);
    batch.end();        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < gs.ennemis.size; i++) {
            Ennemi e = gs.ennemis.get(i);
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
                if (e.phase == 1) shapes.setColor(0.85f, 0.1f, 0.1f, 1f);
                else if (e.phase == 2) shapes.setColor(1f, 0.6f, 0.1f, 1f);
                else shapes.setColor(1f, 0.2f, 0.9f, 1f);
                shapes.rect(bx, by, bw * frac, 8f);
            }
        }
        if (texArrow == null) {
            shapes.setColor(1f, 0.9f, 0.3f, 1f);
            for (int i = 0; i < gs.fleches.size; i++) {
                Fleche f = gs.fleches.get(i);
                if (!f.fromEnemy) {
                    shapes.circle(f.pos.x, f.pos.y, GameScreen.ARROW_RADIUS, 10);
                }
            }
        }
        if (texPlayer == null && playerVisible) {
            shapes.setColor(1f, 0.4f, 0.7f, 1f);
            shapes.circle(gs.playerPos.x, gs.playerPos.y, GameScreen.PLAYER_RADIUS, 24);
        }

        for (int i = 0; i < gs.ennemis.size; i++) {
            drawEnemyStunStars(gs.ennemis.get(i));
        }

        gs.particles.draw(shapes);
        drawDizzyStars(gs);
        shapes.end();

        for (int i = 0; i < gs.weaponDrops.size; i++) {
            WeaponDrop d = gs.weaponDrops.get(i);
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
        gs.dungeon.drawMiniMap(shapes, MINIMAP_X, MINIMAP_Y);
        drawStick(gs.moveStick, 1f, 1f, 1f);
        drawStick(gs.aimStick, 1f, 0.5f, 0.5f);
        shapes.end();

        gs.zoom.draw(shapes, batch, font);

        drawOpenButton(gs);
        drawStunButton(gs);
        drawXpBar(gs);

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, gs.scoreText, 20, H - 20);
        font.draw(batch, gs.livesText, W - 160, H - 20);

        if (gs.bossFleeMessageTimer > 0f) {
            float alpha = Math.min(1f, gs.bossFleeMessageTimer);
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

        if (gs.bossPhaseMessageTimer > 0f) {
            float alpha = Math.min(1f, gs.bossPhaseMessageTimer);
            font.getData().setScale(3.2f);
            String phaseMsg = "PHASE " + gs.bossPhaseMessage + " !";
            layout.setText(font, phaseMsg);
            float msgX = W / 2f - layout.width / 2f;
            float msgY = H / 2f + 150f;

            Color phaseColor;
            if (gs.bossPhaseMessage == 2) phaseColor = new Color(1f, 0.6f, 0.1f, alpha);
            else phaseColor = new Color(1f, 0.2f, 0.9f, alpha);

            font.setColor(0f, 0f, 0f, alpha * 0.8f);
            font.draw(batch, phaseMsg, msgX + 4f, msgY - 4f);
            font.setColor(phaseColor);
            font.draw(batch, phaseMsg, msgX, msgY);

            font.getData().setScale(2f);
            font.setColor(Color.WHITE);
        }

        if (gs.isStunned()) {
            font.getData().setScale(1.8f);
            font.setColor(1f, 0.9f, 0.3f, 0.9f);
            String stunMsg = "ETOURDI !";
            layout.setText(font, stunMsg);
            font.draw(batch, stunMsg, W / 2f - layout.width / 2f, H / 2f - 100f);
            font.getData().setScale(2f);
            font.setColor(Color.WHITE);
        }

        if (gs.victory) {
            font.setColor(1f, 0.85f, 0.2f, 1f);
            font.draw(batch, "VICTOIRE !", W / 2f - 110, H / 2f + 30);
            font.setColor(Color.WHITE);
            font.draw(batch, "Touche l'ecran pour rejouer", W / 2f - 220, H / 2f - 20);
        } else if (gs.gameOver) {
            font.setColor(1f, 0.4f, 0.4f, 1f);
            font.draw(batch, "GAME OVER", W / 2f - 100, H / 2f + 30);
            font.setColor(Color.WHITE);
            font.draw(batch, "Touche l'ecran pour rejouer", W / 2f - 220, H / 2f - 20);
        }
        batch.end();

        if (gs.hitFlash > 0f) {
            float alpha = Math.min(0.55f, gs.hitFlash / GameScreen.HIT_FLASH_DURATION * 0.55f);
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

        if (gs.isStunned()) {
            float a = Math.min(0.35f, gs.stunTimer / GameScreen.STUN_DURATION * 0.35f);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            uiViewport.apply();
            shapes.setProjectionMatrix(uiCamera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1f, 0.9f, 0.2f, a * 0.3f);
            shapes.rect(0f, 0f, W, H);
            shapes.end();
            shapes.setColor(1f, 1f, 1f, 1f);
        }
    }
}