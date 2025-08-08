package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Endermite;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import mk.kvlzx.MysthicKnockBack;

public class CombatManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, Long> lastHitTime = new HashMap<>();
    private final Map<UUID, Vector> pendingKnockback = new HashMap<>();
    private final Map<UUID, Long> knockbackCooldown = new HashMap<>();
    private final Map<UUID, BukkitRunnable> powerupKnockbackTasks = new HashMap<>();
    private static final long KNOCKBACK_COOLDOWN_MS = 200; // 200ms cooldown entre knockbacks

    private double horizontalKnockback = MysthicKnockBack.getInstance().getMainConfig().getHorizontalKnockback();
    private double verticalKnockback = MysthicKnockBack.getInstance().getMainConfig().getVerticalKnockback();
    private double knockbackResistanceReduction = MysthicKnockBack.getInstance().getMainConfig().getKnockbackReduction();
    private double sprintMultiplier = MysthicKnockBack.getInstance().getMainConfig().getKnockbackSprintMultiplier();
    
    // Nuevos valores específicos para flechas
    private double arrowHorizontalKnockback = MysthicKnockBack.getInstance().getMainConfig().getHorizontalKnockbackArrow();
    private double arrowSprintMultiplier =  MysthicKnockBack.getInstance().getMainConfig().getSprintKnockbackArrow();
    
    // NUEVO: Valores específicos para endermites
    private double endermiteHorizontalKnockback = MysthicKnockBack.getInstance().getMainConfig().getKnockbackHorizontalEndermite();
    private double endermiteVerticalKnockback = MysthicKnockBack.getInstance().getMainConfig().getKnockbackVerticalEndermite();
    private int endermiteKnockbackLevel = MysthicKnockBack.getInstance().getMainConfig().getKnockbackLevelEndermite();
    
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
        applyCustomKnockback(victim, attacker, false);
    }

    public void applyCustomKnockback(Player victim, Player attacker, boolean isArrow) {
        // MEJORADO: Verificar si ya hay knockback pendiente muy reciente
        if (!canApplyKnockback(victim.getUniqueId())) {
            return;
        }
        
        // Calcular knockback personalizado
        Vector knockback = calculateCustomKnockback(attacker, victim, isArrow);

        if (knockback != null) {
            UUID victimUUID = victim.getUniqueId();
            
            // MEJORADO: Si ya hay un knockback pendiente, usar el más fuerte
            Vector existingKnockback = pendingKnockback.get(victimUUID);
            if (existingKnockback != null) {
                // Comparar magnitudes y usar el más fuerte
                double existingMagnitude = existingKnockback.lengthSquared();
                double newMagnitude = knockback.lengthSquared();
                
                if (newMagnitude <= existingMagnitude) {
                    return; // El knockback actual es más débil, no aplicar
                }
            }
            
            pendingKnockback.put(victimUUID, knockback);
            knockbackCooldown.put(victimUUID, System.currentTimeMillis());

            // Aplicar knockback en el siguiente tick
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline() && pendingKnockback.containsKey(victimUUID)) {
                    Vector finalKnockback = pendingKnockback.remove(victimUUID);
                    if (finalKnockback != null) {
                        victim.setVelocity(finalKnockback);
                    }
                }
            }, 1L);
        }
    }
    
    // NUEVO: Método específico para knockback de endermites
    public void applyEndermiteKnockback(Player victim, Player owner, Endermite endermite) {
        if (!canApplyKnockback(victim.getUniqueId())) {
            return;
        }
        
        Vector knockback = calculateEndermiteKnockback(owner, victim, endermite);

        if (knockback != null) {
            UUID victimUUID = victim.getUniqueId();
            
            // MEJORADO: Aplicar la misma lógica de knockback más fuerte
            Vector existingKnockback = pendingKnockback.get(victimUUID);
            if (existingKnockback != null) {
                double existingMagnitude = existingKnockback.lengthSquared();
                double newMagnitude = knockback.lengthSquared();
                
                if (newMagnitude <= existingMagnitude) {
                    return;
                }
            }
            
            pendingKnockback.put(victimUUID, knockback);
            knockbackCooldown.put(victimUUID, System.currentTimeMillis());

            // Aplicar knockback en el siguiente tick
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline() && pendingKnockback.containsKey(victimUUID)) {
                    Vector finalKnockback = pendingKnockback.remove(victimUUID);
                    if (finalKnockback != null) {
                        victim.setVelocity(finalKnockback);
                    }
                }
            }, 1L);
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
        }

        // Calcular knockback base - usar valores específicos para flechas
        double horizontal = isArrow ? arrowHorizontalKnockback : horizontalKnockback;
        double vertical = verticalKnockback;
        double currentSprintMultiplier = isArrow ? arrowSprintMultiplier : sprintMultiplier;

        // Verificar si el atacante está corriendo (sprint)
        if (attacker.isSprinting()) {
            horizontal *= currentSprintMultiplier;
        }

        // Aplicar encantamiento Knockback con valores más altos
        ItemStack weapon = attacker.getItemInHand();
        if (weapon != null && weapon.containsEnchantment(Enchantment.KNOCKBACK)) {
            int knockbackLevel = weapon.getEnchantmentLevel(Enchantment.KNOCKBACK);
            // Para flechas, el multiplicador es aún mayor
            double knockbackMultiplier = isArrow ? plugin.getMainConfig().getMaxKnockbackHorizontalArrow() : plugin.getMainConfig().getMaxKnockbackHorizontal();
            horizontal += knockbackLevel * knockbackMultiplier;
        }

        // Verificar si el atacante tiene el powerup de knockback
        if (hasKnockbackPowerup(attacker)) {
            horizontal += plugin.getMainConfig().getPowerUpKnockbackEffectMultiplier();
        }

        // Para flechas, también verificar el encantamiento Punch en el arco
        if (isArrow && weapon != null && weapon.getType() == Material.BOW) {
            if (weapon.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                int punchLevel = weapon.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK);
                horizontal += punchLevel * 0.5; // Multiplicador para Punch
            }
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

        // Usar valores base específicos para endermites
        double horizontal = endermiteHorizontalKnockback;
        double vertical = endermiteVerticalKnockback;

        // Aplicar el nivel de knockback del endermite (simula Knockback II)
        horizontal += endermiteKnockbackLevel * 1.2;

        // Si el dueño está corriendo, aplicar multiplicador (el endermite "hereda" el sprint)
        if (owner.isSprinting()) {
            horizontal *= sprintMultiplier;
        }

        // Verificar si el dueño tiene items con knockback adicional
        ItemStack ownerWeapon = owner.getItemInHand();
        if (ownerWeapon != null && ownerWeapon.containsEnchantment(Enchantment.KNOCKBACK)) {
            int knockbackLevel = ownerWeapon.getEnchantmentLevel(Enchantment.KNOCKBACK);
            horizontal += knockbackLevel * 0.8; // Multiplicador reducido para endermites
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
        // Límites más altos para flechas
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
        knockbackCooldown.clear();
        
        // Cancelar todas las tareas de powerup activas
        for (BukkitRunnable task : powerupKnockbackTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        powerupKnockbackTasks.clear();
    }

    // Método para verificar si hay knockback pendente (usado en CombatListener)
    public boolean hasPendingKnockback(UUID playerUUID) {
        return pendingKnockback.containsKey(playerUUID);
    }

    // REMOVIDO: applyPrioritizedKnockback - ya no necesario con la nueva lógica
    
    private boolean canApplyKnockback(UUID victimUUID) {
        if (!knockbackCooldown.containsKey(victimUUID)) {
            return true;
        }
        
        long lastKnockback = knockbackCooldown.get(victimUUID);
        return System.currentTimeMillis() - lastKnockback > KNOCKBACK_COOLDOWN_MS;
    }

    public void addPowerupKnockback(Player player, int duration) {
        UUID playerUUID = player.getUniqueId();
        
        // Si ya tiene un powerup activo, cancelar el anterior
        removePowerupKnockback(player);
        
        // Crear y programar la tarea de remoción
        BukkitRunnable removalTask = new BukkitRunnable() {
            @Override
            public void run() {
                powerupKnockbackTasks.remove(playerUUID);
            }
        };
        
        // Guardar la tarea y programarla
        powerupKnockbackTasks.put(playerUUID, removalTask);
        removalTask.runTaskLater(plugin, duration * 20L); // Convertir segundos a ticks
    }

    public void removePowerupKnockback(Player player) {
        UUID playerUUID = player.getUniqueId();
        BukkitRunnable task = powerupKnockbackTasks.remove(playerUUID);
        
        if (task != null) {
            task.cancel();
        }
    }

    public boolean hasKnockbackPowerup(Player player) {
        return powerupKnockbackTasks.containsKey(player.getUniqueId());
    }
}
