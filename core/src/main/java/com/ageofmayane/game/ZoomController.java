package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ZoomController {

    public static final float ZOOM_MIN = 0.5f;
    public static final float ZOOM_MAX = 2.0f;
    public static final float ZOOM_STEP = 1.5f;
    public static final float BTN_SIZE = 70f;
    public static final float BTN_MARGIN = 30f;
    public static final float BTN_TOP_OFFSET = 80f;

    private float zoom = 1f;
    private boolean plusPressed = false;
    private boolean minusPressed = false;

    private final float worldW;
    private final float worldH;

    public ZoomController(float worldW, float worldH) {
        this.worldW = worldW;
        this.worldH = worldH;
    }

    public boolean handleTouch(float worldX, float worldY, float delta) {
        if (isInside(worldX, worldY, plusCX(), plusCY())) {
            plusPressed = true;
            applyZoom(delta);
            return true;
        }
        if (isInside(worldX, worldY, minusCX(), minusCY())) {
            minusPressed = true;
            applyZoom(delta);
            return true;
        }
        return false;
    }

    public void resetFrame() {
        plusPressed = false;
        minusPressed = false;
    }

    public void applyZoom(float delta) {
        if (plusPressed) {
            zoom = Math.min(ZOOM_MAX, zoom + ZOOM_STEP * delta);
        }
        if (minusPressed) {
            zoom = Math.max(ZOOM_MIN, zoom - ZOOM_STEP * delta);
        }
    }

    public void applyToCamera(OrthographicCamera camera) {
        camera.viewportWidth = worldW;
        camera.viewportHeight = worldH;
        camera.zoom = 1f / zoom;
        camera.position.set(worldW / 2f, worldH / 2f, 0f);
        camera.update();
    }

    public void draw(ShapeRenderer shapes, SpriteBatch batch, BitmapFont font) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(plusPressed ? new Color(0.4f, 0.9f, 0.4f, 0.95f)
                                    : new Color(0.15f, 0.55f, 0.15f, 0.85f));
        shapes.circle(plusCX(), plusCY(), BTN_SIZE / 2f, 32);

        shapes.setColor(minusPressed ? new Color(0.9f, 0.4f, 0.4f, 0.95f)
                                     : new Color(0.55f, 0.15f, 0.15f, 0.85f));
        shapes.circle(minusCX(), minusCY(), BTN_SIZE / 2f, 32);

        shapes.end();

        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "+", plusCX() - 12f, plusCY() + 14f);
        font.draw(batch, "-", minusCX() - 8f, minusCY() + 14f);
        batch.end();
    }

    private boolean isInside(float px, float py, float cx, float cy) {
        float dx = px - cx;
        float dy = py - cy;
        return (dx * dx + dy * dy) < (BTN_SIZE / 2f) * (BTN_SIZE / 2f);
    }

    private float plusCX() {
        return worldW - BTN_MARGIN - BTN_SIZE / 2f;
    }
    private float plusCY() {
        return worldH - BTN_TOP_OFFSET - BTN_SIZE / 2f;
    }
    private float minusCX() {
        return worldW - BTN_MARGIN - BTN_SIZE * 1.5f - 10f;
    }
    private float minusCY() {
        return worldH - BTN_TOP_OFFSET - BTN_SIZE / 2f;
    }

    public float getZoom() { return zoom; }
    public void setZoom(float z) { zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, z)); }
    public void reset() { zoom = 1f; }
}