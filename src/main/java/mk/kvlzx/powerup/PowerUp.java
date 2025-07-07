package mk.kvlzx.powerup;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class PowerUp {
    private final PowerUpType type;
    private final Location location;
    private Item droppedItem;
    private ArmorStand hologram;
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
        spawnPowerUp();
    }

    private void spawnPowerUp() {
        if (location.getWorld() == null) return;

        // Crear el item visual
        spawnItem();
        
        // Crear el holograma
        createHologram();
        
        // Iniciar animaciones
        startAnimations();
        
        // Iniciar task de verificación
        startCheckTask();
    }

    private void spawnItem() {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type.getDisplayName());
            meta.setLore(type.getLore());
            item.setItemMeta(meta);
        }

        // Spawn del item en el centro del bloque, 0.5 bloques arriba del suelo
        Location itemLocation = location.clone().add(0.5, 1.2, 0.5);
        droppedItem = location.getWorld().dropItem(itemLocation, item);
        droppedItem.setVelocity(new Vector(0, 0, 0));
        droppedItem.setPickupDelay(Integer.MAX_VALUE);
        droppedItem.setCustomName(type.getDisplayName());
        droppedItem.setCustomNameVisible(false); // Lo manejamos con el holograma
        droppedItem.setFallDistance(0);
    }

    private void createHologram() {
        // Crear holograma principal (título)
        Location hologramLocation = location.clone().add(0.5, 2.5, 0.5);
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

        // Crear hologramas para el lore
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
        }
    }

    private void startAnimations() {
        animationTask = new BukkitRunnable() {
            private double angle = 0;
            private double bobOffset = 0;
            
            @Override
            public void run() {
                if (removed || droppedItem == null || droppedItem.isDead()) {
                    cancel();
                    return;
                }

                // Rotación del item
                Location itemLoc = droppedItem.getLocation();
                itemLoc.setYaw(itemLoc.getYaw() + 5);
                
                // Movimiento de bobbing (subir y bajar)
                double bobHeight = Math.sin(bobOffset) * 0.1;
                Location newLoc = location.clone().add(0.5, 1.2 + bobHeight, 0.5);
                newLoc.setYaw(itemLoc.getYaw());
                
                droppedItem.teleport(newLoc);
                
                // Rotación del holograma
                if (hologram != null && !hologram.isDead()) {
                    Location hologramLoc = hologram.getLocation();
                    hologramLoc.setYaw(hologramLoc.getYaw() + 2);
                    hologram.teleport(hologramLoc);
                }
                
                angle += 0.1;
                bobOffset += 0.2;
            }
        };
        animationTask.runTaskTimer(plugin, 0L, 2L);
    }

    private void startCheckTask() {
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (removed || isExpired()) {
                    cancel();
                    return;
                }

                // Verificar si hay jugadores cerca
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
        
        // Aplicar el efecto según el tipo
        switch (type) {
            case SPEED:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1));
                break;
            case JUMP:
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 30, 1));
                break;
            case STRENGTH:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 20, 0));
                break;
            case HEALTH:
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 1));
                break;
            case INVISIBILITY:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 15, 0));
                break;
            case KNOCKBACK:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 15, 0));
                // Agregar metadata para el knockback especial
                player.setMetadata("knockback_powerup", new FixedMetadataValue(plugin, System.currentTimeMillis() + 15000));
                break;
        }

        // Mensaje al jugador
        player.sendMessage(MessageUtils.getColor("&aYou have picked up a " + type.getDisplayName() + " &apowerup!"));
        
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
        return System.currentTimeMillis() - spawnTime > 90000; // 90 segundos
    }

    public boolean isCollected() {
        return collected;
    }

    public void remove() {
        if (removed) return;
        removed = true;
        
        // Cancelar tasks
        if (animationTask != null) {
            animationTask.cancel();
        }
        if (checkTask != null) {
            checkTask.cancel();
        }
        
        // Remover item
        if (droppedItem != null && !droppedItem.isDead()) {
            droppedItem.remove();
        }
        
        // Remover hologramas
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
        }
        
        // Remover todos los armor stands (hologramas de lore) en el área
        location.getWorld().getNearbyEntities(location, 2, 2, 2).forEach(entity -> {
            if (entity instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) entity;
                if (!stand.isVisible() && stand.isMarker()) {
                    stand.remove();
                }
            }
        });
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
