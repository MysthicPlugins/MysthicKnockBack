package kk.kvlzx.arena;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

public class Arena {
    private final String name;
    private final Map<ZoneType, Zone> zones; // Cambiado de String a ZoneType
    private Location spawnPoint;
    private int borderSize = 150; // Default border size

    public Arena(String name) {
        this.name = name;
        this.zones = new HashMap<>();
    }

    public void setZone(ZoneType type, Zone zone) { // Cambiado de String a ZoneType
        zones.put(type, zone);
    }

    public Zone getZone(String type) {
        return zones.get(ZoneType.fromString(type));
    }

    public String getName() {
        return name;
    }

    public void setSpawnPoint(Location location) {
        this.spawnPoint = location;
    }

    public Location getSpawnLocation() {
        if (spawnPoint != null) {
            return spawnPoint;
        }
        // Fallback al centro de la zona spawn si no hay spawnpoint definido
        Zone spawnZone = zones.get(ZoneType.SPAWN);
        if (spawnZone == null) return null;
        
        Location min = spawnZone.getMin();
        Location max = spawnZone.getMax();
        return new Location(
            min.getWorld(),
            (min.getX() + max.getX()) / 2,
            min.getY(),
            (min.getZ() + max.getZ()) / 2,
            0, 0
        );
    }

    public void setBorderSize(int size) {
        this.borderSize = size;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public boolean isInsideBorder(Location loc) {
        if (spawnPoint == null) return true;
        
        int xDiff = Math.abs(loc.getBlockX() - spawnPoint.getBlockX());
        int zDiff = Math.abs(loc.getBlockZ() - spawnPoint.getBlockZ());
        
        return xDiff <= borderSize && zDiff <= borderSize;
    }
}
