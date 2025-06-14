package mk.kvlzx.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.endermite.CustomEndermite;
import net.minecraft.server.v1_8_R3.WorldServer;

public class EndermiteListener implements Listener {

    private final MysthicKnockBack plugin;
    private final Map<UUID, CustomEndermite> playerEndermites = new HashMap<>();
    
    public EndermiteListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEndermiteSpawn(CreatureSpawnEvent event) {
        // Cancelar el spawn default de un endermite
        if (event.getEntityType() == EntityType.ENDERMITE && 
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT) {
            
            event.setCancelled(true);
            
            // Encontrar el jugador más cerca (el que tiro la perla)
            Player owner = findNearestPlayer(event.getLocation());
            if (owner != null) {
                // Remover el endermite anterior (si existe)
                removeExistingEndermite(owner);
                
                // Spawnear el endermite
                spawnCustomEndermite(event.getLocation(), owner);
            }
        }
    }
    
    private Player findNearestPlayer(Location location) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(location);
            if (distance < minDistance && distance <= 10) { // Maximum 10 blocks distance
                minDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }
    
    private void spawnCustomEndermite(Location location, Player owner) {
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        CustomEndermite customEndermite = new CustomEndermite(nmsWorld, owner);
        
        // Setear la location
        customEndermite.setLocation(location.getX(), location.getY(), location.getZ(), 
            location.getYaw(), location.getPitch());
        
        // Agregar al mundo el endermite
        nmsWorld.addEntity(customEndermite);
        
        // Guardar la referencia
        playerEndermites.put(owner.getUniqueId(), customEndermite);
        
        // Hacer que el dueño se suba al endermite
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (customEndermite.isAlive() && owner.isOnline()) {
                customEndermite.getBukkitEntity().setPassenger(owner);
            }
        }, 1L);
        
        // Eliminar el endermite luego de 30 segundos
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeEndermite(owner, customEndermite);
        }, 600L); // 30 segundos
    }
    
    private void removeExistingEndermite(Player player) {
        CustomEndermite existingEndermite = playerEndermites.get(player.getUniqueId());
        if (existingEndermite != null) {
            removeEndermite(player, existingEndermite);
        }
    }
    
    private void removeEndermite(Player player, CustomEndermite endermite) {
        if (endermite != null && endermite.isAlive()) {
            // Bajar al dueño del endermite si estaba arriba
            if (endermite.getBukkitEntity().getPassenger() != null) {
                endermite.getBukkitEntity().setPassenger(null);
            }
            endermite.die();
        }
        playerEndermites.remove(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeExistingEndermite(player);
    }
    
    public CustomEndermite getPlayerEndermite(Player player) {
        return playerEndermites.get(player.getUniqueId());
    }
    
    public boolean hasEndermite(Player player) {
        CustomEndermite endermite = playerEndermites.get(player.getUniqueId());
        return endermite != null && endermite.isAlive();
    }
}
