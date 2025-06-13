package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.enchantments.Enchantment;

import mk.kvlzx.MysthicKnockBack;

public class CombatManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, Long> lastHitTime = new HashMap<>();
    private final Map<UUID, Vector> pendingKnockback = new HashMap<>();
    
    private double horizontalKnockback = 0.7;
    private double verticalKnockback = 0.385;
    private double knockbackResistanceReduction = 0.5;
    private double sprintMultiplier = 1.8;
    private int hitDelay = 500; // millisegundos
    
    public CombatManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }
    
    public boolean canHit(Player attacker) {
        UUID attackerUUID = attacker.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        if (lastHitTime.containsKey(attackerUUID)) {
            long timeSinceLastHit = currentTime - lastHitTime.get(attackerUUID);
            if (timeSinceLastHit < hitDelay) {
                return false;
            }
        }
        
        lastHitTime.put(attackerUUID, currentTime);
        return true;
    }
    
    public void applyCustomKnockback(Player victim, Player attacker) {
        // Calcular knockback personalizado
        Vector knockback = calculateCustomKnockback(attacker, victim);
        
        if (knockback != null) {
            UUID victimUUID = victim.getUniqueId();
            pendingKnockback.put(victimUUID, knockback);
            
            // Aplicar knockback en el siguiente tick
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline() && pendingKnockback.containsKey(victimUUID)) {
                    victim.setVelocity(pendingKnockback.get(victimUUID));
                    pendingKnockback.remove(victimUUID);
                }
            }, 1L);
        }
    }
    
    private Vector calculateCustomKnockback(Player attacker, Player victim) {
        // Para self-damage, usar dirección basada en la mirada del jugador
        Vector direction;
        if (attacker.equals(victim)) {
            // Usar la dirección donde está mirando el jugador para self-knockback
            direction = victim.getLocation().getDirection().normalize();
        } else {
            // Obtener dirección del knockback normal
            direction = victim.getLocation().toVector()
                    .subtract(attacker.getLocation().toVector())
                    .normalize();
            
            // Verificar si la dirección es válida (no es cero)
            if (direction.lengthSquared() < 0.01) {
                // Si los jugadores están en la misma posición, usar dirección aleatoria
                direction = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5).normalize();
            }
        }
        
        // Calcular knockback base
        double horizontal = horizontalKnockback;
        double vertical = verticalKnockback;
        
        // Verificar si el atacante está corriendo (sprint)
        if (attacker.isSprinting()) {
            horizontal *= sprintMultiplier;
        }
        
        // Aplicar encantamiento Knockback con valores más altos
        ItemStack weapon = attacker.getItemInHand();
        if (weapon != null && weapon.containsEnchantment(Enchantment.KNOCKBACK)) {
            int knockbackLevel = weapon.getEnchantmentLevel(Enchantment.KNOCKBACK);
            // Aumentado el multiplicador de 0.5 a 1.2 para que sea más notorio
            horizontal += knockbackLevel * 1.2;
        }
        
        // Reducir por resistencia al knockback
        double resistance = getKnockbackResistance(victim);
        horizontal *= (1.0 - (resistance * knockbackResistanceReduction));
        vertical *= (1.0 - (resistance * knockbackResistanceReduction));
        
        // Crear vector final
        Vector knockback = new Vector(
            direction.getX() * horizontal,
            vertical,
            direction.getZ() * horizontal
        );
        
        // Aplicar límites de velocidad estilo Hypixel (aumentados)
        knockback = applyVelocityLimits(knockback);
        
        return knockback;
    }

    private double getKnockbackResistance(Player player) {
        // Acá se puede agregar lógica para detectar la resistencia al knockback si es necesario
        double resistance = 0.0;
        
        return Math.max(0.0, Math.min(1.0, resistance));
    }
    
    private Vector applyVelocityLimits(Vector velocity) {
        double maxHorizontal = 0.9;
        double maxVertical = 0.6;
        
        // Limitar componentes horizontales
        double horizontalMagnitude = Math.sqrt(
            velocity.getX() * velocity.getX() + 
            velocity.getZ() * velocity.getZ()
        );
        
        if (horizontalMagnitude > maxHorizontal) {
            double ratio = maxHorizontal / horizontalMagnitude;
            velocity.setX(velocity.getX() * ratio);
            velocity.setZ(velocity.getZ() * ratio);
        }
        
        // Limitar componente vertical
        if (velocity.getY() > maxVertical) {
            velocity.setY(maxVertical);
        }
        
        return velocity;
    }
    
    public void cleanup() {
        lastHitTime.clear();
        pendingKnockback.clear();
    }
    
    // Método para verificar si hay knockback pendente (usado en CombatListener)
    public boolean hasPendingKnockback(UUID playerUUID) {
        return pendingKnockback.containsKey(playerUUID);
    }
}
