package mk.kvlzx.powerup;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.Vector3f;
import net.minecraft.server.v1_8_R3.WorldServer;

public class PowerUp {
    private final PowerUpType type;
    private final Location location;
    private ArmorStand itemStand;
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
        
        // Crear el item visual como ArmorStand
        spawnItemStand();
        
        // Iniciar animaciones
        startAnimations();
        
        // Iniciar task de verificación
        startCheckTask();
    }

    private void spawnItemStand() {
        ItemStack item = new ItemStack(type.getMaterial());
        
        // Debug: Verificar que el item no sea null y tenga material válido
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Creating PowerUp item - Material: " + type.getMaterial() + ", Item: " + item);
        
        if (item.getType() == Material.AIR) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Item material is AIR! This will cause invisible items.");
            return;
        }

        // Spawn del ArmorStand ARRIBA del holograma principal
        Location itemLocation = location.clone().add(0.5, 3.0, 0.5);
        itemStand = (ArmorStand) location.getWorld().spawnEntity(itemLocation, EntityType.ARMOR_STAND);
        
        // Configurar el ArmorStand para que sea invisible y tenga el item
        itemStand.setVisible(false);
        itemStand.setGravity(false);
        itemStand.setCanPickupItems(false);
        itemStand.setMarker(true);
        itemStand.setSmall(true);
        itemStand.setArms(true); // Habilitar brazos para poder sostener items
        itemStand.setBasePlate(false);
        
        // Debug: Verificar que el ArmorStand se creó correctamente
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: ArmorStand created at: " + itemLocation + ", ID: " + itemStand.getEntityId());
        
        // Intentar múltiples métodos para mostrar el item
        boolean success = false;
        
        // Método 1: Intentar NMS primero (casco)
        if (setArmorStandHelmet(itemStand, item)) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Successfully set helmet using NMS");
            success = true;
        }
        
        // Método 2: Si NMS falla, intentar con mano derecha y pose personalizada
        if (!success) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: NMS failed, trying hand method");
            if (setArmorStandHand(itemStand, item)) {
                MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Successfully set item in hand");
                success = true;
            }
        }
        
        // Método 3: Fallback a método normal
        if (!success) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Hand method failed, using normal helmet method");
            itemStand.setHelmet(item);
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Set helmet using normal method");
        }
        
        // Debug: Verificar el estado final del ArmorStand
        debugArmorStandState(itemStand);
    }

    /**
     * Método para establecer el casco del ArmorStand usando NMS
     */
    private boolean setArmorStandHelmet(ArmorStand armorStand, ItemStack item) {
        try {
            // Obtener la entidad NMS del ArmorStand
            Entity nmsEntity = ((CraftEntity) armorStand).getHandle();
            
            if (nmsEntity instanceof EntityArmorStand) {
                EntityArmorStand nmsArmorStand = (EntityArmorStand) nmsEntity;
                
                // Convertir el ItemStack de Bukkit a NMS
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
                
                // Debug: Verificar conversión NMS
                MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: NMS item conversion - Original: " + item + ", NMS: " + nmsItem);
                
                // Establecer el item como casco (slot 4 = helmet)
                nmsArmorStand.setEquipment(4, nmsItem);
                
                // Actualizar para todos los jugadores cercanos
                updateArmorStandForNearbyPlayers(nmsArmorStand);
                
                return true;
            }
        } catch (Exception e) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: NMS helmet method failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Método alternativo: poner el item en la mano derecha con pose personalizada
     */
    private boolean setArmorStandHand(ArmorStand armorStand, ItemStack item) {
        try {
            // Establecer el item en la mano derecha
            armorStand.setItemInHand(item);
            
            // Obtener la entidad NMS para configurar la pose
            Entity nmsEntity = ((CraftEntity) armorStand).getHandle();
            
            if (nmsEntity instanceof EntityArmorStand) {
                EntityArmorStand nmsArmorStand = (EntityArmorStand) nmsEntity;
                
                // Configurar la pose del brazo derecho para que parezca que está en la cabeza
                // Crear un Vector3f para la rotación (x, y, z en radianes)
                Vector3f rightArmPose = new Vector3f(
                    (float) Math.toRadians(-90), // Rotar hacia arriba
                    (float) Math.toRadians(0),   // Sin rotación lateral
                    (float) Math.toRadians(0)    // Sin rotación de torsión
                );
                
                // Establecer la pose del brazo derecho
                nmsArmorStand.setRightArmPose(rightArmPose);
                
                // Actualizar para todos los jugadores cercanos
                updateArmorStandForNearbyPlayers(nmsArmorStand);
                
                MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Set hand item and pose successfully");
                return true;
            }
        } catch (Exception e) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Hand method failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Actualizar el ArmorStand para todos los jugadores cercanos
     */
    private void updateArmorStandForNearbyPlayers(EntityArmorStand nmsArmorStand) {
        try {
            // Obtener el mundo NMS
            WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
            
            // Enviar múltiples paquetes para asegurar la sincronización
            int entityId = nmsArmorStand.getId();
            
            // Paquete de equipment para casco
            if (nmsArmorStand.getEquipment(4) != null) {
                PacketPlayOutEntityEquipment helmetPacket = new PacketPlayOutEntityEquipment(
                    entityId, 4, nmsArmorStand.getEquipment(4)
                );
                sendPacketToNearbyPlayers(helmetPacket, nmsWorld);
            }
            
            // Paquete de equipment para mano derecha
            if (nmsArmorStand.getEquipment(0) != null) {
                PacketPlayOutEntityEquipment handPacket = new PacketPlayOutEntityEquipment(
                    entityId, 0, nmsArmorStand.getEquipment(0)
                );
                sendPacketToNearbyPlayers(handPacket, nmsWorld);
            }
            
            // Paquete de metadata para poses
            DataWatcher dataWatcher = nmsArmorStand.getDataWatcher();
            PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(entityId, dataWatcher, false);
            sendPacketToNearbyPlayers(metadataPacket, nmsWorld);
            
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Sent update packets to nearby players");
            
        } catch (Exception e) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Failed to update ArmorStand for nearby players: " + e.getMessage());
        }
    }

    private void sendPacketToNearbyPlayers(Packet<?> packet, WorldServer nmsWorld) {
        try {
            for (EntityHuman entityHuman : nmsWorld.players) {
                if (entityHuman instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entityHuman;
                    if (player.getBukkitEntity().getLocation().distance(location) <= 64) {
                        player.playerConnection.sendPacket(packet);
                    }
                }
            }
        } catch (Exception e) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Failed to send packet: " + e.getMessage());
        }
    }

    /**
     * Debug: Mostrar el estado actual del ArmorStand
     */
    private void debugArmorStandState(ArmorStand armorStand) {
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: ArmorStand State:");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "  - Visible: " + armorStand.isVisible());
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "  - Has Arms: " + armorStand.hasArms());
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "  - Helmet: " + armorStand.getHelmet());
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "  - Hand Item: " + armorStand.getItemInHand());
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "  - Location: " + armorStand.getLocation());
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "  - Is Marker: " + armorStand.isMarker());
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "  - Is Small: " + armorStand.isSmall());
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "  - Entity ID: " + armorStand.getEntityId());
        
        // Verificar jugadores cercanos
        int nearbyPlayers = 0;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= 64) {
                nearbyPlayers++;
            }
        }
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "  - Nearby players: " + nearbyPlayers);
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

                // Rotación del ArmorStand
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
                    MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Animation tick " + tickCounter + " - ArmorStand still alive: " + !itemStand.isDead());
                    debugArmorStandState(itemStand);
                }
                
                // Actualizar cada cierto tiempo para asegurar que el item se muestre
                if (yaw % 45 == 0) { // Cada 9 ticks (45/5)
                    refreshArmorStandDisplay();
                }
            }
        };
        animationTask.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Refrescar la visualización del ArmorStand para evitar problemas de renderizado
     */
    private void refreshArmorStandDisplay() {
        if (itemStand == null || itemStand.isDead()) return;
        
        try {
            Entity nmsEntity = ((CraftEntity) itemStand).getHandle();
            
            if (nmsEntity instanceof EntityArmorStand) {
                EntityArmorStand nmsArmorStand = (EntityArmorStand) nmsEntity;
                updateArmorStandForNearbyPlayers(nmsArmorStand);
            }
        } catch (Exception e) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Failed to refresh ArmorStand display: " + e.getMessage());
        }
    }

    private void startCheckTask() {
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (removed || isExpired()) {
                    MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: PowerUp expired or removed, cleaning up");
                    remove();
                    cancel();
                    return;
                }

                // Verificar si hay jugadores cerca
                for (Player player : location.getWorld().getPlayers()) {
                    if (isNearPlayer(player)) {
                        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Player " + player.getName() + " picked up PowerUp");
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
        
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Removing PowerUp at " + location);
        
        // Cancelar tasks
        if (animationTask != null) {
            animationTask.cancel();
        }
        if (checkTask != null) {
            checkTask.cancel();
        }
        
        // Remover ArmorStand del item
        if (itemStand != null && !itemStand.isDead()) {
            itemStand.remove();
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Removed item ArmorStand");
        }
        
        // Remover holograma principal
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Removed main hologram");
        }
        
        // Remover todos los hologramas del lore
        for (ArmorStand loreHologram : loreHolograms) {
            if (loreHologram != null && !loreHologram.isDead()) {
                loreHologram.remove();
            }
        }
        loreHolograms.clear();
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: Removed " + loreHolograms.size() + " lore holograms");
        
        // Cleanup adicional por si acaso (mantener como respaldo)
        location.getWorld().getNearbyEntities(location, 2, 2, 2).forEach(entity -> {
            if (entity instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) entity;
                if (!stand.isVisible() && stand.isMarker()) {
                    stand.remove();
                }
            }
        });
        
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "DEBUG: PowerUp cleanup completed");
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