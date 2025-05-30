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
        ItemStack cooldownItem = original.clone();
        cooldownItem.setAmount(seconds);
        player.getInventory().setItem(slot, cooldownItem);

        BukkitRunnable task = new BukkitRunnable() {
            int timeLeft = seconds;
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                String zone = plugin.getArenaManager().getPlayerZone(player);
                if (zone == null || !zone.equals(ZoneType.PVP.getId())) {
                    clearCooldown(player, cooldownType);
                    ItemStack restoredItem = original.clone();
                    restoredItem.setAmount(1);
                    player.getInventory().setItem(slot, restoredItem);
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    ItemStack restoredItem = original.clone();
                    restoredItem.setAmount(1);
                    player.getInventory().setItem(slot, restoredItem);
                    cancel();
                    return;
                }
                
                cooldownItem.setAmount(timeLeft);
                player.getInventory().setItem(slot, cooldownItem);
                timeLeft--;
            }
        };

        task.runTaskTimer(plugin, 0, 20);
        cooldownTasks.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(task);
    }

    public void clearCooldown(Player player, String type) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns != null) {
            playerCooldowns.remove(type);
        }
    }

    public void clearAllCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
        List<BukkitRunnable> tasks = cooldownTasks.remove(player.getUniqueId());
        if (tasks != null) {
            tasks.forEach(BukkitRunnable::cancel);
        }
    }
}
