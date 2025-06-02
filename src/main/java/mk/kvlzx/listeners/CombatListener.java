package mk.kvlzx.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.ZoneType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatListener implements Listener {
    private final MysthicKnockBack plugin;
    private final Map<UUID, UUID> lastAttacker = new HashMap<>();
    private final Map<UUID, Long> lastAttackTime = new HashMap<>();
    private final Map<UUID, Long> lastKnockbackTimes = new HashMap<>();
    private static final long COMBAT_TIMEOUT = 10000; // 10 segundos

    public CombatListener(MysthicKnockBack plugin) {
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = null;

        // Verificar si el daño es directo o por proyectil
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
            }
        }

        if (attacker == null) return;

        // Verificar si la arena está cambiando
        if (plugin.getScoreboardManager().isArenaChanging()) {
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
        applyCustomKnockback(victim, attacker);
        event.setDamage(0.0D);
    }

    private void applyCustomKnockback(Player victim, Player attacker) {
        // Reducir valores base de KB
        double baseH = 0.35; // Reducido de 0.45
        double baseV = 0.35; // Reducido de 0.75
        final double sprintBonus = 0.15; // Reducido de 0.2

        // Si el palo tiene Empuje, reducimos ambos
        ItemStack weapon = attacker.getItemInHand();
        int kbLevel = (weapon != null && weapon.getType() != Material.AIR && 
                weapon.containsEnchantment(Enchantment.KNOCKBACK)) 
                ? weapon.getEnchantmentLevel(Enchantment.KNOCKBACK)
                : 0;
        if (kbLevel > 0) {
            baseH *= 0.5;  // Reducido de 0.6
            baseV *= 0.5;  // Reducido de 0.6
        }

        // Dirección normalizada
        Vector dir = victim.getLocation().toVector()
                        .subtract(attacker.getLocation().toVector())
                        .setY(0)
                        .normalize();

        double hMult = baseH + (attacker.isSprinting() ? sprintBonus : 0);
        double vMult = baseV;

        // Sistema anti-KB acumulativo
        Long lastKnockbackTime = lastKnockbackTimes.get(victim.getUniqueId());
        long currentTime = System.currentTimeMillis();
        
        if (lastKnockbackTime != null && currentTime - lastKnockbackTime < 500) {
            // Reducir el KB si el jugador recibió otro golpe recientemente
            hMult *= 0.7;
            vMult *= 0.5;
        }
        lastKnockbackTimes.put(victim.getUniqueId(), currentTime);

        // Clamp vertical
        vMult = Math.min(vMult, 0.4); // Máximo vertical reducido

        // Solo aplicar KB si el jugador está en el suelo o cerca de él
        if (victim.isOnGround() || victim.getLocation().getBlock().getRelative(0, -1, 0).getType() != Material.AIR) {
            Vector kb = new Vector(dir.getX() * hMult, vMult, dir.getZ() * hMult);
            victim.setVelocity(kb);
        } else {
            // Si está en el aire, aplicar KB reducido
            Vector kb = new Vector(dir.getX() * hMult * 0.6, vMult * 0.4, dir.getZ() * hMult * 0.6);
            victim.setVelocity(kb);
        }
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
