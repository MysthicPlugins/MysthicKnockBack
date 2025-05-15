package kk.kvlzx.arena;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;

public class Arena {
    private final String name;
    private final Map<String, Zone> zones;
    private Location spawnPoint;

    public Arena(String name) {
        this.name = name;
        this.zones = new HashMap<>();
    }

    public void setZone(String type, Zone zone) {
        zones.put(type.toLowerCase(), zone);
    }

    public Zone getZone(String type) {
        return zones.get(type.toLowerCase());
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
        Zone spawnZone = zones.get("spawn");
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
}
