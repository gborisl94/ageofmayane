package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;

/** Rarete d'une arme : determine la couleur et les bonus. */
public enum WeaponRarity {

    COMMON(0.7f, 0.7f, 0.7f),        // Gris clair
    RARE(0.3f, 0.6f, 1.0f),          // Bleu
    EPIC(0.7f, 0.3f, 1.0f),          // Violet
    LEGENDARY(1.0f, 0.8f, 0.2f);     // Or

    public final Color color;

    WeaponRarity(float r, float g, float b) {
        this.color = new Color(r, g, b, 1f);
    }

    public Color colorWithAlpha(float alpha) {
        return new Color(color.r, color.g, color.b, alpha);
    }
}
