package com.ageofmayane.game;

public class Objectifs {

    public static final int TYPE_ENNEMIS = 0;
    public static final int TYPE_COFFRES = 1;
    public static final int TYPE_ETAGE   = 2;

    public static class Objectif {
        public int type;
        public int cible;
        public int progression;
        public boolean termine;
        public boolean recompenseDonnee;
    }
public final com.badlogic.gdx.utils.Array<Objectif> liste
    = new com.badlogic.gdx.utils.Array<Objectif>();

public void init() {
    liste.clear();
    liste.add(creer(TYPE_ENNEMIS, 10));
    liste.add(creer(TYPE_COFFRES, 2));
    liste.add(creer(TYPE_ETAGE,   1));
}

private Objectif creer(int type, int cible) {
    Objectif o = new Objectif();
    o.type = type;
    o.cible = cible;
    o.progression = 0;
    o.termine = false;
    o.recompenseDonnee = false;
    return o;
}
public void incrEnnemi() {
    for (Objectif o : liste) {
        if (o.type == TYPE_ENNEMIS && !o.termine) {
            o.progression++;
            if (o.progression >= o.cible) o.termine = true;
        }
    }
}

public void incrCoffre() {
    for (Objectif o : liste) {
        if (o.type == TYPE_COFFRES && !o.termine) {
            o.progression++;
            if (o.progression >= o.cible) o.termine = true;
        }
    }
}
    public void atteindreEtage() {
        for (Objectif o : liste) {
            if (o.type == TYPE_ETAGE && !o.termine) {
                o.progression = 1;
                o.termine = true;
            }
        }
    }

    public int recupererRecompenses() {
        int xp = 0;
        for (Objectif o : liste) {
            if (o.termine && !o.recompenseDonnee) {
                o.recompenseDonnee = true;
                xp += 30;
            }
        }
        return xp;
    }
}
