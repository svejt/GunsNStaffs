package io.github.roguelike.world;

/** Ena soba (en zaslon) na mestu (gridX, gridY) v mreži sob. */
public class Room {
    private final int gridX, gridY;
    private final char label;
    private boolean cleared;

    public Room(int gridX, int gridY, char label) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.label = label;
    }

    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }

    /** Črka, ki označuje sobo: m, u, d, l, r. */
    public char getLabel() { return label; }

    /** Soba je "cleared", ko v njej ni več enemyjev (začetna soba je cleared od začetka). */
    public boolean isCleared() { return cleared; }
    public void setCleared(boolean cleared) { this.cleared = cleared; }
}
