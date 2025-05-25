package kk.kvlzx.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;

import java.util.*;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.arena.ZoneType;
import kk.kvlzx.items.CustomItem;
import kk.kvlzx.items.CustomItem.ItemType;

public class ItemListener implements Listener {
    private static final int COOLDOWN_SECONDS = 10;
    private static final String COOLDOWN_BOW = "BOW";
    private static final String COOLDOWN_FEATHER = "FEATHER";
    private static final String COOLDOWN_PLATE = "PLATE";

    private final KvKnockback plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, ItemStack> savedArrows = new HashMap<>();
    private final Map<Location, BukkitRunnable> plateTimers = new HashMap<>();
    private final Map<UUID, List<BukkitRunnable>> cooldownTasks = new HashMap<>();
    private final Map<UUID, BukkitRunnable> speedTasks = new HashMap<>(); // Nuevo mapa para las tasks de speed

    public ItemListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    // Método para verificar si un jugador está en cooldown para un tipo de ítem
    private boolean isOnCooldown(Player player, String type) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        return playerCooldowns != null && playerCooldowns.getOrDefault(type, 0L) > System.currentTimeMillis();
    }

    // Método para establecer un cooldown para un tipo de ítem
    private void setCooldown(Player player, String type, int seconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                 .put(type, System.currentTimeMillis() + seconds * 1000);
    }

    // Método para encontrar un ítem por tipo en el inventario
    private ItemStack findItemByType(Player player, Material type) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == type) {
                return item;
            }
        }
        return null;
    }

    // Método para encontrar el slot de un ítem por tipo
    private int findSlotByType(Player player, Material type) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() == type) {
                return i;
            }
        }
        return -1;
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Verificar si la arena está cambiando
        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }

        if (isOnCooldown(player, COOLDOWN_BOW)) {
            event.setCancelled(true);
            return;
        }

        UUID uuid = player.getUniqueId();

        ItemStack arrow = findItemByType(player, Material.ARROW);
        int arrowSlot = findSlotByType(player, Material.ARROW);

        if (arrow != null) {
            savedArrows.put(uuid, arrow.clone());
            player.getInventory().setItem(arrowSlot, null);
            player.updateInventory();
        }

        ItemStack bow = findItemByType(player, Material.BOW);
        int bowSlot = findSlotByType(player, Material.BOW);

        if (bow != null) {
            bow.setAmount(1);
            player.getInventory().setItem(bowSlot, bow);
            player.updateInventory();
            setCooldown(player, COOLDOWN_BOW, COOLDOWN_SECONDS);
            startCooldownVisual(player, bow, bowSlot, COOLDOWN_SECONDS);
            restoreArrowAndBowLater(player, arrowSlot, bowSlot);
        }
    }

    @EventHandler
    public void onFeatherUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Verificar si la arena está cambiando
        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }

        ItemStack feather = findItemByType(player, Material.FEATHER);
        int featherSlot = findSlotByType(player, Material.FEATHER);

        if (event.getItem() != null && event.getItem().getType() == Material.FEATHER &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            if (isOnCooldown(player, COOLDOWN_FEATHER)) return;

            if (feather != null) {
                // Cancelar task anterior si existe
                if (speedTasks.containsKey(player.getUniqueId())) {
                    speedTasks.get(player.getUniqueId()).cancel();
                }

                player.setWalkSpeed(0.4f);
                feather.setAmount(1);
                player.getInventory().setItem(featherSlot, feather);
                player.updateInventory();
                setCooldown(player, COOLDOWN_FEATHER, COOLDOWN_SECONDS);
                startCooldownVisual(player, feather, featherSlot, COOLDOWN_SECONDS);

                // Nueva task para resetear velocidad
                BukkitRunnable speedTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        resetPlayerSpeed(player.getUniqueId());
                        speedTasks.remove(player.getUniqueId());
                    }
                };
                speedTask.runTaskLater(plugin, COOLDOWN_SECONDS * 20L);
                speedTasks.put(player.getUniqueId(), speedTask);
            }
        }
    }

    @EventHandler
    public void onStepPlate(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;
        
        // Verificar si la arena está cambiando
        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block == null) return;

        if (block.getType() == Material.GOLD_PLATE && plateTimers.containsKey(block.getLocation())) {

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Vector direction = player.getLocation().getDirection().setY(0).normalize().multiply(0.6);
                Vector velocity = new Vector(direction.getX(), 1.2, direction.getZ());
                player.setVelocity(velocity);
            }, 1L);

            player.playSound(player.getLocation(), Sound.FIREWORK_TWINKLE2, 1.0f, 1.0f);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                block.setType(Material.AIR);
            }, COOLDOWN_SECONDS * 20L);

            plateTimers.get(block.getLocation()).cancel();
            plateTimers.remove(block.getLocation());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Verificar si la arena está cambiando
        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }

        if (blockType.isBlock()) {
            int itemSlot = findSlotByType(player, blockType);
            ItemStack stack = player.getInventory().getItem(itemSlot);

            if (stack != null) {
                if (blockType == Material.GOLD_PLATE) {
                    if (isOnCooldown(player, COOLDOWN_PLATE)) {
                        event.setCancelled(true);
                        return;
                    }
                    stack.setAmount(1);
                    setCooldown(player, COOLDOWN_PLATE, COOLDOWN_SECONDS);
                    startCooldownVisual(player, stack, itemSlot, COOLDOWN_SECONDS);
                    startPlateTimer(block.getLocation());
                } else {
                    stack.setAmount(64); // Siempre mantener en 64 los bloques
                }
                player.getInventory().setItem(itemSlot, stack);
                player.updateInventory();
            }
            // Programar la eliminación del bloque después de 5 segundos (20 ticks = 1 segundo)
            Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), 100L);
        }
    }

    @EventHandler
    public void onPearlLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) return;
        EnderPearl pearl = (EnderPearl) event.getEntity();
        if (!(pearl.getShooter() instanceof Player)) return;

        // Verificar si la arena está cambiando
        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }
    }

    private void startPlateTimer(Location location) {
        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (location.getBlock().getType() == Material.GOLD_PLATE) {
                    location.getBlock().setType(Material.AIR);
                }
                plateTimers.remove(location);
            }
        };
        timer.runTaskLater(plugin, 20L * 10);
        plateTimers.put(location, timer);
    }

    private void restoreArrowAndBowLater(Player player, int arrowSlot, int bowSlot) {
        UUID uuid = player.getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                if (savedArrows.containsKey(uuid)) {
                    ItemStack arrow = savedArrows.remove(uuid);
                    if (player.getInventory().getItem(arrowSlot) == null) {
                        player.getInventory().setItem(arrowSlot, arrow);
                    }
                    player.updateInventory();
                }

                ItemStack restoredBow = CustomItem.create(ItemType.BOW);
                restoredBow.setDurability((short) 0);
                restoredBow.setAmount(1);
                player.getInventory().setItem(bowSlot, restoredBow);
                player.updateInventory();
            }
        }.runTaskLater(plugin, COOLDOWN_SECONDS * 20L);
    }

    // Método para iniciar el cooldown visual
    private void startCooldownVisual(Player player, ItemStack original, int slot, int seconds) {
        if (original == null) return;
        ItemStack cooldownItem = original.clone();
        cooldownItem.setAmount(seconds);
        player.getInventory().setItem(slot, cooldownItem);
        player.updateInventory();

        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = seconds;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                // Verificar si el jugador está muerto
                if (player.isDead()) {
                    cancel();
                    return;
                }

                // Verificar si el jugador está en spawn o no está en pvp
                String zone = plugin.getArenaManager().getPlayerZone(player);
                if (zone == null || !zone.equals(ZoneType.PVP.getId())) {
                    // Restaurar item inmediatamente y cancelar cooldown
                    ItemStack restoredItem = original.clone();
                    restoredItem.setAmount(0);
                    player.getInventory().setItem(slot, restoredItem);
                    player.updateInventory();
                    // Limpiar cooldowns
                    cooldowns.get(player.getUniqueId()).clear();
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    ItemStack restoredItem = original.clone();
                    restoredItem.setAmount(1);
                    player.getInventory().setItem(slot, restoredItem);
                    player.updateInventory();
                    cancel();
                    return;
                }
                cooldownItem.setAmount(timeLeft);
                player.getInventory().setItem(slot, cooldownItem);
                player.updateInventory();
                timeLeft--;
            }
        };

        task.runTaskTimer(plugin, 0, 20);
        cooldownTasks.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(task);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Resetear velocidad si muere
        if (speedTasks.containsKey(player.getUniqueId())) {
            speedTasks.get(player.getUniqueId()).cancel();
            speedTasks.remove(player.getUniqueId());
            resetPlayerSpeed(player.getUniqueId());
        }

        // Eliminar perlas lanzadas
        player.getWorld().getEntities().stream()
            .filter(entity -> entity.getType() == EntityType.ENDER_PEARL)
            .filter(entity -> ((EnderPearl) entity).getShooter() == player)
            .forEach(entity -> entity.remove());
    }

    // Método para resetear la velocidad del jugador
    private void resetPlayerSpeed(UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().setWalkSpeed(0.2f);
        }
    }
}
