package mk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import mk.kvlzx.items.CustomItem.ItemType;
import mk.kvlzx.managers.RankManager;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.items.ItemsManager;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.TitleUtils;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.arena.Zone;
import mk.kvlzx.cosmetics.DeathMessageItem;
import mk.kvlzx.cosmetics.DeathSoundItem;
import mk.kvlzx.cosmetics.KillMessageItem;
import mk.kvlzx.cosmetics.KillSoundItem;
import mk.kvlzx.hotbar.PlayerHotbar;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.menu.Menu;

public class PlayerListener implements Listener {
    private final MysthicKnockBack plugin;
    private final Random random = new Random();

    private static final List<String> DEATH_MESSAGES = MysthicKnockBack.getInstance().getMessagesConfig().getDeathMessages();

    private static final List<String> KILL_MESSAGES =  MysthicKnockBack.getInstance().getMessagesConfig().getKillMessages();

    public PlayerListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemsManager.giveSpawnItems(player);
        if (plugin.getMainConfig().getJoinTitleEnabled()) {
            TitleUtils.sendTitle(
                player,
                plugin.getMainConfig().getJoinTitleTitle(),
                plugin.getMainConfig().getJoinTitleSubtitle(),
                plugin.getMainConfig().getJoinTitleFadeIn(),
                plugin.getMainConfig().getJoinTitleStay(),
                plugin.getMainConfig().getJoinTitleFadeOut()
            );
        }
        
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

        // Verificar si la arena está cambiando al conectarse
        if (plugin.getScoreboardManager().isArenaChanging()) {
            // Si la arena está cambiando, congelar al jugador temporalmente
            player.setWalkSpeed(0.0f);
            player.setFoodLevel(0);
            player.setSaturation(0.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 128, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1, false, false));
            player.setNoDamageTicks(100);
            
            // Programar la restauración de movimiento cuando termine el cambio de arena
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Verificar cada tick si la arena ya no está cambiando
                    if (!plugin.getScoreboardManager().isArenaChanging()) {
                        // Restaurar movimiento normal
                        player.setWalkSpeed(0.2f);
                        player.setFoodLevel(20);
                        player.setSaturation(20.0f);
                        player.removePotionEffect(PotionEffectType.JUMP);
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                        
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                        
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L); // Verificar cada tick
        } else {
            // Si no hay cambio de arena, asegurar velocidad normal
            player.setWalkSpeed(0.2f);
        }
        
        plugin.getScoreboardManager().updatePlayerZone(player, currentArena);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        
        // Verificar si el jugador está en combate
        CombatListener combatListener = plugin.getCombatListener();
        Player killer = combatListener.getLastAttacker(player);

        if (killer != null) {
            // El jugador se desconectó en combate
            stats.addDeath();
            stats.resetStreak();
            PlayerStats killerStats = PlayerStats.getStats(killer.getUniqueId());
            killerStats.addKill();
            
            // Dar perla al killer
            givePearlToKiller(killer);

            // Mensaje de muerte por desconexión
            String messageName = plugin.getCosmeticManager().getPlayerKillMessage(killer.getUniqueId());
            String killMessage;
            if (messageName.equals("default")) {
                killMessage = KILL_MESSAGES.get(random.nextInt(KILL_MESSAGES.size()));
            } else {
                KillMessageItem messageItem = KillMessageItem.getByName(messageName);
                killMessage = messageItem != null ? messageItem.getMessage() : KILL_MESSAGES.get(0);
            }
            
            Bukkit.broadcastMessage(MessageUtils.getColor(
                killMessage.replace("%killer%", killer.getName())
                            .replace("%victim%", player.getName())
            ));

            // Reproducir sonido de kill al asesino
            String soundName = plugin.getCosmeticManager().getPlayerKillSound(killer.getUniqueId());
            if (!soundName.equals("none")) {
                KillSoundItem soundItem = KillSoundItem.getByName(soundName);
                if (soundItem != null) {
                    killer.playSound(
                        killer.getLocation(),
                        soundItem.getSound(),
                        soundItem.getVolume(),
                        soundItem.getPitch()
                    );
                }
            }
        }

        combatListener.resetCombat(player);

        stats.updatePlayTime();
        
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena != null) {
            plugin.getArenaManager().removePlayerFromArena(player, currentArena);
        }
        plugin.getScoreboardManager().removePlayer(player);

        String currentMusic = plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId());
        if (currentMusic != null && !currentMusic.equals("none")) {
            plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), "none");
            // Detener la música del jugador
            plugin.getMusicManager().stopMusicForPlayer(player);
        }

        plugin.getItemVerificationManager().removePlayer(player);
        event.setQuitMessage(null);
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
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (player.getGameMode() == GameMode.CREATIVE) return; // Permitir en modo creativo
        
        if (block.getType() == Material.JUKEBOX) {
            // Si es una jukebox de música, cancelar la ruptura
            for (Map.Entry<UUID, Location> entry : plugin.getMusicManager().getPlayerJukeboxes().entrySet()) {
                if (block.getLocation().equals(entry.getValue())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        
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

    private void handlePlayerDeath(Player player) {
        String soundName = plugin.getCosmeticManager().getPlayerDeathSound(player.getUniqueId());
        if (!soundName.equals("none")) {
            DeathSoundItem soundItem = DeathSoundItem.getByName(soundName);
            if (soundItem != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.playSound(
                            player.getLocation(), 
                            soundItem.getSound(), 
                            soundItem.getVolume(), 
                            soundItem.getPitch()
                        );
                        
                    }
                }.runTaskLater(plugin, 2L);
            }
        }

        // Eliminar solo las perlas que están en vuelo
        player.getWorld().getEntities().stream()
            .filter(entity -> entity.getType() == EntityType.ENDER_PEARL)
            .filter(entity -> ((EnderPearl) entity).getShooter() == player)
            .forEach(entity -> entity.remove());

        stopMusic(player);
    }

    private void handlePlayerKill(Player player) {
        String soundName = plugin.getCosmeticManager().getPlayerKillSound(player.getUniqueId());
        if (!soundName.equals("none")) {
            KillSoundItem soundItem = KillSoundItem.getByName(soundName);
            if (soundItem != null) {
                player.playSound(
                    player.getLocation(),
                    soundItem.getSound(),
                    soundItem.getVolume(),
                    soundItem.getPitch()
                );
            }
        }
    }

    private void stopMusic(Player player) {
        String currentMusic = plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId());
        if (currentMusic != null && !currentMusic.equals("none")) {
            plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), "none");
            // Detener la música del jugador
            plugin.getMusicManager().stopMusicForPlayer(player);
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
                    return;
                }

                CombatListener combatListener = plugin.getCombatListener();
                Player killer = combatListener.getLastAttacker(player);

                if (killer != null && killer != player) {
                    givePearlToKiller(killer);
                    playerStats.addDeath();
                    playerStats.resetStreak();
                    PlayerStats killerStats = PlayerStats.getStats(killer.getUniqueId());
                    killerStats.addKill();

                    // Broadcast kill message
                    String messageName = plugin.getCosmeticManager().getPlayerKillMessage(killer.getUniqueId());
                    String killMessage;
                    if (messageName.equals("default")) {
                        killMessage = KILL_MESSAGES.get(random.nextInt(KILL_MESSAGES.size()));
                    } else {
                        KillMessageItem messageItem = KillMessageItem.getByName(messageName);
                        killMessage = messageItem != null ? messageItem.getMessage() : KILL_MESSAGES.get(0);
                    }
                    Bukkit.broadcastMessage(MessageUtils.getColor(
                        killMessage.replace("%killer%", killer.getName())
                                    .replace("%victim%", player.getName())
                    ));
                    handlePlayerKill(killer);
                    handlePlayerDeath(player);

                    // Resetear el combate después de una muerte
                    plugin.getCombatListener().resetCombat(player);
                    stopMusic(player);
                } else {
                    playerStats.addDeath();
                    playerStats.resetStreak();

                    // Natural death message
                    String messageName = plugin.getCosmeticManager().getPlayerDeathMessage(player.getUniqueId());
                    String deathMessage;
                    if (messageName.equals("default")) {
                        deathMessage = DEATH_MESSAGES.get(random.nextInt(DEATH_MESSAGES.size()));
                    } else {
                        DeathMessageItem messageItem = DeathMessageItem.getByName(messageName);
                        deathMessage = messageItem != null ? messageItem.getMessage() : DEATH_MESSAGES.get(0);
                    }
                    Bukkit.broadcastMessage(MessageUtils.getColor(
                        String.format(deathMessage, player.getName())
                    ));
                    // Manejar el sonido de muerte del jugador
                    handlePlayerDeath(player);
                }

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
                    // Cerrar menú si tiene uno abierto
                    Menu openMenu = plugin.getMenuManager().getOpenMenu(player);
                    if (openMenu != null) {
                        player.closeInventory();
                        plugin.getMenuManager().closeMenu(player);
                    }
                    plugin.getArenaManager().setPlayerZone(player, foundArena, foundZone);

                    switch (foundZone) {
                        case "spawn":
                            ItemsManager.giveSpawnItems(player);
                            player.spigot().setCollidesWithEntities(false);
                            break;
                        case "pvp":
                            ItemsManager.givePvPItems(player);
                            player.spigot().setCollidesWithEntities(true);
                            break;
                        case "void":
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

        // Encontrar el slot configurado para la perla en el layout del jugador
        int pearlSlot = findConfiguredSlotByType(killer, Material.ENDER_PEARL);
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
    }

    // Método para encontrar el slot configurado de un item por tipo en el layout del jugador
    private int findConfiguredSlotByType(Player player, Material type) {
        ItemStack[] playerLayout = PlayerHotbar.getPlayerLayout(player.getUniqueId());
        
        for (int i = 0; i < Math.min(playerLayout.length, 9); i++) { // Solo buscar en la hotbar
            if (playerLayout[i] != null && playerLayout[i].getType() == type) {
                return i;
            }
        }
        
        // Si no encuentra el tipo específico en el layout, buscar en el inventario actual como fallback
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < 9; i++) {
            if (contents[i] != null && contents[i].getType() == type) {
                return i;
            }
        }
        
        return -1; // No se encontró el item configurado
    }

    private void respawnPlayerAtSpawn(Player player, Arena arena) {
        PlayerStats playerStats = PlayerStats.getStats(player.getUniqueId());
        
        // Si la arena está cambiando, asegurarse de que el jugador no pueda moverse
        if (plugin.getScoreboardManager().isArenaChanging()) {
            player.setWalkSpeed(0.0f);
        } else {
            player.setWalkSpeed(0.2f);
        }
        
        Location spawnLoc = arena.getSpawnLocation();
        if (spawnLoc != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(spawnLoc);
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                    ItemsManager.giveSpawnItems(player);
                    RankManager.updatePlayerRank(player, playerStats.getElo());
                    
                    player.setNoDamageTicks(40);
                    
                    // Mostrar el borde de la arena al respawn
                    plugin.getArenaManager().showArenaBorder(arena);
                    
                    // Actualizar la zona del jugador al respawn
                    plugin.getScoreboardManager().updatePlayerZone(player, arena.getName());
                    
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}