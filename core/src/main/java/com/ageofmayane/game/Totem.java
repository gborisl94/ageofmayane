package com.ageofmayane.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Totem {
    public enum Type { FOUDRE, SOIN }
    
    public Vector2 pos = new Vector2();
    public Type type;
    public float hp = 5f;
    public float radius = 25f;
    public float timer = 0f;
    public boolean alive = true;

    public Totem(float x, float y, Type type) {
        this.pos.set(x, y);
        this.type = type;
    }

    public void update(GameScreen gs, float delta) {
        if (!alive) return;
        
        timer += delta;
        
        if (type == Type.FOUDRE) {
            // Tire un éclair toutes les 2.5 secondes
            if (timer >= 2.5f) {
                timer = 0f;
                // Inflige 10 dégâts au joueur s'il est dans la salle (pas d'esquive possible)
                if (gs.invulnerable <= 0f) {
                    gs.hp -= 10;
                    gs.damage.add(gs.playerPos.x, gs.playerPos.y + 30f, "-10", true);
                    gs.triggerShake();
                    gs.particles.spawnLightning(gs.playerPos.x, gs.playerPos.y, 0f, 10);
                    
                    if (gs.hp <= 0) {
                        gs.hp = 0;
                        gs.gameOver = true;
                        gs.endTimer = 0f;
                    }
                }
            }
        } else if (type == Type.SOIN) {
            // Soigne le joueur de 5 PV toutes les 3 secondes
            if (timer >= 3f) {
                timer = 0f;
                if (gs.hp < GameScreen.MAX_HP) {
                    gs.hp = Math.min(GameScreen.MAX_HP, gs.hp + 5);
                    gs.particles.spawnHalo(pos.x, pos.y, 40f, com.badlogic.gdx.graphics.Color.GREEN);
                }
            }
        }
    }

    public void render(ShapeRenderer shapes) {
        if (!alive) return;
        
        // Base du totem
        shapes.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapes.rect(pos.x - radius, pos.y - radius, radius * 2f, radius * 2f);
        
        // Couleur selon le type
        if (type == Type.FOUDRE) {
            shapes.setColor(1f, 0.2f, 0.2f, 1f); // Rouge
        } else {
            shapes.setColor(0.2f, 1f, 0.4f, 1f); // Vert
        }
        
        // Tête du totem (un cercle)
        shapes.circle(pos.x, pos.y + 10f, radius * 0.8f, 16);
    }
}