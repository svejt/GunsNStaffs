package io.github.roguelike.world;

/** Ena soba (en zaslon) na mestu (gridX, gridY) v mreži sob. */
public class Room {
    /** Črka boss sobe (na minimapi in kot velika črka v sobi). */
    public static final char BOSS_LABEL = 'B';

    private final int gridX, gridY;
    private char label;
    private boolean cleared;
    private boolean boss;
    private int distanceFromStart; // število vrat od začetne sobe (nastavi Dungeon)

    public Room(int gridX, int gridY, char label) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.label = label;
    }

    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }

    /** Črka, ki označuje sobo: m (začetna), 1-9, * ali B (boss). */
    public char getLabel() { return label; }

    /** Soba je "cleared", ko v njej ni več enemyjev (začetna soba je cleared od začetka). */
    public boolean isCleared() { return cleared; }
    public void setCleared(boolean cleared) { this.cleared = cleared; }

    /** Boss soba je najbolj oddaljena soba od začetne. Označi jo Dungeon.generate(...). */
    public boolean isBoss() { return boss; }

    void markAsBoss() {
        this.boss = true;
        this.label = BOSS_LABEL;
    }

    /** Najmanjše število vrat, ki jih mora igralec prečkati od začetne sobe do te. */
    public int getDistanceFromStart() { return distanceFromStart; }

    void setDistanceFromStart(int distanceFromStart) { this.distanceFromStart = distanceFromStart; }
}
