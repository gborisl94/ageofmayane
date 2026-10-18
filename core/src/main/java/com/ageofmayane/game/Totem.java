package com.ageofmayane.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.Comparator;

public class Totem {
    public enum Type { FOUDRE, SOIN }
    
    public Vector2 pos = new Vector2();
    public Type type;
    public float hp = 5f;
    public float radius = 25f;
    public float timer = 0f;
    public float attackTimer = 0f; // Timer séparé pour les tirs
    public boolean alive = true;

    public Totem(float x, float y, Type type) {
        this.pos.set(x, y);
        this.type = type;
    public float hp = 20f; // Vie du totem
public float maxHp = 20f; // Vie max pour la jauge
public float hitTimer = 0f; // Pour éviter que les ennemis tapent 60 fois par seconde
    }

    public void update(GameScreen gs, float delta) {
        if (!alive) return;
        
        // On avance les deux timers
        timer += delta; 
        attackTimer += delta; 
        
        if (type == Type.FOUDRE) {
            // Tire un éclair toutes les 1.5 secondes
            if (attackTimer >= 1.5f) {
                attackTimer = 0f;
                
                // --- CALCUL DE LA PROGRESSION (NIVEAU DU JOUEUR) ---
                int playerLevel = gs.xpSystem.level;
                // Portée de base : 300. Augmente de 50 par niveau (max 800)
                float range = Math.min(800f, 300f + (playerLevel - 1) * 50f);
                // Nombre de cibles : 1 au niv 1, +1 tous les 2 niveaux, max 6
                int maxTargets = Math.min(6, 1 + (playerLevel / 2)); 
                
                // On cherche les ennemis dans la portée
                Array<Ennemi> targets = new Array<Ennemi>();
                for (int i = 0; i < gs.ennemis.size; i++) {
                    Ennemi e = gs.ennemis.get(i);
                    if (e.hp <= 0) continue;
                    float d = e.pos.dst(pos);
                    if (d < range) {
                        targets.add(e);
                    }
                }
                
                // On trie les cibles par distance (les plus proches en premier)
                targets.sort(new Comparator<Ennemi>() {
                    @Override
                    public int compare(Ennemi a, Ennemi b) {
                        return Float.compare(a.pos.dst(pos), b.pos.dst(pos));
                    }
                });
                
                // On frappe les N ennemis les plus proches
                int hitCount = Math.min(maxTargets, targets.size);
                for (int i = 0; i < hitCount; i++) {
                    Ennemi target = targets.get(i);
                    
                    // Inflige des dégâts (2 PV par tir)
                    target.hp -= 2; 
                    gs.damage.add(target.pos.x, target.pos.y + 20f, "-2", false); 
                    drawLightningBolt(gs, pos.x, pos.y + 10f, target.pos.x, target.pos.y);
                    gs.particles.spawnLightning(target.pos.x, target.pos.y, 0f, 4);

                    // Si l'ennemi tombe à 0 PV, on le tue immédiatement
                    if (target.hp <= 0) {
                        gs.particles.spawnEnemyExplosion(target.pos.x, target.pos.y, new Color(0.2f, 0.9f, 0.9f, 1f));
                        gs.particles.spawnBlood(target.pos.x, target.pos.y, 10);
                        gs.ennemis.removeValue(target, true);
                        
                        gs.setScore(gs.getScore() + 1);
                        gs.xpSystem.addXp(GameScreen.XP_PER_ENEMY);
                        
                        // Fait tomber une flèche
                        gs.flechesSol.add(new FlecheSol(target.pos.x, target.pos.y, MathUtils.random(0f, 360f)));
                    }
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

    // Dessine un éclair en zigzag entre le totem et la cible
    private void drawLightningBolt(GameScreen gs, float x1, float y1, float x2, float y2) {
        int steps = 8;
        for (int i = 0; i < steps; i++) {
            float t = i / (float) (steps - 1);
            float cx = x1 + (x2 - x1) * t;
            float cy = y1 + (y2 - y1) * t;
            
            if (i != 0 && i != steps - 1) {
                cx += MathUtils.random(-15f, 15f);
                cy += MathUtils.random(-15f, 15f);
            }
            gs.particles.spawnLightning(cx, cy, 0f, 1);
        }
    }

    public void render(ShapeRenderer shapes) {
        if (!alive) return;
        
        // Effet de pulsation du totem
        float pulse = 1f + 0.1f * MathUtils.sin(timer * 6f);
        float r = radius * pulse;
        
        // Base du totem (carré gris)
        shapes.setColor(0.2f, 0.2f, 0.2f, 0.8f); 
        shapes.rect(pos.x - r, pos.y - r, r * 2f, r * 2f);
        
        // Tête du totem (cercle rouge ou vert)
        if (type == Type.FOUDRE) {
            shapes.setColor(1f, 0.1f, 0.1f, 1f); 
        } else {
            shapes.setColor(0.2f, 1f, 0.4f, 1f); 
        }
        shapes.circle(pos.x, pos.y + 10f, radius * 0.8f, 16);
        
        // Cristal central blanc
        shapes.setColor(1f, 1f, 1f, 0.8f);
        shapes.circle(pos.x, pos.y + 10f, radius * 0.3f, 12);
    }
}