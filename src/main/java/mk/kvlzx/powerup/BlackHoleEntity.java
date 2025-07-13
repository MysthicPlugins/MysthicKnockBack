package mk.kvlzx.powerup;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.WorldServer;

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
    private BukkitRunnable itemPreservationTask;
    private boolean isActive = false;
    private boolean isRepulsing = false;
    private final String itemIdentifier;
    
    public BlackHoleEntity(MysthicKnockBack plugin, Location location, Player thrower) {
        this.plugin = plugin;
        this.location = location.clone();
        this.thrower = thrower;
        this.itemIdentifier = "BLACKHOLE_ITEM_" + System.currentTimeMillis();
        
        // Debug: Información de creación
        plugin.getLogger().info("§a[BlackHole DEBUG] Creando BlackHoleEntity:");
        plugin.getLogger().info("§a[BlackHole DEBUG] - Location: " + location.toString());
        plugin.getLogger().info("§a[BlackHole DEBUG] - Thrower: " + thrower.getName());
        plugin.getLogger().info("§a[BlackHole DEBUG] - World: " + (location.getWorld() != null ? location.getWorld().getName() : "NULL"));
    }
    
    /**
     * Spawna el agujero negro
     */
    public void spawn() {
        plugin.getLogger().info("§a[BlackHole DEBUG] Intentando spawnear agujero negro...");
        
        if (location.getWorld() == null) {
            plugin.getLogger().warning("§c[BlackHole DEBUG] World es null, no se puede spawnear!");
            return;
        }
        
        plugin.getLogger().info("§a[BlackHole DEBUG] World válido, procediendo con spawn...");
        
        // Crear el ArmorStand elevado
        Location standLocation = location.clone().add(0, 2, 0);
        plugin.getLogger().info("§a[BlackHole DEBUG] Spawneando ArmorStand en: " + standLocation.toString());
        
        try {
            blackHoleStand = (ArmorStand) location.getWorld().spawnEntity(standLocation, EntityType.ARMOR_STAND);
            blackHoleStand.setVisible(false);
            blackHoleStand.setGravity(false);
            blackHoleStand.setCanPickupItems(false);
            blackHoleStand.setMarker(true);
            blackHoleStand.setSmall(true);
            blackHoleStand.setArms(false);
            blackHoleStand.setBasePlate(false);
            
            plugin.getLogger().info("§a[BlackHole DEBUG] ArmorStand creado exitosamente!");
            
            // Crear el item visual del agujero negro
            ItemStack blackHoleItemStack = new ItemStack(Material.OBSIDIAN);
            plugin.getLogger().info("§a[BlackHole DEBUG] Creando item de obsidiana...");
            
            blackHoleItem = location.getWorld().dropItem(standLocation, blackHoleItemStack);
            blackHoleItem.setPickupDelay(Integer.MAX_VALUE);
            blackHoleItem.setVelocity(new Vector(0, 0, 0));
            blackHoleItem.setTicksLived(1);
            blackHoleItem.setCustomName(MessageUtils.getColor(plugin.getMainConfig().getPowerUpBlackHoleName()));
            blackHoleItem.setCustomNameVisible(true);
            
            // Metadata para identificación
            blackHoleItem.setMetadata("BLACKHOLE_PROTECTED", new FixedMetadataValue(plugin, true));
            blackHoleItem.setMetadata("BLACKHOLE_ID", new FixedMetadataValue(plugin, itemIdentifier));
            
            plugin.getLogger().info("§a[BlackHole DEBUG] Item creado, intentando montar al ArmorStand...");
            
            // Montar el item al ArmorStand usando NMS
            if (mountItemToArmorStand(blackHoleStand, blackHoleItem)) {
                plugin.getLogger().info("§a[BlackHole DEBUG] Item montado exitosamente!");
            } else {
                plugin.getLogger().warning("§c[BlackHole DEBUG] Fallo al montar item al ArmorStand!");
            }
            
            isActive = true;
            
            // Iniciar todas las tareas
            plugin.getLogger().info("§a[BlackHole DEBUG] Iniciando tareas...");
            startAnimationTask();
            startEffectTask();
            startItemPreservationTask();
            startAttractionPhase();
            
            // Sonido de spawn
            location.getWorld().playSound(location, Sound.ENDERDRAGON_GROWL, 2.0f, 0.5f);
            plugin.getLogger().info("§a[BlackHole DEBUG] Agujero negro spawneado completamente!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("§c[BlackHole DEBUG] Error al spawnear agujero negro: " + e.getMessage());
            e.printStackTrace();
            // Cleanup en caso de error
            cleanup();
        }
    }
    
    /**
     * Monta el item al ArmorStand usando NMS (copiado de PowerUp)
     */
    private boolean mountItemToArmorStand(ArmorStand armorStand, Item item) {
        try {
            plugin.getLogger().info("§a[BlackHole DEBUG] Intentando montar item usando NMS...");
            
            Entity nmsArmorStand = ((CraftEntity) armorStand).getHandle();
            Entity nmsItem = ((CraftEntity) item).getHandle();
            
            if (nmsArmorStand.passenger != null) {
                plugin.getLogger().warning("§c[BlackHole DEBUG] ArmorStand ya tiene un pasajero!");
                return false;
            }
            
            nmsItem.mount(nmsArmorStand);
            
            if (nmsArmorStand.passenger == nmsItem) {
                plugin.getLogger().info("§a[BlackHole DEBUG] Montado exitosamente, actualizando para jugadores cercanos...");
                updateMountForNearbyPlayers(nmsArmorStand);
                return true;
            } else {
                plugin.getLogger().warning("§c[BlackHole DEBUG] Fallo al montar - passenger no coincide!");
                return false;
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("§c[BlackHole DEBUG] Error mounting item to ArmorStand: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Actualiza el montaje para jugadores cercanos (copiado de PowerUp)
     */
    private void updateMountForNearbyPlayers(Entity nmsArmorStand) {
        try {
            WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
            
            if (nmsArmorStand.passenger != null) {
                PacketPlayOutAttachEntity attachPacket = new PacketPlayOutAttachEntity(
                    0, nmsArmorStand.passenger, nmsArmorStand
                );
                
                int playersUpdated = 0;
                for (EntityHuman entityHuman : nmsWorld.players) {
                    if (entityHuman instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) entityHuman;
                        if (player.getBukkitEntity().getLocation().distance(location) <= 64) {
                            player.playerConnection.sendPacket(attachPacket);
                            playersUpdated++;
                        }
                    }
                }
                plugin.getLogger().info("§a[BlackHole DEBUG] Paquete de montaje enviado a " + playersUpdated + " jugadores");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("§c[BlackHole DEBUG] Error updating mount for nearby players: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tarea de preservación del item (copiado de PowerUp)
     */
    private void startItemPreservationTask() {
        plugin.getLogger().info("§a[BlackHole DEBUG] Iniciando tarea de preservación del item...");
        
        itemPreservationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || blackHoleItem == null || !blackHoleItem.isValid()) {
                    plugin.getLogger().info("§a[BlackHole DEBUG] Cancelando tarea de preservación - item inválido");
                    cancel();
                    return;
                }

                // Mantener el item "fresco"
                blackHoleItem.setTicksLived(1);
                
                // Asegurar que no se pueda recoger
                if (blackHoleItem.getPickupDelay() != Integer.MAX_VALUE) {
                    blackHoleItem.setPickupDelay(Integer.MAX_VALUE);
                }
                
                // Mantener el nombre personalizado
                if (blackHoleItem.getCustomName() == null || !blackHoleItem.getCustomName().contains(itemIdentifier)) {
                    blackHoleItem.setCustomName(MessageUtils.getColor(plugin.getMainConfig().getPowerUpBlackHoleName()));
                    blackHoleItem.setCustomNameVisible(true);
                }
                
                // Mantener metadata
                if (!blackHoleItem.hasMetadata("BLACKHOLE_PROTECTED")) {
                    blackHoleItem.setMetadata("BLACKHOLE_PROTECTED", new FixedMetadataValue(plugin, true));
                    blackHoleItem.setMetadata("BLACKHOLE_ID", new FixedMetadataValue(plugin, itemIdentifier));
                }
                
                // Mantener velocidad cero
                if (blackHoleItem.getVelocity().length() > 0.1) {
                    blackHoleItem.setVelocity(new Vector(0, 0, 0));
                }
                
                // Verificar montaje
                if (blackHoleItem.getVehicle() != blackHoleStand) {
                    plugin.getLogger().warning("§c[BlackHole DEBUG] Item desmontado, remontando...");
                    refreshMount();
                }
            }
        };
        
        itemPreservationTask.runTaskTimer(plugin, 0L, 20L);
    }
    
    /**
     * Refresca el montaje del item (copiado de PowerUp)
     */
    private void refreshMount() {
        if (blackHoleStand == null || blackHoleStand.isDead() || blackHoleItem == null || !blackHoleItem.isValid()) {
            plugin.getLogger().warning("§c[BlackHole DEBUG] No se puede refrescar montaje - entidades inválidas");
            return;
        }
        
        try {
            if (blackHoleItem.getVehicle() != blackHoleStand) {
                Entity nmsArmorStand = ((CraftEntity) blackHoleStand).getHandle();
                Entity nmsItem = ((CraftEntity) blackHoleItem).getHandle();
                
                nmsItem.mount(nmsArmorStand);
                updateMountForNearbyPlayers(nmsArmorStand);
                plugin.getLogger().info("§a[BlackHole DEBUG] Montaje refrescado exitosamente");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("§c[BlackHole DEBUG] Error refreshing mount: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Inicia la animación del agujero negro
     */
    private void startAnimationTask() {
        plugin.getLogger().info("§a[BlackHole DEBUG] Iniciando tarea de animación...");
        
        animationTask = new BukkitRunnable() {
            private float yaw = 0;
            private int tickCounter = 0;
            
            @Override
            public void run() {
                if (!isActive || blackHoleStand == null || blackHoleStand.isDead()) {
                    plugin.getLogger().info("§a[BlackHole DEBUG] Cancelando animación - agujero negro inactivo");
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
                
                tickCounter++;
                
                // Refrescar montaje periódicamente
                if (tickCounter % 60 == 0) {
                    refreshMount();
                }
            }
        };
        
        animationTask.runTaskTimer(plugin, 0L, 2L);
    }
    
    /**
     * Inicia los efectos visuales del agujero negro
     */
    private void startEffectTask() {
        plugin.getLogger().info("§a[BlackHole DEBUG] Iniciando tarea de efectos...");
        
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || blackHoleStand == null || blackHoleStand.isDead()) {
                    plugin.getLogger().info("§a[BlackHole DEBUG] Cancelando efectos - agujero negro inactivo");
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
        plugin.getLogger().info("§a[BlackHole DEBUG] Iniciando fase de atracción...");
        plugin.getLogger().info("§a[BlackHole DEBUG] - Radio de atracción: " + plugin.getMainConfig().getPowerUpBlackHoleAttractionRadius());
        plugin.getLogger().info("§a[BlackHole DEBUG] - Fuerza de atracción: " + plugin.getMainConfig().getPowerUpBlackHoleAttractionForce());
        plugin.getLogger().info("§a[BlackHole DEBUG] - Duración: " + plugin.getMainConfig().getPowerUpBlackHoleAttractionDuration() + " segundos");
        
        attractionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || blackHoleStand == null || blackHoleStand.isDead()) {
                    plugin.getLogger().info("§a[BlackHole DEBUG] Cancelando atracción - agujero negro inactivo");
                    cancel();
                    return;
                }
                
                Location blackHoleLocation = blackHoleStand.getLocation();
                double attractionRadius = plugin.getMainConfig().getPowerUpBlackHoleAttractionRadius();
                double attractionForce = plugin.getMainConfig().getPowerUpBlackHoleAttractionForce();
                
                int playersAffected = 0;
                
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
                        playersAffected++;
                        
                        // Sonido de atracción
                        if (Math.random() < 0.3) {
                            player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 0.5f, 2.0f);
                        }
                    }
                }
                
                // Debug cada 20 ticks (1 segundo)
                if (System.currentTimeMillis() % 1000 < 50) {
                    plugin.getLogger().info("§a[BlackHole DEBUG] Atracción activa - Jugadores afectados: " + playersAffected);
                }
            }
        };
        
        attractionTask.runTaskTimer(plugin, 0L, 3L);
        
        // Programar el cambio a la fase de repulsión
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("§a[BlackHole DEBUG] Cambiando a fase de repulsión...");
                startRepulsionPhase();
            }
        }.runTaskLater(plugin, 20L * plugin.getMainConfig().getPowerUpBlackHoleAttractionDuration());
    }
    
    /**
     * Inicia la fase de repulsión
     */
    private void startRepulsionPhase() {
        plugin.getLogger().info("§a[BlackHole DEBUG] Iniciando fase de repulsión...");
        
        if (attractionTask != null) {
            attractionTask.cancel();
            plugin.getLogger().info("§a[BlackHole DEBUG] Tarea de atracción cancelada");
        }
        
        isRepulsing = true;
        
        // Sonido de cambio de fase
        location.getWorld().playSound(location, Sound.EXPLODE, 2.0f, 0.8f);
        
        plugin.getLogger().info("§a[BlackHole DEBUG] - Radio de repulsión: " + plugin.getMainConfig().getPowerUpBlackHoleRepulsionForce());
        plugin.getLogger().info("§a[BlackHole DEBUG] - Fuerza de repulsión: " + plugin.getMainConfig().getPowerUpBlackHoleRepulsionForce());
        plugin.getLogger().info("§a[BlackHole DEBUG] - Duración: " + plugin.getMainConfig().getPowerUpBlackHoleRepulsionDuration() + " segundos");
        
        repulsionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || blackHoleStand == null || blackHoleStand.isDead()) {
                    plugin.getLogger().info("§a[BlackHole DEBUG] Cancelando repulsión - agujero negro inactivo");
                    cancel();
                    return;
                }
                
                Location blackHoleLocation = blackHoleStand.getLocation();
                double repulsionRadius = plugin.getMainConfig().getPowerUpBlackHoleRepulsionForce();
                double repulsionForce = plugin.getMainConfig().getPowerUpBlackHoleRepulsionForce();
                
                int playersAffected = 0;
                
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
                        playersAffected++;
                        
                        // Sonido de repulsión
                        if (Math.random() < 0.4) {
                            player.playSound(player.getLocation(), Sound.EXPLODE, 0.8f, 1.2f);
                        }
                    }
                }
                
                // Debug cada 20 ticks (1 segundo)
                if (System.currentTimeMillis() % 1000 < 50) {
                    plugin.getLogger().info("§a[BlackHole DEBUG] Repulsión activa - Jugadores afectados: " + playersAffected);
                }
            }
        };
        
        repulsionTask.runTaskTimer(plugin, 0L, 2L);
        
        // Programar la destrucción del agujero negro
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("§a[BlackHole DEBUG] Tiempo de vida agotado, destruyendo agujero negro...");
                destroy();
            }
        }.runTaskLater(plugin, 20L * plugin.getMainConfig().getPowerUpBlackHoleRepulsionDuration());
    }
    
    /**
     * Destruye el agujero negro
     */
    public void destroy() {
        plugin.getLogger().info("§a[BlackHole DEBUG] Destruyendo agujero negro...");
        
        if (!isActive) {
            plugin.getLogger().info("§a[BlackHole DEBUG] Agujero negro ya estaba inactivo");
            return;
        }
        
        isActive = false;
        
        // Cancelar todas las tareas
        cancelAllTasks();
        
        // Efecto final
        if (blackHoleStand != null && !blackHoleStand.isDead()) {
            Location finalLoc = blackHoleStand.getLocation();
            finalLoc.getWorld().playSound(finalLoc, Sound.EXPLODE, 3.0f, 0.5f);
            plugin.getLogger().info("§a[BlackHole DEBUG] Efecto final reproducido");
        }
        
        // Remover entidades
        cleanup();
        
        plugin.getLogger().info("§a[BlackHole DEBUG] Agujero negro destruido completamente");
    }
    
    /**
     * Cancela todas las tareas activas
     */
    private void cancelAllTasks() {
        if (animationTask != null) {
            animationTask.cancel();
            plugin.getLogger().info("§a[BlackHole DEBUG] Tarea de animación cancelada");
        }
        if (effectTask != null) {
            effectTask.cancel();
            plugin.getLogger().info("§a[BlackHole DEBUG] Tarea de efectos cancelada");
        }
        if (attractionTask != null) {
            attractionTask.cancel();
            plugin.getLogger().info("§a[BlackHole DEBUG] Tarea de atracción cancelada");
        }
        if (repulsionTask != null) {
            repulsionTask.cancel();
            plugin.getLogger().info("§a[BlackHole DEBUG] Tarea de repulsión cancelada");
        }
        if (itemPreservationTask != null) {
            itemPreservationTask.cancel();
            plugin.getLogger().info("§a[BlackHole DEBUG] Tarea de preservación cancelada");
        }
    }
    
    /**
     * Limpia todas las entidades
     */
    private void cleanup() {
        // Remover item
        if (blackHoleItem != null && blackHoleItem.isValid()) {
            blackHoleItem.remove();
            plugin.getLogger().info("§a[BlackHole DEBUG] Item de obsidiana removido");
        }
        
        // Remover ArmorStand
        if (blackHoleStand != null && !blackHoleStand.isDead()) {
            blackHoleStand.remove();
            plugin.getLogger().info("§a[BlackHole DEBUG] ArmorStand removido");
        }
        
        // Limpiar entidades cercanas relacionadas
        cleanupNearbyBlackHoleEntities();
    }
    
    /**
     * Limpia entidades cercanas relacionadas con el agujero negro
     */
    private void cleanupNearbyBlackHoleEntities() {
        if (location.getWorld() == null) return;
        
        location.getWorld().getNearbyEntities(location, 3, 3, 3).forEach(entity -> {
            if (entity instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) entity;
                if (!stand.isVisible() && (stand.isMarker() || stand.isSmall())) {
                    stand.remove();
                }
            } else if (entity instanceof Item) {
                Item item = (Item) entity;
                if ((item.getPickupDelay() == Integer.MAX_VALUE && 
                        item.getCustomName() != null && 
                        item.getCustomName().contains("BLACKHOLE_ITEM")) ||
                    item.hasMetadata("BLACKHOLE_PROTECTED") ||
                    (item.hasMetadata("BLACKHOLE_ID") && 
                    item.getMetadata("BLACKHOLE_ID").get(0).asString().equals(itemIdentifier))) {
                    item.remove();
                }
            }
        });
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
    
    public String getItemIdentifier() {
        return itemIdentifier;
    }
}