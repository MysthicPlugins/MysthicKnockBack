package kk.kvlzx.arena;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

public class Arena {
    private final String name;
    private final Map<ZoneType, Zone> zones; // Cambiado de String a ZoneType
    private Location spawnPoint;
    private Integer borderSize; // Cambiar a Integer para poder ser null

    public Arena(String name) {
        this.name = name;
        this.zones = new HashMap<>();
        this.borderSize = null; // Inicialmente null hasta que se defina
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

    public Integer getBorderSize() {
        return borderSize;
    }

    public boolean hasBorder() {
        return borderSize != null;
    }

    public boolean isInsideBorder(Location playerLoc) {
        if (!hasBorder()) return true;
        
        Location center;
        if (spawnPoint != null) {
            center = spawnPoint;
        } else {
            center = playerLoc;
        }
        
        // Verificar si está dentro del cuadrado usando coordenadas X y Z
        int xDiff = Math.abs(playerLoc.getBlockX() - center.getBlockX());
        int zDiff = Math.abs(playerLoc.getBlockZ() - center.getBlockZ());
        
        // Si cualquier diferencia es mayor que el tamaño del borde, está fuera
        return xDiff <= borderSize && zDiff <= borderSize;
    }
}
