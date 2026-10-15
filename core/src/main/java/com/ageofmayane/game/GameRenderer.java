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
    static final float MINIMAP_X = W / 2f - 60f;
    static final float MINIMAP_Y = H - 170f;
    static final float FLOOR_TILE = 80f;
    static final float WALL_TILE = 40f;
    static final float XP_BAR_X = 20f;
    static final float XP_BAR_Y = H - 100f;
    static final float XP_BAR_W = 260f;
    static final float XP_BAR_H = 20f;
    static final float ATT_BAR_X = 20f;
    static final float ATT_BAR_Y = XP_BAR_Y - 32f;
    static final float ATT_BAR_W = 260f;
    static final float ATT_BAR_H = 18f;

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

    private final Color[] rainbow = {
        new Color(0.2f, 0.4f, 1f, 1f),
        new Color(1f, 0.15f, 0.15f, 1f),
        new Color(1f, 1f, 1f, 1f),
        new Color(0.1f, 0.1f, 0.1f, 1f),
        new Color(0.2f, 0.4f, 1f, 1f),
        new Color(1f, 0.15f, 0.15f, 1f)
    };

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
    }

    private void drawSprite(Texture t, float cx, float cy, float h, float rotation) {
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

        private void drawStick(Stick s, float r, float g, float b, float mult) {
        if (!s.active()) return;
        shapes.setColor(r, g, b, 0.15f * mult);
        shapes.circle(s.origin.x, s.origin.y, GameScreen.STICK_RADIUS, 32);
        shapes.setColor(r, g, b, 0.45f * mult);
        shapes.circle(s.current.x, s.current.y, 45f, 24);
    }
        private void drawArcAim(GameScreen gs) {
        if (!gs.arcTendu) return;
        try {
            float cx = gs.playerPos.x;
            float cy = gs.playerPos.y;
            float radius = 200f;
            shapes.setColor(0.3f, 0.7f, 1f, 0.012f);
            shapes.circle(cx, cy, radius, 48);
            shapes.setColor(0.3f, 0.7f, 1f, 0.08f);
            shapes.circle(cx, cy, radius, 48);
            float rad = gs.arcAngle * MathUtils.degRad;
            float ox = cx + MathUtils.cos(rad) * radius;
            float oy = cy + MathUtils.sin(rad) * radius;
            shapes.setColor(0.6f, 1f, 1f, 0.9f);
            shapes.circle(ox, oy, 12f, 16);
            shapes.setColor(1f, 1f, 1f, 1f);
            shapes.circle(ox, oy, 5f, 12);
        } catch (Exception e) {
        }
    }

    private void drawFlechesSol(GameScreen gs) {
    if (gs.flechesSol == null || gs.flechesSol.size == 0) return;
    if (texArrow == null) return;
    float size = 32f;
    float w = size;
    float h = size * texArrow.getHeight() / (float) texArrow.getWidth();
    batch.begin();
    batch.setColor(Color.WHITE);
    for (int i = 0; i < gs.flechesSol.size; i++) {
        FlecheSol fs = gs.flechesSol.get(i);
        batch.draw(texArrow,
            fs.pos.x - w / 2f, fs.pos.y - h / 2f,
            w / 2f, h / 2f, w, h,
            1f, 1f, fs.angle,
            0, 0, texArrow.getWidth(), texArrow.getHeight(),
            false, false);
    }
    batch.setColor(Color.WHITE);
    batch.end();
}
             
    private void drawChest(Chest c) {
        Texture t = c.opened ? texChestOpen : texChestClosed;
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
    font.draw(batch, txt, bx + (bw - layout.width) / 2f, by + bh * 0.7f);
    String txt2 = "LE COFFRE";
    layout.setText(font, txt2);
    font.draw(batch, txt2, bx + (bw - layout.width) / 2f, by + bh * 0.35f);
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
        shapes.rect(bx, by + bs - bs * ratio, bs, bs * ratio);
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
    layout.setText(font, txt);
    font.setColor(1f, 1f, 1f, 1f);
    font.draw(batch, txt, bx + (bs - layout.width) / 2f, by + bs * 0.22f);
    String sub = ready ? "PRET" : String.format("%.1fs", gs.stunCooldown);
    layout.setText(font, sub);
    font.setColor(ready ? new Color(0.2f, 0.6f, 0.2f, 1f) : new Color(1f, 0.4f, 0.4f, 1f));
    font.draw(batch, sub, bx + (bs - layout.width) / 2f, by + bs - 8f);
    font.getData().setScale(2f);
    font.setColor(Color.WHITE);
    batch.end();
}

private void drawAutoAimButton(GameScreen gs) {
    float bx = GameScreen.AIM_BTN_X;
    float by = GameScreen.AIM_BTN_Y;
    float bs = GameScreen.AIM_BTN_SIZE;
    boolean ready = gs.isAutoAimReady();
    boolean active = gs.autoAimActive;
    shapes.begin(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(0.05f, 0.05f, 0.1f, 0.95f);
    shapes.rect(bx - 3f, by - 3f, bs + 6f, bs + 6f);
    if (active) shapes.setColor(1f, 0.85f, 0.2f, 0.98f);
    else if (ready) shapes.setColor(0.2f, 0.75f, 0.35f, 0.95f);
    else shapes.setColor(0.3f, 0.3f, 0.35f, 0.9f);
    shapes.rect(bx, by, bs, bs);
    if (active) {
        shapes.setColor(0.6f, 0.4f, 0.05f, 0.7f);
        shapes.rect(bx, by, bs * gs.getAutoAimDurationRatio(), 8f);
    } else if (!ready) {
        shapes.setColor(0.15f, 0.15f, 0.2f, 0.75f);
        shapes.rect(bx, by, bs, bs * gs.getAutoAimCooldownRatio());
    }
    float cx = bx + bs / 2f;
    float cy = by + bs / 2f + 5f;
    if (active || ready) shapes.setColor(1f, 1f, 1f, 0.9f);
    else shapes.setColor(0.7f, 0.7f, 0.7f, 0.7f);
    for (int i = 0; i < 16; i++) {
        float a = i * 22.5f;
        shapes.rect(cx + MathUtils.cosDeg(a) * 22f - 1.5f, cy + MathUtils.sinDeg(a) * 22f - 1.5f, 3f, 3f);
        shapes.rect(cx + MathUtils.cosDeg(a) * 12f - 1f, cy + MathUtils.sinDeg(a) * 12f - 1f, 2f, 2f);
    }
    shapes.end();
    batch.begin();
    font.getData().setScale(1.3f);
    String txt = active ? "ACTIF" : (ready ? "PRET" : "RECHARGE");
    layout.setText(font, txt);
    font.setColor(1f, 1f, 1f, 1f);
    font.draw(batch, txt, bx + (bs - layout.width) / 2f, by + 22f);
    String sub = active ? String.format("%.1fs", gs.autoAimTimer)
                        : (ready ? "AIM" : String.format("%.1fs", gs.autoAimCooldown));
    layout.setText(font, sub);
    font.draw(batch, sub, bx + (bs - layout.width) / 2f, by + bs - 10f);
    font.getData().setScale(2f);
    batch.end();
}

private void drawGlitchButton(GameScreen gs) {
    float bx = GameScreen.GLITCH_BTN_X;
    float by = GameScreen.GLITCH_BTN_Y;
    float bs = GameScreen.GLITCH_BTN_SIZE;
    boolean ready = gs.glitchCooldown <= 0f;
    shapes.begin(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(0.05f, 0.05f, 0.1f, 0.95f);
    shapes.rect(bx - 3f, by - 3f, bs + 6f, bs + 6f);
    if (ready) shapes.setColor(0.2f, 0.9f, 1f, 0.95f);
    else shapes.setColor(0.3f, 0.3f, 0.4f, 0.9f);
    shapes.rect(bx, by, bs, bs);
    if (!ready) {
        float ratio = Math.max(0f, Math.min(1f, gs.glitchCooldown / GameScreen.GLITCH_COOLDOWN));
        shapes.setColor(0.1f, 0.1f, 0.15f, 0.75f);
        shapes.rect(bx, by + bs - bs * ratio, bs, bs * ratio);
    }
    shapes.end();
    batch.begin();
    font.getData().setScale(1.2f);
    String txt = "GLITCH";
    layout.setText(font, txt);
    font.setColor(1f, 1f, 1f, 1f);
    font.draw(batch, txt, bx + (bs - layout.width) / 2f, by + 22f);
    String sub = ready ? "DASH" : String.format("%.1fs", gs.glitchCooldown);
    layout.setText(font, sub);
    font.setColor(ready ? new Color(0.2f, 0.1f, 0f, 1f) : new Color(1f, 0.7f, 0.7f, 1f));
    font.draw(batch, sub, bx + (bs - layout.width) / 2f, by + bs - 10f);
    font.getData().setScale(2f);
    font.setColor(Color.WHITE);
    batch.end();
}
  private void drawDashedH(float x1, float x2, float y, float thick, float dash, Color c) {
    shapes.setColor(c);
    float len = x2 - x1;
    int count = (int) Math.max(1, len / dash);
    float step = len / count;
    for (int i = 0; i < count; i++) {
        shapes.rect(x1 + i * step, y - thick / 2f, step * 0.6f, thick);
    }
}

private void drawDashedV(float x, float y1, float y2, float thick, float dash, Color c) {
    shapes.setColor(c);
    float len = Math.abs(y2 - y1);
    float dir = (y2 > y1) ? 1f : -1f;
    int count = (int) Math.max(1, len / dash);
    float step = len / count;
    for (int i = 0; i < count; i++) {
        shapes.rect(x - thick / 2f, y1 + i * step * dir, thick, step * 0.6f);
    }
}

private void drawAutoAimVisual(GameScreen gs) {
    if (gs.autoTarget == null) return;
    if (gs.attention <= 0f) return;
    if (gs.aimStick.active()) return;
    if (!gs.autoAimActive) return;
    Ennemi target = gs.autoTarget;
    float cx = target.pos.x + gs.aimTrembleX;
    float cy = target.pos.y + gs.aimTrembleY;
    Color reticleColor;
    if (gs.chargeLevel == 3) reticleColor = new Color(1f, 0.9f, 0.2f, 1f);
    else if (gs.chargeLevel == 2) reticleColor = new Color(1f, 0.6f, 0.1f, 1f);
    else reticleColor = new Color(1f, 0.25f, 0.25f, 0.95f);
    float r = target.radius * 1.4f + 10f;
    float len = 14f;
    float thick = 4f;
    float dash = 6f;
    drawDashedH(cx - r, cx - r + len, cy + r, thick, dash, reticleColor);
    drawDashedV(cx - r, cy + r, cy + r - len, thick, dash, reticleColor);
    drawDashedH(cx + r - len, cx + r, cy + r, thick, dash, reticleColor);
    drawDashedV(cx + r, cy + r, cy + r - len, thick, dash, reticleColor);
    drawDashedH(cx - r, cx - r + len, cy - r, thick, dash, reticleColor);
    drawDashedV(cx - r, cy - r, cy - r + len, thick, dash, reticleColor);
    drawDashedH(cx + r - len, cx + r, cy - r, thick, dash, reticleColor);
    drawDashedV(cx + r, cy - r, cy - r + len, thick, dash, reticleColor);
    shapes.setColor(reticleColor);
    shapes.rect(cx - 2f, cy - 2f, 4f, 4f);
}

private void drawTargetAcquiredText(GameScreen gs) {
    if (gs.targetAcquiredTimer <= 0f) return;
    if (gs.autoTarget == null) return;
    if (!gs.autoAimActive) return;
    float alpha = Math.min(1f, gs.targetAcquiredTimer * 2f);
    if (gs.targetAcquiredTimer < 0.5f) alpha = gs.targetAcquiredTimer / 0.5f;
    Ennemi target = gs.autoTarget;
    float cx = target.pos.x;
    float cy = target.pos.y + target.radius + 42f;
    String txt = "Target acquired.";
    font.getData().setScale(1.4f);
    layout.setText(font, txt);
    float tx = cx - layout.width / 2f;
    font.setColor(1f, 0.85f, 0.2f, alpha);
    font.draw(batch, txt, tx, cy);
    font.getData().setScale(2f);
    font.setColor(Color.WHITE);
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

private void drawAttentionGauge(GameScreen gs) {
    float bx = ATT_BAR_X;
    float by = ATT_BAR_Y;
    float bw = ATT_BAR_W;
    float bh = ATT_BAR_H;
    float ratio = gs.attention / GameScreen.ATTENTION_MAX;
    shapes.begin(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(0.05f, 0.05f, 0.1f, 0.95f);
    shapes.rect(bx - 2f, by - 2f, bw + 4f, bh + 4f);
    Color col;
    if (ratio > 0.5f) col = new Color(0.3f, 0.8f, 1f, 1f);
    else if (ratio > 0.2f) col = new Color(1f, 0.8f, 0.2f, 1f);
    else col = new Color(1f, 0.3f, 0.3f, 1f);
    shapes.setColor(col);
    shapes.rect(bx, by, bw * ratio, bh);
    shapes.end();
    batch.begin();
    font.getData().setScale(1.1f);
    font.setColor(1f, 1f, 1f, 1f);
    font.draw(batch, "ATTENTION", bx + 4f, by + bh - 3f);
    String val = ((int) gs.attention) + "";
    layout.setText(font, val);
    font.draw(batch, val, bx + bw - layout.width - 4f, by + bh - 3f);
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
    }
}

private void drawGlitchEffect(GameScreen gs) {
    if (gs.glitchEffectTimer <= 0f) return;
    float alpha = gs.glitchEffectTimer / 0.4f;
    uiViewport.apply();
    shapes.setProjectionMatrix(uiCamera.combined);
    shapes.begin(ShapeRenderer.ShapeType.Filled);
    for (int i = 0; i < 10; i++) {
        float by = MathUtils.random(0f, GameScreen.H);
        float bh = MathUtils.random(4f, 22f);
        float bx = MathUtils.random(-50f, 50f);
        if (Math.random() < 0.5f) shapes.setColor(0.2f, 0.9f, 1f, alpha * 0.55f);
        else shapes.setColor(1f, 0.2f, 0.9f, alpha * 0.55f);
        shapes.rect(bx, by, GameScreen.W, bh);
    }
    shapes.end();
}
      void draw(GameScreen gs) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.35f, 0.25f, 0.2f, 1f);
        Array<Rectangle> allSolids = gs.dungeon.getSolids();
        for (int i = 0; i < allSolids.size; i++) {
            Rectangle s = allSolids.get(i);
            if (s.width < 40f || s.height < 40f) {
                shapes.rect(s.x, s.y, s.width, s.height);
            }
        }
        shapes.end();

        if (gs.chest != null) drawChest(gs.chest);

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
            } else batch.setColor(Color.WHITE);
            if (e.fleeing) batch.setColor(1f, 1f, 1f, Math.max(0f, 1f - e.fleeTimer / 2.5f));
            if (e.hitPulse > 0f) {
                float pulsePct = e.hitPulse / GameScreen.ENEMY_HIT_PULSE_DURATION;
                scale *= 1f + 0.6f * pulsePct;
                int colorIndex = ((int) (e.hitPulse * 40f)) % 6;
                batch.setColor(rainbow[colorIndex]);
            }
            drawSprite(t, e.pos.x, e.pos.y, e.size * scale,
                    e.angle + GameScreen.ENEMY_ROT);
        }
        batch.setColor(Color.WHITE);

        for (int i = 0; i < gs.fleches.size; i++) {
            Fleche f = gs.fleches.get(i);
            if (f.fromEnemy) {
                batch.end();
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(enemyProjectileColor);
                shapes.circle(f.pos.x, f.pos.y, 9f, 16);
                shapes.end();
                batch.begin();
            } else if (texArrow != null) {
                drawSprite(texArrow, f.pos.x, f.pos.y, GameScreen.ARROW_SIZE,
                        f.angle + GameScreen.ARROW_ROT);
            }
        }

        if (texPlayer != null && playerVisible) {
            if (gs.invulnerable > 0f) {
                int colorIndex = ((int) (gs.invulnerable * 15f)) % 6;
                batch.setColor(rainbow[colorIndex]);
            } else {
                batch.setColor(Color.WHITE);
            }
            drawSprite(texPlayer, gs.playerPos.x, gs.playerPos.y,
                    GameScreen.PLAYER_SIZE, gs.facing + GameScreen.PLAYER_ROT);
            batch.setColor(Color.WHITE);
        }

        drawTargetAcquiredText(gs);
        gs.damage.draw(batch, font);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (texPlayer == null && playerVisible) {
            shapes.setColor(1f, 0.4f, 0.7f, 1f);
            shapes.circle(gs.playerPos.x, gs.playerPos.y, GameScreen.PLAYER_RADIUS, 24);
        }
        for (int i = 0; i < gs.ennemis.size; i++) drawEnemyStunStars(gs.ennemis.get(i));
        gs.particles.draw(shapes);
        drawDizzyStars(gs);
        drawAutoAimVisual(gs);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
drawArcAim(gs);
shapes.end();
        drawFlechesSol(gs);
     for (int i = 0; i < gs.flecheDrops.size; i++) {
    FlecheDrop fd = gs.flecheDrops.get(i);
    float pulse = 0.7f + 0.3f * MathUtils.sin(fd.life * 6f);
    shapes.begin(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(1f, 0.9f, 0.3f, 0.6f * pulse);
    shapes.circle(fd.pos.x, fd.pos.y, 14f, 16);
    shapes.setColor(1f, 0.85f, 0.2f, 0.9f);
    shapes.rect(fd.pos.x - 3f, fd.pos.y - 10f, 6f, 20f);
    shapes.triangle(
        fd.pos.x - 6f, fd.pos.y + 10f,
        fd.pos.x + 6f, fd.pos.y + 10f,
        fd.pos.x, fd.pos.y + 16f);
    shapes.end();
     }
     for (int i = 0; i < gs.weaponDrops.size; i++) {
            WeaponDrop d = gs.weaponDrops.get(i);
            Texture t = loadDropTexture(d.weapon.spriteName);
            if (t != null) {
                batch.begin();
                batch.setColor(Color.WHITE);
                batch.draw(t, d.pos.x - 16f, d.pos.y - 8f, 32f, 16f);
                batch.end();
            }
        }

        uiViewport.apply();
        shapes.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        gs.dungeon.drawMiniMap(shapes, MINIMAP_X, MINIMAP_Y);
        drawStick(gs.moveStick, 1f, 1f, 1f, 1f);
        drawStick(gs.aimStick, 1f, 0.5f, 0.5f, 0.35f);
        shapes.end();

        gs.zoom.draw(shapes, batch, font);
        drawOpenButton(gs);
        drawStunButton(gs);
        drawGlitchButton(gs);
        drawAutoAimButton(gs);
        drawXpBar(gs);
        drawAttentionGauge(gs);

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, gs.scoreText, 20, H - 20);
        font.draw(batch, gs.livesText, W - 160, H - 20);
        font.setColor(1f, 0.9f, 0.3f, 1f);
        font.draw(batch, "Fleches : " + gs.nbFleches + " / 50", W - 360, H - 50);
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        font.setColor(1f, 0.75f, 0.3f, 1f);
        font.draw(batch, "Etage " + gs.currentFloor + " / " + GameScreen.TOTAL_FLOORS,
                W / 2f - 80f, H - 20);
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
        batch.end();

        drawGlitchEffect(gs);

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
        }
    }
}
