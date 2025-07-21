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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.items.CustomItem.ItemType;
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
    private BukkitRunnable itemPreservationTask;
    private final MysthicKnockBack plugin;
    private final String itemIdentifier;

    public PowerUp(PowerUpType type, Location location, MysthicKnockBack plugin) {
        this.type = type;
        this.location = location.clone();
        this.spawnTime = System.currentTimeMillis();
        this.plugin = plugin;
        this.loreHolograms = new ArrayList<>();
        this.itemIdentifier = "POWERUP_ITEM_" + System.currentTimeMillis();
        spawnPowerUp();
    }

    private void spawnPowerUp() {
        if (location.getWorld() == null) return;

        createHologram();
        spawnItemStand();
        startAnimations();
        startCheckTask();
        startItemPreservationTask();
    }

    private void spawnItemStand() {
        ItemStack item = new ItemStack(type.getMaterial());
        
        if (item.getType() == Material.AIR) {
            return;
        }

        Location itemLocation = location.clone().add(0.5, 1.4, 0.5);
        
        itemStand = (ArmorStand) location.getWorld().spawnEntity(itemLocation, EntityType.ARMOR_STAND);
        itemStand.setVisible(false);
        itemStand.setGravity(false);
        itemStand.setCanPickupItems(false);
        itemStand.setMarker(false);
        itemStand.setSmall(true);
        itemStand.setArms(false);
        itemStand.setBasePlate(false);
        
        droppedItem = location.getWorld().dropItem(itemLocation, item);
        droppedItem.setPickupDelay(Integer.MAX_VALUE);
        droppedItem.setVelocity(new Vector(0, 0, 0));
        droppedItem.setTicksLived(1);
        
        droppedItem.setCustomName("§f§l" + itemIdentifier);
        droppedItem.setCustomNameVisible(false);
        
        droppedItem.setMetadata("POWERUP_PROTECTED", new FixedMetadataValue(plugin, true));
        droppedItem.setMetadata("POWERUP_ID", new FixedMetadataValue(plugin, itemIdentifier));
        
        if (!mountItemToArmorStand(itemStand, droppedItem)) {
            plugin.getLogger().warning("Failed to mount item to ArmorStand for PowerUp: " + type.name());
        }
    }

    private void startItemPreservationTask() {
        itemPreservationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (removed || droppedItem == null || !droppedItem.isValid()) {
                    cancel();
                    return;
                }

                droppedItem.setTicksLived(1);
                
                if (droppedItem.getPickupDelay() != Integer.MAX_VALUE) {
                    droppedItem.setPickupDelay(Integer.MAX_VALUE);
                }
                
                if (droppedItem.getCustomName() == null || !droppedItem.getCustomName().contains(itemIdentifier)) {
                    droppedItem.setCustomName("§f§l" + itemIdentifier);
                    droppedItem.setCustomNameVisible(false);
                }
                
                if (!droppedItem.hasMetadata("POWERUP_PROTECTED")) {
                    droppedItem.setMetadata("POWERUP_PROTECTED", new FixedMetadataValue(plugin, true));
                    droppedItem.setMetadata("POWERUP_ID", new FixedMetadataValue(plugin, itemIdentifier));
                }
                
                if (droppedItem.getVelocity().length() > 0.1) {
                    droppedItem.setVelocity(new Vector(0, 0, 0));
                }
            }
        };
        itemPreservationTask.runTaskTimer(plugin, 0L, 20L);
    }

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
            plugin.getLogger().warning("Error mounting item to ArmorStand: " + e.getMessage());
            return false;
        }
    }

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
            plugin.getLogger().warning("Error updating mount for nearby players: " + e.getMessage());
        }
    }

    private void createHologram() {
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

                Location newLoc = location.clone().add(0.5, 1.4, 0.5);
                newLoc.setYaw(yaw);
                
                itemStand.teleport(newLoc);
                yaw += 5;
                
                if (yaw >= 360) {
                    yaw = 0;
                }
                
                tickCounter++;
                
                if (tickCounter % 60 == 0) {
                    refreshMount();
                }
            }
        };
        animationTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void refreshMount() {
        if (itemStand == null || itemStand.isDead() || droppedItem == null || !droppedItem.isValid()) {
            return;
        }
        
        try {
            if (droppedItem.getVehicle() != itemStand) {
                Entity nmsArmorStand = ((CraftEntity) itemStand).getHandle();
                Entity nmsItem = ((CraftEntity) droppedItem).getHandle();
                
                nmsItem.mount(nmsArmorStand);
                updateMountForNearbyPlayers(nmsArmorStand);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error refreshing mount: " + e.getMessage());
        }
    }

    private void startCheckTask() {
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (removed || isExpired()) {
                    remove();
                    cancel();
                    return;
                }

                for (Player player : location.getWorld().getPlayers()) {
                    if (isNearPlayer(player)) {
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
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.getByName(plugin.getMainConfig().getPowerUpJump1EffectId()), 
                        type.getJumpDuration(), 
                        type.getJumpLevel()
                    ));
                break;
            case INVISIBILITY:
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.getByName(plugin.getMainConfig().getPowerUpInvisibilityEffectId()), 
                        plugin.getMainConfig().getPowerUpInvisibilityEffectDuration() * 20, 
                        plugin.getMainConfig().getPowerUpInvisibilityEffectLevel()
                    ));
                break;
            case KNOCKBACK:
                // Aplicar powerup de knockback al jugador
                plugin.getCombatManager().addPowerupKnockback(player, 
                    plugin.getMainConfig().getPowerUpKnockbackEffectDuration());
                break;
            case EXPLOSIVE_ARROW:
                player.setMetadata("explosive_arrow", new FixedMetadataValue(plugin, true));
                
                if (plugin.getCooldownManager().isOnCooldown(player, "BOW")) {
                    player.getInventory().setItem(9, CustomItem.create(ItemType.ARROW));
                    plugin.getCooldownManager().clearCooldown(player, "BOW");
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.hasMetadata("explosive_arrow")) {
                            player.removeMetadata("explosive_arrow", plugin);
                        }
                    }
                }.runTaskLater(plugin, plugin.getMainConfig().getPowerUpExplosiveArrowEffectDuration() * 20);
                break;
            case BLACK_HOLE:
                // Dar el item de agujero negro al jugador
                BlackHoleItem blackHoleItem = new BlackHoleItem(plugin);
                ItemStack blackHoleItemStack = blackHoleItem.createBlackHoleItem();
                
                // Buscar un slot libre en el inventario
                int freeSlot = player.getInventory().firstEmpty();
                if (freeSlot != -1) {
                    player.getInventory().setItem(freeSlot, blackHoleItemStack);
                }
                
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + plugin.getMainConfig().getPowerUpBlackHoleItemPickupMessage()));
                break;
            case DOUBLE_PEARL:
                // Aplicar metadata al jugador para indicar que tiene el powerup de doble perla
                player.setMetadata("double_pearl_powerup", new FixedMetadataValue(plugin, true));
                
                // Programar la remoción del efecto después de 15 segundos
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.hasMetadata("double_pearl_powerup")) {
                            player.removeMetadata("double_pearl_powerup", plugin);
                            player.sendMessage(MessageUtils.getColor(
                                MysthicKnockBack.getPrefix() + 
                                plugin.getMainConfig().getPowerUpDoublePearlExpiredMessage()
                            ));
                        }
                    }
                }.runTaskLater(plugin, plugin.getMainConfig().getPowerUpDoublePearlEffectDuration() * 20);
                break;
        }

        player.sendMessage(MessageUtils.getColor(
                MysthicKnockBack.getPrefix() + 
                plugin.getMainConfig().getPowerUpMessagePickup()
                .replace("%powerup%", type.getDisplayName())
            ));
        
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.5f);
        remove();
    }

    public boolean isNearPlayer(Player player) {
        if (player.getWorld() != location.getWorld()) return false;
        return player.getLocation().distance(location) <= plugin.getMainConfig().getPowerUpPickupRadius();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > plugin.getMainConfig().getPowerUpTime() * 1000;
    }

    public boolean isCollected() {
        return collected;
    }

    public void remove() {
        if (removed) return;
        removed = true;
        
        if (animationTask != null) {
            animationTask.cancel();
        }
        if (checkTask != null) {
            checkTask.cancel();
        }
        if (itemPreservationTask != null) {
            itemPreservationTask.cancel();
        }
        
        if (droppedItem != null && droppedItem.isValid()) {
            droppedItem.remove();
        }
        
        if (itemStand != null && !itemStand.isDead()) {
            itemStand.remove();
        }
        
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
        }
        
        for (ArmorStand loreHologram : loreHolograms) {
            if (loreHologram != null && !loreHologram.isDead()) {
                loreHologram.remove();
            }
        }
        loreHolograms.clear();
        
        cleanupNearbyPowerUpEntities();
    }

    private void cleanupNearbyPowerUpEntities() {
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
                        item.getCustomName().contains("POWERUP_ITEM")) ||
                    item.hasMetadata("POWERUP_PROTECTED") ||
                    (item.hasMetadata("POWERUP_ID") && 
                    item.getMetadata("POWERUP_ID").get(0).asString().equals(itemIdentifier))) {
                    item.remove();
                }
            }
        });
    }

    public boolean isMyItem(Item item) {
        return item != null && 
                item.isValid() && 
                ((item.getCustomName() != null && item.getCustomName().contains(itemIdentifier)) ||
                (item.hasMetadata("POWERUP_ID") && 
                item.getMetadata("POWERUP_ID").get(0).asString().equals(itemIdentifier)));
    }

    // Getters
    public PowerUpType getType() { return type; }
    public Location getLocation() { return location.clone(); }
    public boolean isRemoved() { return removed; }
    public String getItemIdentifier() { return itemIdentifier; }

}