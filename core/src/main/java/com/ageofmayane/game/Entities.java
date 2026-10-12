package com.ageofmayane.game;

import com.badlogic.gdx.math.Vector2;

/** Une fleche tiree par le joueur ou par le boss. */
class Fleche {
    final Vector2 pos = new Vector2();
    final Vector2 vel = new Vector2();
    float angle = 0f;
    float life = 1.5f;
    boolean fromEnemy = false;
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

    boolean fleeing = false;
    float fleeTimer = 0f;

    int phase = 1;
    float attackCooldown = 0f;
    float spawnCooldown = 0f;
    float phaseChangeFlash = 0f;

    // Etourdissement (capacite STUN du joueur)
    float stunned = 0f;
    float stunBase = 0f;
}