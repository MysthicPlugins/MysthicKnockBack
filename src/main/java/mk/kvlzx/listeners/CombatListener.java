package mk.kvlzx.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
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
    private final Map<UUID, AttackerInfo> lastAttacker = new HashMap<>();
    private static final long COMBAT_TIMEOUT = 15000; // Aumentado a 15 segundos

    public CombatListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    // Clase interna para almacenar información del atacante
    public static class AttackerInfo {
        private final UUID attackerUUID;
        private final String attackerName;
        private final long attackTime;
        
        public AttackerInfo(UUID attackerUUID, String attackerName, long attackTime) {
            this.attackerUUID = attackerUUID;
            this.attackerName = attackerName;
            this.attackTime = attackTime;
        }
        
        public UUID getAttackerUUID() { return attackerUUID; }
        public String getAttackerName() { return attackerName; }
        public long getAttackTime() { return attackTime; }
        
        @Override
        public String toString() {
            return "AttackerInfo{name=" + attackerName + ", uuid=" + attackerUUID + ", time=" + attackTime + "}";
        }
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
                // Solo registrar el atacante si no es el mismo jugador
                if (!shooter.equals(victim)) {
                    attacker = shooter;
                }
                // Aplicar knockback incluso si es self-damage
                plugin.getCombatManager().applyCustomKnockback(victim, shooter);
                event.setDamage(0.0D);
                return;
            }
        } else if (event.getDamager() instanceof EnderPearl) {
            return; // Permitir el kb vanilla de las perlas
        }

        if (attacker == null) return;

        // Verificar cooldown de hit
        if (!plugin.getCombatManager().canHit(attacker)) {
            event.setCancelled(true);
            return;
        }

        // Verificar estados de la arena
        if (plugin.getScoreboardManager().isArenaChanging() ||
            isInSpawn(victim) || isInSpawn(attacker)) {
            event.setCancelled(true);
            return;
        }

        // Registrar el último atacante con información completa
        AttackerInfo attackerInfo = new AttackerInfo(
            attacker.getUniqueId(), 
            attacker.getName(), 
            System.currentTimeMillis()
        );
        lastAttacker.put(victim.getUniqueId(), attackerInfo);
        
        // Debug log
        plugin.getLogger().info("Atacante registrado: " + attacker.getName() + " -> " + victim.getName());

        // Aplicar knockback personalizado usando el CombatManager
        plugin.getCombatManager().applyCustomKnockback(victim, attacker);
        event.setDamage(0.0D);
    }

    // Método mejorado que devuelve información completa del atacante
    public AttackerInfo getLastAttackerInfo(Player victim) {
        AttackerInfo attackerInfo = lastAttacker.get(victim.getUniqueId());
        
        if (attackerInfo == null) return null;
        
        // Verifica si el último ataque fue hace menos del timeout
        if (System.currentTimeMillis() - attackerInfo.getAttackTime() > COMBAT_TIMEOUT) {
            lastAttacker.remove(victim.getUniqueId());
            return null;
        }
        
        return attackerInfo;
    }

    // Método legacy para mantener compatibilidad
    public Player getLastAttacker(Player victim) {
        AttackerInfo attackerInfo = getLastAttackerInfo(victim);
        if (attackerInfo == null) return null;
        
        return plugin.getServer().getPlayer(attackerInfo.getAttackerUUID());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            ((Player) event.getEntity()).setFoodLevel(20);
            ((Player) event.getEntity()).setSaturation(20.0f);
        }
    }

    // Limpiar datos cuando un jugador se desconecta
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        lastAttacker.remove(playerUUID);
        
        // También limpiar si este jugador era el atacante de alguien más
        lastAttacker.entrySet().removeIf(entry -> 
            entry.getValue().getAttackerUUID().equals(playerUUID)
        );
    }

    private boolean isInSpawn(Player player) {
        String zoneId = plugin.getArenaManager().getPlayerZone(player);
        return zoneId != null && zoneId.equals(ZoneType.SPAWN.getId());
    }
}
