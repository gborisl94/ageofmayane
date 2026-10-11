package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Gere le zoom (boutons + et -) en espace ECRAN.
 *
 * Utilisation :
 *   - chaque frame : resetFrame(), puis handleTouch(x, y) pour chaque toucher
 *     (coordonnees converties avec la camera UI), puis update(delta)
 *   - applyToCamera(...) applique le zoom a la camera du MONDE
 *   - draw(...) dessine les boutons : les matrices de projection de
 *     shapes et batch doivent deja etre celles de la camera UI (fixe)
 */
public class ZoomController {

    // ----- Constantes configurables -----
    public static final float MIN_ZOOM = 0.5f;
    public static final float MAX_ZOOM = 2.0f;
    public static final float ZOOM_SPEED = 1.5f;      // puissances de 2 par seconde
    public static final float BUTTON_SIZE = 70f;
    public static final float MARGIN_RIGHT = 30f;
    public static final float OFFSET_BELOW_HUD = 80f;
    public static final float BUTTON_GAP = 16f;
    public static final float TOUCH_PADDING = 6f;      // zone tactile un peu plus large

    private final float minusX;
    private final float plusX;
    private final float buttonY;

    private float level = 1f;      // 1 = normal, 2 = zoom x2, 0.5 = dezoom
    private boolean plusHeld;
    private boolean minusHeld;

    private final GlyphLayout layout = new GlyphLayout();
    private String label = "x1.0";
    private int labelTenths = 10;

    /** screenW / screenH : taille de l'espace UI (ici 1280 x 720). */
    public ZoomController(float screenW, float screenH) {
        plusX = screenW - MARGIN_RIGHT - BUTTON_SIZE;
        minusX = plusX - BUTTON_GAP - BUTTON_SIZE;
        buttonY = screenH - OFFSET_BELOW_HUD - BUTTON_SIZE;
    }

    // ---------------------------------------------------------------
    // Entrees
    // ---------------------------------------------------------------

    /** A appeler une fois par frame, avant les handleTouch. */
    public void resetFrame() {
        plusHeld = false;
        minusHeld = false;
    }

    /**
     * x, y en espace UI. Renvoie true si le toucher est sur un bouton
     * (le toucher est alors "consomme").
     */
    public boolean handleTouch(float x, float y) {
        if (inside(x, y, plusX)) {
            plusHeld = true;
            return true;
        }
        if (inside(x, y, minusX)) {
            minusHeld = true;
            return true;
        }
        return false;
    }

    private boolean inside(float x, float y, float bx) {
        return x >= bx - TOUCH_PADDING && x <= bx + BUTTON_SIZE + TOUCH_PADDING
                && y >= buttonY - TOUCH_PADDING && y <= buttonY + BUTTON_SIZE + TOUCH_PADDING;
    }

    // ---------------------------------------------------------------
    // Mise a jour
    // ---------------------------------------------------------------

    /** Applique le zoom continu tant qu'un bouton est maintenu. */
    public void update(float delta) {
        if (!(delta > 0f)) return; // ignore aussi NaN
        if (plusHeld && !minusHeld) {
            level *= (float) Math.pow(2.0, ZOOM_SPEED * delta);
        } else if (minusHeld && !plusHeld) {
            level /= (float) Math.pow(2.0, ZOOM_SPEED * delta);
        }
        level = MathUtils.clamp(level, MIN_ZOOM, MAX_ZOOM);
    }

    /**
     * Applique le zoom a la camera du monde. La camera suit (targetX, targetY)
     * sans jamais sortir du monde ; si on dezoome assez pour voir tout le
     * monde, elle reste centree.
     */
    public void applyToCamera(OrthographicCamera cam, float targetX, float targetY,
                              float worldW, float worldH) {
        cam.zoom = 1f / level;
        float halfW = worldW * 0.5f * cam.zoom;
        float halfH = worldH * 0.5f * cam.zoom;

        float cx = (halfW >= worldW * 0.5f)
                ? worldW * 0.5f
                : MathUtils.clamp(targetX, halfW, worldW - halfW);
        float cy = (halfH >= worldH * 0.5f)
                ? worldH * 0.5f
                : MathUtils.clamp(targetY, halfH, worldH - halfH);

        cam.position.set(cx, cy, 0f);
        cam.update();
    }

    // ---------------------------------------------------------------
    // Affichage
    // ---------------------------------------------------------------

    public void draw(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float aPlus = (level >= MAX_ZOOM - 0.001f) ? 0.25f : (plusHeld ? 0.95f : 0.6f);
        float aMinus = (level <= MIN_ZOOM + 0.001f) ? 0.25f : (minusHeld ? 0.95f : 0.6f);

        float cy = buttonY + BUTTON_SIZE * 0.5f;
        float plusCx = plusX + BUTTON_SIZE * 0.5f;
        float minusCx = minusX + BUTTON_SIZE * 0.5f;
        float bar = BUTTON_SIZE * 0.12f;
        float len = BUTTON_SIZE * 0.5f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Fonds
        shapes.setColor(0.20f, 0.75f, 0.30f, aPlus);
        shapes.rect(plusX, buttonY, BUTTON_SIZE, BUTTON_SIZE);
        shapes.setColor(0.85f, 0.25f, 0.25f, aMinus);
        shapes.rect(minusX, buttonY, BUTTON_SIZE, BUTTON_SIZE);

        // Symboles
        shapes.setColor(1f, 1f, 1f, 0.95f);
        shapes.rect(minusCx - len / 2f, cy - bar / 2f, len, bar);          // -
        shapes.rect(plusCx - len / 2f, cy - bar / 2f, len, bar);           // +
        shapes.rect(plusCx - bar / 2f, cy - len / 2f, bar, len);

        shapes.end();

        // Niveau de zoom sous les boutons
        int tenths = Math.round(level * 10f);
        if (tenths != labelTenths) {
            labelTenths = tenths;
            label = "x" + (tenths / 10) + "." + (tenths % 10);
        }
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, label);
        float midX = (minusX + plusX + BUTTON_SIZE) * 0.5f;
        font.draw(batch, label, midX - layout.width * 0.5f, buttonY - 8f);
        batch.end();
    }

    // ---------------------------------------------------------------
    // Divers
    // ---------------------------------------------------------------

    public float getLevel() {
        return level;
    }

    public void setLevel(float l) {
        level = MathUtils.clamp(l, MIN_ZOOM, MAX_ZOOM);
    }

    public void reset() {
        level = 1f;
        plusHeld = false;
        minusHeld = false;
    }
}