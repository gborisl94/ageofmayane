package com.ageofmayane.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** Etat global du donjon : salle courante, portes, collisions, mini-carte. */
public class DungeonManager {

    /** Distance du bord ou le joueur apparait apres avoir franchi une porte. */
    public static final float ARRIVAL_MARGIN = 55f;

    static final float MM_CELL = 20f;   // pas de la mini-carte
    static final float MM_DRAW = 18f;   // taille dessinee d'une cellule

    private final DungeonGenerator generator = new DungeonGenerator();

    private Room[][] grid = new Room[DungeonGenerator.GRID][DungeonGenerator.GRID];
    private final Array<Room> rooms = new Array<Room>();
    private Room start;
    private Room bossRoom;
    private Room current;

    private final Array<Rectangle> solids = new Array<Rectangle>();
    private final Rectangle doorTmp = new Rectangle();

    // ---------------------------------------------------------------
    // Generation
    // ---------------------------------------------------------------

    /** Genere un nouveau donjon (jamais d'exception vers l'exterieur). */
    public void generate() {
        DungeonGenerator.Result res = null;
        try {
            res = generator.generate();
            if (!isValid(res)) res = null;
        } catch (Throwable t) {
            Gdx.app.error("Mayane", "Echec generation donjon", t);
            res = null;
        }
        if (res == null) res = generator.emergency();

        grid = res.grid;
        rooms.clear();
        rooms.addAll(res.rooms);
        start = res.start;
        bossRoom = res.boss;
        current = start;
        start.visited = true;
        rebuildSolids();
    }

    private boolean isValid(DungeonGenerator.Result r) {
        return r != null && r.grid != null && r.start != null && r.boss != null
                && r.boss != r.start && r.rooms.size >= 2;
    }

    // ---------------------------------------------------------------
    // Acces
    // ---------------------------------------------------------------

    public Room getCurrent() { return current; }
    public Room getStart() { return start; }
    public Room getBossRoom() { return bossRoom; }
    public int getRoomCount() { return rooms.size; }
    public boolean isCurrentBossRoom() { return current != null && current == bossRoom; }
    public boolean doorsOpen() { return current != null && current.cleared; }

    public Room getRoomAt(int gx, int gy) {
        if (gx < 0 || gy < 0 || gx >= DungeonGenerator.GRID || gy >= DungeonGenerator.GRID) {
            return null;
        }
        return grid[gx][gy];
    }

    // ---------------------------------------------------------------
    // Portes / progression
    // ---------------------------------------------------------------

    /** Reconstruit la liste des solides (a appeler quand l'etat des portes change). */
    public void rebuildSolids() {
        solids.clear();
        if (current != null) current.collectSolids(solids, !current.cleared);
    }

    /** La salle est nettoyee : les portes s'ouvrent. */
    public void markCleared() {
        if (current == null) return;
        current.cleared = true;
        rebuildSolids();
    }

    /**
     * Renvoie la direction de la porte que le joueur est en train de franchir,
     * ou -1. Une porte n'est franchissable que si la salle est nettoyee.
     */
    public int checkDoorExit(Vector2 p) {
        if (current == null || !current.cleared) return -1;
        float half = Room.DOOR_LEN * 0.5f;
        float midX = Room.ROOM_W * 0.5f;
        float midY = Room.ROOM_H * 0.5f;
        float edge = Room.WALL_T * 0.5f;

        if (current.hasDoor(Room.NORTH) && p.y >= Room.ROOM_H - edge
                && Math.abs(p.x - midX) <= half) return Room.NORTH;
        if (current.hasDoor(Room.SOUTH) && p.y <= edge
                && Math.abs(p.x - midX) <= half) return Room.SOUTH;
        if (current.hasDoor(Room.EAST) && p.x >= Room.ROOM_W - edge
                && Math.abs(p.y - midY) <= half) return Room.EAST;
        if (current.hasDoor(Room.WEST) && p.x <= edge
                && Math.abs(p.y - midY) <= half) return Room.WEST;
        return -1;
    }

    /**
     * Passe dans la salle voisine et replace le joueur pres de la porte
     * d'arrivee. Renvoie la nouvelle salle (ou null si impossible).
     */
    public Room moveThrough(int dir, Vector2 playerPos) {
        if (current == null || !current.hasDoor(dir)) return null;
        Room next = getRoomAt(current.gx + Room.DX[dir], current.gy + Room.DY[dir]);
        if (next == null) return null;

        current = next;
        next.visited = true;

        float midX = Room.ROOM_W * 0.5f;
        float midY = Room.ROOM_H * 0.5f;
        switch (dir) {
            case Room.NORTH: playerPos.set(midX, ARRIVAL_MARGIN); break;
            case Room.SOUTH: playerPos.set(midX, Room.ROOM_H - ARRIVAL_MARGIN); break;
            case Room.EAST:  playerPos.set(ARRIVAL_MARGIN, midY); break;
            default:         playerPos.set(Room.ROOM_W - ARRIVAL_MARGIN, midY); break;
        }
        rebuildSolids();
        return next;
    }

    // ---------------------------------------------------------------
    // Collisions
    // ---------------------------------------------------------------

    /** Repousse un cercle hors de tous les solides (murs, portes fermees, obstacles). */
    public void collide(Vector2 pos, float r) {
        for (int pass = 0; pass < 3; pass++) {
            boolean any = false;
            for (int i = 0; i < solids.size; i++) {
                if (resolveCircleRect(pos, r, solids.get(i))) any = true;
            }
            if (!any) break;
        }
    }

    private static boolean resolveCircleRect(Vector2 c, float r, Rectangle rect) {
        float nx = MathUtils.clamp(c.x, rect.x, rect.x + rect.width);
        float ny = MathUtils.clamp(c.y, rect.y, rect.y + rect.height);
        float dx = c.x - nx;
        float dy = c.y - ny;
        float d2 = dx * dx + dy * dy;
        if (d2 >= r * r) return false;

        if (d2 > 0.000001f) {
            float d = (float) Math.sqrt(d2);
            float push = r - d;
            c.x += dx / d * push;
            c.y += dy / d * push;
        } else {
            // Centre dans le rectangle : sortir par le bord le plus proche
            float left = c.x - rect.x;
            float right = rect.x + rect.width - c.x;
            float bottom = c.y - rect.y;
            float top = rect.y + rect.height - c.y;
            float m = Math.min(Math.min(left, right), Math.min(bottom, top));
            if (m == left) c.x = rect.x - r;
            else if (m == right) c.x = rect.x + rect.width + r;
            else if (m == bottom) c.y = rect.y - r;
            else c.y = rect.y + rect.height + r;
        }
        return true;
    }

    /** Vrai si un cercle (x, y, r) touche un solide. */
    public boolean circleHitsWall(float x, float y, float r) {
        for (int i = 0; i < solids.size; i++) {
            Rectangle s = solids.get(i);
            float nx = MathUtils.clamp(x, s.x, s.x + s.width);
            float ny = MathUtils.clamp(y, s.y, s.y + s.height);
            float dx = x - nx;
            float dy = y - ny;
            if (dx * dx + dy * dy < r * r) return true;
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Dessin (le ShapeRenderer doit etre en mode Filled)
    // ---------------------------------------------------------------

    /** Sol, murs, obstacles et portes de la salle courante. */
    public void drawRoom(ShapeRenderer shapes) {
        if (current == null) return;

        // Sol
        shapes.setColor(0.11f, 0.11f, 0.16f, 1f);
        shapes.rect(0f, 0f, Room.ROOM_W, Room.ROOM_H);

        // Murs + obstacles (+ portes fermees, recouvertes ensuite en rouge)
        shapes.setColor(0.25f, 0.25f, 0.3f, 1f);
        for (int i = 0; i < solids.size; i++) {
            Rectangle s = solids.get(i);
            shapes.rect(s.x, s.y, s.width, s.height);
        }

        // Portes
        boolean open = current.cleared;
        for (int d = 0; d < 4; d++) {
            if (!current.hasDoor(d)) continue;
            if (open) shapes.setColor(0.4f, 0.9f, 0.4f, 1f);
            else shapes.setColor(0.7f, 0.2f, 0.2f, 1f);
            current.getDoorRect(d, doorTmp);
            shapes.rect(doorTmp.x, doorTmp.y, doorTmp.width, doorTmp.height);
        }
    }

    /** Mini-carte 5x5 ; (x, y) = coin bas-gauche de la grille (espace UI). */
    public void drawMiniMap(ShapeRenderer shapes, float x, float y) {
        for (int gx = 0; gx < DungeonGenerator.GRID; gx++) {
            for (int gy = 0; gy < DungeonGenerator.GRID; gy++) {
                Room r = grid[gx][gy];
                if (r == null) shapes.setColor(0f, 0f, 0f, 0.85f);
                else if (r == current) shapes.setColor(1f, 0.9f, 0.1f, 1f);
                else if (r.visited) shapes.setColor(0.2f, 0.75f, 0.3f, 1f);
                else shapes.setColor(0.25f, 0.25f, 0.3f, 1f);
                shapes.rect(x + gx * MM_CELL, y + gy * MM_CELL, MM_DRAW, MM_DRAW);
            }
        }

        // Petits traits entre les salles reliees
        shapes.setColor(0.5f, 0.5f, 0.55f, 1f);
        float gap = MM_CELL - MM_DRAW;
        float off = (MM_DRAW - 4f) * 0.5f;
        for (int i = 0; i < rooms.size; i++) {
            Room r = rooms.get(i);
            if (r.hasDoor(Room.EAST)) {
                shapes.rect(x + r.gx * MM_CELL + MM_DRAW, y + r.gy * MM_CELL + off, gap, 4f);
            }
            if (r.hasDoor(Room.NORTH)) {
                shapes.rect(x + r.gx * MM_CELL + off, y + r.gy * MM_CELL + MM_DRAW, 4f, gap);
            }
        }
    }
}