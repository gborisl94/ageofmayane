package com.ageofmayane.game;

import com.badlogic.gdx.math.Vector2;

/**
 * Fleche tiree par le joueur.
 */
public class Fleche {

    public final Vector2 pos = new Vector2();
    public final Vector2 vel = new Vector2();
    public float angle = 0f;
    public float life = 1.5f;

    public Fleche() {
    }
}