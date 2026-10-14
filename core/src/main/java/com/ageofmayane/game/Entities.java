package com.ageofmayane.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

class Fleche {
    final Vector2 pos = new Vector2();
    final Vector2 vel = new Vector2();
    float angle = 0f;
    float life = 1.5f;
    boolean fromEnemy = false;
    int damage = 1;
}
class FlecheSol {
    final Vector2 pos = new Vector2();
    float angle = 0f;

    FlecheSol(float x, float y, float a) {
        pos.set(x, y);
        angle = a;
    }

}

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
    float stunned = 0f;
    float stunBase = 0f;
    float hitPulse = 0f;
    float attackPulse = 0f;

    // --- AJOUTS POUR LA VENGEANCE ---
    boolean berserker        = false;
    int     shieldCharges    = 0;
    int     vengeanceLevel   = 0;
    Array<VengeanceBonus> vengeanceBonuses = null;
    float   teleportTimer    = 0f;
    float   summonTimer      = 0f;

    // --- AJOUTS POUR L'IA ---
    public int aiState = 0;
    public float patrolTimer = 0f;
    public float patrolDirX = 0f;
    public float patrolDirY = 0f;
}
