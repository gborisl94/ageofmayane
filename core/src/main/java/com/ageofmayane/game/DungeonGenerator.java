package com.ageofmayane.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/** Genere le graphe de salles, les portes, les obstacles et les ennemis. */
public class DungeonGenerator {

    public static final int GRID = 5;
    public static final int MIN_ROOMS = 6;
    public static final int MAX_ROOMS = 8;
    public static final int START_X = 2;
    public static final int START_Y = 2;

    /** Resultat de la generation. */
    public static class Result {
        public Room[][] grid;
        public final Array<Room> rooms = new Array<Room>();
        public Room start;
        public Room boss;
    }

    // Couloirs gardes libres (pour les portes et l'arrivee du joueur)
    private final Rectangle laneV = new Rectangle(
            Room.ROOM_W * 0.5f - 110f, 0f, 220f, Room.ROOM_H);
    private final Rectangle laneH = new Rectangle(
            0f, Room.ROOM_H * 0.5f - 110f, Room.ROOM_W, 220f);
    private final Rectangle tmpRect = new Rectangle();

    public Result generate() {
        Result r = new Result();
        r.grid = new Room[GRID][GRID];

        int target = MathUtils.random(MIN_ROOMS, MAX_ROOMS);
        r.start = addRoom(r, START_X, START_Y);

        // 1) Croissance en arbre : toutes les salles sont reliees
        int attempts = 0;
        while (r.rooms.size < target && attempts < 2000) {
            attempts++;
            Room from = r.rooms.get(MathUtils.random(r.rooms.size - 1));
            int d = MathUtils.random(3);
            int nx = from.gx + Room.DX[d];
            int ny = from.gy + Room.DY[d];
            if (!inGrid(nx, ny) || r.grid[nx][ny] != null) continue;
            Room to = addRoom(r, nx, ny);
            connect(from, to, d);
        }

        // 2) Repli deterministe si jamais on n'a pas atteint le minimum
        if (r.rooms.size < MIN_ROOMS) fallbackFill(r);

        // 3) Quelques portes supplementaires (boucles)
        for (int i = 0; i < r.rooms.size; i++) {
            Room a = r.rooms.get(i);
            for (int d = 0; d < 4; d++) {
                if (d != Room.EAST && d != Room.NORTH) continue;
                if (a.hasDoor(d)) continue;
                int nx = a.gx + Room.DX[d];
                int ny = a.gy + Room.DY[d];
                if (!inGrid(nx, ny)) continue;
                Room b = r.grid[nx][ny];
                if (b != null && MathUtils.randomBoolean(0.25f)) connect(a, b, d);
            }
        }

        // 4) Distances depuis le depart, puis boss = salle la plus eloignee
        computeDistances(r);
        r.boss = pickBossRoom(r);

        // 5) Contenu des salles
        for (int i = 0; i < r.rooms.size; i++) {
            Room room = r.rooms.get(i);
            if (room == r.start) {
                room.enemyCount = 0;
                room.cleared = true;
                room.visited = true;
            } else if (room == r.boss) {
                room.boss = true;
                room.enemyCount = MathUtils.random(0, 2);
                addObstacles(room, MathUtils.random(0, 2));
            } else {
                int count = 2 + room.distance + MathUtils.random(0, 1);
                room.enemyCount = MathUtils.clamp(count, 3, 6);
                addObstacles(room, MathUtils.random(0, 4));
            }
        }
        return r;
    }

    /** Donjon minimal (depart + boss) utilise si la generation echoue. */
    public Result emergency() {
        Result r = new Result();
        r.grid = new Room[GRID][GRID];
        r.start = addRoom(r, START_X, START_Y);
        Room b = addRoom(r, START_X + 1, START_Y);
        connect(r.start, b, Room.EAST);
        r.start.cleared = true;
        r.start.visited = true;
        r.start.distance = 0;
        b.distance = 1;
        b.boss = true;
        b.enemyCount = 0;
        r.boss = b;
        return r;
    }

    // ---------------------------------------------------------------

    private Room addRoom(Result r, int x, int y) {
        Room room = new Room(x, y);
        r.grid[x][y] = room;
        r.rooms.add(room);
        return room;
    }

    private void connect(Room a, Room b, int dirFromAToB) {
        a.setDoor(dirFromAToB, true);
        b.setDoor(Room.opposite(dirFromAToB), true);
    }

    private boolean inGrid(int x, int y) {
        return x >= 0 && y >= 0 && x < GRID && y < GRID;
    }

    private void fallbackFill(Result r) {
        boolean added = true;
        while (r.rooms.size < MIN_ROOMS && added) {
            added = false;
            for (int x = 0; x < GRID && r.rooms.size < MIN_ROOMS; x++) {
                for (int y = 0; y < GRID && r.rooms.size < MIN_ROOMS; y++) {
                    if (r.grid[x][y] != null) continue;
                    for (int d = 0; d < 4; d++) {
                        int nx = x + Room.DX[d];
                        int ny = y + Room.DY[d];
                        if (!inGrid(nx, ny) || r.grid[nx][ny] == null) continue;
                        Room neighbour = r.grid[nx][ny];
                        Room created = addRoom(r, x, y);
                        connect(neighbour, created, Room.opposite(d));
                        added = true;
                        break;
                    }
                }
            }
        }
    }

    private void computeDistances(Result r) {
        for (int i = 0; i < r.rooms.size; i++) r.rooms.get(i).distance = -1;
        Array<Room> queue = new Array<Room>();
        r.start.distance = 0;
        queue.add(r.start);
        int head = 0;
        while (head < queue.size) {
            Room cur = queue.get(head++);
            for (int d = 0; d < 4; d++) {
                if (!cur.hasDoor(d)) continue;
                int nx = cur.gx + Room.DX[d];
                int ny = cur.gy + Room.DY[d];
                if (!inGrid(nx, ny)) continue;
                Room n = r.grid[nx][ny];
                if (n != null && n.distance < 0) {
                    n.distance = cur.distance + 1;
                    queue.add(n);
                }
            }
        }
        // Securite : une salle non atteinte recoit une distance par defaut
        for (int i = 0; i < r.rooms.size; i++) {
            if (r.rooms.get(i).distance < 0) r.rooms.get(i).distance = 1;
        }
    }

    private Room pickBossRoom(Result r) {
        int maxD = -1;
        for (int i = 0; i < r.rooms.size; i++) {
            Room room = r.rooms.get(i);
            if (room != r.start && room.distance > maxD) maxD = room.distance;
        }
        Array<Room> candidates = new Array<Room>();
        for (int i = 0; i < r.rooms.size; i++) {
            Room room = r.rooms.get(i);
            if (room != r.start && room.distance == maxD) candidates.add(room);
        }
        if (candidates.size == 0) return r.rooms.get(r.rooms.size - 1);
        return candidates.get(MathUtils.random(candidates.size - 1));
    }

    private void addObstacles(Room room, int count) {
        for (int n = 0; n < count; n++) {
            for (int attempt = 0; attempt < 30; attempt++) {
                float w = MathUtils.random(70f, 160f);
                float h = MathUtils.random(70f, 140f);
                float x = MathUtils.random(80f, Room.ROOM_W - 80f - w);
                float y = MathUtils.random(80f, Room.ROOM_H - 80f - h);
                Rectangle cand = new Rectangle(x, y, w, h);

                if (cand.overlaps(laneV) || cand.overlaps(laneH)) continue;

                boolean ok = true;
                for (int i = 0; i < room.obstacles.size; i++) {
                    Rectangle o = room.obstacles.get(i);
                    tmpRect.set(o.x - 50f, o.y - 50f, o.width + 100f, o.height + 100f);
                    if (cand.overlaps(tmpRect)) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) continue;

                room.obstacles.add(cand);
                break;
            }
        }
    }
}