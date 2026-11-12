package io.github.roguelike.world;

/** Stran sobe, na kateri so lahko vrata. Koordinate sobe: x raste v desno, y raste navzgor. */
public enum Side {
    UP(0, 1),
    DOWN(0, -1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    /** Premik v mreži sob, ko greš skozi vrata na tej strani. */
    public final int dx, dy;

    Side(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Side opposite() {
        switch (this) {
            case UP:    return DOWN;
            case DOWN:  return UP;
            case LEFT:  return RIGHT;
            default:    return LEFT;
        }
    }
}
