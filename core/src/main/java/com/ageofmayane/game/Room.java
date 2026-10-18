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

        final float outerMargin = WALL_T + 40f;
        final float minRoomW = 220f;
        final float maxRoomW = 400f;
        final float minRoomH = 180f;
        final float maxRoomH = 320f;
        final float wallThick = WALL_T;
        final float corridorThick = 90f;

        int chamberCount = 3 + rng.nextInt(3);
        Array<Rectangle> chambers = new Array<Rectangle>();

        for (int attempt = 0; attempt < 100 && chambers.size < chamberCount; attempt++) {
            float w = minRoomW + rng.nextFloat() * (maxRoomW - minRoomW);
            float h = minRoomH + rng.nextFloat() * (maxRoomH - minRoomH);
            float x = outerMargin + rng.nextFloat() * (ROOM_W - w - outerMargin * 2f);
            float y = outerMargin + rng.nextFloat() * (ROOM_H - h - outerMargin * 2f);

            Rectangle candidate = new Rectangle(x, y, w, h);

            boolean overlap = false;
            for (int i = 0; i < chambers.size; i++) {
                Rectangle other = chambers.get(i);
                Rectangle padded = new Rectangle(
                        other.x - corridorThick - wallThick,
                        other.y - corridorThick - wallThick,
                        other.width + (corridorThick + wallThick) * 2f,
                        other.height + (corridorThick + wallThick) * 2f);
                if (candidate.overlaps(padded)) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) chambers.add(candidate);
        }

        if (chambers.size < 2) return;

        for (int i = 0; i < chambers.size; i++) {
            Rectangle c = chambers.get(i);
            obstacles.add(new Rectangle(c.x - wallThick, c.y - wallThick,
                    c.width + wallThick * 2f, wallThick));
            obstacles.add(new Rectangle(c.x - wallThick, c.y + c.height,
                    c.width + wallThick * 2f, wallThick));
            obstacles.add(new Rectangle(c.x - wallThick, c.y,
                    wallThick, c.height));
            obstacles.add(new Rectangle(c.x + c.width, c.y,
                    wallThick, c.height));
        }

        for (int i = 0; i < chambers.size - 1; i++) {
            Rectangle a = chambers.get(i);
            Rectangle b = chambers.get(i + 1);
            float ax = a.x + a.width * 0.5f;
            float ay = a.y + a.height * 0.5f;
            float bx = b.x + b.width * 0.5f;
            float by = b.y + b.height * 0.5f;

            Rectangle hCorridor = new Rectangle(
                    Math.min(ax, bx), ay - corridorThick * 0.5f,
                    Math.abs(bx - ax), corridorThick);
            Rectangle vCorridor = new Rectangle(
                    bx - corridorThick * 0.5f, Math.min(ay, by),
                    corridorThick, Math.abs(by - ay));

            carveCorridor(hCorridor);
            carveCorridor(vCorridor);
        }
    }

    private void carveCorridor(Rectangle corridor) {
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Rectangle o = obstacles.get(i);
            if (o.overlaps(corridor)) {
                obstacles.removeIndex(i);
                if (o.width > o.height) {
                    if (corridor.x > o.x) {
                        float cut = Math.min(corridor.x - o.x, o.width);
                        if (cut > 1f) obstacles.add(new Rectangle(o.x, o.y, cut, o.height));
                    }
                    if (corridor.x + corridor.width < o.x + o.width) {
                        float cutX = corridor.x + corridor.width;
                        float cutW = o.x + o.width - cutX;
                        if (cutW > 1f) obstacles.add(new Rectangle(cutX, o.y, cutW, o.height));
                    }
                } else {
                    if (corridor.y > o.y) {
                        float cut = Math.min(corridor.y - o.y, o.height);
                        if (cut > 1f) obstacles.add(new Rectangle(o.x, o.y, o.width, cut));
                    }
                    if (corridor.y + corridor.height < o.y + o.height) {
                        float cutY = corridor.y + corridor.height;
                        float cutH = o.y + o.height - cutY;
                        if (cutH > 1f) obstacles.add(new Rectangle(o.x, cutY, o.width, cutH));
                    }
                }
            }
        }
    }
}