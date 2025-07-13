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
        plugin.getLogger().info("DEBUG: BlackHoleProjectile.launch() iniciado");
        
        Location startLocation = thrower.getEyeLocation();
        Vector direction = thrower.getLocation().getDirection();
        
        plugin.getLogger().info("DEBUG: Ubicación de inicio: " + startLocation);
        plugin.getLogger().info("DEBUG: Dirección: " + direction);
        
        // Crear el item proyectil
        String materialId = plugin.getMainConfig().getPowerUpBlackHoleItemId();
        plugin.getLogger().info("DEBUG: Creando item proyectil con material: " + materialId);
        
        try {
            ItemStack projectileItemStack = new ItemStack(Material.valueOf(materialId));
            projectileItem = thrower.getWorld().dropItem(startLocation, projectileItemStack);
            projectileItem.setPickupDelay(Integer.MAX_VALUE);
            projectileItem.setVelocity(direction.multiply(1.5));
            
            plugin.getLogger().info("DEBUG: Item proyectil creado exitosamente");
            plugin.getLogger().info("DEBUG: Velocidad inicial: " + projectileItem.getVelocity());
            
            // Iniciar la tarea del proyectil
            startProjectileTask();
        } catch (Exception e) {
            plugin.getLogger().severe("DEBUG: Error al crear item proyectil: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Inicia la tarea que controla el movimiento del proyectil
     */
    private void startProjectileTask() {
        plugin.getLogger().info("DEBUG: Iniciando tarea del proyectil");
        
        projectileTask = new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (hasLanded) {
                    plugin.getLogger().info("DEBUG: Proyectil ya ha aterrizado, cancelando tarea");
                    cancel();
                    return;
                }
                
                if (projectileItem == null) {
                    plugin.getLogger().info("DEBUG: projectileItem es null, cancelando tarea");
                    cancel();
                    return;
                }
                
                if (!projectileItem.isValid()) {
                    plugin.getLogger().info("DEBUG: projectileItem no es válido, cancelando tarea");
                    cancel();
                    return;
                }
                
                ticks++;
                
                if (ticks % 10 == 0) { // Log cada 10 ticks para no spam
                    plugin.getLogger().info("DEBUG: Proyectil tick " + ticks + ", ubicación: " + projectileItem.getLocation());
                    plugin.getLogger().info("DEBUG: Velocidad actual: " + projectileItem.getVelocity());
                    plugin.getLogger().info("DEBUG: En el suelo: " + projectileItem.isOnGround());
                }
                
                // Verificar si ha tocado el suelo o ha pasado mucho tiempo
                if (projectileItem.isOnGround() || ticks > 100) {
                    plugin.getLogger().info("DEBUG: Proyectil debe aterrizar - En suelo: " + projectileItem.isOnGround() + ", Ticks: " + ticks);
                    landProjectile();
                    cancel();
                    return;
                }
                
                // Reducir la velocidad gradualmente
                Vector velocity = projectileItem.getVelocity();
                velocity.multiply(0.98);
                projectileItem.setVelocity(velocity);
            }
        };
        
        projectileTask.runTaskTimer(plugin, 0L, 1L);
        plugin.getLogger().info("DEBUG: Tarea del proyectil iniciada");
    }
    
    /**
     * Maneja cuando el proyectil aterriza
     */
    private void landProjectile() {
        plugin.getLogger().info("DEBUG: landProjectile() llamado");
        
        if (hasLanded) {
            plugin.getLogger().info("DEBUG: Ya ha aterrizado, saliendo");
            return;
        }
        
        hasLanded = true;
        plugin.getLogger().info("DEBUG: Marcando como aterrizado");
        
        Location landLocation = projectileItem.getLocation();
        plugin.getLogger().info("DEBUG: Ubicación de aterrizaje: " + landLocation);
        
        // Remover el item proyectil
        projectileItem.remove();
        plugin.getLogger().info("DEBUG: Item proyectil removido");
        
        // Crear el agujero negro en la ubicación
        plugin.getLogger().info("DEBUG: Creando entidad de agujero negro");
        BlackHoleEntity blackHole = new BlackHoleEntity(plugin, landLocation, thrower);
        blackHole.spawn();
        plugin.getLogger().info("DEBUG: Entidad de agujero negro creada");
        
        // Remover el metadata del jugador después de que el agujero negro termine
        int totalDuration = plugin.getMainConfig().getPowerUpBlackHoleAttractionDuration() + 
                            plugin.getMainConfig().getPowerUpBlackHoleRepulsionDuration() + 2;
        plugin.getLogger().info("DEBUG: Programando remoción de metadata en " + totalDuration + " segundos");
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (thrower.hasMetadata("blackhole_active")) {
                    thrower.removeMetadata("blackhole_active", plugin);
                    plugin.getLogger().info("DEBUG: Metadata 'blackhole_active' removido del jugador");
                } else {
                    plugin.getLogger().info("DEBUG: Jugador no tenía metadata 'blackhole_active'");
                }
            }
        }.runTaskLater(plugin, 20L * totalDuration);
    }
    
    /**
     * Cancela el proyectil
     */
    public void cancel() {
        plugin.getLogger().info("DEBUG: BlackHoleProjectile.cancel() llamado");
        
        if (projectileTask != null) {
            projectileTask.cancel();
            plugin.getLogger().info("DEBUG: Tarea del proyectil cancelada");
        }
        
        if (projectileItem != null && projectileItem.isValid()) {
            projectileItem.remove();
            plugin.getLogger().info("DEBUG: Item proyectil removido en cancel()");
        }
    }
}