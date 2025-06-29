package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Endermite;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import mk.kvlzx.MysthicKnockBack;

public class CombatManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, Long> lastHitTime = new HashMap<>();
    private final Map<UUID, Vector> pendingKnockback = new HashMap<>();
    
    private int hitDelay = 500; // millisegundos
    
    public CombatManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }
    
    // Métodos helper para obtener valores de configuración dinámicamente
    private double getHorizontalKnockback() {
        return plugin.getMainConfig().getHorizontalKnockback();
    }
    
    private double getVerticalKnockback() {
        return plugin.getMainConfig().getVerticalKnockback();
    }
    
    private double getKnockbackResistanceReduction() {
        return plugin.getMainConfig().getKnockbackReduction();
    }
    
    private double getSprintMultiplier() {
        return plugin.getMainConfig().getKnockbackSprintMultiplier();
    }
    
    private double getArrowHorizontalKnockback() {
        return plugin.getMainConfig().getHorizontalKnockbackArrow();
    }
    
    private double getArrowSprintMultiplier() {
        return plugin.getMainConfig().getSprintKnockbackArrow();
    }
    
    private double getEndermiteHorizontalKnockback() {
        return plugin.getMainConfig().getKnockbackHorizontalEndermite();
    }
    
    private double getEndermiteVerticalKnockback() {
        return plugin.getMainConfig().getKnockbackVerticalEndermite();
    }
    
    private int getEndermiteKnockbackLevel() {
        return plugin.getMainConfig().getKnockbackLevelEndermite();
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
        applyCustomKnockback(victim, attacker, false);
    }
    
    public void applyCustomKnockback(Player victim, Player attacker, boolean isArrow) {
        // Calcular knockback personalizado
        Vector knockback = calculateCustomKnockback(attacker, victim, isArrow);
        
        if (knockback != null) {
            UUID victimUUID = victim.getUniqueId();
            pendingKnockback.put(victimUUID, knockback);
            
            if (victim.isOnline() && pendingKnockback.containsKey(victimUUID)) {
                victim.setVelocity(pendingKnockback.get(victimUUID));
                pendingKnockback.remove(victimUUID);
            }
        }
    }
    
    // NUEVO: Método específico para knockback de endermites
    public void applyEndermiteKnockback(Player victim, Player owner, Endermite endermite) {
        Vector knockback = calculateEndermiteKnockback(owner, victim, endermite);
        
        if (knockback != null) {
            UUID victimUUID = victim.getUniqueId();
            pendingKnockback.put(victimUUID, knockback);
            
            if (victim.isOnline() && pendingKnockback.containsKey(victimUUID)) {
                victim.setVelocity(pendingKnockback.get(victimUUID));
                pendingKnockback.remove(victimUUID);
            }
        }
    }
    
    private Vector calculateCustomKnockback(Player attacker, Player victim, boolean isArrow) {
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
        
        // Calcular knockback base - usar valores específicos para flechas (valores dinámicos)
        double horizontal = isArrow ? getArrowHorizontalKnockback() : getHorizontalKnockback();
        double vertical = getVerticalKnockback();
        double currentSprintMultiplier = isArrow ? getArrowSprintMultiplier() : getSprintMultiplier();
        
        // Verificar si el atacante está corriendo (sprint)
        if (attacker.isSprinting()) {
            horizontal *= currentSprintMultiplier;
        }
        
        // Aplicar encantamiento Knockback con valores más altos
        ItemStack weapon = attacker.getItemInHand();
        if (weapon != null && weapon.containsEnchantment(Enchantment.KNOCKBACK)) {
            int knockbackLevel = weapon.getEnchantmentLevel(Enchantment.KNOCKBACK);
            // Para flechas, el multiplicador es aún mayor
            double knockbackMultiplier = isArrow ? 1.5 : 1.2;
            horizontal += knockbackLevel * knockbackMultiplier;
        }
        
        // Para flechas, también verificar el encantamiento Punch en el arco
        if (isArrow && weapon != null && weapon.getType() == Material.BOW) {
            if (weapon.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                int punchLevel = weapon.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK);
                horizontal += punchLevel * 0.5; // Multiplicador para Punch
            }
        }
        
        // Reducir por resistencia al knockback (valor dinámico)
        double resistance = getKnockbackResistance(victim);
        horizontal *= (1.0 - (resistance * getKnockbackResistanceReduction()));
        vertical *= (1.0 - (resistance * getKnockbackResistanceReduction()));
        
        // Crear vector final
        Vector knockback = new Vector(
            direction.getX() * horizontal,
            vertical,
            direction.getZ() * horizontal
        );
        
        // Aplicar límites de velocidad - más altos para flechas
        knockback = applyVelocityLimits(knockback, isArrow);
        
        return knockback;
    }
    
    // NUEVO: Método para calcular knockback específico de endermites
    private Vector calculateEndermiteKnockback(Player owner, Player victim, Endermite endermite) {
        // Obtener dirección del knockback desde el endermite hacia la víctima
        Vector direction = victim.getLocation().toVector()
                .subtract(endermite.getLocation().toVector())
                .normalize();
        
        // Verificar si la dirección es válida
        if (direction.lengthSquared() < 0.01) {
            // Si están en la misma posición, usar dirección aleatoria
            direction = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5).normalize();
        }
        
        // Usar valores base específicos para endermites (valores dinámicos)
        double horizontal = getEndermiteHorizontalKnockback();
        double vertical = getEndermiteVerticalKnockback();
        
        // Aplicar el nivel de knockback del endermite (simula Knockback II)
        horizontal += getEndermiteKnockbackLevel() * 1.2;
        
        // Si el dueño está corriendo, aplicar multiplicador (el endermite "hereda" el sprint)
        if (owner.isSprinting()) {
            horizontal *= getSprintMultiplier();
        }
        
        // Verificar si el dueño tiene items con knockback adicional
        ItemStack ownerWeapon = owner.getItemInHand();
        if (ownerWeapon != null && ownerWeapon.containsEnchantment(Enchantment.KNOCKBACK)) {
            int knockbackLevel = ownerWeapon.getEnchantmentLevel(Enchantment.KNOCKBACK);
            horizontal += knockbackLevel * 0.8; // Multiplicador reducido para endermites
        }
        
        // Reducir por resistencia al knockback (valor dinámico)
        double resistance = getKnockbackResistance(victim);
        horizontal *= (1.0 - (resistance * getKnockbackResistanceReduction()));
        vertical *= (1.0 - (resistance * getKnockbackResistanceReduction()));
        
        // Crear vector final
        Vector knockback = new Vector(
            direction.getX() * horizontal,
            vertical,
            direction.getZ() * horizontal
        );
        
        // Aplicar límites de velocidad específicos para endermites
        knockback = applyEndermiteVelocityLimits(knockback);
        
        return knockback;
    }

    private double getKnockbackResistance(Player player) {
        // Acá se puede agregar lógica para detectar la resistencia al knockback si es necesario
        double resistance = 0.0;
        
        return Math.max(0.0, Math.min(1.0, resistance));
    }
    
    private Vector applyVelocityLimits(Vector velocity, boolean isArrow) {
        // Límites más altos para flechas (valores dinámicos)
        double maxHorizontal = isArrow ? plugin.getMainConfig().getMaxKnockbackHorizontalArrow() : plugin.getMainConfig().getMaxKnockbackHorizontal();
        double maxVertical = plugin.getMainConfig().getMaxKnockbackVertical();
        
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
    
    // NUEVO: Límites de velocidad específicos para endermites
    private Vector applyEndermiteVelocityLimits(Vector velocity) {
        double maxHorizontal = 1.5; // Límite específico para endermites
        double maxVertical = 0.5;
        
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
