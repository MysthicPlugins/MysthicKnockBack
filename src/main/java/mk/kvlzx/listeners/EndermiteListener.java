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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
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
    
    public EndermiteListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEndermiteSpawn(CreatureSpawnEvent event) {
        EntityType entityType = event.getEntityType();
        SpawnReason spawnReason = event.getSpawnReason();
        
        // Comprobar si el Endermite es generado por ender pearl
        if (entityType == EntityType.ENDERMITE) {
            Endermite endermite = (Endermite) event.getEntity();
            
            // Buscar al jugador más cercano (quien lanzó la ender pearl)
            Player owner = findNearestPlayer(endermite.getLocation());
            if (owner != null) {
                owner.sendMessage("Razon de spawneo : " + spawnReason.name());
                setupEndermitePet(endermite, owner);
            }
        }
    }
    
    private Player findNearestPlayer(Location location) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(location);
            if (distance < minDistance && distance <= 10) { // Máximo 10 bloques de distancia
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
        endermite.setHealth(20.0); // Salud completa
        endermite.setMaxHealth(20.0); // Salud máxima
        endermite.setLeashHolder(owner);
        endermite.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false)); // Efecto de velocidad
        
        // Montar al jugador en el endermite
        endermite.setPassenger(owner);
        
        // Iniciar el sistema de countdown y nombre
        startEndermiteCountdown(endermite, owner);
        
        // Iniciar el comportamiento de ataque
        startAttackBehavior(endermite, owner);
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
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!endermite.isValid() || endermite.isDead()) {
                return;
            }
            
            for (Entity entity : endermite.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    
                    // No atacar al dueño
                    if (!target.getUniqueId().equals(owner.getUniqueId())) {
                        // Hacer que el endermite ataque al jugador
                        if (endermite instanceof Creature) {
                            ((Creature) endermite).setTarget(target);
                        }
                        break; // Solo atacar a un jugador a la vez
                    }
                }
            }
        }, 0L, 20L); // Cada segundo
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Endermite) {
            UUID endermiteId = event.getEntity().getUniqueId();
            cleanupEndermite(endermiteId);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remover endermites del jugador que se desconecta
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : endermiteOwners.entrySet()) {
            if (entry.getValue().equals(player.getUniqueId())) {
                // Buscar el endermite en todos los mundos
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
        
        // Limpiar las referencias
        for (UUID endermiteId : toRemove) {
            cleanupEndermite(endermiteId);
        }
    }
    
    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getVehicle() instanceof Endermite && event.getExited() instanceof Player) {
            Endermite endermite = (Endermite) event.getVehicle();
            Player player = (Player) event.getExited();
            
            // Verificar si es el dueño del endermite
            UUID endermiteId = endermite.getUniqueId();
            UUID ownerId = endermiteOwners.get(endermiteId);
            
            if (ownerId != null && ownerId.equals(player.getUniqueId())) {
                // Permitir que el jugador se baje, pero el endermite sigue siendo suyo
                // Opcional: Se puede hacer que el endermite siga al jugador
                followOwner(endermite, player);
            }
        }
    }
    
    private void followOwner(Endermite endermite, Player owner) {
        UUID endermiteId = endermite.getUniqueId();
        
        BukkitTask followTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (!endermite.isValid() || endermite.isDead() || !owner.isOnline()) {
                    BukkitTask task = followTasks.remove(endermiteId);
                    if (task != null) {
                        task.cancel();
                    }
                    return;
                }
                
                // Si el endermite está lejos del dueño, teletransportarlo
                Location ownerLoc = owner.getLocation();
                Location endermiteLoc = endermite.getLocation();
                
                if (ownerLoc.distance(endermiteLoc) > 10) {
                    Location teleportLoc = ownerLoc.clone().add(
                        (Math.random() - 0.5) * 4, // Posición aleatoria cerca del jugador
                        1,
                        (Math.random() - 0.5) * 4
                    );
                    endermite.teleport(teleportLoc);
                }
            }
        }, 0L, 40L); // Cada 2 segundos
        
        followTasks.put(endermiteId, followTask);
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
    }
}
