package com.ageofmayane.game;

import com.badlogic.gdx.math.Vector2;

/** Une arme posee au sol, en attente d'etre ramassee par le joueur. */
public class WeaponDrop {

    public final Vector2 pos = new Vector2();
    public final Weapon weapon;
    public float pulseTime = 0f;
    public float lifeTime = 20f;

    public WeaponDrop(float x, float y, Weapon weapon) {
        this.pos.set(x, y);
        this.weapon = weapon;
    }

    public boolean update(float delta) {
        pulseTime += delta;
        lifeTime -= delta;
        return lifeTime > 0f;
    }
}
