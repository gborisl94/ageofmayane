package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ZoomController {

    // --- Configuration ---
    public static final float ZOOM_MIN = 0.5f;
    public static final float ZOOM_MAX = 2.0f;
    public static final float ZOOM_STEP = 1.5f;

    // Positions et tailles (dans l'espace écran, W x H)
    public static final float BTN_SIZE = 70f;
    public static final float BTN_MARGIN = 30f;
    public static final float BTN_TOP_OFFSET = 80f;
    public static final float BTN_SPACING = 15f;

    private final float screenW;
    private final float screenH;

    private float zoom = 1f;
    private boolean plusPressed = false;
    private boolean minusPressed = false;

    public ZoomController(float screenW, float screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
    }

    // --- Positions des boutons (espace écran) ---

    public float plusCX() {
        return screenW - BTN_MARGIN - BTN_SIZE / 2f;
    }
    public float plusCY() {
        return screenH - BTN_TOP_OFFSET - BTN_SIZE / 2f;
    }
    public float minusCX() {
        return screenW - BTN_MARGIN - BTN_SIZE - BTN_SPACING - BTN_SIZE / 2f;
    }
    public float minusCY() {
        return screenH - BTN_TOP_OFFSET - BTN_SIZE / 2f;
    }

    // --- Interaction ---

    /**
     * À appeler une fois par frame avant de traiter les touches.
     */
    public void resetFrame() {
        plusPressed = false;
        minusPressed = false;
    }

    /**
     * Teste si la touche (en coordonnées écran) est sur un bouton.
     * @return true si le toucher a été consommé.
     */
    public boolean handleTouch(float screenX, float screenY, float delta) {
        if (isInside(screenX, screenY, plusCX(), plusCY())) {
            plusPressed = true;
            applyZoom(delta);
            return true;
        }
        if (isInside(screenX, screenY, minusCX(), minusCY())) {
            minusPressed = true;
            applyZoom(delta);
            return true;
        }
        return false;
    }

    /**
     * À appeler si le bouton reste maintenu (déjà géré dans handleTouch,
     * mais utile si tu veux gérer le maintien séparément).
     */
    public void applyZoom(float delta) {
        if (plusPressed) {
            zoom = Math.min(ZOOM_MAX, zoom + ZOOM_STEP * delta);
        }
        if (minusPressed) {
            zoom = Math.max(ZOOM_MIN, zoom - ZOOM_STEP * delta);
        }
    }

    // --- Rendu ---

    /**
     * Dessine les 2 boutons + les symboles + et -.
     * Doit être appelé avec une projectionMatrix en ESPACE ÉCRAN.
     */
    public void draw(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        // Fond des boutons
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Bouton +
        shapes.setColor(plusPressed ? new Color(0.3f, 0.9f, 0.3f, 0.95f)
                                    : new Color(0.15f, 0.55f, 0.15f, 0.9f));
        shapes.circle(plusCX(), plusCY(), BTN_SIZE / 2f, 40);

        // Bouton -
        shapes.setColor(minusPressed ? new Color(0.9f, 0.3f, 0.3f, 0.95f)
                                     : new Color(0.55f, 0.15f, 0.15f, 0.9f));
        shapes.circle(minusCX(), minusCY(), BTN_SIZE / 2f, 40);

        shapes.end();

        // Symboles + et - (en blanc, centrés)
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "+", plusCX() - 10f, plusCY() + 12f);
        font.draw(batch, "-", minusCX() - 6f, minusCY() + 12f);
        batch.end();
    }

    // --- Application à la caméra ---

    /**
     * Applique le zoom à la caméra du MONDE (pas à la caméra UI).
     */
    public void applyToCamera(OrthographicCamera camera) {
        camera.viewportWidth = screenW;
        camera.viewportHeight = screenH;
        camera.zoom = 1f / zoom;
        camera.position.set(screenW / 2f, screenH / 2f, 0f);
        camera.update();
    }

    // --- Helpers ---

    private boolean isInside(float px, float py, float cx, float cy) {
        float dx = px - cx;
        float dy = py - cy;
        float r = BTN_SIZE / 2f;
        return (dx * dx + dy * dy) < (r * r);
    }

    public float getZoom() { return zoom; }

    public void setZoom(float z) {
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, z));
    }

    public void reset() { zoom = 1f; }
}