package mk.kvlzx.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
    private final Map<UUID, List<UUID>> playerEndermites = new HashMap<>(); // UUID del jugador -> Lista de endermites
    private final Map<UUID, BukkitTask> endermiteTasks = new HashMap<>(); // UUID del endermite -> Task de countdown
    private final Map<UUID, BukkitTask> followTasks = new HashMap<>(); // UUID del endermite -> Task de seguimiento
    private final Map<UUID, BukkitTask> attackTasks = new HashMap<>(); // UUID del endermite -> Task de ataque
    private final Map<UUID, UUID> currentAttackTargets = new HashMap<>(); // UUID del endermite -> UUID del objetivo actual
    
    private static final int MAX_ENDERMITES_PER_PLAYER = MysthicKnockBack.getInstance().getMainConfig().getEndermiteLimit();
    
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
            if (plugin.getMainConfig().getEndermiteEnabled()) {
                if (owner != null) {
                    // Verificar límite de endermites
                    if (canPlayerHaveMoreEndermites(owner)) {
                        setupEndermitePet(endermite, owner);
                        owner.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + plugin.getMainConfig().getEndermiteSpawnMessage()));
                    } else {
                        // Si ya tiene el máximo, cancelar el spawn
                        event.setCancelled(true);
                        owner.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +  " " + plugin.getMainConfig().getEndermiteLimitMessage().replace("%limit%", String.valueOf(MAX_ENDERMITES_PER_PLAYER))));
                    }
                }
            } else {
                // Si los endermites están deshabilitados, cancelar el spawn
                event.setCancelled(true);
            }
        }
    }
    
    private boolean canPlayerHaveMoreEndermites(Player player) {
        UUID playerId = player.getUniqueId();
        List<UUID> playerEndermiteList = playerEndermites.get(playerId);
        
        if (playerEndermiteList == null) {
            return true;
        }
        
        // Limpiar endermites muertos de la lista
        playerEndermiteList.removeIf(endermiteId -> {
            boolean exists = false;
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getUniqueId().equals(endermiteId) && entity instanceof Endermite && entity.isValid()) {
                        exists = true;
                        break;
                    }
                }
                if (exists) break;
            }
            return !exists;
        });
        
        return playerEndermiteList.size() < MAX_ENDERMITES_PER_PLAYER;
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
        UUID ownerId = owner.getUniqueId();
        
        endermiteOwners.put(endermiteId, ownerId);
        
        // Agregar a la lista de endermites del jugador
        playerEndermites.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(endermiteId);
        
        // Configurar el endermite
        endermite.setRemoveWhenFarAway(false);
        endermite.setCanPickupItems(false);
        endermite.setCustomNameVisible(true);
        endermite.setMaxHealth(8.0);
        endermite.setHealth(8.0);
        
        endermite.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false));
        endermite.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 3, false, false));
        endermite.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 4, false, false));
        
        // Iniciar el sistema de countdown y nombre
        startEndermiteCountdown(endermite, owner);
        
        // Iniciar el comportamiento de ataque y seguimiento combinado
        startCombinedBehavior(endermite, owner);
    }
    
    private void startEndermiteCountdown(Endermite endermite, Player owner) {
        UUID endermiteId = endermite.getUniqueId();
        final int[] timeLeft = {plugin.getMainConfig().getEndermiteTime()};
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!endermite.isValid() || endermite.isDead()) {
                cleanupEndermite(endermiteId);
                return;
            }
            
            // Actualizar nombre con tiempo restante
            String displayName = MessageUtils.getColor(plugin.getMainConfig().getEndermiteName())
                    .replace("%time%", String.valueOf(timeLeft[0]))
                    .replace("%player%", owner.getName());
            endermite.setCustomName(displayName);
            endermite.setCustomNameVisible(true);
            
            timeLeft[0]--;
            
            if (timeLeft[0] <= 0) {
                endermite.remove();
                cleanupEndermite(endermiteId);
            }
        }, 0L, 20L); // Cada segundo
        
        endermiteTasks.put(endermiteId, task);
    }
    
    private void startCombinedBehavior(Endermite endermite, Player owner) {
        UUID endermiteId = endermite.getUniqueId();
        
        BukkitTask behaviorTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!endermite.isValid() || endermite.isDead() || !owner.isOnline()) {
                return;
            }
            
            if (!(endermite instanceof Creature)) return;
            Creature creature = (Creature) endermite;
            
            // Verificar si el endermite está apuntando al dueño y cancelar el targeting
            LivingEntity currentTarget = creature.getTarget();
            if (currentTarget instanceof Player && 
                currentTarget.getUniqueId().equals(owner.getUniqueId())) {
                creature.setTarget(null);
                currentTarget = null;
            }
            
            // Obtener el objetivo actual almacenado
            UUID currentStoredTargetId = currentAttackTargets.get(endermiteId);
            Player currentStoredTarget = null;
            
            if (currentStoredTargetId != null) {
                currentStoredTarget = Bukkit.getPlayer(currentStoredTargetId);
            }
            
            // Si hay un objetivo almacenado, verificar si sigue siendo válido
            if (currentStoredTarget != null && currentStoredTarget.isOnline()) {
                double distanceToOwner = currentStoredTarget.getLocation().distance(owner.getLocation());
                
                // Si el objetivo se alejó más de 10 bloques del dueño, dejar de atacarlo
                if (distanceToOwner > 10) {
                    currentAttackTargets.remove(endermiteId);
                    creature.setTarget(null);
                    currentStoredTarget = null;
                }
            } else {
                // El objetivo ya no es válido, limpiarlo
                currentAttackTargets.remove(endermiteId);
                currentStoredTarget = null;
            }
            
            // Si tenemos un objetivo válido, continuar atacándolo
            if (currentStoredTarget != null) {
                creature.setTarget(currentStoredTarget);
                return; // No seguir al dueño mientras esté atacando
            }
            
            // No hay objetivo current, buscar nuevos enemigos válidos
            Player newTarget = null;
            double closestDistance = Double.MAX_VALUE;
            
            for (Entity entity : endermite.getNearbyEntities(10, 10, 10)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    
                    // No atacar al dueño
                    if (!target.getUniqueId().equals(owner.getUniqueId())) {
                        double distanceToEndermite = endermite.getLocation().distance(target.getLocation());
                        double distanceToOwner = target.getLocation().distance(owner.getLocation());
                        
                        // El jugador debe estar dentro de 10 bloques del dueño para ser considerado
                        if (distanceToOwner <= 10 && distanceToEndermite < closestDistance) {
                            closestDistance = distanceToEndermite;
                            newTarget = target;
                        }
                    }
                }
            }
            
            // Si encontramos un nuevo objetivo, empezar a atacarlo
            if (newTarget != null) {
                currentAttackTargets.put(endermiteId, newTarget.getUniqueId());
                creature.setTarget(newTarget);
                return; // No seguir al dueño si encontramos un objetivo
            }
            
            // No hay objetivos que atacar, seguir al dueño
            double distanceToOwner = endermite.getLocation().distance(owner.getLocation());
            
            // Si el dueño está muy lejos (más de 10 bloques), teletransportar el endermite
            if (distanceToOwner > 10) {
                Location teleportLocation = owner.getLocation().clone();
                // Buscar una ubicación segura cerca del jugador
                teleportLocation = findSafeTeleportLocation(teleportLocation);
                endermite.teleport(teleportLocation);
                creature.setTarget(null); // Limpiar el objetivo después del tp
            } else if (distanceToOwner > 3.5) {
                // Si está lejos pero no muy lejos, seguir normalmente
                creature.setTarget(owner);
            } else {
                // Si está cerca, no seguir
                if (creature.getTarget() != null && creature.getTarget().equals(owner)) {
                    creature.setTarget(null);
                }
            }
            
        }, 0L, 5L); // Cada cuarto de segundo para mayor responsividad
        
        attackTasks.put(endermiteId, behaviorTask);
    }
    
    private Location findSafeTeleportLocation(Location ownerLocation) {
        Location teleportLocation = ownerLocation.clone();
        
        // Intentar encontrar un bloque sólido cerca del jugador
        for (int y = -2; y <= 2; y++) {
            Location testLocation = teleportLocation.clone().add(0, y, 0);
            if (testLocation.getBlock().getType().isSolid() && 
                testLocation.clone().add(0, 1, 0).getBlock().getType() == Material.AIR &&
                testLocation.clone().add(0, 2, 0).getBlock().getType() == Material.AIR) {
                return testLocation.add(0, 1, 0);
            }
        }
        
        // Si no encuentra un lugar seguro, usar la ubicación del jugador
        return ownerLocation;
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
        UUID playerId = player.getUniqueId();
        
        // Obtener la lista de endermites del jugador
        List<UUID> playerEndermiteList = playerEndermites.get(playerId);
        if (playerEndermiteList != null) {
            List<UUID> toRemove = new ArrayList<>(playerEndermiteList);
            
            for (UUID endermiteId : toRemove) {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getUniqueId().equals(endermiteId) && entity instanceof Endermite) {
                            entity.remove();
                            break;
                        }
                    }
                }
                cleanupEndermite(endermiteId);
            }
        }
        
        // Limpiar la lista del jugador
        playerEndermites.remove(playerId);
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

    public Player getEndermiteOwner(Endermite endermite) {
        if (endermite == null) {
            return null;
        }
        
        UUID endermiteId = endermite.getUniqueId();
        UUID ownerId = endermiteOwners.get(endermiteId);
        
        if (ownerId == null) {
            return null;
        }
        
        // Obtener el jugador usando el UUID
        Player owner = Bukkit.getPlayer(ownerId);
        
        // Verificar que el jugador esté conectado
        if (owner == null || !owner.isOnline()) {
            return null;
        }
        
        return owner;
    }
    
    private void cleanupEndermite(UUID endermiteId) {
        // Obtener el dueño del endermite
        UUID ownerId = endermiteOwners.remove(endermiteId);
        
        // Remover de la lista del jugador si existe
        if (ownerId != null) {
            List<UUID> playerEndermiteList = playerEndermites.get(ownerId);
            if (playerEndermiteList != null) {
                playerEndermiteList.remove(endermiteId);
                if (playerEndermiteList.isEmpty()) {
                    playerEndermites.remove(ownerId);
                }
            }
        }
        
        currentAttackTargets.remove(endermiteId);
        
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
    
    // Método público para limpiar todos los endermites (llamado al apagar el servidor)
    public void cleanupAllEndermites() {
        // Remover todos los endermites del mundo
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Endermite) {
                    UUID endermiteId = entity.getUniqueId();
                    if (endermiteOwners.containsKey(endermiteId)) {
                        entity.remove();
                    }
                }
            }
        }
        
        // Cancelar todas las tareas
        for (BukkitTask task : endermiteTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        
        for (BukkitTask task : followTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        
        for (BukkitTask task : attackTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        
        // Limpiar todos los mapas
        endermiteOwners.clear();
        playerEndermites.clear();
        endermiteTasks.clear();
        followTasks.clear();
        attackTasks.clear();
        currentAttackTargets.clear();
    }
}
