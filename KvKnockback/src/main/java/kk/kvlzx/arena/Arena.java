package kk.kvlzx.arena;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;

public class Arena {
    private final String name;
    private final Map<String, Zone> zones;

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

    public Location getSpawnLocation() {
        Zone spawnZone = zones.get("spawn");
        if (spawnZone == null) return null;
        
        // Obtiene el centro de la zona de spawn
        Location min = spawnZone.getMin();
        Location max = spawnZone.getMax();
        return new Location(
            min.getWorld(),
            (min.getX() + max.getX()) / 2,
            min.getY(),
            (min.getZ() + max.getZ()) / 2,
            0, 0 // yaw y pitch en 0
        );
    }
}
