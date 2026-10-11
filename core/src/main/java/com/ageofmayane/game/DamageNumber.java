package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Un nombre de degats flottant qui apparait quand on touche un ennemi.
 * Il monte doucement puis disparait apres un court delai.
 */
public class DamageNumber {

    public final Vector2 pos = new Vector2();
    public final String text;
    public float life = 0.8f;      // duree de vie en secondes
    public float maxLife = 0.8f;
    public float speedY = 90f;     // vitesse de montee
    public final Color color = new Color(1f, 0.9f, 0.2f, 1f);  // jaune

    public DamageNumber(float x, float y, String text) {
        this.pos.set(x, y);
        this.text = text;
    }

    /** Renvoie true si encore vivant. */
    public boolean update(float delta) {
        pos.y += speedY * delta;
        life -= delta;
        return life > 0f;
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        float alpha = Math.max(0f, life / maxLife);
        Color old = font.getColor().cpy();
        font.setColor(color.r, color.g, color.b, alpha);
        font.draw(batch, text, pos.x - 8f, pos.y);
        font.setColor(old);
    }
}