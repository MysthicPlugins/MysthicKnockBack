package mk.kvlzx.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.ZoneType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatListener implements Listener {
    private final MysthicKnockBack plugin;
    private final Map<UUID, UUID> lastAttacker = new HashMap<>();
    private final Map<UUID, Long> lastAttackTime = new HashMap<>();
    private static final long COMBAT_TIMEOUT = 10000; // 10 segundos

    public CombatListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        DamageCause cause = event.getCause();

        // Solo cancelamos el daño por caída
        if (cause == DamageCause.FALL) {
            event.setCancelled(true);
        }
        event.setDamage(0.0D);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = null;
        
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            
            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                attacker = shooter;
                
                // Solo registrar como atacante si NO es self-damage
                if (!shooter.equals(victim)) {
                    lastAttacker.put(victim.getUniqueId(), shooter.getUniqueId());
                    lastAttackTime.put(victim.getUniqueId(), System.currentTimeMillis());
                }
                
                // Aplicar knockback personalizado para flechas
                plugin.getCombatManager().applyCustomKnockback(victim, shooter);
                event.setDamage(0.0D);
                return;
            }
        } else if (event.getDamager() instanceof EnderPearl) {
            // Permitir el kb personalizado de las perlas
            EnderPearl pearl = (EnderPearl) event.getDamager();
            if (pearl.getShooter() instanceof Player) {
                Player thrower = (Player) pearl.getShooter();
                plugin.getCombatManager().applyCustomKnockback(victim, thrower);
            }
            event.setDamage(0.0D);
            return;
        }

        // Verificar que el atacante no sea nulo
        if (attacker == null) {
            return;
        }

        // Verificar cooldown de hit
        if (!plugin.getCombatManager().canHit(attacker)) {
            event.setCancelled(true);
            return;
        }

        // Verificar estados de la arena
        if (plugin.getScoreboardManager().isArenaChanging()) {
            event.setCancelled(true);
            return;
        }
        
        if (isInSpawn(victim)) {
            event.setCancelled(true);
            return;
        }
        
        if (isInSpawn(attacker)) {
            event.setCancelled(true);
            return;
        }

        // Registrar el último atacante
        lastAttacker.put(victim.getUniqueId(), attacker.getUniqueId());
        lastAttackTime.put(victim.getUniqueId(), System.currentTimeMillis());

        // Aplicar knockback personalizado usando el CombatManager
        plugin.getCombatManager().applyCustomKnockback(victim, attacker);
        event.setDamage(0.0D);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Si tenemos knockback pendiente, cancelar el evento vanilla
        if (plugin.getCombatManager().hasPendingKnockback(playerUUID)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            ((Player) event.getEntity()).setFoodLevel(20);
            ((Player) event.getEntity()).setSaturation(20.0f);
        }
    }

    public Player getLastAttacker(Player victim) {
        UUID lastAttackerUUID = lastAttacker.get(victim.getUniqueId());
        Long lastAttackTimeStamp = lastAttackTime.get(victim.getUniqueId());
        
        if (lastAttackerUUID == null || lastAttackTimeStamp == null) {
            return null;
        }
        
        // Verifica si el último ataque fue hace menos de 10 segundos
        long timeDiff = System.currentTimeMillis() - lastAttackTimeStamp;
        
        if (timeDiff > COMBAT_TIMEOUT) {
            lastAttacker.remove(victim.getUniqueId());
            lastAttackTime.remove(victim.getUniqueId());
            return null;
        }
        
        Player attackerPlayer = plugin.getServer().getPlayer(lastAttackerUUID);
        
        return attackerPlayer;
    }

    // Método para resetear el combate cuando un jugador muere
    public void resetCombat(Player player) {
        lastAttacker.remove(player.getUniqueId());
        lastAttackTime.remove(player.getUniqueId());
    }

    private boolean isInSpawn(Player player) {
        String zoneId = plugin.getArenaManager().getPlayerZone(player);
        return zoneId != null && zoneId.equals(ZoneType.SPAWN.getId());
    }
}
