package mk.kvlzx.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class IgnoreData {
    private final MysthicKnockBack plugin;
    private final CustomConfig ignoreConfig;

    public IgnoreData(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.ignoreConfig = new CustomConfig("ignore.yml", "data", plugin);
        this.ignoreConfig.registerConfig();
    }

    public void saveIgnoreData(UUID playerUUID, Set<UUID> ignoredPlayers) {
        String playerPath = "players." + playerUUID.toString();
        
        if (ignoredPlayers.isEmpty()) {
            // Si no hay jugadores ignorados, eliminar la entrada
            ignoreConfig.getConfig().set(playerPath, null);
        } else {
            // Convertir UUIDs a strings para guardar
            List<String> ignoredList = new ArrayList<>();
            for (UUID ignoredUUID : ignoredPlayers) {
                ignoredList.add(ignoredUUID.toString());
            }
            ignoreConfig.getConfig().set(playerPath + ".ignored", ignoredList);
        }
        
        ignoreConfig.saveConfig();
    }

    public Set<UUID> loadIgnoreData(UUID playerUUID) {
        String playerPath = "players." + playerUUID.toString() + ".ignored";
        Set<UUID> ignoredPlayers = new HashSet<>();
        
        if (ignoreConfig.getConfig().contains(playerPath)) {
            List<String> ignoredList = ignoreConfig.getConfig().getStringList(playerPath);
            for (String uuidStr : ignoredList) {
                try {
                    ignoredPlayers.add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException e) {
                    // Si hay un UUID inválido, lo ignoramos y continuamos
                    plugin.getLogger().warning("Invalid UUID found in ignore data: " + uuidStr);
                }
            }
        }
        
        return ignoredPlayers;
    }

    public void saveAllIgnoreData(Map<UUID, Set<UUID>> ignoredPlayers) {
        // Limpiar datos existentes
        ignoreConfig.getConfig().set("players", null);
        
        // Guardar todos los datos
        for (Map.Entry<UUID, Set<UUID>> entry : ignoredPlayers.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                saveIgnoreData(entry.getKey(), entry.getValue());
            }
        }
    }

    public Map<UUID, Set<UUID>> loadAllIgnoreData() {
        Map<UUID, Set<UUID>> ignoredPlayers = new HashMap<>();
        
        if (!ignoreConfig.getConfig().contains("players")) {
            return ignoredPlayers;
        }
        
        ConfigurationSection playersSection = ignoreConfig.getConfig().getConfigurationSection("players");
        if (playersSection == null) {
            return ignoredPlayers;
        }
        
        for (String playerUUIDStr : playersSection.getKeys(false)) {
            try {
                UUID playerUUID = UUID.fromString(playerUUIDStr);
                Set<UUID> ignored = loadIgnoreData(playerUUID);
                if (!ignored.isEmpty()) {
                    ignoredPlayers.put(playerUUID, ignored);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid player UUID found in ignore data: " + playerUUIDStr);
            }
        }
        
        return ignoredPlayers;
    }

    public boolean hasIgnoreData(UUID playerUUID) {
        return ignoreConfig.getConfig().contains("players." + playerUUID.toString());
    }

    public void deletePlayerIgnoreData(UUID playerUUID) {
        ignoreConfig.getConfig().set("players." + playerUUID.toString(), null);
        ignoreConfig.saveConfig();
    }

    public ConfigurationSection getConfig() {
        return ignoreConfig.getConfig();
    }
}
