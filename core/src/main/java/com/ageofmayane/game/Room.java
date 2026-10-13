package com.ageofmayane.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/** Une salle du donjon (1280 x 720). */
public class Room {

    public static final float ROOM_W = 1280f;
    public static final float ROOM_H = 720f;
    public static final float WALL_T = 20f;
    public static final float DOOR_LEN = 100f;

    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final int[] DX = {0, 1, 0, -1};
    public static final int[] DY = {1, 0, -1, 0};

    public final int gx;
    public final int gy;

    private final boolean[] doors = new boolean[4];

    public boolean visited = false;
    public boolean cleared = false;
    public boolean boss = false;
    public int enemyCount = 0;
    public int distance = 0;

    public final Array<Rectangle> obstacles = new Array<Rectangle>();

    public Room(int gx, int gy) {
        this.gx = gx;
        this.gy = gy;
    }
public static int opposite(int dir) {
    return (dir + 2) & 3;
}

public boolean hasDoor(int dir) {
    return dir >= 0 && dir < 4 && doors[dir];
}

public void setDoor(int dir, boolean value) {
    if (dir >= 0 && dir < 4) doors[dir] = value;
}

public int doorCount() {
    int n = 0;
    for (int i = 0; i < 4; i++) if (doors[i]) n++;
    return n;
}

public Rectangle getDoorRect(int dir, Rectangle out) {
    float cx = ROOM_W * 0.5f - DOOR_LEN * 0.5f;
    float cy = ROOM_H * 0.5f - DOOR_LEN * 0.5f;
    switch (dir) {
        case NORTH: return out.set(cx, ROOM_H - WALL_T, DOOR_LEN, WALL_T);
        case SOUTH: return out.set(cx, 0f, DOOR_LEN, WALL_T);
        case EAST:  return out.set(ROOM_W - WALL_T, cy, WALL_T, DOOR_LEN);
        default:    return out.set(0f, cy, WALL_T, DOOR_LEN);
    }
}

public void collectSolids(Array<Rectangle> out, boolean doorsClosed) {
    horizontalWall(out, ROOM_H - WALL_T, doors[NORTH]);
    horizontalWall(out, 0f, doors[SOUTH]);
    verticalWall(out, ROOM_W - WALL_T, doors[EAST]);
    verticalWall(out, 0f, doors[WEST]);

    if (doorsClosed) {
        for (int d = 0; d < 4; d++) {
            if (doors[d]) out.add(getDoorRect(d, new Rectangle()));
        }
    }
    for (int i = 0; i < obstacles.size; i++) {
        out.add(obstacles.get(i));
    }
}

private static void horizontalWall(Array<Rectangle> out, float y, boolean gap) {
    float half = DOOR_LEN * 0.5f;
    if (!gap) {
        out.add(new Rectangle(0f, y, ROOM_W, WALL_T));
    } else {
        float side = ROOM_W * 0.5f - half;
        out.add(new Rectangle(0f, y, side, WALL_T));
        out.add(new Rectangle(ROOM_W * 0.5f + half, y, side, WALL_T));
    }
}

private static void verticalWall(Array<Rectangle> out, float x, boolean gap) {
    float half = DOOR_LEN * 0.5f;
    if (!gap) {
        out.add(new Rectangle(x, 0f, WALL_T, ROOM_H));
    } else {
        float side = ROOM_H * 0.5f - half;
        out.add(new Rectangle(x, 0f, WALL_T, side));
        out.add(new Rectangle(x, ROOM_H * 0.5f + half, WALL_T, side));
    }
}
    public void generateInternalWalls(long seed, boolean isBoss, boolean isStart) {
        obstacles.clear();
        if (isBoss || isStart) return;

        java.util.Random rng = new java.util.Random(seed);
        final int cols = 3;
        final int rows = 3;
        final float cellW = ROOM_W / cols;
        final float cellH = ROOM_H / rows;
        final float wallThick = WALL_T * 1.5f;
        final float margin = WALL_T + 60f;

        for (int cx = 1; cx < cols; cx++) {
            float x = cx * cellW - wallThick * 0.5f;
            for (int cy = 0; cy < rows; cy++) {
                float cyTop = cy * cellH;
                float cyBot = (cy + 1) * cellH;
                if (rng.nextFloat() < 0.4f) continue;
                float yStart = Math.max(cyTop + margin, margin);
                float yEnd = Math.min(cyBot - margin, ROOM_H - margin);
                float h = yEnd - yStart;
                if (h > 60f) {
                    obstacles.add(new Rectangle(x, yStart, wallThick, h));
                }
            }
        }

        for (int cy = 1; cy < rows; cy++) {
            float y = cy * cellH - wallThick * 0.5f;
            for (int cx = 0; cx < cols; cx++) {
                float cxL = cx * cellW;
                float cxR = (cx + 1) * cellW;
                if (rng.nextFloat() < 0.4f) continue;
                float xStart = Math.max(cxL + margin, margin);
                float xEnd = Math.min(cxR - margin, ROOM_W - margin);
                float w = xEnd - xStart;
                if (w > 60f) {
                    obstacles.add(new Rectangle(xStart, y, w, wallThick));
                }
            }
        }
    }
}