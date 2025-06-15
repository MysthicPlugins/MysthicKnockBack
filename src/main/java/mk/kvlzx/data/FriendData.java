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

public class FriendData {
    private final MysthicKnockBack plugin;
    private final CustomConfig friendConfig;

    public FriendData(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.friendConfig = new CustomConfig("friends.yml", "data", plugin);
        this.friendConfig.registerConfig();
    }

    public void saveFriends(Map<UUID, Set<UUID>> friends) {
        // Limpiar datos existentes
        friendConfig.getConfig().set("friends", null);
        
        for (Map.Entry<UUID, Set<UUID>> entry : friends.entrySet()) {
            String playerUUID = entry.getKey().toString();
            List<String> friendUUIDs = new ArrayList<>();
            
            for (UUID friendUUID : entry.getValue()) {
                friendUUIDs.add(friendUUID.toString());
            }
            
            friendConfig.getConfig().set("friends." + playerUUID, friendUUIDs);
        }
        
        friendConfig.saveConfig();
    }

    public void savePendingRequests(Map<UUID, Set<UUID>> pendingRequests) {
        // Limpiar datos existentes
        friendConfig.getConfig().set("pendingRequests", null);
        
        for (Map.Entry<UUID, Set<UUID>> entry : pendingRequests.entrySet()) {
            String fromUUID = entry.getKey().toString();
            List<String> toUUIDs = new ArrayList<>();
            
            for (UUID toUUID : entry.getValue()) {
                toUUIDs.add(toUUID.toString());
            }
            
            friendConfig.getConfig().set("pendingRequests." + fromUUID, toUUIDs);
        }
        
        friendConfig.saveConfig();
    }

    public void savePlayerUUIDs(Map<String, UUID> playerUUIDs) {
        // Limpiar datos existentes
        friendConfig.getConfig().set("playerUUIDs", null);
        
        for (Map.Entry<String, UUID> entry : playerUUIDs.entrySet()) {
            friendConfig.getConfig().set("playerUUIDs." + entry.getKey(), entry.getValue().toString());
        }
        
        friendConfig.saveConfig();
    }

    public Map<UUID, Set<UUID>> loadFriends() {
        Map<UUID, Set<UUID>> friends = new HashMap<>();
        
        if (!friendConfig.getConfig().contains("friends")) {
            return friends;
        }
        
        ConfigurationSection friendsSection = friendConfig.getConfig().getConfigurationSection("friends");
        if (friendsSection == null) {
            return friends;
        }
        
        for (String playerUUIDStr : friendsSection.getKeys(false)) {
            try {
                UUID playerUUID = UUID.fromString(playerUUIDStr);
                List<String> friendUUIDStrings = friendsSection.getStringList(playerUUIDStr);
                Set<UUID> friendUUIDs = new HashSet<>();
                
                for (String friendUUIDStr : friendUUIDStrings) {
                    try {
                        friendUUIDs.add(UUID.fromString(friendUUIDStr));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid friend UUID: " + friendUUIDStr);
                    }
                }
                
                friends.put(playerUUID, friendUUIDs);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid player UUID: " + playerUUIDStr);
            }
        }
        
        return friends;
    }

    public Map<UUID, Set<UUID>> loadPendingRequests() {
        Map<UUID, Set<UUID>> pendingRequests = new HashMap<>();
        
        if (!friendConfig.getConfig().contains("pendingRequests")) {
            return pendingRequests;
        }
        
        ConfigurationSection requestsSection = friendConfig.getConfig().getConfigurationSection("pendingRequests");
        if (requestsSection == null) {
            return pendingRequests;
        }
        
        for (String fromUUIDStr : requestsSection.getKeys(false)) {
            try {
                UUID fromUUID = UUID.fromString(fromUUIDStr);
                List<String> toUUIDStrings = requestsSection.getStringList(fromUUIDStr);
                Set<UUID> toUUIDs = new HashSet<>();
                
                for (String toUUIDStr : toUUIDStrings) {
                    try {
                        toUUIDs.add(UUID.fromString(toUUIDStr));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid request target UUID: " + toUUIDStr);
                    }
                }
                
                pendingRequests.put(fromUUID, toUUIDs);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid request sender UUID: " + fromUUIDStr);
            }
        }
        
        return pendingRequests;
    }

    public Map<String, UUID> loadPlayerUUIDs() {
        Map<String, UUID> playerUUIDs = new HashMap<>();
        
        if (!friendConfig.getConfig().contains("playerUUIDs")) {
            return playerUUIDs;
        }
        
        ConfigurationSection uuidsSection = friendConfig.getConfig().getConfigurationSection("playerUUIDs");
        if (uuidsSection == null) {
            return playerUUIDs;
        }
        
        for (String playerName : uuidsSection.getKeys(false)) {
            try {
                String uuidStr = uuidsSection.getString(playerName);
                if (uuidStr != null) {
                    UUID uuid = UUID.fromString(uuidStr);
                    playerUUIDs.put(playerName, uuid);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID for player " + playerName + ": " + uuidsSection.getString(playerName));
            }
        }
        
        return playerUUIDs;
    }

    public void saveAllData(Map<UUID, Set<UUID>> friends, Map<UUID, Set<UUID>> pendingRequests, Map<String, UUID> playerUUIDs) {
        saveFriends(friends);
        savePendingRequests(pendingRequests);
        savePlayerUUIDs(playerUUIDs);
    }

    public ConfigurationSection getConfig() {
        return friendConfig.getConfig();
    }
}
