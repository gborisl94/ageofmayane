package com.ageofmayane.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public enum VengeanceBonus {
    BERSERKER  ("Berserker",   "Degats +50%, vitesse +30% sous 50% PV"),
    VAMPIRIC   ("Vampirique",  "Soigne 20% des degats infliges"),
    SHIELDED   ("Blinde",      "Absorbe les 3 premieres attaques"),
    TELEPORTER ("Teleporteur", "Se teleporte pres du joueur toutes les 5s"),
    SUMMONER   ("Invocateur",  "Invoque 2 sbires toutes les 10s"),
    EXPLOSIVE  ("Explosif",    "Explose a sa mort (degats de zone)"),
    FROZEN     ("Glacial",     "Ralentit le joueur au contact"),
    REGENERATOR("Regenerant",  "Recupere 1% PV par seconde");

    public final String displayName;
    public final String description;

    VengeanceBonus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public static VengeanceBonus random(Array<VengeanceBonus> excluded) {
        Array<VengeanceBonus> available = new Array<VengeanceBonus>();
        for (VengeanceBonus b : values()) {
            if (excluded == null || !excluded.contains(b, true)) available.add(b);
        }
        if (available.size == 0) return BERSERKER;
        return available.get(MathUtils.random(available.size - 1));
    }
}