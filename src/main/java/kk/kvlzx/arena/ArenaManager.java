package kk.kvlzx.arena;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

import kk.kvlzx.KvKnockback;

public class ArenaManager {
    private final KvKnockback plugin;
    private final Map<String, Arena> arenas;

    public ArenaManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
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
            Region region = worldEdit.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
            if (region == null) return false;

            Location min = new Location(player.getWorld(), 
                region.getMinimumPoint().getX(),
                region.getMinimumPoint().getY(), 
                region.getMinimumPoint().getZ());
            
            Location max = new Location(player.getWorld(),
                region.getMaximumPoint().getX(),
                region.getMaximumPoint().getY(),
                region.getMaximumPoint().getZ());

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
}
