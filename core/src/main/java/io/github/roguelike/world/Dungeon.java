package io.github.roguelike.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Mreža sob. Vrata obstajajo povsod, kjer ima soba soseda.
 * Postavitev se generira proceduralno z generate(...), ki samo kliče addRoom(...).
 */
public class Dungeon {
    private final Map<Long, Room> rooms = new HashMap<>();
    private final List<Room> roomList = new ArrayList<>();
    private Room start;

    /**
     * Naključna povezana postavitev med minRooms in maxRooms sob (vključno) na mreži.
     * Začetna soba 'm' je na (0, 0); vsaka nova soba se prilepi ob že obstoječo,
     * zato so vse sobe dosegljive. Označene so z m, nato 1, 2, 3, ...
     */
    public static Dungeon generate(Random random, int minRooms, int maxRooms) {
        int roomCount = minRooms + random.nextInt(maxRooms - minRooms + 1);

        Dungeon dungeon = new Dungeon();
        dungeon.start = dungeon.addRoom(0, 0, 'm');
        dungeon.start.setCleared(true); // v začetni sobi ni enemyjev

        Side[] sides = Side.values();
        while (dungeon.roomList.size() < roomCount) {
            // izberi naključno obstoječo sobo in naključno stran; če je tam prosto, dodaj sobo
            Room from = dungeon.roomList.get(random.nextInt(dungeon.roomList.size()));
            Side side = sides[random.nextInt(sides.length)];
            int x = from.getGridX() + side.dx;
            int y = from.getGridY() + side.dy;
            if (dungeon.getRoomAt(x, y) != null) continue;

            int number = dungeon.roomList.size(); // 1, 2, 3, ... (m je 0)
            dungeon.addRoom(x, y, number < 10 ? (char) ('0' + number) : '*');
        }
        return dungeon;
    }

    public Room addRoom(int gridX, int gridY, char label) {
        Room room = new Room(gridX, gridY, label);
        rooms.put(key(gridX, gridY), room);
        roomList.add(room);
        return room;
    }

    public Room getStart() { return start; }

    public int getRoomCount() { return roomList.size(); }

    public List<Room> getRooms() { return roomList; }

    public Room getRoomAt(int gridX, int gridY) {
        return rooms.get(key(gridX, gridY));
    }

    /** Soseda na dani strani ali null, če tam ni sobe. */
    public Room getNeighbor(Room room, Side side) {
        return getRoomAt(room.getGridX() + side.dx, room.getGridY() + side.dy);
    }

    public boolean hasDoor(Room room, Side side) {
        return getNeighbor(room, side) != null;
    }

    private static long key(int x, int y) {
        return ((long) x << 32) ^ (y & 0xffffffffL);
    }
}
