package kk.kvlzx.arena;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Arena {
    private final String name;
    private final Map<ZoneType, Zone> zones; // Cambiado de String a ZoneType
    private Location spawnPoint;
    private VirtualBorder border;

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

    public void setBorder(Location center, double size) {
        this.border = new VirtualBorder(center, size);
    }

    public void showBorder(Player player) {
        if (border != null) {
            border.show(player);
        }
    }

    public void hideBorder(Player player) {
        if (border != null) {
            border.hide(player);
        }
    }

    public boolean hasBorder() {
        return border != null;
    }

    public void cleanup() {
        if (border != null) {
            border.cleanup();
        }
    }
    
    // Método para refrescar el borde a un jugador específico
    public void refreshBorder(Player player) {
        if (border != null && player.isOnline()) {
            border.show(player);
        }
    }
}
