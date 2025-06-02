package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.enchantments.Enchantment;

import mk.kvlzx.MysthicKnockBack;

public class CombatManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, Long> lastHitTime = new HashMap<>();
    private final Map<UUID, Long> lastKnockbackTimes = new HashMap<>();
    private static final long HIT_DELAY = 500; // 0.5 segundos en milisegundos
    private static final long CLEANUP_DELAY = 30 * 20L; // 30 segundos en ticks
    private static final long DATA_EXPIRY = 60000L; // 60 segundos en milisegundos

    public CombatManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::cleanupOldData, CLEANUP_DELAY, CLEANUP_DELAY);
    }

    private void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        
        // Limpiar lastHitTime
        lastHitTime.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > DATA_EXPIRY);
        
        // Limpiar lastKnockbackTimes
        lastKnockbackTimes.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > DATA_EXPIRY);
    }

    public boolean canHit(Player attacker) {
        long currentTime = System.currentTimeMillis();
        Long lastHit = lastHitTime.get(attacker.getUniqueId());
        
        if (lastHit != null && currentTime - lastHit < HIT_DELAY) {
            return false;
        }
        
        lastHitTime.put(attacker.getUniqueId(), currentTime);
        return true;
    }

    public void applyCustomKnockback(Player victim, Player attacker) {
        // Reducir valores base de KB
        double baseH = 0.35;
        double baseV = 0.35;
        final double sprintBonus = 0.15;

        // Si el item tiene Empuje, reducimos ambos
        ItemStack weapon = attacker.getItemInHand();
        int kbLevel = (weapon != null && weapon.getType() != Material.AIR && 
                weapon.containsEnchantment(Enchantment.KNOCKBACK)) 
                ? weapon.getEnchantmentLevel(Enchantment.KNOCKBACK)
                : 0;
        if (kbLevel > 0) {
            baseH *= 0.5;
            baseV *= 0.5;
        }

        // Dirección normalizada
        Vector dir = victim.getLocation().toVector()
                        .subtract(attacker.getLocation().toVector())
                        .setY(0)
                        .normalize();

        double hMult = baseH + (attacker.isSprinting() ? sprintBonus : 0);
        double vMult = baseV;

        // Sistema anti-KB acumulativo
        Long lastKnockbackTime = lastKnockbackTimes.get(victim.getUniqueId());
        long currentTime = System.currentTimeMillis();
        
        if (lastKnockbackTime != null && currentTime - lastKnockbackTime < 500) {
            hMult *= 0.7;
            vMult *= 0.5;
        }
        lastKnockbackTimes.put(victim.getUniqueId(), currentTime);

        // Clamp vertical
        vMult = Math.min(vMult, 0.4);

        // Solo aplicar KB si el jugador está en el suelo o cerca de él
        if (victim.isOnGround() || victim.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR) {
            Vector kb = new Vector(dir.getX() * hMult, vMult, dir.getZ() * hMult);
            victim.setVelocity(kb);
        } else {
            Vector kb = new Vector(dir.getX() * hMult * 0.6, vMult * 0.4, dir.getZ() * hMult * 0.6);
            victim.setVelocity(kb);
        }
    }

    public void cleanup() {
        lastHitTime.clear();
        lastKnockbackTimes.clear();
    }
}
