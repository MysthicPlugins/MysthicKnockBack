package kk.kvlzx.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import kk.kvlzx.KvKnockback;

public class KnockbackUtils {
    
    // Constantes de knockback
    private static final double BASE_HORIZONTAL_KB = 0.35;
    private static final double BASE_VERTICAL_KB = 0.35;
    private static final double VERTICAL_LIMIT = 0.4;
    private static final double SPRINT_BONUS = 0.1;
    private static final double DISTANCE_BONUS_MULTIPLIER = 0.03;
    private static final double KB_ENCHANTMENT_BONUS = 0.2;
    
    /**
     * Aplica knockback personalizado a un jugador.
     * 
     * @param event El evento de daño
     * @param damaged El jugador que recibe el knockback
     * @param attacker El jugador que ataca
     * @param plugin Instancia del plugin
     */
    public static void applyCustomKnockback(EntityDamageByEntityEvent event, Player damaged, Player attacker, KvKnockback plugin) {
        // Revisar item y encantamientos
        ItemStack item = attacker.getInventory().getItemInHand();
        int knockbackLevel = 0;
        if (item != null && item.containsEnchantment(Enchantment.KNOCKBACK)) {
            knockbackLevel = item.getEnchantmentLevel(Enchantment.KNOCKBACK);
        }
        
        // Dirección
        Vector direction = damaged.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
        
        // Sprint bonus
        double sprintFactor = attacker.isSprinting() ? SPRINT_BONUS : 0.0;
        
        // Distancia bonus
        double distance = attacker.getLocation().distance(damaged.getLocation());
        double distanceFactor = distance * DISTANCE_BONUS_MULTIPLIER; // mientras más lejos, más knockback
        
        // Knockback final
        double finalHorizontalKB = BASE_HORIZONTAL_KB + sprintFactor + (knockbackLevel * KB_ENCHANTMENT_BONUS) + distanceFactor;
        double finalVerticalKB = BASE_VERTICAL_KB;
        
        Vector knockback = new Vector(direction.getX() * finalHorizontalKB, finalVerticalKB, direction.getZ() * finalHorizontalKB);
        
        // Limitar altura máxima
        if (knockback.getY() > VERTICAL_LIMIT) {
            knockback.setY(VERTICAL_LIMIT);
        }
        
        damaged.setVelocity(knockback);

        attacker.sendMessage("Aplicando knockback custom a " + damaged.getName());
        damaged.sendMessage("Recibiendo knockback custom de " + attacker.getName());
        
        // Cancelar daño
        event.setDamage(0.0D);
    }
}