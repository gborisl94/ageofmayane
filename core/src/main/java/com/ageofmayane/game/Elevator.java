package com.ageofmayane.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Elevator {

    public enum State { LOCKED, UNLOCKED, ACTIVATED }

    private State state = State.LOCKED;
    private final Rectangle bounds;
    private float pulseTimer = 0f;

    public Elevator(float x, float y, float w, float h) {
        this.bounds = new Rectangle(x, y, w, h);
    }

    public void update(float delta, boolean unlockedCondition) {
        pulseTimer += delta;
        if (state == State.LOCKED && unlockedCondition) {
            state = State.UNLOCKED;
        }
    }

    public boolean tryActivate(Vector2 playerPos, float playerRadius) {
        if (state != State.UNLOCKED) return false;
        float cx = bounds.x + bounds.width / 2f;
        float cy = bounds.y + bounds.height / 2f;
        float r = bounds.width / 2f + playerRadius;
        if (playerPos.dst2(cx, cy) <= r * r) {
            state = State.ACTIVATED;
            return true;
        }
        return false;
    }

    public void forceUnlock() { if (state == State.LOCKED) state = State.UNLOCKED; }
    public void reset() { state = State.LOCKED; pulseTimer = 0f; }

    public State getState()      { return state; }
    public boolean isLocked()    { return state == State.LOCKED; }
    public boolean isUnlocked()  { return state == State.UNLOCKED; }
    public boolean isActivated() { return state == State.ACTIVATED; }
    public Rectangle getBounds() { return bounds; }

    public void render(ShapeRenderer shapes) {
        switch (state) {
            case LOCKED:    shapes.setColor(0.4f, 0.4f, 0.4f, 1f); break;
            case UNLOCKED:  shapes.setColor(0.2f, 1f, 0.4f, 1f);   break;
            case ACTIVATED: shapes.setColor(1f, 0.8f, 0.2f, 1f);   break;
        }
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}