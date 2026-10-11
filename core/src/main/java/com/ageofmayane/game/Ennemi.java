package com.ageofmayane.game;

import com.badlogic.gdx.math.Vector2;

/**
 * Ennemi du jeu (normal ou boss).
 * Utilise par GameScreen et GameRenderer.
 */
public class Ennemi {

    public final Vector2 pos = new Vector2();
    public float angle = 0f;
    public int hp = 1;
    public int maxHp = 1;
    public float radius = 18f;
    public float size = 56f;
    public boolean boss = false;

    public Ennemi() {
    }

    public Ennemi(float x, float y) {
        pos.set(x, y);
    }
}