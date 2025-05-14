package kk.kvlzx.listeners;

import org.bukkit.event.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import kk.kvlzx.items.Pearl;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.items.ItemsManager;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.arena.Arena;
import kk.kvlzx.arena.Zone;
import kk.kvlzx.stats.PlayerStats;

public class PlayerListener implements Listener {
    private final KvKnockback plugin;

    public PlayerListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemsManager.giveSpawnItems(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            Zone voidZone = arena.getZone("void");
            if (voidZone != null && voidZone.isInside(to)) {
                CombatListener combatListener = plugin.getCombatListener();
                Player killer = combatListener.getLastAttacker(player);
                
                if (killer != null && killer != player) {
                    givePearlToKiller(killer);
                    player.damage(1000.0, killer); // Dar la kill del jugador al killer
                    respawnPlayerAtSpawn(player, arena);
                } else {
                    player.damage(1000.0); // Sin killer
                    respawnPlayerAtSpawn(player, arena);
                }
                return;
            }
        }
        
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            Zone currentZone = null;
            
            // Mirar por cada zona
            for (String zoneType : new String[]{"spawn", "pvp", "void"}) {
                Zone zone = arena.getZone(zoneType);
                if (zone != null && zone.isInside(to)) {
                    currentZone = zone;
                    String lastZone = plugin.getArenaManager().getPlayerZone(player);
                    
                    if (!zone.getType().equals(lastZone)) {
                        // Actualizar la zona del jugador
                        plugin.getArenaManager().setPlayerZone(player, zone.getType());
                        
                        // Mandar mensaje (DEBUG)
                        switch (zone.getType()) {
                            case "spawn":
                                player.sendMessage(MessageUtils.getColor("&aHas entrado a la zona de Spawn"));
                                ItemsManager.giveSpawnItems(player);
                                break;
                            case "pvp":
                                player.sendMessage(MessageUtils.getColor("&cHas entrado a la zona de PvP"));
                                ItemsManager.givePvPItems(player);
                                break;
                            case "void":
                                player.sendMessage(MessageUtils.getColor("&7Has entrado a la zona de Void"));
                                break;
                        }
                    }
                    break;
                }
            }
            
            if (currentZone == null) {
                plugin.getArenaManager().setPlayerZone(player, null);
            }
        }
    }

    private void givePearlToKiller(Player killer) {
        int pearlSlot = 8;
        ItemStack currentItem = killer.getInventory().getItem(pearlSlot);

        Pearl pearl = new Pearl(
            "&5 Perla",
            Arrays.asList(MessageUtils.getColor("&8 Cada lanzamiento reescribe tu destino.")),
            Material.ENDER_PEARL
        );
        ItemStack pearlItem = pearl.getItem();
        pearlItem.setAmount(1);

        if (currentItem == null || currentItem.getType() == Material.AIR) {
            killer.getInventory().setItem(pearlSlot, pearlItem);
        } else if (currentItem.getType() == Material.ENDER_PEARL) {
            int currentAmount = currentItem.getAmount();
            if (currentAmount < 128) {
                currentItem.setAmount(currentAmount + 1);
                killer.getInventory().setItem(pearlSlot, currentItem);
            }
        }
        killer.updateInventory();

        PlayerStats killerStats = PlayerStats.getStats(killer.getUniqueId());
        killerStats.addKill();
    }

    private void respawnPlayerAtSpawn(Player player, Arena arena) {
        PlayerStats playerStats = PlayerStats.getStats(player.getUniqueId());
        playerStats.addDeath();
        
        Location spawnLoc = arena.getSpawnLocation();
        if (spawnLoc != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.spigot().respawn();
                    player.teleport(spawnLoc);
                    ItemsManager.giveSpawnItems(player);
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}