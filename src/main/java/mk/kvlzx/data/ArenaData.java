package mk.kvlzx.data;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.arena.Zone;
import mk.kvlzx.arena.ZoneType;
import mk.kvlzx.utils.LocationUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class ArenaData {
    private final MysthicKnockBack plugin;
    private final CustomConfig arenaConfig;

    public ArenaData(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.arenaConfig = new CustomConfig("arenas.yml", "data", plugin);
        this.arenaConfig.registerConfig();
    }

    public void saveArena(Arena arena) {
        String basePath = "arenas." + arena.getName();
        
        // Guardar cada zona
        for (ZoneType type : ZoneType.values()) {
            Zone zone = arena.getZone(type.getId());
            if (zone != null) {
                String zonePath = basePath + ".zones." + type.getId();
                arenaConfig.getConfig().set(zonePath + ".loc1", LocationUtils.serialize(zone.getMin()));
                arenaConfig.getConfig().set(zonePath + ".loc2", LocationUtils.serialize(zone.getMax()));
            }
        }

        // Guardar spawnpoint si existe
        Location spawn = arena.getSpawnLocation();
        if (spawn != null) {
            arenaConfig.getConfig().set(basePath + ".spawnpoint", LocationUtils.serialize(spawn));
        }

        // Guardar tamaño del borde
        arenaConfig.getConfig().set(basePath + ".borderSize", arena.getBorderSize());

        arenaConfig.saveConfig();
    }

    public void saveActiveArena(String arenaName) {
        arenaConfig.getConfig().set("activeArena", arenaName);
        arenaConfig.saveConfig();
    }

    public String loadActiveArena() {
        return arenaConfig.getConfig().getString("activeArena");
    }

    public void loadArena(String arenaName, Arena arena) {
        String basePath = "arenas." + arenaName + ".zones";

        if (!arenaConfig.getConfig().contains(basePath)) return;

        // Cargar zonas
        for (String zoneTypeStr : arenaConfig.getConfig().getConfigurationSection(basePath).getKeys(false)) {
            ZoneType zoneType = ZoneType.fromString(zoneTypeStr);
            if (zoneType == null) continue;

            String zonePath = basePath + "." + zoneTypeStr;
            String loc1Str = arenaConfig.getConfig().getString(zonePath + ".loc1");
            String loc2Str = arenaConfig.getConfig().getString(zonePath + ".loc2");

            if (loc1Str != null && loc2Str != null) {
                Zone zone = new Zone(
                    LocationUtils.deserialize(loc1Str),
                    LocationUtils.deserialize(loc2Str),
                    zoneType
                );
                arena.setZone(zoneType, zone);
            }
        }

        // Cargar spawnpoint específico
        String spawnPath = "arenas." + arenaName + ".spawnpoint";
        if (arenaConfig.getConfig().contains(spawnPath)) {
            Location spawn = LocationUtils.deserialize(arenaConfig.getConfig().getString(spawnPath));
            if (spawn != null) {
                arena.setSpawnPoint(spawn);
            }
        }

        // Cargar tamaño del borde
        String borderPath = "arenas." + arenaName + ".borderSize";
        if (arenaConfig.getConfig().contains(borderPath)) {
            arena.setBorderSize(arenaConfig.getConfig().getInt(borderPath));
        }
    }

    public boolean hasArenaData(String arenaName) {
        return arenaConfig.getConfig().contains("arenas." + arenaName);
    }

    public void deleteArena(String arenaName) {
        arenaConfig.getConfig().set("arenas." + arenaName, null);
        arenaConfig.saveConfig();
    }

    public ConfigurationSection getConfig() {
        return arenaConfig.getConfig();
    }
}
