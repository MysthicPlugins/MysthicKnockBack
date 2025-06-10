package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.enchantments.Enchantment;

import mk.kvlzx.MysthicKnockBack;

public class CombatManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, Long> lastHitTime = new HashMap<>();
    private static final Map<UUID, Long> lastKnockbackTimes = new HashMap<>();

    // Constantes ajustadas para KB más pegado al suelo
    private static final double BASE_HORIZONTAL = 0.35;   // KB horizontal base
    private static final double BASE_VERTICAL = 0.08;     // KB vertical reducido (era 0.15)
    private static final double SPRINT_BONUS = 0.15;      // Bonus por esprintar
    private static final double SPRINT_BONUS_VERTICAL = 0.02; // Bonus vertical muy reducido (era 0.10)
    private static final double NO_KB_ITEM_REDUCTION = 0.60; // Reducción para PvP a mano
    private static final double AIR_COMBO_HORIZONTAL = 0.28; // KB horizontal en combos aéreos
    private static final double AIR_COMBO_VERTICAL = 0.02;   // KB vertical en combos aéreos reducido (era 0.12)
    private static final double KNOCKBACK_ENCHANT_H_REDUCTION = 0.20; // Reducción horizontal por KB
    private static final double KNOCKBACK_ENCHANT_V_REDUCTION = 0.90; // Reducción vertical mayor (era 0.80)
    private static final double ANTI_KB_MULTIPLIER = 0.4;  // Reducción para KB acumulativo
    private static final long COMBO_WINDOW = 500;          // Ventana de tiempo para combos (ms)
    private static final long CLEANUP_DELAY = 30 * 20L;    // 30 segundos en ticks
    private static final long DATA_EXPIRY = 60000L;       // 60 segundos en milisegundos

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
        
        if (lastHit != null && currentTime - lastHit < COMBO_WINDOW) {
            return false;
        }
        
        lastHitTime.put(attacker.getUniqueId(), currentTime);
        return true;
    }

    public void applyCustomKnockback(Player victim, Player attacker) {
        ItemStack weapon = attacker.getItemInHand();
        boolean isNoKbItem = weapon == null || weapon.getType() == Material.AIR || 
                            !weapon.containsEnchantment(Enchantment.KNOCKBACK);
        int kbLevel = (!isNoKbItem) ? weapon.getEnchantmentLevel(Enchantment.KNOCKBACK) : 0;

        boolean isOnGround = ((LivingEntity) victim).isOnGround() ||
                victim.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR;

        Vector dir;
        if (attacker == victim) {
            dir = victim.getLocation().getDirection().normalize().multiply(1.5);
        } else {
            dir = victim.getLocation().toVector()
                    .subtract(attacker.getLocation().toVector())
                    .setY(0)
                    .normalize();
        }

        double hMult = BASE_HORIZONTAL;
        double vMult = BASE_VERTICAL;

        // Ajustar según el contexto
        if (kbLevel > 0) {
            // Nuevo cálculo para palo con KB - más reducción vertical
            hMult *= KNOCKBACK_ENCHANT_H_REDUCTION;
            vMult *= KNOCKBACK_ENCHANT_V_REDUCTION;
        } else if (isNoKbItem) {
            hMult *= NO_KB_ITEM_REDUCTION;
            vMult *= NO_KB_ITEM_REDUCTION;
        }

        // Sprint bonus corregido
        if (attacker.isSprinting()) {
            hMult += SPRINT_BONUS; // Cambio de *= a += para mejor control
            vMult += SPRINT_BONUS_VERTICAL; // Solo suma el bonus vertical pequeño
        }

        Long lastKnockbackTime = lastKnockbackTimes.get(victim.getUniqueId());
        long currentTime = System.currentTimeMillis();
        boolean isCombo = lastKnockbackTime != null && (currentTime - lastKnockbackTime) < COMBO_WINDOW;

        if (isCombo && !isOnGround) {
            hMult = AIR_COMBO_HORIZONTAL;
            vMult = AIR_COMBO_VERTICAL;
        } else if (isCombo) {
            hMult *= ANTI_KB_MULTIPLIER;
            vMult *= ANTI_KB_MULTIPLIER;
        }

        lastKnockbackTimes.put(victim.getUniqueId(), currentTime);

        if (isOnGround || isCombo) {
            Vector kb = new Vector(dir.getX() * hMult, vMult, dir.getZ() * hMult);
            victim.setVelocity(kb);
        } else {
            // KB reducido para golpes en el aire (no combo) - aún más pegado al suelo
            Vector kb = new Vector(dir.getX() * hMult * 0.6, vMult * 0.2, dir.getZ() * hMult * 0.6);
            victim.setVelocity(kb);
        }
    }

    public void cleanup() {
        lastHitTime.clear();
        lastKnockbackTimes.clear();
    }
}
