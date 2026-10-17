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
    private void drawArcTensionText(GameScreen gs) {
        if (!gs.arcBtnActive) return;
        float bx = GameScreen.ARC_BTN_X;
        float by = GameScreen.ARC_BTN_Y;
        float bs = GameScreen.ARC_BTN_SIZE;
        batch.begin();
        font.getData().setScale(1.3f);
        font.setColor(1f, 1f, 1f, 1f);
        String txt = gs.arcBtnNiveau + " / 3";
        layout.setText(font, txt);
        font.draw(batch, txt, bx + (bs - layout.width) / 2f, by + bs - 10f);
        font.getData().setScale(2f);
        batch.end();
    }

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
        if (t != null) dropCache.put(name, t);
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

    private void drawStick(Stick s, float r, float g, float b) {
        if (!s.active()) return;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setColor(r, g, b, 0.08f);
        shapes.circle(s.origin.x, s.origin.y, GameScreen.STICK_RADIUS, 32);
        shapes.setColor(r, g, b, 0.20f);
        shapes.circle(s.current.x, s.current.y, 45f, 24);
    }

    private void drawArcTensionButton(GameScreen gs) {
        float bx = GameScreen.ARC_BTN_X;
        float by = GameScreen.ARC_BTN_Y;
        float bs = GameScreen.ARC_BTN_SIZE;
        boolean actif = gs.arcBtnActive;
        int niveau = gs.arcBtnNiveau;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.05f, 0.05f, 0.1f, 0.9f);
        shapes.rect(bx - 3f, by - 3f, bs + 6f, bs + 6f);

        if (actif) {
            if (niveau == 3) shapes.setColor(1f, 0.3f, 0.1f, 0.95f);
            else if (niveau == 2) shapes.setColor(1f, 0.7f, 0.1f, 0.95f);
            else shapes.setColor(1f, 0.9f, 0.2f, 0.95f);
        } else {
            shapes.setColor(0.3f, 0.3f, 0.4f, 0.9f);
        }
        shapes.rect(bx, by, bs, bs);

        shapes.setColor(0.1f, 0.1f, 0.15f, 0.5f);
        shapes.rect(bx + bs * 0.2f, by + bs * 0.4f, bs * 0.6f, 4f);
        shapes.rect(bx + bs * 0.45f, by + bs * 0.2f, 4f, bs * 0.6f);
        shapes.end();
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
        float time = Gdx.graphics.getFrameId() * 0.05f; 
        
        batch.begin();
        for (int i = 0; i < gs.flechesSol.size; i++) {
            FlecheSol fs = gs.flechesSol.get(i);
            float pulse = 1f + 0.1f * MathUtils.sin(time * 5f);
            float currentW = w * pulse;
            float currentH = h * pulse;
            float alpha = 0.7f + 0.3f * MathUtils.sin(time * 5f);
            batch.setColor(1f, 1f, 0.5f, alpha); 
            
            batch.draw(texArrow,
                fs.pos.x - currentW / 2f, fs.pos.y - currentH / 2f,
                currentW / 2f, currentH / 2f, currentW, currentH,
                1f, 1f, fs.angle,
                0, 0, texArrow.getWidth(), texArrow.getHeight(),
                false, false);
                
            batch.setColor(1f, 0.9f, 0.2f, 0.2f * alpha);
            batch.draw(texArrow,
                fs.pos.x - currentW * 0.7f, fs.pos.y - currentH * 0.7f,
                currentW * 1.4f / 2f, currentH * 1.4f / 2f, currentW * 1.4f, currentH * 1.4f,
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
        float r = target.rad