package mk.kvlzx.powerup;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class BlackHoleEntity {
    private final MysthicKnockBack plugin;
    private final Location location;
    private final Player thrower;
    private ArmorStand blackHoleStand;
    private Item blackHoleItem;
    private BukkitRunnable attractionTask;
    private BukkitRunnable repulsionTask;
    private BukkitRunnable animationTask;
    private BukkitRunnable effectTask;
    private boolean isActive = false;
    private boolean isRepulsing = false;
    
    public BlackHoleEntity(MysthicKnockBack plugin, Location location, Player thrower) {
        this.plugin = plugin;
        this.location = location.clone();
        this.thrower = thrower;
    }
    
    /**
     * Spawna el agujero negro
     */
    public void spawn() {
        if (location.getWorld() == null) return;
        
        // Crear el ArmorStand elevado
        Location standLocation = location.clone().add(0, 2, 0);
        blackHoleStand = (ArmorStand) location.getWorld().spawnEntity(standLocation, EntityType.ARMOR_STAND);
        blackHoleStand.setVisible(false);
        blackHoleStand.setGravity(false);
        blackHoleStand.setCanPickupItems(false);
        blackHoleStand.setMarker(true);
        blackHoleStand.setSmall(true);
        blackHoleStand.setArms(false);
        blackHoleStand.setBasePlate(false);
        
        // Crear el item visual del agujero negro
        ItemStack blackHoleItemStack = new ItemStack(Material.OBSIDIAN);
        blackHoleItem = location.getWorld().dropItem(standLocation, blackHoleItemStack);
        blackHoleItem.setPickupDelay(Integer.MAX_VALUE);
        blackHoleItem.setVelocity(new Vector(0, 0, 0));
        blackHoleItem.setCustomName(MessageUtils.getColor(plugin.getMainConfig().getPowerUpBlackHoleName()));
        blackHoleItem.setCustomNameVisible(true);
        
        // Montar el item al ArmorStand
        blackHoleStand.setPassenger(blackHoleItem);
        
        isActive = true;
        
        // Iniciar todas las tareas
        startAnimationTask();
        startEffectTask();
        startAttractionPhase();
        
        // Sonido de spawn
        location.getWorld().playSound(location, Sound.ENDERDRAGON_GROWL, 2.0f, 0.5f);
    }
    
    /**
     * Inicia la animación del agujero negro
     */
    private void startAnimationTask() {
        animationTask = new BukkitRunnable() {
            private float yaw = 0;
            
            @Override
            public void run() {
                if (!isActive || blackHoleStand == null || blackHoleStand.isDead()) {
                    cancel();
                    return;
                }
                
                // Rotar el ArmorStand
                Location newLoc = blackHoleStand.getLocation();
                newLoc.setYaw(yaw);
                blackHoleStand.teleport(newLoc);
                
                yaw += 10;
                if (yaw >= 360) {
                    yaw = 0;
                }
            }
        };
        
        animationTask.runTaskTimer(plugin, 0L, 2L);
    }
    
    /**
     * Inicia los efectos visuales del agujero negro
     */
    private void startEffectTask() {
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || blackHoleStand == null || blackHoleStand.isDead()) {
                    cancel();
                    return;
                }
                
                // Location effectLoc = blackHoleStand.getLocation();
                
                if (isRepulsing) {
                    // Efectos de repulsión
                    // effectLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, effectLoc, 3);
                    // effectLoc.getWorld().spawnParticle(Particle.FLAME, effectLoc, 20, 1, 1, 1, 0.1);
                } else {
                    // Efectos de atracción
                    // effectLoc.getWorld().spawnParticle(Particle.PORTAL, effectLoc, 30, 2, 2, 2, 0.5);
                    // effectLoc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, effectLoc, 10, 1, 1, 1, 0.1);
                    // effectLoc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, effectLoc, 15, 2, 2, 2, 0.3);
                }
            }
        };
        
        effectTask.runTaskTimer(plugin, 0L, 5L);
    }
    
    /**
     * Inicia la fase de atracción
     */
    private void startAttractionPhase() {
        attractionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || blackHoleStand == null || blackHoleStand.isDead()) {
                    cancel();
                    return;
                }
                
                Location blackHoleLocation = blackHoleStand.getLocation();
                double attractionRadius = plugin.getMainConfig().getPowerUpBlackHoleAttractionRadius();
                double attractionForce = plugin.getMainConfig().getPowerUpBlackHoleAttractionForce();
                
                // Atraer jugadores (excepto el que lanzó el agujero negro)
                for (Player player : blackHoleLocation.getWorld().getPlayers()) {
                    if (player.equals(thrower)) continue;
                    
                    double distance = player.getLocation().distance(blackHoleLocation);
                    if (distance <= attractionRadius) {
                        Vector direction = blackHoleLocation.toVector().subtract(player.getLocation().toVector());
                        direction.normalize();
                        
                        // Fuerza inversamente proporcional a la distancia
                        double force = attractionForce / Math.max(distance, 1);
                        direction.multiply(force);
                        
                        // Aplicar velocidad
                        player.setVelocity(direction);
                        
                        // Sonido de atracción
                        if (Math.random() < 0.3) {
                            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 0.5f, 2.0f);
                        }
                    }
                }
            }
        };
        
        attractionTask.runTaskTimer(plugin, 0L, 3L);
        
        // Programar el cambio a la fase de repulsión
        new BukkitRunnable() {
            @Override
            public void run() {
                startRepulsionPhase();
            }
        }.runTaskLater(plugin, 20L * plugin.getMainConfig().getPowerUpBlackHoleAttractionDuration());
    }
    
    /**
     * Inicia la fase de repulsión
     */
    private void startRepulsionPhase() {
        if (attractionTask != null) {
            attractionTask.cancel();
        }
        
        isRepulsing = true;
        
        // Sonido de cambio de fase
        location.getWorld().playSound(location, Sound.EXPLODE, 2.0f, 0.8f);
        
        repulsionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || blackHoleStand == null || blackHoleStand.isDead()) {
                    cancel();
                    return;
                }
                
                Location blackHoleLocation = blackHoleStand.getLocation();
                double repulsionRadius = plugin.getMainConfig().getPowerUpBlackHoleRepulsionForce();
                double repulsionForce = plugin.getMainConfig().getPowerUpBlackHoleRepulsionForce();
                
                // Repeler jugadores (excepto el que lanzó el agujero negro)
                for (Player player : blackHoleLocation.getWorld().getPlayers()) {
                    if (player.equals(thrower)) continue;
                    
                    double distance = player.getLocation().distance(blackHoleLocation);
                    if (distance <= repulsionRadius) {
                        Vector direction = player.getLocation().toVector().subtract(blackHoleLocation.toVector());
                        direction.normalize();
                        
                        // Fuerza de repulsión
                        direction.multiply(repulsionForce);
                        direction.setY(Math.max(direction.getY(), 0.5)); // Asegurar que vayan hacia arriba
                        
                        // Aplicar velocidad
                        player.setVelocity(direction);
                        
                        // Sonido de repulsión
                        if (Math.random() < 0.4) {
                            player.playSound(player.getLocation(), Sound.EXPLODE, 0.8f, 1.2f);
                        }
                    }
                }
            }
        };
        
        repulsionTask.runTaskTimer(plugin, 0L, 2L);
        
        // Programar la destrucción del agujero negro
        new BukkitRunnable() {
            @Override
            public void run() {
                destroy();
            }
        }.runTaskLater(plugin, 20L * plugin.getMainConfig().getPowerUpBlackHoleRepulsionDuration());
    }
    
    /**
     * Destruye el agujero negro
     */
    public void destroy() {
        if (!isActive) return;
        isActive = false;
        
        // Cancelar todas las tareas
        if (animationTask != null) {
            animationTask.cancel();
        }
        if (effectTask != null) {
            effectTask.cancel();
        }
        if (attractionTask != null) {
            attractionTask.cancel();
        }
        if (repulsionTask != null) {
            repulsionTask.cancel();
        }
        
        // Efecto final
        if (blackHoleStand != null && !blackHoleStand.isDead()) {
            Location finalLoc = blackHoleStand.getLocation();
            finalLoc.getWorld().playSound(finalLoc, Sound.EXPLODE, 3.0f, 0.5f);
        }
        
        // Remover entidades
        if (blackHoleItem != null && blackHoleItem.isValid()) {
            blackHoleItem.remove();
        }
        if (blackHoleStand != null && !blackHoleStand.isDead()) {
            blackHoleStand.remove();
        }
    }
    
    // Getters
    public boolean isActive() {
        return isActive;
    }
    
    public Player getThrower() {
        return thrower;
    }
    
    public Location getLocation() {
        return location.clone();
    }
}