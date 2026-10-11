package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Dessine le décor du donjon avec des textures PNG.
 * - Sol : texture sol.png répétée en tuiles 64x64
 * - Murs : texture mur.png étirée sur chaque rectangle
 * - Portes : texture porte.png (normale si ouverte, teintée rouge si fermée)
 *
 * Si une texture est absente, la méthode ne plante PAS : elle laisse
 * simplement le rendu couleur précédent visible.
 */
public class DungeonRenderer {

    private static final float TILE = 64f;

    private Texture texSol;
    private Texture texMur;
    private Texture texPorte;

    private final Color tmpColor = new Color();

    public DungeonRenderer() {
        texSol   = loadSafe("sol.png");
        texMur   = loadSafe("mur.png");
        texPorte = loadSafe("porte.png");

        // Pixel art : filtrage nearest pour rester net
        if (texSol != null)   texSol.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (texMur != null)   texMur.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (texPorte != null) texPorte.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private Texture loadSafe(String name) {
        try {
            return new Texture(Gdx.files.internal(name));
        } catch (Throwable t) {
            Gdx.app.error("Mayane", "Texture decor introuvable : " + name, t);
            return null;
        }
    }

    public boolean hasSol()   { return texSol != null; }
    public boolean hasMur()   { return texMur != null; }
    public boolean hasPorte() { return texPorte != null; }

    /**
     * Dessine le sol en tuiles 64x64 sur toute la salle.
     * Renvoie true si dessiné.
     */
    public boolean renderSol(SpriteBatch batch, Room room) {
        if (texSol == null) return false;

        batch.setColor(Color.WHITE);
        int cols = (int) Math.ceil(Room.ROOM_W / TILE);
        int rows = (int) Math.ceil(Room.ROOM_H / TILE);
        for (int cx = 0; cx < cols; cx++) {
            for (int cy = 0; cy < rows; cy++) {
                batch.draw(texSol, cx * TILE, cy * TILE, TILE, TILE);
            }
        }
        return true;
    }

    /**
     * Dessine les murs (contour + obstacles) en étirant mur.png sur chaque rectangle.
     * On saute les portes : elles sont dessinées par renderPortes().
     */
    public boolean renderMurs(SpriteBatch batch, Room room, boolean doorsClosed, Rectangle doorRect) {
        if (texMur == null) return false;

        batch.setColor(Color.WHITE);

        // Contour + obstacles : on itère les mêmes rectangles que Room.collectSolids
        // mais SANS les portes (dessinées séparément).
        // On utilise un array temporaire interne pour éviter de refaire la logique.
        com.badlogic.gdx.utils.Array<Rectangle> solids = new com.badlogic.gdx.utils.Array<Rectangle>();
        room.collectSolids(solids, false); // on demande sans les portes fermées

        for (int i = 0; i < solids.size; i++) {
            Rectangle r = solids.get(i);
            batch.draw(texMur, r.x, r.y, r.width, r.height);
        }
        return true;
    }

    /**
     * Dessine les portes : texture porte.png.
     * Si la salle n'est pas nettoyée (doorsClosed), on teinte en rouge foncé.
     */
    public boolean renderPortes(SpriteBatch batch, Room room, Rectangle doorRectTmp) {
        if (texPorte == null) return false;

        boolean closed = !room.cleared;
        if (closed) {
            tmpColor.set(0.7f, 0.3f, 0.3f, 1f);
        } else {
            tmpColor.set(Color.WHITE);
        }
        batch.setColor(tmpColor);

        for (int d = 0; d < 4; d++) {
            if (!room.hasDoor(d)) continue;
            Rectangle dr = room.getDoorRect(d, doorRectTmp);
            batch.draw(texPorte, dr.x, dr.y, dr.width, dr.height);
        }
        batch.setColor(Color.WHITE);
        return true;
    }

    public void dispose() {
        if (texSol != null) texSol.dispose();
        if (texMur != null) texMur.dispose();
        if (texPorte != null) texPorte.dispose();
        texSol = null;
        texMur = null;
        texPorte = null;
    }
}