package com.ageofmayane.game;

/** Une arme (arc) avec ses statistiques et sa durabilite. */
public class Weapon {

    public String name;
    public int damage;              // Degats par fleche
    public float fireDelay;         // Secondes entre 2 tirs
    public int shots;               // Nombre de fleches par tir
    public WeaponRarity rarity;
    public String spriteName;       // Nom du PNG (ex: "bow_fire.png")

    // Durabilite
    public int durability;
    public int maxDurability;

    // Prerequis
    public int levelRequired;       // Niveau minimum du joueur

    public Weapon() {
    }

    public Weapon(String name, int damage, float fireDelay, int shots,
                  WeaponRarity rarity, String spriteName,
                  int maxDurability, int levelRequired) {
        this.name = name;
        this.damage = damage;
        this.fireDelay = fireDelay;
        this.shots = shots;
        this.rarity = rarity;
        this.spriteName = spriteName;
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
        this.levelRequired = levelRequired;
    }

    public float getDurabilityPercent() {
        if (maxDurability <= 0) return 0f;
        return (float) durability / maxDurability;
    }

    public boolean isCritical() {
        return getDurabilityPercent() < 0.2f;
    }

    public boolean isBroken() {
        return durability <= 0;
    }

    public boolean canRepair() {
        return durability < maxDurability;
    }

    public Weapon copy() {
        Weapon w = new Weapon();
        w.name = name;
        w.damage = damage;
        w.fireDelay = fireDelay;
        w.shots = shots;
        w.rarity = rarity;
        w.spriteName = spriteName;
        w.maxDurability = maxDurability;
        w.durability = durability;
        w.levelRequired = levelRequired;
        return w;
    }
}
