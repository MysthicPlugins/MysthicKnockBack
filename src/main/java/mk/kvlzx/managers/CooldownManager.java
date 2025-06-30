package mk.kvlzx.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.ZoneType;

import java.util.*;

public class CooldownManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, List<BukkitRunnable>> cooldownTasks = new HashMap<>();
    // Nuevo mapa para rastrear tareas por tipo de cooldown
    private final Map<UUID, Map<String, BukkitRunnable>> activeCooldownTasks = new HashMap<>();

    public CooldownManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(Player player, String type) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        return playerCooldowns != null && playerCooldowns.getOrDefault(type, 0L) > System.currentTimeMillis();
    }

    public void setCooldown(Player player, String type, int seconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(type, System.currentTimeMillis() + seconds * 1000);
    }

    public void startCooldownVisual(Player player, ItemStack original, int slot, int seconds, String cooldownType) {
        if (original == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Cancelar tarea anterior del mismo tipo si existe
        Map<String, BukkitRunnable> playerActiveTasks = activeCooldownTasks.get(playerId);
        if (playerActiveTasks != null && playerActiveTasks.containsKey(cooldownType)) {
            BukkitRunnable oldTask = playerActiveTasks.get(cooldownType);
            if (oldTask != null) {
                oldTask.cancel();
                playerActiveTasks.remove(cooldownType);
            }
        }
        
        ItemStack cooldownItem = original.clone();
        cooldownItem.setAmount(seconds);
        player.getInventory().setItem(slot, cooldownItem);

        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = seconds;
            
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cleanup();
                    return;
                }

                String zone = plugin.getArenaManager().getPlayerZone(player);
                if (zone == null || !zone.equals(ZoneType.PVP.getId())) {
                    clearCooldown(player, cooldownType);
                    player.getInventory().setItem(slot, null);
                    cleanup();
                    return;
                }

                if (plugin.getScoreboardManager().isArenaChanging()) {
                    clearAllCooldowns(player);
                    cleanup();
                    return;
                }

                if (timeLeft <= 0) {
                    ItemStack restoredItem = original.clone();
                    restoredItem.setAmount(1);
                    player.getInventory().setItem(slot, restoredItem);
                    cleanup();
                    return;
                }
                
                cooldownItem.setAmount(timeLeft);
                player.getInventory().setItem(slot, cooldownItem);
                timeLeft--;
            }
            
            private void cleanup() {
                cancel();
                // Remover de activeCooldownTasks
                Map<String, BukkitRunnable> playerTasks = activeCooldownTasks.get(playerId);
                if (playerTasks != null) {
                    playerTasks.remove(cooldownType);
                    if (playerTasks.isEmpty()) {
                        activeCooldownTasks.remove(playerId);
                    }
                }
                // Remover de cooldownTasks
                List<BukkitRunnable> tasks = cooldownTasks.get(playerId);
                if (tasks != null) {
                    tasks.remove(this);
                    if (tasks.isEmpty()) {
                        cooldownTasks.remove(playerId);
                    }
                }
            }
        };

        task.runTaskTimer(plugin, 0, 20);
        
        // Registrar la tarea en ambos mapas
        cooldownTasks.computeIfAbsent(playerId, k -> new ArrayList<>()).add(task);
        activeCooldownTasks.computeIfAbsent(playerId, k -> new HashMap<>()).put(cooldownType, task);
    }

    public void clearCooldown(Player player, String type) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns != null) {
            playerCooldowns.remove(type);
        }
        
        // También cancelar la tarea visual activa de ese tipo
        UUID playerId = player.getUniqueId();
        Map<String, BukkitRunnable> playerActiveTasks = activeCooldownTasks.get(playerId);
        if (playerActiveTasks != null && playerActiveTasks.containsKey(type)) {
            BukkitRunnable task = playerActiveTasks.remove(type);
            if (task != null) {
                task.cancel();
            }
            if (playerActiveTasks.isEmpty()) {
                activeCooldownTasks.remove(playerId);
            }
        }
    }

    public void clearAllCooldowns(Player player) {
        UUID playerId = player.getUniqueId();
        
        cooldowns.remove(playerId);
        
        // Cancelar todas las tareas activas
        Map<String, BukkitRunnable> playerActiveTasks = activeCooldownTasks.remove(playerId);
        if (playerActiveTasks != null) {
            playerActiveTasks.values().forEach(BukkitRunnable::cancel);
        }
        
        List<BukkitRunnable> tasks = cooldownTasks.remove(playerId);
        if (tasks != null) {
            tasks.forEach(BukkitRunnable::cancel);
        }
    }
}
