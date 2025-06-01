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
import mk.kvlzx.stats.PlayerStats;

public class PlayerListener implements Listener {
    private final MysthicKnockBack plugin;
    
    private static final List<String> DEATH_MESSAGES = Arrays.asList(
        "%s se resbaló con una cáscara de plátano. ¡Qué torpe!",
        "%s intentó volar, pero olvidó sus alas.",
        "%s cayó al vacío gritando '¡Volveré!'",
        "%s fue engañado por un espejismo y se estrelló.",
        "%s quiso bailar en el borde y... ¡pum! Al suelo.",
        "%s pensó que era inmortal. Spoiler: no lo era.",
        "%s se tropezó con su propio ego.",
        "%s fue vencido por la gravedad, su peor enemiga.",
        "%s intentó un truco épico y acabó en el suelo.",
        "%s se distrajo mirando un gatito y ¡adiós!",
        "%s creyó que podía correr más rápido que el viento. Nop.",
        "%s se cayó, como su Wi-Fi en plena partida.",
        "%s quiso ser héroe, pero la física dijo 'nop'.",
        "%s se lanzó al vacío con demasiada confianza.",
        "%s fue traicionado por su propia coordinación.",
        "%s pensó que el suelo era lava... y no estaba tan equivocado.",
        "%s intentó un salto mortal y se quedó en mortal.",
        "%s cayó en las mentiras de la plataforma invisible.",
        "%s quiso impresionar y solo impresionó al suelo.",
        "%s gritó '¡Soy invencible!' justo antes de caer."
    );
    
    private final Random random = new Random();

    public PlayerListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear(); // Evita que se dropeen items al morir
        event.setDroppedExp(0); // Evita que se dropee experiencia
        event.setDeathMessage(null); // Evita que se muestre el mensaje de muerte por defecto

        // Selecciona un mensaje de muerte aleatorio y reemplaza %s con el nombre del jugador
        String playerName = event.getEntity().getName();
        String randomMessage = DEATH_MESSAGES.get(random.nextInt(DEATH_MESSAGES.size()));
        String formattedMessage = String.format(randomMessage, playerName);

        // Transmite el mensaje de muerte al servidor
        Bukkit.broadcastMessage(MessageUtils.getColor(formattedMessage));
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