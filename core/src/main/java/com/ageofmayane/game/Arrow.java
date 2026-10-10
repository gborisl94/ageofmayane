package com.ageofmayane.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Arrow {
    public Vector2 position;
    public Vector2 velocity;
    public float radius = 6f;
    public boolean alive = true;

    public Arrow(Vector2 start, Vector2 dir) {
        position = start.cpy();
        velocity = dir.cpy().nor().scl(760f);
    }

    public void update(float delta) {
        position.add(velocity.cpy().scl(delta));
    }

    public void render(ShapeRenderer sr) {
        sr.setColor(Assets.ARROW);
        Vector2 tail = position.cpy().sub(velocity.cpy().nor().scl(14f));
        sr.rectLine(tail.x, tail.y, position.x, position.y, 3f);
        sr.circle(position.x, position.y, 3f, 12);
    }
}