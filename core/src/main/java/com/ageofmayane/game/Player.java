package com.ageofmayane.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Player {
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public Vector2 aim = new Vector2(1, 0);
    public float speed = 320f;
    public float radius = 22f;
    public float fireCooldown = 0f;
    public float fireRate = 0.22f;
    public int hp = 3;
    public float invulnTimer = 0f;

    public void update(float delta) {
        position.add(velocity.cpy().scl(delta));
        if (fireCooldown > 0) fireCooldown -= delta;
        if (invulnTimer > 0) invulnTimer -= delta;
    }

    public boolean canFire() { return fireCooldown <= 0f; }
    public void onFire()     { fireCooldown = fireRate; }
    public boolean isInvulnerable() { return invulnTimer > 0f; }

    public void render(ShapeRenderer sr) {
        boolean blink = isInvulnerable() && ((int)(invulnTimer * 20) % 2 == 0);
        if (!blink) {
            sr.setColor(Assets.PLAYER);
            sr.circle(position.x, position.y, radius, 32);
        }
        Vector2 tip = position.cpy().add(aim.cpy().nor().scl(radius + 14));
        sr.setColor(Assets.PLAYER_AIM);
        sr.rectLine(position.x, position.y, tip.x, tip.y, 4f);
    }
}