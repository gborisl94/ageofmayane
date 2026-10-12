package com.ageofmayane.game;

import com.badlogic.gdx.utils.Array;

public class VengeanceManager {

    private static VengeanceManager instance;

    private int level = 0;
    private final Array<VengeanceBonus> bonuses = new Array<VengeanceBonus>();
    private boolean pendingRespawn = false;

    private VengeanceManager() {}

    public static VengeanceManager get() {
        if (instance == null) instance = new VengeanceManager();
        return instance;
    }

    public int getLevel()                     { return level; }
    public Array<VengeanceBonus> getBonuses() { return bonuses; }
    public boolean isPending()                { return pendingRespawn; }

    public void registerFlee() {
        level++;
        VengeanceBonus b = VengeanceBonus.random(bonuses);
        bonuses.add(b);
        pendingRespawn = true;
    }

    public boolean consumePending() {
        if (!pendingRespawn) return false;
        pendingRespawn = false;
        return true;
    }

    public float getStatMultiplier() { return 1f + 0.30f * level; }

    public boolean has(VengeanceBonus b) { return bonuses.contains(b, true); }

    public void clear() {
        level = 0;
        bonuses.clear();
        pendingRespawn = false;
    }
}