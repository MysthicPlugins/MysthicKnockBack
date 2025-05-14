package kk.kvlzx.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import kk.kvlzx.KvKnockback;

public class ArenaManager {
    private final KvKnockback plugin;
    private final Map<String, Arena> arenas;
    private final Map<UUID, String> playerZones;

    public ArenaManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.playerZones = new HashMap<>();
    }

    public boolean createArena(String name) {
        if (arenas.containsKey(name)) {
            return false;
        }
        Arena arena = new Arena(name);
        arenas.put(name, arena);
        return true;
    }

    public boolean setZone(String arenaName, String zoneType, Player player) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) return false;

        WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        try {
            Selection selection = worldEdit.getSelection(player);
            if (selection == null) return false;

            Location min = selection.getMinimumPoint();
            Location max = selection.getMaximumPoint();

            Zone zone = new Zone(min, max, zoneType.toLowerCase());
            arena.setZone(zoneType.toLowerCase(), zone);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public void setPlayerZone(Player player, String zone) {
        if (zone == null) {
            playerZones.remove(player.getUniqueId());
        } else {
            playerZones.put(player.getUniqueId(), zone);
        }
    }

    public String getPlayerZone(Player player) {
        return playerZones.get(player.getUniqueId());
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }
}
