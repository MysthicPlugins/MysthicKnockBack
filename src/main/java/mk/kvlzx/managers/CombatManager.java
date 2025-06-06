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

    // Constantes configurables para diferentes situaciones
    private static final double BASE_HORIZONTAL = 0.35;   // KB horizontal base
    private static final double BASE_VERTICAL = 0.15;     // KB vertical base (no cambiar)
    private static final double SPRINT_BONUS = 0.15;      // Bonus por esprintar
    private static final double NO_KB_ITEM_REDUCTION = 0.6; // Reducción para PvP a mano o ítems sin KB
    private static final double AIR_COMBO_HORIZONTAL = 0.25; // KB horizontal en combos aéreos
    private static final double AIR_COMBO_VERTICAL = 0.15;   // KB vertical en combos aéreos (ajustado)
    private static final double KNOCKBACK_ENCHANT_H_REDUCTION = 0.2; // Reducción horizontal por encantamiento KB
    private static final double KNOCKBACK_ENCHANT_V_REDUCTION = 0.15; // Ajustado al base vertical
    private static final double ANTI_KB_MULTIPLIER = 0.5;  // Reducción para KB acumulativo
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
        // Obtener el arma del atacante
        ItemStack weapon = attacker.getItemInHand();
        boolean isNoKbItem = weapon == null || weapon.getType() == Material.AIR || 
                                !weapon.containsEnchantment(Enchantment.KNOCKBACK);
        int kbLevel = (!isNoKbItem) ? weapon.getEnchantmentLevel(Enchantment.KNOCKBACK) : 0;

        // Determinar si el jugador está en el suelo
        boolean isOnGround = ((LivingEntity) victim).isOnGround() ||
                victim.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR;

        // Calcular dirección del KB
        Vector dir;
        if (attacker == victim) {
            // Si el atacante es el mismo jugador, usar la dirección a la que mira
            dir = victim.getLocation().getDirection().normalize().multiply(1.5);
        } else {
            // Dirección desde el atacante hacia la víctima
            dir = victim.getLocation().toVector()
                    .subtract(attacker.getLocation().toVector())
                    .setY(0)
                    .normalize();
        }

        // Inicializar valores base de KB
        double hMult = BASE_HORIZONTAL;
        double vMult = BASE_VERTICAL;

        // Ajustar según el contexto
        if (kbLevel > 0) {
            // Reducir KB si hay encantamiento de Empuje
            hMult *= KNOCKBACK_ENCHANT_H_REDUCTION;
            vMult *= KNOCKBACK_ENCHANT_V_REDUCTION;
        } else if (isNoKbItem) {
            // Reducir KB para ítems sin encantamiento de KB (incluye PvP a mano)
            hMult *= NO_KB_ITEM_REDUCTION;
            vMult *= NO_KB_ITEM_REDUCTION;
        }

        if (attacker.isSprinting()) {
            // Bonus por esprintar
            hMult += SPRINT_BONUS;
        }

        // Detectar combos aéreos
        Long lastKnockbackTime = lastKnockbackTimes.get(victim.getUniqueId());
        long currentTime = System.currentTimeMillis();
        boolean isCombo = lastKnockbackTime != null && (currentTime - lastKnockbackTime) < COMBO_WINDOW;

        if (isCombo && !isOnGround) {
            // Ajustar KB para combos aéreos
            hMult = AIR_COMBO_HORIZONTAL;
            vMult = AIR_COMBO_VERTICAL;
        } else if (isCombo) {
            // Reducir KB para combos en el suelo (evitar acumulación excesiva)
            hMult *= ANTI_KB_MULTIPLIER;
            vMult *= ANTI_KB_MULTIPLIER;
        }

        // Actualizar el tiempo del último KB
        lastKnockbackTimes.put(victim.getUniqueId(), currentTime);

        // Aplicar KB solo si el jugador está en el suelo o en un combo aéreo
        if (isOnGround || isCombo) {
            Vector kb = new Vector(dir.getX() * hMult, vMult, dir.getZ() * hMult);
            victim.setVelocity(kb);
        } else {
            // KB reducido para golpes en el aire (no combo)
            Vector kb = new Vector(dir.getX() * hMult * 0.6, vMult * 0.4, dir.getZ() * hMult * 0.6);
            victim.setVelocity(kb);
        }
    }

    public void cleanup() {
        lastHitTime.clear();
        lastKnockbackTimes.clear();
    }
}
