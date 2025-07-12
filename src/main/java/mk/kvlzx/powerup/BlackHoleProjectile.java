package mk.kvlzx.powerup;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mk.kvlzx.MysthicKnockBack;

public class BlackHoleProjectile {
    private final MysthicKnockBack plugin;
    private final Player thrower;
    private Item projectileItem;
    private BukkitRunnable projectileTask;
    private boolean hasLanded = false;
    
    public BlackHoleProjectile(MysthicKnockBack plugin, Player thrower) {
        this.plugin = plugin;
        this.thrower = thrower;
    }
    
    /**
     * Lanza el proyectil de agujero negro
     */
    public void launch() {
        Location startLocation = thrower.getEyeLocation();
        Vector direction = thrower.getLocation().getDirection();
        
        // Crear el item proyectil
        ItemStack projectileItemStack = new ItemStack(Material.valueOf(plugin.getMainConfig().getPowerUpBlackHoleItemId()));
        projectileItem = thrower.getWorld().dropItem(startLocation, projectileItemStack);
        projectileItem.setPickupDelay(Integer.MAX_VALUE);
        projectileItem.setVelocity(direction.multiply(1.5));
        
        // Iniciar la tarea del proyectil
        startProjectileTask();
    }
    
    /**
     * Inicia la tarea que controla el movimiento del proyectil
     */
    private void startProjectileTask() {
        projectileTask = new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (hasLanded || projectileItem == null || !projectileItem.isValid()) {
                    cancel();
                    return;
                }
                
                ticks++;
                
                // Verificar si ha tocado el suelo o ha pasado mucho tiempo
                if (projectileItem.isOnGround() || ticks > 100) {
                    landProjectile();
                    cancel();
                }
                
                // Reducir la velocidad gradualmente
                Vector velocity = projectileItem.getVelocity();
                velocity.multiply(0.98);
                projectileItem.setVelocity(velocity);
            }
        };
        
        projectileTask.runTaskTimer(plugin, 0L, 1L);
    }
    
    /**
     * Maneja cuando el proyectil aterriza
     */
    private void landProjectile() {
        if (hasLanded) return;
        hasLanded = true;
        
        Location landLocation = projectileItem.getLocation();
        
        // Remover el item proyectil
        projectileItem.remove();
        
        // Crear el agujero negro en la ubicación
        BlackHoleEntity blackHole = new BlackHoleEntity(plugin, landLocation, thrower);
        blackHole.spawn();
        
        // Remover el metadata del jugador después de que el agujero negro termine
        new BukkitRunnable() {
            @Override
            public void run() {
                if (thrower.hasMetadata("blackhole_active")) {
                    thrower.removeMetadata("blackhole_active", plugin);
                }
            }
        }.runTaskLater(plugin, 20L * (plugin.getMainConfig().getPowerUpBlackHoleAttractionDuration() + 
                                        plugin.getMainConfig().getPowerUpBlackHoleRepulsionDuration() + 2));
    }
    
    /**
     * Cancela el proyectil
     */
    public void cancel() {
        if (projectileTask != null) {
            projectileTask.cancel();
        }
        
        if (projectileItem != null && projectileItem.isValid()) {
            projectileItem.remove();
        }
    }
}
