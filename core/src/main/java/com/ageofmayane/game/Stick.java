package com.ageofmayane.game;

import com.badlogic.gdx.math.Vector2;

/** Joystick tactile flottant (espace UI). */
class Stick {
    int pointer = -1;
    final Vector2 origin = new Vector2();
    final Vector2 current = new Vector2();
    final Vector2 dir = new Vector2();

    boolean active() {
        return pointer != -1;
    }

    void begin(int p, float x, float y) {
        pointer = p;
        origin.set(x, y);
        current.set(x, y);
        dir.setZero();
    }

    void move(float x, float y) {
        float radius = GameScreen.STICK_RADIUS;
        dir.set(x - origin.x, y - origin.y);
        float len = dir.len();
        if (len > radius) {
            dir.scl(1f / len);
            current.set(origin).add(dir.x * radius, dir.y * radius);
        } else {
            current.set(x, y);
            dir.scl(1f / radius);
        }
    }

    void end() {
        pointer = -1;
        dir.setZero();
    }
}