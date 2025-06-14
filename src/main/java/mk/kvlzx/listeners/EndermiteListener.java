package mk.kvlzx.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class EndermiteListener implements Listener {

    private final MysthicKnockBack plugin;
    private final Map<UUID, UUID> endermiteOwners = new HashMap<>(); // UUID del endermite -> UUID del jugador
    private final Map<UUID, BukkitTask> endermiteTasks = new HashMap<>(); // UUID del endermite -> Task de countdown
    private final Map<UUID, BukkitTask> followTasks = new HashMap<>(); // UUID del endermite -> Task de seguimiento
    private final Map<UUID, BukkitTask> attackTasks = new HashMap<>(); // UUID del endermite -> Task de ataque
    
    public EndermiteListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEndermiteSpawn(CreatureSpawnEvent event) {
        EntityType entityType = event.getEntityType();
        SpawnReason spawnReason = event.getSpawnReason();
        
        // Comprobar si el Endermite es generado por ender pearl
        if (entityType == EntityType.ENDERMITE && spawnReason == SpawnReason.DEFAULT) {
            Endermite endermite = (Endermite) event.getEntity();
            
            // Buscar al jugador más cercano (quien lanzó la ender pearl)
            Player owner = findNearestPlayer(endermite.getLocation());
            if (owner != null) {
                setupEndermitePet(endermite, owner);
            }
        }
    }
    
    private Player findNearestPlayer(Location location) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(location);
            if (distance < minDistance && distance <= 5) { // Máximo 5 bloques de distancia
                minDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }
    
    private void setupEndermitePet(Endermite endermite, Player owner) {
        UUID endermiteId = endermite.getUniqueId();
        endermiteOwners.put(endermiteId, owner.getUniqueId());
        
        // Configurar el endermite
        endermite.setRemoveWhenFarAway(false);
        endermite.setCanPickupItems(false);
        endermite.setMaxHealth(8.0);
        endermite.setHealth(8.0);
        
        endermite.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false));
        endermite.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 3, false, false));
        endermite.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 4, false, false));
        
        // Iniciar el sistema de countdown y nombre
        startEndermiteCountdown(endermite, owner);
        
        // Iniciar el comportamiento de ataque
        startAttackBehavior(endermite, owner);

        // Iniciar el comportamiento de seguir al dueño
        followOwner(endermite, owner);
    }
    
    private void startEndermiteCountdown(Endermite endermite, Player owner) {
        UUID endermiteId = endermite.getUniqueId();
        final int[] timeLeft = {30}; // 30 segundos de vida
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!endermite.isValid() || endermite.isDead()) {
                cleanupEndermite(endermiteId);
                return;
            }
            
            // Actualizar nombre con tiempo restante
            String displayName = MessageUtils.getColor("&e" + owner.getName() + "'s Pet &7(" + timeLeft[0] + "s)");
            endermite.setCustomName(displayName);
            endermite.setCustomNameVisible(true);
            
            timeLeft[0]--;
            
            if (timeLeft[0] <= 0) {
                // Desmontar al jugador si sigue montado
                if (endermite.getPassenger() != null && endermite.getPassenger().equals(owner)) {
                    endermite.setPassenger(null);
                }
                endermite.remove();
                cleanupEndermite(endermiteId);
            }
        }, 0L, 10L); // Cada medio segundo
        
        endermiteTasks.put(endermiteId, task);
    }
    
    private void startAttackBehavior(Endermite endermite, Player owner) {
        UUID endermiteId = endermite.getUniqueId();
        
        BukkitTask attackTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!endermite.isValid() || endermite.isDead()) {
                return;
            }
            
            // Verificar si el endermite está apuntando al dueño y cancelar el targeting
            if (endermite instanceof Creature) {
                Creature creature = (Creature) endermite;
                LivingEntity currentTarget = creature.getTarget();
                
                // Si está apuntando al dueño, cancelar el targeting
                if (currentTarget instanceof Player && 
                    currentTarget.getUniqueId().equals(owner.getUniqueId())) {
                    creature.setTarget(null);
                }
            }
            
            // Buscar enemigos válidos (que no sean el dueño)
            Player validTarget = null;
            double closestDistance = Double.MAX_VALUE;
            
            for (Entity entity : endermite.getNearbyEntities(10, 10, 10)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    
                    // No atacar al dueño
                    if (!target.getUniqueId().equals(owner.getUniqueId())) {
                        double distance = endermite.getLocation().distance(target.getLocation());
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            validTarget = target;
                        }
                    }
                }
            }
            
            // Asignar el objetivo válido más cercano
            if (validTarget != null && endermite instanceof Creature) {
                ((Creature) endermite).setTarget(validTarget);
            }
            
        }, 0L, 10L); // Cada medio segundo para mejor respuesta
        
        attackTasks.put(endermiteId, attackTask);
    }
    
    private void followOwner(Endermite endermite, Player owner) {
        UUID endermiteId = endermite.getUniqueId();

        BukkitTask followTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!endermite.isValid() || endermite.isDead() || !owner.isOnline()) {
                BukkitTask task = followTasks.remove(endermiteId);
                if (task != null) task.cancel();
                return;
            }

            if (!(endermite instanceof Creature)) return;
            Creature creature = (Creature) endermite;

            double distance = endermite.getLocation().distance(owner.getLocation());

            if (distance > 3.5) {
                creature.setTarget(owner);
            } else {
                if (creature.getTarget() != null && creature.getTarget().equals(owner)) {
                    creature.setTarget(null);
                }
            }

        }, 0L, 20L);

        followTasks.put(endermiteId, followTask);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Endermite) {
            cleanupEndermite(event.getEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, UUID> entry : endermiteOwners.entrySet()) {
            if (entry.getValue().equals(player.getUniqueId())) {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getUniqueId().equals(entry.getKey()) && entity instanceof Endermite) {
                            entity.remove();
                            break;
                        }
                    }
                }
                toRemove.add(entry.getKey());
            }
        }

        for (UUID endermiteId : toRemove) {
            cleanupEndermite(endermiteId);
        }
    }

    @EventHandler
    public void onEndermiteAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Endermite && event.getEntity() instanceof Player) {
            Endermite endermite = (Endermite) event.getDamager();
            UUID ownerId = endermiteOwners.get(endermite.getUniqueId());
            if (ownerId != null && event.getEntity().getUniqueId().equals(ownerId)) {
                event.setCancelled(true);
            }
        }
    }
    
    private void cleanupEndermite(UUID endermiteId) {
        endermiteOwners.remove(endermiteId);
        
        BukkitTask task = endermiteTasks.remove(endermiteId);
        if (task != null) {
            task.cancel();
        }
        
        BukkitTask followTask = followTasks.remove(endermiteId);
        if (followTask != null) {
            followTask.cancel();
        }
        
        BukkitTask attackTask = attackTasks.remove(endermiteId);
        if (attackTask != null) {
            attackTask.cancel();
        }
    }
}
