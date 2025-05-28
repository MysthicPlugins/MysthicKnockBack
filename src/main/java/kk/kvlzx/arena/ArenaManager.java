package kk.kvlzx.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.data.ArenaData;

public class ArenaManager {
    private final KvKnockback plugin;
    private final Map<String, Arena> arenas;
    private final Map<UUID, String> playerZones;
    private final Map<String, Set<UUID>> arenaPlayers = new HashMap<>();
    private String currentArena = null;
    private final ArenaData arenaData;

    public ArenaManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.playerZones = new HashMap<>();
        this.arenaData = new ArenaData(plugin);
        loadArenas();
    }

    public boolean createArena(String name) {
        if (arenas.containsKey(name)) {
            return false;
        }
        Arena arena = new Arena(name);
        arenas.put(name, arena);
        arenaPlayers.put(name, new HashSet<>());
        if (currentArena == null) currentArena = name;
        return true;
    }

    public boolean setZone(String arenaName, String zoneTypeStr, Player player) {
        ZoneType zoneType = ZoneType.fromString(zoneTypeStr);
        if (zoneType == null) return false;

        Arena arena = arenas.get(arenaName);
        if (arena == null) return false;

        WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            Selection selection = worldEdit.getSelection(player);
            if (selection == null) return false;

            Location min = selection.getMinimumPoint();
            Location max = selection.getMaximumPoint();

            Zone zone = new Zone(min, max, zoneType);
            arena.setZone(zoneType, zone); // Ahora pasamos ZoneType
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setSpawn(String arenaName, Location location) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) return false;
        arena.setSpawnPoint(location);
        return true;
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public void setPlayerZone(Player player, String arenaName, String zone) {
        if (zone == null) {
            playerZones.remove(player.getUniqueId());
        } else {
            playerZones.put(player.getUniqueId(), arenaName + ":" + zone);
        }
    }

    public String getPlayerZone(Player player) {
        String zoneInfo = playerZones.get(player.getUniqueId());
        return zoneInfo != null ? zoneInfo.split(":")[1] : null;
    }

    public String getPlayerArena(Player player) {
        String zoneInfo = playerZones.get(player.getUniqueId());
        return zoneInfo != null ? zoneInfo.split(":")[0] : null;
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public void addPlayerToArena(Player player, String arenaName) {
        arenaPlayers.get(arenaName).add(player.getUniqueId());
        Arena arena = getArena(arenaName);
        if (arena != null && arena.hasBorder()) {
            arena.showBorder(player);
        }
    }

    public void removePlayerFromArena(Player player, String arenaName) {
        if (arenaPlayers.containsKey(arenaName)) {
            arenaPlayers.get(arenaName).remove(player.getUniqueId());
            Arena arena = getArena(arenaName);
            if (arena != null && arena.hasBorder()) {
                arena.hideBorder(player);
            }
        }
    }

    public Set<Player> getPlayersInArena(String arenaName) {
        Set<Player> players = new HashSet<>();
        if (arenaPlayers.containsKey(arenaName)) {
            for (UUID uuid : arenaPlayers.get(arenaName)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    players.add(player);
                }
            }
        }
        return players;
    }

    public String getCurrentArena() {
        return currentArena;
    }

    public void setCurrentArena(String arenaName) {
        // Verificar si es la misma arena
        if (this.currentArena != null && this.currentArena.equals(arenaName)) return;

        Arena newArena = getArena(arenaName);
        if (newArena == null) return;

        // Obtener la arena anterior
        Arena oldArena = this.currentArena != null ? getArena(this.currentArena) : null;

        // Primero configurar la nueva arena antes de mostrar cualquier borde
        this.currentArena = arenaName;

        // Ocultar el borde antiguo y mostrar el nuevo simultáneamente
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (oldArena != null && oldArena.hasBorder()) {
                oldArena.hideBorder(player);
            }
            
            // Pequeño delay antes de mostrar el nuevo borde para evitar la animación
            if (newArena.hasBorder()) {
                Bukkit.getScheduler().runTaskLater(KvKnockback.getInstance(), () -> {
                    if (player.isOnline()) {
                        newArena.showBorder(player);
                    }
                }, 2L);
            }
        }
    }

    public String getNextArena() {
        List<String> arenaList = new ArrayList<>(arenas.keySet());
        if (arenaList.isEmpty()) return null;
        
        int currentIndex = arenaList.indexOf(currentArena);
        if (currentIndex == -1) return arenaList.get(0);
        
        return arenaList.get((currentIndex + 1) % arenaList.size());
    }

    public Arena getArenaByPlayer(Player player) {
        String arenaName = getPlayerArena(player);
        return arenaName != null ? getArena(arenaName) : null;
    }

    public boolean deleteArena(String name) {
        if (!arenas.containsKey(name)) {
            return false;
        }

        // Si es la arena actual, cambiar a la siguiente
        if (name.equals(currentArena)) {
            String nextArena = getNextArena();
            if (nextArena != null && !nextArena.equals(name)) {
                currentArena = nextArena;
            } else {
                currentArena = null;
            }
        }

        // Remover jugadores de la arena
        Set<UUID> players = arenaPlayers.remove(name);
        if (players != null) {
            players.forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    playerZones.remove(player.getUniqueId());
                }
            });
        }

        arenas.remove(name);
        return true;
    }

    public void loadArenas() {
        File dataFolder = new File(plugin.getDataFolder(), "data");
        File arenaFile = new File(dataFolder, "arenas.yml");
        
        if (!arenaFile.exists()) return;

        ConfigurationSection config = arenaData.getConfig();
        if (!config.contains("arenas")) return;

        for (String arenaName : config.getConfigurationSection("arenas").getKeys(false)) {
            Arena arena = new Arena(arenaName);
            arenaData.loadArena(arenaName, arena);
            arenas.put(arenaName, arena);
            arenaPlayers.put(arenaName, new HashSet<>());
        }

        String activeArena = arenaData.loadActiveArena();
        if (activeArena != null && arenas.containsKey(activeArena)) {
            setCurrentArena(activeArena); // Usar el método modificado en vez de asignación directa
        } else if (!arenas.isEmpty()) {
            setCurrentArena(arenas.keySet().iterator().next());
        }
    }

    public void saveArenas() {
        for (Arena arena : arenas.values()) {
            arenaData.saveArena(arena);
        }
        if (currentArena != null) {
            arenaData.saveActiveArena(currentArena);
        }
    }
}
