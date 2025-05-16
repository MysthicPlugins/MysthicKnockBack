package kk.kvlzx.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear(); // Evita que se dropeen items al morir
        event.setDroppedExp(0); // Evita que se dropee experiencia
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemsManager.giveSpawnItems(player);
        
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena != null) {
            Arena arena = plugin.getArenaManager().getArena(currentArena);
            if (arena != null && arena.getSpawnLocation() != null) {
                player.teleport(arena.getSpawnLocation());
                plugin.getArenaManager().addPlayerToArena(player, currentArena);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena != null) {
            plugin.getArenaManager().removePlayerFromArena(player, currentArena);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String currentArena = plugin.getArenaManager().getPlayerArena(player);
        if (currentArena == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        // Cancelar el poder agarrar items si el jugador está dentro de una arena
        Player player = event.getPlayer();
        String currentArena = plugin.getArenaManager().getPlayerArena(player);
        if (currentArena == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        // Cancelar el poder romper bloques si el jugador está dentro de una arena
        Player player = event.getPlayer();
        String currentArena = plugin.getArenaManager().getPlayerArena(player);
        if (currentArena == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        // Evitar que el jugador clickee su propio inventario (para que no pueda mover items)
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String currentArena = plugin.getArenaManager().getPlayerArena(player);
        if (currentArena == null) return;

        if (event.getClickedInventory() == player.getInventory()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        String currentArena = plugin.getArenaManager().getPlayerArena(player);
        String currentZone = plugin.getArenaManager().getPlayerZone(player);
        
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
            
            String foundZone = null;
            String foundArena = null;
            
            for (String zoneType : new String[]{"spawn", "pvp", "void"}) {
                Zone zone = arena.getZone(zoneType);
                if (zone != null && zone.isInside(to)) {
                    foundZone = zoneType;
                    foundArena = arena.getName();
                    break;
                }
            }
            
            if (foundZone != null) {
                if (!foundZone.equals(currentZone) || !foundArena.equals(currentArena)) {
                    plugin.getArenaManager().setPlayerZone(player, foundArena, foundZone);
                    
                    switch (foundZone) {
                        case "spawn":
                            player.sendMessage(MessageUtils.getColor("&aHas entrado a la zona de Spawn de la arena " + foundArena));
                            ItemsManager.giveSpawnItems(player);
                            break;
                        case "pvp":
                            player.sendMessage(MessageUtils.getColor("&cHas entrado a la zona de PvP de la arena " + foundArena));
                            ItemsManager.givePvPItems(player);
                            break;
                        case "void":
                            player.sendMessage(MessageUtils.getColor("&7Has entrado a la zona de Void de la arena " + foundArena));
                            break;
                    }
                }
                return;
            }
        }
        
        if (currentZone != null) {
            plugin.getArenaManager().setPlayerZone(player, null, null);
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