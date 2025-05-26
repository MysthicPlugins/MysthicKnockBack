package kk.kvlzx.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.arena.ZoneType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {
    private final KvKnockback plugin;
    private final Map<UUID, UUID> lastAttacker = new HashMap<>();
    private final Map<UUID, Long> lastAttackTime = new HashMap<>();
    private static final long COMBAT_TIMEOUT = 10000; // 10 segundos

    public CombatListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        DamageCause cause = event.getCause();

        // Solo cancelamos el daño por caída
        if (cause == DamageCause.FALL) {
            event.setCancelled(true);
        }
        event.setDamage(0.0D);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        // Verificar si la arena está cambiando o si algún jugador tiene NoDamageTicks altos
        if (plugin.getScoreboardManager().isArenaChanging() || 
            victim.getNoDamageTicks() > victim.getMaximumNoDamageTicks() / 2 ||
            attacker.getNoDamageTicks() > attacker.getMaximumNoDamageTicks() / 2) {
            event.setCancelled(true);
            return;
        }

        // Verificar si el jugador está en spawn
        String victimZone = plugin.getArenaManager().getPlayerZone(victim);
        String attackerZone = plugin.getArenaManager().getPlayerZone(attacker);
        
        if (victimZone != null && victimZone.equals(ZoneType.SPAWN.getId()) || 
            attackerZone != null && attackerZone.equals(ZoneType.SPAWN.getId())) {
            event.setCancelled(true);
            return;
        }

        // Registrar el último atacante
        lastAttacker.put(victim.getUniqueId(), attacker.getUniqueId());
        lastAttackTime.put(victim.getUniqueId(), System.currentTimeMillis());

        // Aplicar knockback personalizado
        applyCustomKnockback(event, victim, attacker);
    }

    private void applyCustomKnockback(EntityDamageByEntityEvent event, Player victim, Player attacker) {
        // Constantes de knockback mejoradas
        final double BASE_HORIZONTAL = 0.45;    // Mantener horizontal
        final double BASE_VERTICAL = 0.25;      // Reducido de 0.42 a 0.25
        final double SPRINT_BONUS = 0.2;        // Mantener sprint bonus
        final double FEATHER_BONUS = 0.15;      // Mantener feather bonus
        final double Y_REDUCTION = 0.45;        // Aumentado la reducción de Y
        final double MAX_Y_VELOCITY = 0.35;     // Nuevo: límite máximo de velocidad vertical
        
        // Obtener la dirección del knockback
        double dx = victim.getLocation().getX() - attacker.getLocation().getX();
        double dz = victim.getLocation().getZ() - attacker.getLocation().getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        
        // Normalizar la dirección
        if (distance > 0) {
            dx = dx / distance;
            dz = dz / distance;
        }

        // Calcular multiplicadores
        double horizontalMultiplier = BASE_HORIZONTAL;
        double verticalMultiplier = BASE_VERTICAL;

        // Bonus por sprint
        if (attacker.isSprinting()) {
            horizontalMultiplier += SPRINT_BONUS;
        }

        // Bonus por pluma (velocidad aumentada)
        if (attacker.getWalkSpeed() > 0.2f) {
            horizontalMultiplier += FEATHER_BONUS;
        }

        // Reducir Y cuando el jugador está cayendo y limitar velocidad vertical máxima
        if (!((LivingEntity)victim).isOnGround() && victim.getVelocity().getY() < 0) {
            verticalMultiplier *= Y_REDUCTION;
        }
        
        // Limitar la velocidad vertical máxima
        verticalMultiplier = Math.min(verticalMultiplier, MAX_Y_VELOCITY);

        // Aplicar velocidad con el nuevo sistema de knockback
        victim.setVelocity(victim.getVelocity()
            .setX(dx * horizontalMultiplier)
            .setY(verticalMultiplier)
            .setZ(dz * horizontalMultiplier));

        // Cancelar el daño vanilla
        event.setDamage(0.0D);
    }

    public Player getLastAttacker(Player victim) {
        UUID lastAttackerUUID = lastAttacker.get(victim.getUniqueId());
        Long lastAttackTimeStamp = lastAttackTime.get(victim.getUniqueId());
        
        if (lastAttackerUUID == null || lastAttackTimeStamp == null) return null;
        
        // Verifica si el último ataque fue hace menos de 10 segundos
        if (System.currentTimeMillis() - lastAttackTimeStamp > COMBAT_TIMEOUT) {
            lastAttacker.remove(victim.getUniqueId());
            lastAttackTime.remove(victim.getUniqueId());
            return null;
        }
        
        return plugin.getServer().getPlayer(lastAttackerUUID);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            ((Player) event.getEntity()).setFoodLevel(20);
            ((Player) event.getEntity()).setSaturation(20.0f);
        }
    }
}
