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
import mk.kvlzx.utils.ParticleUtils;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EnumParticle;
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
    }
    
    /**
     * Spawna el agujero negro
     */
    public void spawn() {
        
        if (location.getWorld() == null) {
            return;
        }
        
        // Crear el ArmorStand elevado
        Location standLocation = location.clone().add(0, 2, 0);
        
        try {
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
            blackHoleItem.setTicksLived(1);
            blackHoleItem.setCustomName(MessageUtils.getColor(plugin.getMainConfig().getPowerUpBlackHoleName()));
            blackHoleItem.setCustomNameVisible(true);
            
            // Metadata para identificación
            blackHoleItem.setMetadata("BLACKHOLE_PROTECTED", new FixedMetadataValue(plugin, true));
            blackHoleItem.setMetadata("BLACKHOLE_ID", new FixedMetadataValue(plugin, itemIdentifier));
            
            // Montar el item al ArmorStand usando NMS
            mountItemToArmorStand(blackHoleStand, blackHoleItem);
            
            isActive = true;
            
            // Iniciar todas las tareas
            startAnimationTask();
            startEffectTask();
            startItemPreservationTask();
            startAttractionPhase();
            
            // Sonido de spawn
            location.getWorld().playSound(location, Sound.ENDERDRAGON_GROWL, 2.0f, 0.5f);
            
        } catch (Exception e) {
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
            
            Entity nmsArmorStand = ((CraftEntity) armorStand).getHandle();
            Entity nmsItem = ((CraftEntity) item).getHandle();
            
            if (nmsArmorStand.passenger != null) {
                return false;
            }
            
            nmsItem.mount(nmsArmorStand);
            
            if (nmsArmorStand.passenger == nmsItem) {
                updateMountForNearbyPlayers(nmsArmorStand);
                return true;
            } else {
                return false;
            }
            
        } catch (Exception e) {
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
                
                for (EntityHuman entityHuman : nmsWorld.players) {
                    if (entityHuman instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) entityHuman;
                        if (player.getBukkitEntity().getLocation().distance(location) <= 64) {
                            player.playerConnection.sendPacket(attachPacket);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Tarea de preservación del item (copiado de PowerUp)
     */
    private void startItemPreservationTask() {
        
        itemPreservationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || blackHoleItem == null || !blackHoleItem.isValid()) {
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
            return;
        }
        
        try {
            if (blackHoleItem.getVehicle() != blackHoleStand) {
                Entity nmsArmorStand = ((CraftEntity) blackHoleStand).getHandle();
                Entity nmsItem = ((CraftEntity) blackHoleItem).getHandle();
                
                nmsItem.mount(nmsArmorStand);
                updateMountForNearbyPlayers(nmsArmorStand);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Inicia la animación del agujero negro
     */
    private void startAnimationTask() {
        
        animationTask = new BukkitRunnable() {
            private float yaw = 0;
            private int tickCounter = 0;
            
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
        
        effectTask = new BukkitRunnable() {
            private int tickCounter = 0;
            
            @Override
            public void run() {
                if (!isActive || blackHoleStand == null || blackHoleStand.isDead()) {
                    cancel();
                    return;
                }
                
                Location effectLoc = blackHoleStand.getLocation();
                tickCounter++;
                
                if (isRepulsing) {
                    // Efectos de repulsión - explosivos y agresivos
                    ParticleUtils.spawnParticle(effectLoc, EnumParticle.EXPLOSION_LARGE, 2, 1.0, 0.1);
                    ParticleUtils.spawnParticle(effectLoc, EnumParticle.FLAME, 15, 2.0, 0.1);
                    ParticleUtils.spawnParticle(effectLoc, EnumParticle.SMOKE_LARGE, 10, 1.5, 0.05);
                    ParticleUtils.spawnParticle(effectLoc, EnumParticle.FIREWORKS_SPARK, 8, 1.0, 0.2);
                    
                    // Círculo de fuego cada 10 ticks
                    if (tickCounter % 10 == 0) {
                        ParticleUtils.spawnParticleCircle(effectLoc, EnumParticle.FLAME, 16, 3.0, 0.1);
                    }
                    
                } else {
                    // Efectos de atracción - místicos y absorbentes
                    ParticleUtils.spawnParticle(effectLoc, EnumParticle.PORTAL, 25, 2.0, 0.5);
                    ParticleUtils.spawnParticle(effectLoc, EnumParticle.ENCHANTMENT_TABLE, 12, 1.5, 0.3);
                    ParticleUtils.spawnParticle(effectLoc, EnumParticle.SPELL_WITCH, 8, 1.0, 0.1);
                    ParticleUtils.spawnParticle(effectLoc, EnumParticle.SMOKE_NORMAL, 6, 1.0, 0.05);
                    
                    // Espiral de atracción cada 5 ticks
                    if (tickCounter % 5 == 0) {
                        ParticleUtils.spawnParticleSpiral(effectLoc, EnumParticle.PORTAL, 20, 4.0, 3.0, 0.1);
                    }
                    
                    // Círculo de encantamiento cada 15 ticks
                    if (tickCounter % 15 == 0) {
                        ParticleUtils.spawnParticleCircle(effectLoc, EnumParticle.ENCHANTMENT_TABLE, 12, 2.0, 0.2);
                    }
                }
            }
        };
        
        effectTask.runTaskTimer(plugin, 0L, 3L); // Cada 3 ticks para más fluidez
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
                    String playerZone = plugin.getArenaManager().getPlayerZone(player);
                    if (playerZone != null && playerZone.equals("pvp")) {
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
                    String playerZone = plugin.getArenaManager().getPlayerZone(player);
                    if (playerZone != null && playerZone.equals("pvp")) {
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
        
        if (!isActive) {
            return;
        }
        
        isActive = false;
        
        // Cancelar todas las tareas
        cancelAllTasks();
        
        // Efecto final
        if (blackHoleStand != null && !blackHoleStand.isDead()) {
            Location finalLoc = blackHoleStand.getLocation();
            finalLoc.getWorld().playSound(finalLoc, Sound.EXPLODE, 3.0f, 0.5f);
        }
        
        // Remover entidades
        cleanup();
    }
    
    /**
     * Cancela todas las tareas activas
     */
    private void cancelAllTasks() {
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
        if (itemPreservationTask != null) {
            itemPreservationTask.cancel();
        }
    }
    
    /**
     * Limpia todas las entidades
     */
    private void cleanup() {
        // Remover item
        if (blackHoleItem != null && blackHoleItem.isValid()) {
            blackHoleItem.remove();
        }
        
        // Remover ArmorStand
        if (blackHoleStand != null && !blackHoleStand.isDead()) {
            blackHoleStand.remove();
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