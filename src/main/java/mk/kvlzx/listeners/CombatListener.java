package mk.kvlzx.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Endermite;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.ZoneType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatListener implements Listener {
    private final MysthicKnockBack plugin;
    private final Map<UUID, UUID> lastAttacker = new HashMap<>();
    private final Map<UUID, Long> lastAttackTime = new HashMap<>();
    private final Map<UUID, Long> lastKnockbackTime = new HashMap<>(); // NUEVO: Para evitar knockback múltiple
    private static final long COMBAT_TIMEOUT = MysthicKnockBack.getInstance().getCombatConfig().getCombatLog();
    private static final long KNOCKBACK_IMMUNITY_TIME = 250; // NUEVO: 250ms de inmunidad entre knockbacks

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
        boolean isProjectile = false;
        
        // NUEVO: Verificar inmunidad de knockback reciente
        long currentTime = System.currentTimeMillis();
        Long lastKnockback = lastKnockbackTime.get(victim.getUniqueId());
        boolean hasKnockbackImmunity = lastKnockback != null && 
            (currentTime - lastKnockback) < KNOCKBACK_IMMUNITY_TIME;
        
        if (event.getDamager() instanceof EnderPearl) {
            EnderPearl pearl = (EnderPearl) event.getDamager();
            if (pearl.getShooter() instanceof Player) {
                Player thrower = (Player) pearl.getShooter();
                attacker = thrower;
                isProjectile = true;
                
                // Verificar estados de combate antes de aplicar knockback
                if (shouldCancelCombat(victim, thrower)) {
                    event.setDamage(0.0D);
                    event.setCancelled(true);
                    return;
                }
                
                // Solo aplicar knockback y registrar atacante si NO es self-damage Y no tiene inmunidad
                if (!thrower.equals(victim)) {
                    lastAttacker.put(victim.getUniqueId(), thrower.getUniqueId());
                    lastAttackTime.put(victim.getUniqueId(), currentTime);
                    
                    // Solo aplicar knockback si no tiene inmunidad
                    if (!hasKnockbackImmunity) {
                        plugin.getCombatManager().applyCustomKnockback(victim, thrower, false);
                        lastKnockbackTime.put(victim.getUniqueId(), currentTime);
                    }
                }
                
                event.setDamage(0.0D);
                // ARREGLADO: No hacer return aquí para permitir que se procese la animación
            }
        } else if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            
            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                attacker = shooter;
                isProjectile = true;
                
                // Verificar estados de combate
                if (shouldCancelCombat(victim, shooter)) {
                    event.setDamage(0.0D);
                    event.setCancelled(true);
                    return;
                }
                
                // Solo registrar como atacante si NO es self-damage
                if (!shooter.equals(victim)) {
                    lastAttacker.put(victim.getUniqueId(), shooter.getUniqueId());
                    lastAttackTime.put(victim.getUniqueId(), currentTime);
                }
                
                // Aplicar knockback de flecha solo si no tiene inmunidad
                if (!hasKnockbackImmunity) {
                    plugin.getCombatManager().applyCustomKnockback(victim, shooter, true);
                    lastKnockbackTime.put(victim.getUniqueId(), currentTime);
                }
                
                event.setDamage(0.0D);
                return;
            }
        } else if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();
            
            if (snowball.getShooter() instanceof Player) {
                Player shooter = (Player) snowball.getShooter();
                isProjectile = true;
                
                // Verificar estados de combate
                if (shouldCancelCombat(victim, shooter)) {
                    event.setDamage(0.0D);
                    event.setCancelled(true);
                    return;
                }
                
                // Solo registrar como atacante si NO es self-damage
                if (!shooter.equals(victim)) {
                    lastAttacker.put(victim.getUniqueId(), shooter.getUniqueId());
                    lastAttackTime.put(victim.getUniqueId(), currentTime);
                }
                
                event.setDamage(0.0D);
                return;
            }
        } else if (event.getDamager() instanceof Endermite) {
            Endermite endermite = (Endermite) event.getDamager();
            
            Player owner = plugin.getEndermiteListener().getEndermiteOwner(endermite);
            if (owner != null) {
                // No permitir que el endermite ataque a su propio dueño
                if (owner.equals(victim)) {
                    event.setCancelled(true);
                    return;
                }
                
                // Verificar estados de combate
                if (shouldCancelCombat(victim, owner)) {
                    event.setCancelled(true);
                    return;
                }
                
                // Registrar el dueño del endermite como atacante
                lastAttacker.put(victim.getUniqueId(), owner.getUniqueId());
                lastAttackTime.put(victim.getUniqueId(), currentTime);
                
                // Aplicar knockback específico para endermites solo si no tiene inmunidad
                if (!hasKnockbackImmunity) {
                    plugin.getCombatManager().applyEndermiteKnockback(victim, owner, endermite);
                    lastKnockbackTime.put(victim.getUniqueId(), currentTime);
                }
                
                event.setDamage(0.0D);
                return;
            }
        }

        // Si llegamos aquí y no hay atacante, salir
        if (attacker == null) {
            return;
        }

        // Si el atacante está en modo staff, no aplicar knockback
        if (attacker.hasMetadata("mysthicstaff_modmode")) {
            event.setCancelled(true);
            return;
        }

        // Verificar cooldown de hit (solo para ataques físicos, no proyectiles)
        if (!isProjectile && !plugin.getCombatManager().canHit(attacker)) {
            event.setCancelled(true);
            return;
        }

        // Verificar estados de combate para ataques físicos
        if (!isProjectile && shouldCancelCombat(victim, attacker)) {
            event.setCancelled(true);
            return;
        }

        // Para ataques físicos (no proyectiles), registrar atacante y aplicar knockback
        if (!isProjectile) {
            lastAttacker.put(victim.getUniqueId(), attacker.getUniqueId());
            lastAttackTime.put(victim.getUniqueId(), currentTime);

            // Aplicar knockback solo si no tiene inmunidad
            if (!hasKnockbackImmunity) {
                plugin.getCombatManager().applyCustomKnockback(victim, attacker);
                lastKnockbackTime.put(victim.getUniqueId(), currentTime);
            }
        }
        
        event.setDamage(0.0D);
    }
    
    // NUEVO: Método helper para verificar si el combate debe ser cancelado
    private boolean shouldCancelCombat(Player victim, Player attacker) {
        // Verificar estados de la arena
        if (plugin.getArenaChangeManager().isArenaChanging()) {
            return true;
        }
        
        if (isInSpawn(victim) || isInSpawn(attacker)) {
            return true;
        }
        
        return false;
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
        UUID playerUUID = player.getUniqueId();
        lastAttacker.remove(playerUUID);
        lastAttackTime.remove(playerUUID);
        lastKnockbackTime.remove(playerUUID); // NUEVO: Limpiar inmunidad de knockback
    }

    private boolean isInSpawn(Player player) {
        String zoneId = plugin.getArenaManager().getPlayerZone(player);
        return zoneId != null && zoneId.equals(ZoneType.SPAWN.getId());
    }
}