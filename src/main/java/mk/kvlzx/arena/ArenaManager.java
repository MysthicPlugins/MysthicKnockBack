package mk.kvlzx.arena;

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

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.data.ArenaData;
import mk.kvlzx.powerup.PowerUp;
import mk.kvlzx.powerup.PowerUpManager;

public class ArenaManager {
    private final MysthicKnockBack plugin;
    private final Map<String, Arena> arenas;
    private final Map<UUID, String> playerZones;
    private final Map<String, Set<UUID>> arenaPlayers = new HashMap<>();
    private String currentArena = null;
    private final ArenaData arenaData;
    private PowerUpManager powerUpManager; // Nuevo campo

    public ArenaManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.playerZones = new HashMap<>();
        this.arenaData = new ArenaData(plugin);
        loadArenas();
        // Inicializar el sistema de powerups después de cargar las arenas
        this.powerUpManager = new PowerUpManager(plugin, this);
    }

    public boolean createArena(String name) {
        if (arenas.containsKey(name)) {
            return false;
        }
        Arena arena = new Arena(name);
        arenas.put(name, arena);
        arenaPlayers.put(name, new HashSet<>());
        if (currentArena == null) currentArena = name;
        
        // Agregar la arena al sistema de powerups
        if (powerUpManager != null) {
            powerUpManager.addArena(name);
        }
        
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
    }

    public void removePlayerFromArena(Player player, String arenaName) {
        if (arenaPlayers.containsKey(arenaName)) {
            arenaPlayers.get(arenaName).remove(player.getUniqueId());
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

    public boolean setBorder(String arenaName, int size) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) return false;
        arena.setBorderSize(size);
        return true;
    }

    public void showArenaBorder(Arena arena) {
        if (arena == null || arena.getBorderSize() <= 0) return;

        // Esperar 5 ticks antes de mostrar el nuevo borde
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location spawn = arena.getSpawnLocation();
            if (spawn == null) return;

            int size = arena.getBorderSize();
            WorldBorder border = new WorldBorder(spawn, size);
            
            // Solo mostrar a los jugadores en esta arena
            for (UUID playerUUID : arenaPlayers.get(arena.getName())) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    border.show(player);
                }
            }
        }, 5L);
    }

    private void hideCurrentBorder() {
        if (currentArena == null) return;
        Arena arena = arenas.get(currentArena);
        if (arena == null || arena.getBorderSize() <= 0) return;

        // Solo ocultar a los jugadores que estaban en esta arena
        Set<UUID> players = arenaPlayers.get(currentArena);
        if (players != null) {
            for (UUID playerUUID : players) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    WorldBorder.removeBorder(player);
                }
            }
        }
    }

    public void setCurrentArena(String arenaName) {
        hideCurrentBorder(); // Primero ocultar el borde a los jugadores de la arena actual
        this.currentArena = arenaName; // Actualizar la arena actual
        if (arenaName != null) {
            showArenaBorder(getArena(arenaName)); // Mostrar el nuevo borde a los jugadores de la nueva arena
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

        // Limpiar powerups de la arena antes de eliminarla
        if (powerUpManager != null) {
            powerUpManager.removeArena(name);
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
            setCurrentArena(activeArena);
            Arena arena = getArena(activeArena);
            if (arena != null) {
                showArenaBorder(arena);
            }
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

    // Nuevos métodos para el sistema de powerups
    public PowerUpManager getPowerUpManager() {
        return powerUpManager;
    }

    public void forcePowerUpSpawn(String arenaName) {
        if (powerUpManager != null) {
            powerUpManager.forcePowerUpSpawn(arenaName);
        }
    }

    public List<PowerUp> getPowerUpsInArena(String arenaName) {
        if (powerUpManager != null) {
            return powerUpManager.getPowerUpsInArena(arenaName);
        }
        return new ArrayList<>();
    }

    // Método para limpiar recursos cuando se cierra el plugin
    public void shutdown() {
        if (powerUpManager != null) {
            powerUpManager.shutdown();
        }
    }
}
