package com.ageofmayane.game;

import com.badlogic.gdx.math.Vector2;

/** Une fleche tiree par le joueur. */
class Fleche {
    final Vector2 pos = new Vector2();
    final Vector2 vel = new Vector2();
    float angle = 0f;
    float life = 1.5f;
}

/** Un ennemi (ou le boss). */
class Ennemi {
    final Vector2 pos = new Vector2();
    float angle = 0f;
    boolean boss = false;
    int hp = 1;
    int maxHp = 1;
    float radius = GameScreen.ENEMY_RADIUS;
    float size = GameScreen.ENEMY_SIZE;

    // Etat de fuite (pour le champion)
    boolean fleeing = false;
    float fleeTimer = 0f;
}