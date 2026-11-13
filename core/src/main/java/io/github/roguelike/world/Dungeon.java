package io.github.roguelike.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
    private Room bossRoom;

    /**
     * Naključna povezana postavitev med minRooms in maxRooms sob (vključno) na mreži.
     * Začetna soba 'm' je na (0, 0); vsaka nova soba se prilepi ob že obstoječo,
     * zato so vse sobe dosegljive. Označene so z m, nato 1, 2, 3, ...
     * Na koncu se najbolj oddaljena soba od začetne določi za boss sobo ('B').
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
        dungeon.assignBossRoom();
        return dungeon;
    }

    /**
     * Izračuna razdaljo (število vrat) vsake sobe od začetne in najbolj oddaljeno označi kot boss sobo.
     * Pri izenačenju odloči zračna razdalja v mreži, nato vrstni red nastanka (rezultat je torej
     * deterministična funkcija postavitve).
     *
     * Boss soba nikoli ni edina pot do kake druge sobe: če bi bila, bi bila ta druga soba še dlje od
     * začetka in bi bila boss ona. Zato je mogoče vse ostale sobe počistiti, ne da bi vstopili v boss sobo.
     */
    private void assignBossRoom() {
        if (roomList.size() < 2) return; // samo začetna soba -> ni boss sobe

        // BFS po vratih (vrata so povsod, kjer je sosednja soba)
        Map<Room, Integer> distance = new HashMap<>();
        Deque<Room> queue = new ArrayDeque<>();
        distance.put(start, 0);
        queue.add(start);
        while (!queue.isEmpty()) {
            Room room = queue.poll();
            for (Side side : Side.values()) {
                Room neighbor = getNeighbor(room, side);
                if (neighbor == null || distance.containsKey(neighbor)) continue;
                distance.put(neighbor, distance.get(room) + 1);
                queue.add(neighbor);
            }
        }

        Room farthest = null;
        int farthestDoors = -1;
        long farthestAir = -1;
        for (Room room : roomList) {
            if (room == start) continue;
            int doors = distance.get(room);
            long dx = room.getGridX() - start.getGridX();
            long dy = room.getGridY() - start.getGridY();
            long air = dx * dx + dy * dy;
            if (doors > farthestDoors || (doors == farthestDoors && air > farthestAir)) {
                farthest = room;
                farthestDoors = doors;
                farthestAir = air;
            }
        }

        for (Room room : roomList) room.setDistanceFromStart(distance.get(room));
        bossRoom = farthest;
        bossRoom.markAsBoss();
    }

    public Room addRoom(int gridX, int gridY, char label) {
        Room room = new Room(gridX, gridY, label);
        rooms.put(key(gridX, gridY), room);
        roomList.add(room);
        return room;
    }

    public Room getStart() { return start; }

    /** Najbolj oddaljena soba od začetne ali null, če dungeon nima boss sobe. */
    public Room getBossRoom() { return bossRoom; }

    /** True, ko so vse sobe razen boss sobe cleared (začetna soba je cleared od začetka). */
    public boolean areStandardRoomsCleared() {
        for (Room room : roomList) {
            if (room != bossRoom && !room.isCleared()) return false;
        }
        return true;
    }

    /** Boss soba je zaklenjena, dokler vse ostale sobe niso cleared. */
    public boolean isBossRoomLocked() {
        return bossRoom != null && !areStandardRoomsCleared();
    }

    /** True, če vrata na dani strani vodijo v še zaklenjeno boss sobo. */
    public boolean isDoorToLockedBossRoom(Room room, Side side) {
        Room neighbor = getNeighbor(room, side);
        return neighbor != null && neighbor == bossRoom && isBossRoomLocked();
    }

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
