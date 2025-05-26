package kk.kvlzx.arena;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;

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
        if (this.border != null) {
            this.border.cleanup();
        }
        this.border = new VirtualBorder(center, size);
        
        // Mostrar el nuevo borde a todos los jugadores en la arena
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            String playerArena = KvKnockback.getInstance().getArenaManager().getPlayerArena(player);
            if (playerArena != null && playerArena.equals(this.name)) {
                showBorder(player);
            }
        }
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

    public void updateBorderSize(double size) {
        if (border != null) {
            border.updateSize(size);
        }
    }
}
