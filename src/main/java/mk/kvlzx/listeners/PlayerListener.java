package mk.kvlzx.listeners;

import org.bukkit.entity.Player;
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
import mk.kvlzx.cosmetics.KillMessageItem;
import mk.kvlzx.stats.PlayerStats;

public class PlayerListener implements Listener {
    private final MysthicKnockBack plugin;
    private final Random random = new Random();

    private static final List<String> DEATH_MESSAGES = Arrays.asList(
        "&b%s &fslipped on a banana peel. &aHow clumsy!",
        "&b%s &finvented trying to fly, but forgot their wings.",
        "&b%s &ffell into the void shouting &a'I'll be back!'",
        "&b%s &fwas tricked by a mirage and crashed.",
        "&b%s &fwanted to dance on the edge and... &aouch! &fTo the ground.",
        "&b%s &fthought they were immortal. &aSpoiler: &ethey weren't.",
        "&b%s &ftripped over their own ego.",
        "&b%s &fwas defeated by gravity, their worst enemy.",
        "&b%s &finvented an epic trick and ended up on the ground.",
        "&b%s &fgot distracted watching a kitten and &a goodbye!",
        "&b%s &fbelieved they could run faster than the wind. &aNop.",
        "&b%s &ffell, just like their Wi-Fi in the middle of a match.",
        "&b%s &fwanted to be a hero, but physics said &a'nope'.",
        "&b%s &fjumped into the void with &atoo much &fconfidence.",
        "&b%s &fwas betrayed by their own coordination.",
        "&b%s &fthought the ground was lava... and wasn't entirely wrong.",
        "&b%s &finvented a somersault and stayed in mortal.",
        "&b%s &ffell for the lies of the invisible platform.",
        "&b%s &fwanted to impress and only impressed the ground.",
        "&b%s &fshouted &a'I'm invincible!' &fjust before falling."
    );

    private static final List<String> KILL_MESSAGES = Arrays.asList(
        "&b{killer} &fhas sent &b{victim} &fon a one-way trip to the void!",
        "&b{victim} &ftried to fly, but &b{killer} &fcut their wings.",
        "&b{killer} &fgave &b{victim} &fan epic push to the beyond!",
        "&b{victim} &fthought they could, but &b{killer} &fsaid '&aNOPE, to the ground!'",
        "&b{killer} &fturned &b{victim} &finto a shooting star... that didn't go far!",
        "&b{victim} &fwanted to dance with &b{killer}&f, but ended up dancing with death.",
        "&b{killer} &ftaught &b{victim} &fthat gravity doesn't forgive!",
        "&b{victim} &fdreamed of victory, but &b{killer} &fwoke them with a blow.",
        "&b{killer} &fsent &b{victim} &fto explore the bottom of the map!",
        "&b{killer} &fgave &b{victim} &fan express ticket to the lobby of the fallen!"
    );

    private static final Material[] ALLOWED_SPAWN_ITEMS = {
        Material.SKULL_ITEM
    };

    public PlayerListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemsManager.giveSpawnItems(player);
        TitleUtils.sendTitle(
            player, 
            "&a Welcome to &6KnockbackFFA&a!", 
            "&fDominate the arena and leave your mark!.",
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
                        killMessage.replace("{killer}", killer.getName())
                                    .replace("{victim}", player.getName())
                    ));
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
                    plugin.getArenaManager().setPlayerZone(player, foundArena, foundZone);
                    
                    switch (foundZone) {
                        case "spawn":
                            player.sendMessage(MessageUtils.getColor("&aYou have entered the Spawn zone of the arena! " + foundArena));
                            ItemsManager.giveSpawnItems(player);
                            player.spigot().setCollidesWithEntities(false);
                            checkAndRemoveIllegalItems(player); // Añadir esta línea
                            break;
                        case "pvp":
                            player.sendMessage(MessageUtils.getColor("&cYou have entered the PvP zone of the arena! " + foundArena));
                            ItemsManager.givePvPItems(player);
                            player.spigot().setCollidesWithEntities(true); // Activar colisiones en pvp
                            break;
                        case "void":
                            player.sendMessage(MessageUtils.getColor("&7You have entered the Void zone of the arena! " + foundArena));
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
                    player.teleport(spawnLoc);
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                    ItemsManager.giveSpawnItems(player);
                    RankManager.updatePlayerRank(player, playerStats.getElo());
                    
                    player.setNoDamageTicks(40);
                    
                    // Actualizar la zona del jugador
                    plugin.getArenaManager().setPlayerZone(player, arena.getName(), "spawn");
                    
                    // Mostrar el borde de la arena al respawn
                    plugin.getArenaManager().showArenaBorder(arena);

                    // Si la arena está cambiando, asegurarse de que el jugador no pueda moverse
                    if (plugin.getScoreboardManager().isArenaChanging()) {
                        player.setWalkSpeed(0.0f);
                    }
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
        
        // Si la arena está cambiando, asegurarse de que el jugador no pueda moverse
        if (plugin.getScoreboardManager().isArenaChanging()) {
            player.setWalkSpeed(0.0f);
        }
    }

    // Añadir este nuevo método
    private void checkAndRemoveIllegalItems(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String currentZone = plugin.getArenaManager().getPlayerZone(player);
                // Cancelar el task si el jugador ya no está en spawn o está offline
                if (!player.isOnline() || currentZone == null || !currentZone.equals("spawn")) {
                    this.cancel();
                    return;
                }

                // Verificar cada slot del inventario
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && !isAllowedInSpawn(item.getType())) {
                        player.getInventory().setItem(i, null);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // Verificar cada 5 ticks (0.25 segundos)
    }

    private boolean isAllowedInSpawn(Material material) {
        for (Material allowed : ALLOWED_SPAWN_ITEMS) {
            if (material == allowed) {
                return true;
            }
        }
        return false;
    }
}