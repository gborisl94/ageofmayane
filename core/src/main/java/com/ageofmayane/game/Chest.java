package com.ageofmayane.game;

import com.badlogic.gdx.math.Vector2;

/** Un coffre au tresor, place dans une salle du donjon. */
public class Chest {

    public final Vector2 pos = new Vector2();
    public boolean opened = false;
    public float openAnim = 0f;
    public float pulseTime = 0f;

    public Chest(float x, float y) {
        this.pos.set(x, y);
    }

    public void update(float delta) {
        pulseTime += delta;
        if (opened && openAnim < 1f) {
            openAnim += delta * 3f;
            if (openAnim > 1f) openAnim = 1f;
        }
    }

    public void open() {
        if (!opened) {
            opened = true;
            openAnim = 0f;
        }
    }
}