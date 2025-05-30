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
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;

import java.util.*;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.items.CustomItem;
import kk.kvlzx.items.CustomItem.ItemType;
import kk.kvlzx.utils.BlockUtils;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;

public class ItemListener implements Listener {
    private static final int COOLDOWN_SECONDS = 10;
    private static final String COOLDOWN_BOW = "BOW";
    private static final String COOLDOWN_FEATHER = "FEATHER";
    private static final String COOLDOWN_PLATE = "PLATE";

    private final KvKnockback plugin;
    private final Map<UUID, ItemStack> savedArrows = new HashMap<>();
    private final Map<Location, BukkitRunnable> plateTimers = new HashMap<>();
    private final Map<UUID, BukkitRunnable> speedTasks = new HashMap<>(); // Nuevo mapa para las tasks de speed
    private static final Set<Location> placedBlocks = new HashSet<>(); // Set para almacenar bloques colocados
    private final Map<Location, List<BukkitRunnable>> blockAnimationTasks = new HashMap<>();

    public ItemListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getCooldownManager().isOnCooldown(player, COOLDOWN_BOW)) {
            event.setCancelled(true);
            return;
        }

        UUID uuid = player.getUniqueId();
        ItemStack arrow = findItemByType(player, Material.ARROW);
        int arrowSlot = findSlotByType(player, Material.ARROW);

        if (arrow != null) {
            savedArrows.put(uuid, arrow.clone());
            player.getInventory().setItem(arrowSlot, null);
        }

        ItemStack bow = findItemByType(player, Material.BOW);
        int bowSlot = findSlotByType(player, Material.BOW);

        if (bow != null) {
            bow.setAmount(1);
            player.getInventory().setItem(bowSlot, bow);
            plugin.getCooldownManager().setCooldown(player, COOLDOWN_BOW, COOLDOWN_SECONDS);
            plugin.getCooldownManager().startCooldownVisual(player, bow, bowSlot, COOLDOWN_SECONDS, COOLDOWN_BOW);
            restoreArrowAndBowLater(player, arrowSlot, bowSlot);
        }
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
    public void onFeatherUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }

        ItemStack feather = findItemByType(player, Material.FEATHER);
        int featherSlot = findSlotByType(player, Material.FEATHER);

        if (event.getItem() != null && event.getItem().getType() == Material.FEATHER &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            if (plugin.getCooldownManager().isOnCooldown(player, COOLDOWN_FEATHER)) return;

            if (feather != null) {
                if (speedTasks.containsKey(player.getUniqueId())) {
                    speedTasks.get(player.getUniqueId()).cancel();
                }

                player.setWalkSpeed(0.4f);
                feather.setAmount(1);
                player.getInventory().setItem(featherSlot, feather);
                plugin.getCooldownManager().setCooldown(player, COOLDOWN_FEATHER, COOLDOWN_SECONDS);
                plugin.getCooldownManager().startCooldownVisual(player, feather, featherSlot, COOLDOWN_SECONDS, COOLDOWN_FEATHER);

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
        
        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (block == null || block.getType() != Material.GOLD_PLATE) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Vector direction = player.getLocation().getDirection().setY(0).normalize().multiply(0.6);
            Vector velocity = new Vector(direction.getX(), 1.2, direction.getZ());
            player.setVelocity(velocity);
            player.playSound(player.getLocation(), Sound.FIREWORK_TWINKLE2, 1.0f, 1.0f);
        }, 1L);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (player.getGameMode() == GameMode.CREATIVE) return; // No hacer nada si está creativo 

        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }

        String zone = plugin.getArenaManager().getPlayerZone(player);

        // Verificar si el jugador está en zona PvP
        if (zone == null || !zone.equals("pvp")) {
            event.setCancelled(true);
            return;
        }

        // Cancelar cualquier animación anterior en esa ubicación
        cancelBlockAnimation(block.getLocation());

        if (blockType.isBlock()) {
            int itemSlot = findSlotByType(player, blockType);
            ItemStack stack = player.getInventory().getItem(itemSlot);

            if (stack != null) {
                if (blockType == Material.GOLD_PLATE) {
                    if (plugin.getCooldownManager().isOnCooldown(player, COOLDOWN_PLATE)) {
                        event.setCancelled(true);
                        return;
                    }
                    plugin.getCooldownManager().setCooldown(player, COOLDOWN_PLATE, COOLDOWN_SECONDS);
                    plugin.getCooldownManager().startCooldownVisual(player, stack, itemSlot, COOLDOWN_SECONDS, COOLDOWN_PLATE);
                    startPlateTimer(block.getLocation());
                } else {
                    // Solo aplicar animación y añadir a placedBlocks si NO es una placa
                    stack.setAmount(64);
                    placedBlocks.add(block.getLocation());
                    startBlockBreakAnimation(block);
                }
                player.getInventory().setItem(itemSlot, stack);
            }
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
                placedBlocks.remove(location);
            }
        };
        timer.runTaskLater(plugin, 20L * 10); // 10 segundos
        plateTimers.put(location, timer);
    }

    private void restoreArrowAndBowLater(Player player, int arrowSlot, int bowSlot) {
        UUID uuid = player.getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                String zone = plugin.getArenaManager().getPlayerZone(player);
                if (zone != null && zone.equals("spawn")) return;

                if (savedArrows.containsKey(uuid)) {
                    ItemStack arrow = savedArrows.remove(uuid);
                    if (player.getInventory().getItem(arrowSlot) == null) {
                        player.getInventory().setItem(arrowSlot, arrow);
                    }
                }

                ItemStack restoredBow = CustomItem.create(ItemType.BOW);
                player.getInventory().setItem(bowSlot, restoredBow);
            }
        }.runTaskLater(plugin, COOLDOWN_SECONDS * 20L);
    }

    // Método para resetear la velocidad del jugador
    private void resetPlayerSpeed(UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline() && offlinePlayer.getPlayer() != null) {
            offlinePlayer.getPlayer().setWalkSpeed(0.2f);
        }
    }

    private void cancelBlockAnimation(Location location) {
        List<BukkitRunnable> tasks = blockAnimationTasks.remove(location);
        if (tasks != null) {
            tasks.forEach(BukkitRunnable::cancel);
            
            // Limpiar la animación enviando stage 0
            Block block = location.getBlock();
            for (Player player : block.getWorld().getPlayers()) {
                if (player.getLocation().distance(location) <= 32) {
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                        new PacketPlayOutBlockBreakAnimation(
                            block.hashCode(),
                            new BlockPosition(block.getX(), block.getY(), block.getZ()),
                            -1
                        )
                    );
                }
            }
        }
    }

    private void startBlockBreakAnimation(Block block) {
        List<BukkitRunnable> tasks = new ArrayList<>();
        blockAnimationTasks.put(block.getLocation(), tasks);

        final int TOTAL_TIME = 100; // 5 segundos
        final int STAGES = 10;
        final int DELAY_PER_STAGE = TOTAL_TIME / STAGES;

        // Crear una animación con intervalos uniformes
        for (int i = 0; i < STAGES; i++) {
            final byte stage = (byte)i;
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (block.getType() == Material.AIR) {
                        cancel();
                        return;
                    }

                    for (Player player : block.getWorld().getPlayers()) {
                        if (player.getLocation().distance(block.getLocation()) <= 32) {
                            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                                new PacketPlayOutBlockBreakAnimation(
                                    block.hashCode(),
                                    new BlockPosition(block.getX(), block.getY(), block.getZ()),
                                    stage
                                )
                            );
                        }
                    }
                }
            };
            task.runTaskLater(plugin, i * DELAY_PER_STAGE);
            tasks.add(task);
        }

        // Tarea final para romper el bloque
        BukkitRunnable breakTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType() != Material.AIR) {
                    block.setType(Material.AIR);
                    placedBlocks.remove(block.getLocation());
                    block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
                }
                blockAnimationTasks.remove(block.getLocation());
            }
        };
        breakTask.runTaskLater(plugin, TOTAL_TIME);
        tasks.add(breakTask);
    }

    public static void cleanup() {
        // Eliminar todos los bloques colocados
        for (Location loc : placedBlocks) {
            Block block = loc.getBlock();
            if (BlockUtils.isDecorativeBlock(block.getType())) {
                block.setType(Material.AIR);
            }
        }
        placedBlocks.clear();
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
}
