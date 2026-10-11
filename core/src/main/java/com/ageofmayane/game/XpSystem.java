package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Gere l'experience, les niveaux et la sauvegarde du joueur. */
public class XpSystem {

    private static final String PREFS_NAME = "AgeOfMayane";
    private static final String KEY_LEVEL = "player_level";
    private static final String KEY_XP = "player_xp";

    public int level = 1;
    public int xp = 0;
    public int xpRequired = 50;
    public int pendingLevelUps = 0;   // Pour les notifications

    private final Preferences prefs;

    public XpSystem() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        load();
    }

    /** Charge les valeurs sauvegardees. */
    private void load() {
        try {
            level = prefs.getInteger(KEY_LEVEL, 1);
            xp = prefs.getInteger(KEY_XP, 0);
        } catch (Throwable t) {
            level = 1;
            xp = 0;
        }
        if (level < 1) level = 1;
        if (xp < 0) xp = 0;
        xpRequired = 50 * level;
    }

    /** Sauvegarde les valeurs. */
    private void save() {
        try {
            prefs.putInteger(KEY_LEVEL, level);
            prefs.putInteger(KEY_XP, xp);
            prefs.flush();
        } catch (Throwable t) {
            Gdx.app.error("Mayane", "Erreur sauvegarde XP", t);
        }
    }

    /** Ajoute de l'XP et gere la montee de niveau. */
    public void addXp(int amount) {
        if (amount <= 0) return;
        xp += amount;
        while (xp >= xpRequired && level < 99) {
            xp -= xpRequired;
            level++;
            pendingLevelUps++;
            xpRequired = 50 * level;
        }
        save();
    }

    /** Ratio 0..1 pour dessiner la barre. */
    public float getProgress() {
        if (xpRequired <= 0) return 0f;
        return Math.min(1f, (float) xp / xpRequired);
    }

    /** Consomme une notification de level-up (renvoie true si dispo). */
    public boolean consumeLevelUp() {
        if (pendingLevelUps > 0) {
            pendingLevelUps--;
            return true;
        }
        return false;
    }

    /** Remet tout a zero (utile pour debug). */
    public void reset() {
        level = 1;
        xp = 0;
        xpRequired = 50;
        pendingLevelUps = 0;
        save();
    }
            }
