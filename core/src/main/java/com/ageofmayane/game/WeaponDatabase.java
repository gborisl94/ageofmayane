package com.ageofmayane.game;

/** Base de donnees des arcs disponibles dans le jeu. */
public class WeaponDatabase {

    private WeaponDatabase() {}

    public static Weapon woodBow() {
        return new Weapon("Arc en bois", 1, 0.18f, 1,
                WeaponRarity.COMMON, "bow_wood.png", 30, 1);
    }

    public static Weapon reinforcedBow() {
        return new Weapon("Arc renforce", 2, 0.18f, 1,
                WeaponRarity.COMMON, "bow_reinforced.png", 60, 2);
    }

    public static Weapon fastBow() {
        return new Weapon("Arc rapide", 1, 0.10f, 1,
                WeaponRarity.RARE, "bow_fast.png", 100, 3);
    }

    public static Weapon tripleBow() {
        return new Weapon("Arc triple", 1, 0.20f, 3,
                WeaponRarity.RARE, "bow_triple.png", 150, 4);
    }

    public static Weapon fireBow() {
        return new Weapon("Arc de feu", 2, 0.15f, 1,
                WeaponRarity.EPIC, "bow_fire.png", 200, 6);
    }

    public static Weapon iceBow() {
        return new Weapon("Arc de glace", 2, 0.18f, 1,
                WeaponRarity.EPIC, "bow_ice.png", 220, 7);
    }

    public static Weapon legendaryBow() {
        return new Weapon("Arc legendaire", 3, 0.12f, 1,
                WeaponRarity.LEGENDARY, "bow_legendary.png", 350, 9);
    }

    public static Weapon voidBow() {
        return new Weapon("Arc du neant", 4, 0.15f, 1,
                WeaponRarity.LEGENDARY, "bow_void.png", 400, 10);
    }

    public static Weapon brokenBow() {
        return new Weapon("Arc casse", 0, 0.30f, 1,
                WeaponRarity.COMMON, "bow_broken.png", 0, 0);
    }

    /** Arc aleatoire (pour les drops d'ennemis). */
    public static Weapon randomDrop() {
        float r = (float) Math.random();
        if (r < 0.50f) return woodBow();
        if (r < 0.75f) return reinforcedBow();
        if (r < 0.85f) return fastBow();
        if (r < 0.93f) return tripleBow();
        if (r < 0.97f) return fireBow();
        if (r < 0.99f) return iceBow();
        if (r < 0.995f) return legendaryBow();
        return voidBow();
    }

    /** Arc garanti Rare ou mieux (pour les coffres). */
    public static Weapon randomChestLoot() {
        float r = (float) Math.random();
        if (r < 0.60f) return fastBow();
        if (r < 0.75f) return tripleBow();
        if (r < 0.90f) return fireBow();
        if (r < 0.97f) return iceBow();
        if (r < 0.99f) return legendaryBow();
        return voidBow();
    }
}