package mk.kvlzx.powerup;

import java.util.ArrayList;
import java.util.List;

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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.WorldServer;

public class PowerUp {
    private final PowerUpType type;
    private final Location location;
    private ArmorStand itemStand;
    private Item droppedItem;
    private ArmorStand hologram;
    private List<ArmorStand> loreHolograms;
    private final long spawnTime;
    private boolean collected = false;
    private boolean removed = false;
    private BukkitRunnable animationTask;
    private BukkitRunnable checkTask;
    private final MysthicKnockBack plugin;

    public PowerUp(PowerUpType type, Location location, MysthicKnockBack plugin) {
        this.type = type;
        this.location = location.clone();
        this.spawnTime = System.currentTimeMillis();
        this.plugin = plugin;
        this.loreHolograms = new ArrayList<>();
        spawnPowerUp();
    }

    private void spawnPowerUp() {
        if (location.getWorld() == null) return;

        // Crear el holograma primero
        createHologram();
        
        // Crear el item visual usando el método DecentHolograms
        spawnItemStand();
        
        // Iniciar animaciones
        startAnimations();
        
        // Iniciar task de verificación
        startCheckTask();
    }

    private void spawnItemStand() {
        ItemStack item = new ItemStack(type.getMaterial());
        
        // Debug: Verificar que el item no sea null y tenga material válido
        plugin.getLogger().info("DEBUG: Creating PowerUp item - Material: " + type.getMaterial() + ", Item: " + item);
        
        if (item.getType() == Material.AIR) {
            plugin.getLogger().warning("DEBUG: Item material is AIR! This will cause invisible items.");
            return;
        }

        // Posición para el ArmorStand (arriba del holograma)
        Location itemLocation = location.clone().add(0.5, 3.0, 0.5);
        
        // Crear el ArmorStand invisible
        itemStand = (ArmorStand) location.getWorld().spawnEntity(itemLocation, EntityType.ARMOR_STAND);
        itemStand.setVisible(false);
        itemStand.setGravity(false);
        itemStand.setCanPickupItems(false);
        itemStand.setMarker(false); // IMPORTANTE: No marker para poder tener passengers
        itemStand.setSmall(true);
        itemStand.setArms(false);
        itemStand.setBasePlate(false);
        
        // Debug: Verificar que el ArmorStand se creó correctamente
        plugin.getLogger().info("DEBUG: ArmorStand created at: " + itemLocation + ", ID: " + itemStand.getEntityId());
        
        // Dropear el item en la misma posición
        droppedItem = location.getWorld().dropItem(itemLocation, item);
        droppedItem.setPickupDelay(Integer.MAX_VALUE); // No se puede recoger
        droppedItem.setVelocity(new Vector(0, 0, 0)); // Sin velocidad
        
        plugin.getLogger().info("DEBUG: Dropped item created at: " + itemLocation + ", ID: " + droppedItem.getEntityId());
        
        // Usar NMS para montar el item al ArmorStand
        if (mountItemToArmorStand(itemStand, droppedItem)) {
            plugin.getLogger().info("DEBUG: Successfully mounted item to ArmorStand using NMS");
        } else {
            plugin.getLogger().warning("DEBUG: Failed to mount item using NMS");
        }
        
        // Debug: Verificar el estado final
        debugArmorStandState(itemStand);
    }

    /**
     * Montar el item al ArmorStand usando NMS
     */
    private boolean mountItemToArmorStand(ArmorStand armorStand, Item item) {
        try {
            // Obtener las entidades NMS
            Entity nmsArmorStand = ((CraftEntity) armorStand).getHandle();
            Entity nmsItem = ((CraftEntity) item).getHandle();
            
            plugin.getLogger().info("DEBUG: Mounting item - ArmorStand NMS: " + nmsArmorStand.getClass().getSimpleName() + 
                                    ", Item NMS: " + nmsItem.getClass().getSimpleName());
            
            // Verificar que no tenga passengers previos
            if (nmsArmorStand.passenger != null) {
                plugin.getLogger().warning("DEBUG: ArmorStand already has a passenger: " + nmsArmorStand.passenger);
                return false;
            }
            
            // Montar el item al ArmorStand
            nmsItem.mount(nmsArmorStand);
            
            plugin.getLogger().info("DEBUG: Mount operation completed. ArmorStand passenger: " + nmsArmorStand.passenger);
            
            // Verificar que el montaje fue exitoso
            if (nmsArmorStand.passenger == nmsItem) {
                plugin.getLogger().info("DEBUG: Item successfully mounted to ArmorStand");
                
                // Enviar paquetes de actualización a los jugadores cercanos
                updateMountForNearbyPlayers(nmsArmorStand);
                
                return true;
            } else {
                plugin.getLogger().warning("DEBUG: Mount failed - passenger mismatch");
                return false;
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("DEBUG: Failed to mount item to ArmorStand: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualizar el montaje para todos los jugadores cercanos
     */
    private void updateMountForNearbyPlayers(Entity nmsArmorStand) {
        try {
            // Obtener el mundo NMS
            WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
            
            // Crear paquete de attach
            if (nmsArmorStand.passenger != null) {
                PacketPlayOutAttachEntity attachPacket = new PacketPlayOutAttachEntity(
                    0, // leash = 0 para montaje normal
                    nmsArmorStand.passenger, 
                    nmsArmorStand
                );
                
                // Enviar a todos los jugadores cercanos
                for (EntityHuman entityHuman : nmsWorld.players) {
                    if (entityHuman instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) entityHuman;
                        if (player.getBukkitEntity().getLocation().distance(location) <= 64) {
                            player.playerConnection.sendPacket(attachPacket);
                        }
                    }
                }
                
                plugin.getLogger().info("DEBUG: Sent attach packets to nearby players");
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("DEBUG: Failed to update mount for nearby players: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Debug: Mostrar el estado actual del ArmorStand
     */
    private void debugArmorStandState(ArmorStand armorStand) {
        plugin.getLogger().info("DEBUG: ArmorStand State:");
        plugin.getLogger().info("  - Visible: " + armorStand.isVisible());
        plugin.getLogger().info("  - Has Arms: " + armorStand.hasArms());
        plugin.getLogger().info("  - Location: " + armorStand.getLocation());
        plugin.getLogger().info("  - Is Marker: " + armorStand.isMarker());
        plugin.getLogger().info("  - Is Small: " + armorStand.isSmall());
        plugin.getLogger().info("  - Entity ID: " + armorStand.getEntityId());
        plugin.getLogger().info("  - Passenger: " + armorStand.getPassenger());
        
        // Debug del item dropeado
        if (droppedItem != null) {
            plugin.getLogger().info("DEBUG: Dropped Item State:");
            plugin.getLogger().info("  - Item Type: " + droppedItem.getItemStack().getType());
            plugin.getLogger().info("  - Location: " + droppedItem.getLocation());
            plugin.getLogger().info("  - Entity ID: " + droppedItem.getEntityId());
            plugin.getLogger().info("  - Is Valid: " + droppedItem.isValid());
            plugin.getLogger().info("  - Vehicle: " + droppedItem.getVehicle());
        }
        
        // Verificar jugadores cercanos
        int nearbyPlayers = 0;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= 64) {
                nearbyPlayers++;
            }
        }
        plugin.getLogger().info("  - Nearby players: " + nearbyPlayers);
    }

    private void createHologram() {
        // Crear holograma principal (título) - más cerca del suelo
        Location hologramLocation = location.clone().add(0.5, 1.5, 0.5);
        hologram = (ArmorStand) location.getWorld().spawnEntity(hologramLocation, EntityType.ARMOR_STAND);
        hologram.setCustomName(type.getDisplayName());
        hologram.setCustomNameVisible(true);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setCanPickupItems(false);
        hologram.setMarker(true);
        hologram.setSmall(true);
        hologram.setArms(false);
        hologram.setBasePlate(false);

        // Crear hologramas para el lore y guardarlos en la lista
        List<String> lore = type.getLore();
        for (int i = 0; i < lore.size(); i++) {
            Location loreLocation = hologramLocation.clone().add(0, -0.3 * (i + 1), 0);
            ArmorStand loreHologram = (ArmorStand) location.getWorld().spawnEntity(loreLocation, EntityType.ARMOR_STAND);
            loreHologram.setCustomName(lore.get(i));
            loreHologram.setCustomNameVisible(true);
            loreHologram.setVisible(false);
            loreHologram.setGravity(false);
            loreHologram.setCanPickupItems(false);
            loreHologram.setMarker(true);
            loreHologram.setSmall(true);
            loreHologram.setArms(false);
            loreHologram.setBasePlate(false);
            
            loreHolograms.add(loreHologram);
        }
    }

    private void startAnimations() {
        animationTask = new BukkitRunnable() {
            private float yaw = 0;
            private int tickCounter = 0;
            
            @Override
            public void run() {
                if (removed || itemStand == null || itemStand.isDead()) {
                    cancel();
                    return;
                }

                // Rotación del ArmorStand (y su passenger)
                Location newLoc = location.clone().add(0.5, 3.0, 0.5);
                newLoc.setYaw(yaw);
                
                itemStand.teleport(newLoc);
                yaw += 5; // Incrementar rotación
                
                // Resetear yaw para evitar overflow
                if (yaw >= 360) {
                    yaw = 0;
                }
                
                tickCounter++;
                
                // Debug periódico cada 5 segundos (100 ticks)
                if (tickCounter % 100 == 0) {
                    plugin.getLogger().info("DEBUG: Animation tick " + tickCounter + " - ArmorStand alive: " + !itemStand.isDead() + 
                                            ", Item alive: " + (droppedItem != null && droppedItem.isValid()));
                    
                    // Verificar que el item sigue montado
                    if (droppedItem != null && droppedItem.isValid()) {
                        plugin.getLogger().info("DEBUG: Item vehicle: " + droppedItem.getVehicle());
                        plugin.getLogger().info("DEBUG: ArmorStand passenger: " + itemStand.getPassenger());
                    }
                }
                
                // Refrescar montaje cada cierto tiempo
                if (tickCounter % 60 == 0) { // Cada 3 segundos
                    refreshMount();
                }
            }
        };
        animationTask.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Refrescar el montaje para evitar desincronización
     */
    private void refreshMount() {
        if (itemStand == null || itemStand.isDead() || droppedItem == null || !droppedItem.isValid()) {
            return;
        }
        
        try {
            // Verificar que el item siga montado
            if (droppedItem.getVehicle() != itemStand) {
                plugin.getLogger().info("DEBUG: Item dismounted, attempting to remount...");
                
                // Intentar remontar
                Entity nmsArmorStand = ((CraftEntity) itemStand).getHandle();
                Entity nmsItem = ((CraftEntity) droppedItem).getHandle();
                
                nmsItem.mount(nmsArmorStand);
                updateMountForNearbyPlayers(nmsArmorStand);
                
                plugin.getLogger().info("DEBUG: Remount attempt completed");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("DEBUG: Failed to refresh mount: " + e.getMessage());
        }
    }

    private void startCheckTask() {
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (removed || isExpired()) {
                    plugin.getLogger().info("DEBUG: PowerUp expired or removed, cleaning up");
                    remove();
                    cancel();
                    return;
                }

                // Verificar si hay jugadores cerca
                for (Player player : location.getWorld().getPlayers()) {
                    if (isNearPlayer(player)) {
                        plugin.getLogger().info("DEBUG: Player " + player.getName() + " picked up PowerUp");
                        applyEffect(player);
                        cancel();
                        return;
                    }
                }
            }
        };
        checkTask.runTaskTimer(plugin, 0L, 5L);
    }

    public void applyEffect(Player player) {
        if (collected || removed) return;
        
        collected = true;

        switch (type) {
            case JUMP_1:
            case JUMP_2:
            case JUMP_3:
            case JUMP_4:
                // Usar los métodos del enum para obtener duración y nivel
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * type.getJumpDuration(), type.getJumpLevel()));
                break;
            case INVISIBILITY:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0));
                break;
            case KNOCKBACK:
                ItemStack knocker = null;
                int originalKnockbackLevel = 0;
                int knockerSlot = -1;

                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && item.containsEnchantment(Enchantment.KNOCKBACK)) {
                        knocker = item;
                        originalKnockbackLevel = item.getEnchantmentLevel(Enchantment.KNOCKBACK);
                        knockerSlot = i;
                        break;
                    }
                }

                if (knocker != null) {
                    final ItemStack finalKnocker = knocker;
                    final int finalOriginalKnockbackLevel = originalKnockbackLevel;
                    final int finalKnockerSlot = knockerSlot;

                    // Aumentar el knockback a 5
                    ItemStack powerupKnocker = finalKnocker.clone();
                    powerupKnocker.removeEnchantment(Enchantment.KNOCKBACK);
                    powerupKnocker.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
                    player.getInventory().setItem(finalKnockerSlot, powerupKnocker);

                    // Programar la restauración del knockback
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Restaurar el knockback original
                            ItemStack currentKnocker = player.getInventory().getItem(finalKnockerSlot);
                            if (currentKnocker != null && currentKnocker.isSimilar(powerupKnocker)) {
                                ItemStack restoredKnocker = currentKnocker.clone();
                                restoredKnocker.removeEnchantment(Enchantment.KNOCKBACK);
                                if (finalOriginalKnockbackLevel > 0) {
                                    restoredKnocker.addUnsafeEnchantment(Enchantment.KNOCKBACK, finalOriginalKnockbackLevel);
                                }
                                player.getInventory().setItem(finalKnockerSlot, restoredKnocker);
                            }
                        }
                    }.runTaskLater(plugin, 200L); // 10 segundos (10 * 20 ticks)
                }
                break;
        }

        player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have picked up a " + type.getDisplayName() + " &apowerup!"));
        
        // Efectos visuales/sonoros
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
        
        // Remover el powerup
        remove();
    }

    public boolean isNearPlayer(Player player) {
        if (player.getWorld() != location.getWorld()) return false;
        return player.getLocation().distance(location) <= 1.8; // Radio de 1.8 bloques
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > 30000; // 30 segundos
    }

    public boolean isCollected() {
        return collected;
    }

    public void remove() {
        if (removed) return;
        removed = true;
        
        plugin.getLogger().info("DEBUG: Removing PowerUp at " + location);
        
        // Cancelar tasks
        if (animationTask != null) {
            animationTask.cancel();
        }
        if (checkTask != null) {
            checkTask.cancel();
        }
        
        // Remover el item dropeado primero
        if (droppedItem != null && droppedItem.isValid()) {
            droppedItem.remove();
            plugin.getLogger().info("DEBUG: Removed dropped item");
        }
        
        // Remover ArmorStand del item
        if (itemStand != null && !itemStand.isDead()) {
            itemStand.remove();
            plugin.getLogger().info("DEBUG: Removed item ArmorStand");
        }
        
        // Remover holograma principal
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
            plugin.getLogger().info("DEBUG: Removed main hologram");
        }
        
        // Remover todos los hologramas del lore
        int loreCount = loreHolograms.size();
        for (ArmorStand loreHologram : loreHolograms) {
            if (loreHologram != null && !loreHologram.isDead()) {
                loreHologram.remove();
            }
        }
        loreHolograms.clear();
        plugin.getLogger().info("DEBUG: Removed " + loreCount + " lore holograms");
        
        // Cleanup adicional por si acaso
        location.getWorld().getNearbyEntities(location, 3, 3, 3).forEach(entity -> {
            if (entity instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) entity;
                if (!stand.isVisible() && (stand.isMarker() || stand.isSmall())) {
                    stand.remove();
                }
            } else if (entity instanceof Item) {
                Item item = (Item) entity;
                if (item.getPickupDelay() == Integer.MAX_VALUE) {
                    item.remove();
                }
            }
        });
        
        plugin.getLogger().info("DEBUG: PowerUp cleanup completed");
    }

    public PowerUpType getType() {
        return type;
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean isRemoved() {
        return removed;
    }
}