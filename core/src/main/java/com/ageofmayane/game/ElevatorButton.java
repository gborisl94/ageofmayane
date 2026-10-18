package com.ageofmayane.game;

import com.badlogic.gdx.math.Rectangle;

public class ElevatorButton {

    private final Rectangle bounds;
    private String label = "MONTER";
    private boolean visible = false;
    private float cooldown = 0f;
    private static final float COOLDOWN_TIME = 0.4f;

    public ElevatorButton(float x, float y, float w, float h) {
        this.bounds = new Rectangle(x, y, w, h);
    }

    public Rectangle getBounds()   { return bounds; }
    public boolean isVisible()     { return visible; }
    public boolean isReady()       { return cooldown <= 0f; }
    public String getLabel()       { return label; }
    public void setLabel(String l) { this.label = l; }
    public void show()             { visible = true; }
    public void hide()             { visible = false; }

    public void update(float delta) { if (cooldown > 0f) cooldown -= delta; }

    public boolean handleClick(float screenX, float screenY) {
        if (!visible || cooldown > 0f) return false;
        if (!bounds.contains(screenX, screenY)) return false;
        cooldown = COOLDOWN_TIME;
        return true;
    }

    /** Rendu du rectangle (utilise ShapeRenderer deja en mode Filled). */
    public void renderShape(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapes) {
        if (!visible) return;
        shapes.setColor(0.1f, 0.7f, 0.2f, 0.9f);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}