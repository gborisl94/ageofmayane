package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
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
    public float attackTimer = 0f; // Timer séparé pour les tirs (toutes les 1.5s)
    public boolean alive = true;

    public Totem(float x, float y, Type type) {
        this.pos.set(x, y);
        this.type = type;
    }

    public void update(GameScreen gs, float delta) {
        if (!alive) return;
      System.out.println("Totem en vie ! Timer : " + timer + " AttackTimer : " + attackTimer);
        
        timer += delta; // Sert à calculer la durée de vie (10 secondes)
        attackTimer += delta; // Sert à calculer le temps entre deux tirs
        
        if (type == Type.FOUDRE) {
            // Tire un éclair toutes les 1.5 secondes
            if (attackTimer >= 1.5f) {
                attackTimer = 0f;
                
                // Cherche l'ennemi le plus proche dans un rayon de 450 pixels
                Ennemi target = null;
                float minDist = 450f; 
                for (int i = 0; i < gs.ennemis.size; i++) {
                    Ennemi e = gs.ennemis.get(i);
                    float d = e.pos.dst(pos);
                    if (d < minDist) {
                        minDist = d;
                        target = e;
                    }
                }
                
                // Si un ennemi est trouvé, on le foudroie !
                if (target != null) {
                    if (target != null) {
    // Inflige des dégâts
    target.hp -= 2; 
    gs.damage.add(target.pos.x, target.pos.y + 20f, "-2", false); 
    drawLightningBolt(gs, pos.x, pos.y + 10f, target.pos.x, target.pos.y);
    gs.particles.spawnLightning(target.pos.x, target.pos.y, 0f, 4);

    // --- VERIFICATION DE MORT ---
    if (target.hp <= 0) {
        // On le tue et on le retire de la liste !
        gs.particles.spawnEnemyExplosion(target.pos.x, target.pos.y, new Color(0.2f, 0.9f, 0.9f, 1f));
        gs.particles.spawnBlood(target.pos.x, target.pos.y, 10);
        gs.ennemis.removeValue(target, true);
        
        gs.setScore(gs.getScore() + 1);
        gs.xpSystem.addXp(GameScreen.XP_PER_ENEMY);
        
        // Fait tomber une flèche
        gs.flechesSol.add(new FlecheSol(target.pos.x, target.pos.y, MathUtils.random(0f, 360f)));
    }
} // Affiche "-2"
                    
                    // Dessine un vrai éclair entre le totem et l'ennemi
                    drawLightningBolt(gs, pos.x, pos.y + 10f, target.pos.x, target.pos.y);
                    
                    // Ajoute quelques particules d'impact sur l'ennemi
                    gs.particles.spawnLightning(target.pos.x, target.pos.y, 0f, 4);
                }
            }
        } else if (type == Type.SOIN) {
            // Soigne le joueur de 5 PV toutes les 2 secondes
            if (attackTimer >= 2f) {
                attackTimer = 0f;
                if (gs.hp < GameScreen.MAX_HP) {
                    gs.hp = Math.min(GameScreen.MAX_HP, gs.hp + 5);
                    gs.particles.spawnHalo(pos.x, pos.y, 40f, Color.GREEN);
                }
            }
        }
    }

    // Méthode pour dessiner un éclair en zigzag
    private void drawLightningBolt(GameScreen gs, float x1, float y1, float x2, float y2) {
        // On ajoute beaucoup de particules de type "éclair" pour faire un effet de trait
        int steps = 8;
        for (int i = 0; i < steps; i++) {
            float t = i / (float) (steps - 1);
            float cx = x1 + (x2 - x1) * t;
            float cy = y1 + (y2 - y1) * t;
            
            // Ajout d'un petit décalage aléatoire pour l'effet zigzag
            if (i != 0 && i != steps - 1) {
                cx += MathUtils.random(-15f, 15f);
                cy += MathUtils.random(-15f, 15f);
            }
            gs.particles.spawnLightning(cx, cy, 0f, 1);
        }
    }

    public void render(ShapeRenderer shapes) {
        if (!alive) return;
        
        // Effet de base (un carré sombre qui pulse)
        float pulse = 1f + 0.1f * MathUtils.sin(timer * 6f);
        float r = radius * pulse;
        
        shapes.setColor(0.2f, 0.2f, 0.2f, 0.8f); // Gris foncé
        shapes.rect(pos.x - r, pos.y - r, r * 2f, r * 2f);
        
        // Couleur de la tête selon le type
        if (type == Type.FOUDRE) {
            shapes.setColor(1f, 0.1f, 0.1f, 1f); // Rouge vif
        } else {
            shapes.setColor(0.2f, 1f, 0.4f, 1f); // Vert vif
        }
        
        // Tête du totem (un cercle)
        shapes.circle(pos.x, pos.y + 10f, radius * 0.8f, 16);
        
        // Petit point blanc au centre pour faire "œil" ou "cristal"
        shapes.setColor(1f, 1f, 1f, 0.8f);
        shapes.circle(pos.x, pos.y + 10f, radius * 0.3f, 12);
    }
}