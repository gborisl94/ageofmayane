package com.ageofmayane.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public enum Type { SLOW, FAST }
    public Vector2 position;
    public float speed;
    public float radius;
    public float hp;
    public Type type;

    public Enemy(float x, float y, Type type) {
        this.position = new Vector2(x, y);
        this.type = type;
        if (type == Type.SLOW) { speed = 90f; radius = 20f; hp = 1; }
        else                   { speed = 180f; radius = 15f; hp = 1; }
    }

    public void update(float delta, Vector2 target) {
        Vector2 dir = target.cpy().sub(position);
        if (dir.len2() > 0.01f) {
            dir.nor().scl(speed * delta);
            position.add(dir);
        }
    }

    public void render(ShapeRenderer sr) {
        sr.setColor(type == Type.FAST ? Assets.ENEMY_FAST : Assets.ENEMY);
        sr.circle(position.x, position.y, radius, 24);
    }
}