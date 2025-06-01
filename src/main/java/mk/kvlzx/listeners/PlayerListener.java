package mk.kvlzx.listeners;

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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mk.kvlzx.items.CustomItem.ItemType;
import mk.kvlzx.managers.RankManager;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.items.ItemsManager;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.TitleUtils;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.arena.Zone;
import mk.kvlzx.cosmetics.DeathMessageItem;
import mk.kvlzx.stats.PlayerStats;

public class PlayerListener implements Listener {
    private final MysthicKnockBack plugin;

    private final Random random = new Random();
    
    private static final List<String> DEATH_MESSAGES = Arrays.asList(
        "&b%s &ese resbaló con una cáscara de plátano. &6¡Qué torpe!",
        "&b%s &einventó intentó volar, pero olvidó sus alas.",
        "&b%s &ecayó al vacío gritando &6'¡Volveré!'",
        "&b%s &efue engañado por un espejismo y se estrelló.",
        "&b%s &equiso bailar en el borde y... &6¡pum! &eAl suelo.",
        "&b%s &epensó que era inmortal. &6Spoiler: &eno lo era.",
        "&b%s &ese tropezó con su propio ego.",
        "&b%s &efue vencido por la gravedad, su peor enemiga.",
        "&b%s &einventó un truco épico y acabó en el suelo.",
        "&b%s &ese distrajo mirando un gatito y &6¡adiós!",
        "&b%s &ecreyó que podía correr más rápido que el viento. &6Nop.",
        "&b%s &ese cayó, como su Wi-Fi en plena partida.",
        "&b%s &equiso ser héroe, pero la física dijo &6'nop'.",
        "&b%s &ese lanzó al vacío con &6demasiada &econfianza.",
        "&b%s &efue traicionado por su propia coordinación.",
        "&b%s &epensó que el suelo era lava... y no estaba tan equivocado.",
        "&b%s &einventó un salto mortal y se quedó en mortal.",
        "&b%s &ecayó en las mentiras de la plataforma invisible.",
        "&b%s &equiso impresionar y solo impresionó al suelo.",
        "&b%s &egritó &6'¡Soy invencible!' &ejusto antes de caer."
    );

    public PlayerListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setDeathMessage(null);

        Player victim = event.getEntity();
        Player killer = plugin.getCombatListener().getLastAttacker(victim);

        // Solo mostrar mensaje si murió solo
        if (killer == null) {
            String messageName = plugin.getCosmeticManager().getPlayerDeathMessage(victim.getUniqueId());
            String deathMessage;
            
            if (messageName.equals("default")) {
                deathMessage = DEATH_MESSAGES.get(random.nextInt(DEATH_MESSAGES.size()));
            } else {
                DeathMessageItem messageItem = DeathMessageItem.getByName(messageName);
                deathMessage = messageItem != null ? messageItem.getMessage() : DEATH_MESSAGES.get(0);
            }
            
            String formattedMessage = String.format(deathMessage, victim.getName());
            Bukkit.broadcastMessage(MessageUtils.getColor(formattedMessage));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemsManager.giveSpawnItems(player);
        TitleUtils.sendTitle(
            player, 
            "&a¡Bienvenido a &6KnockbackFFA&a!", 
            "&fDomina la arena y deja tu marca.",
            20, 60, 20
        );
        
        // Actualizar rango
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        RankManager.updatePlayerRank(player, stats.getElo());
        
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena != null) {
            Arena arena = plugin.getArenaManager().getArena(currentArena);
            if (arena != null && arena.getSpawnLocation() != null) {
                player.teleport(arena.getSpawnLocation());
                plugin.getArenaManager().addPlayerToArena(player, currentArena);
                // Mostrar el borde al reconectarse
                plugin.getArenaManager().showArenaBorder(arena);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        stats.updatePlayTime();
        stats.resetStreak();
        
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena != null) {
            plugin.getArenaManager().removePlayerFromArena(player, currentArena);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return; // Permitir en modo creativo
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return; // Permitir en modo creativo
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return; // Permitir en modo creativo
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode() == GameMode.CREATIVE) return; // Permitir en modo creativo
        
        // Cancelar cualquier click en el inventario del jugador
        if (event.getClickedInventory() != null && 
            (event.getClickedInventory().getType() == InventoryType.PLAYER || 
                event.getClick().isKeyboardClick())) {
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
                PlayerStats playerStats = PlayerStats.getStats(player.getUniqueId());
                if (!playerStats.canDie()) {
                    return; // Si el jugador está en cooldown de muerte, ignorar
                }
                CombatListener combatListener = plugin.getCombatListener();
                Player killer = combatListener.getLastAttacker(player);
                
                if (killer != null && killer != player) {
                    givePearlToKiller(killer);
                    player.damage(1000.0, killer);
                } else {
                    player.damage(1000.0);
                }
                
                // Teletransportar inmediatamente al jugador al spawn
                respawnPlayerAtSpawn(player, arena);
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
                            player.spigot().setCollidesWithEntities(false); // Desactivar colisiones en spawn
                            break;
                        case "pvp":
                            player.sendMessage(MessageUtils.getColor("&cHas entrado a la zona de PvP de la arena " + foundArena));
                            ItemsManager.givePvPItems(player);
                            player.spigot().setCollidesWithEntities(true); // Activar colisiones en pvp
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
        // Verificar si el killer está en zona pvp
        String killerZone = plugin.getArenaManager().getPlayerZone(killer);
        if (killerZone == null || !killerZone.equals("pvp")) {
            return; // No dar perla si no está en zona pvp
        }

        // Encontrar el slot donde debería ir la perla según el layout del jugador
        int pearlSlot = findSlotByType(killer, Material.ENDER_PEARL);
        if (pearlSlot == -1) return; // Si no tiene configurada la perla, no dar nada

        ItemStack currentItem = killer.getInventory().getItem(pearlSlot);
        ItemStack pearlItem = CustomItem.create(ItemType.PEARL);
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

    // Método para encontrar el slot de un item por tipo
    private int findSlotByType(Player player, Material type) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < 9; i++) { // Solo buscar en la hotbar
            if (contents[i] != null && contents[i].getType() == type) {
                return i;
            }
        }
        return 8; // Si no encuentra el slot configurado, usar el último slot como fallback
    }

    private void respawnPlayerAtSpawn(Player player, Arena arena) {
        PlayerStats playerStats = PlayerStats.getStats(player.getUniqueId());
        playerStats.addDeath();
        playerStats.resetStreak();
        
        // Resetear velocidad a la default
        player.setWalkSpeed(0.2f);
        
        Location spawnLoc = arena.getSpawnLocation();
        if (spawnLoc != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.spigot().respawn();
                    player.teleport(spawnLoc);
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                    ItemsManager.giveSpawnItems(player);
                    RankManager.updatePlayerRank(player, playerStats.getElo());
                    
                    player.setNoDamageTicks(40);
                    
                    // Mostrar el borde de la arena al respawn
                    plugin.getArenaManager().showArenaBorder(arena);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Actualizar el rango en el respawn por si acaso
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        RankManager.updatePlayerRank(player, stats.getElo());
    }
}