package mk.kvlzx.powerup;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
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

public class PowerUp {
    private final PowerUpType type;
    private final Location location;
    private ArmorStand itemStand; // Cambio: ArmorStand en lugar de Item
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

        // Spawn del ArmorStand ARRIBA del holograma principal
        Location itemLocation = location.clone().add(0.5, 3.0, 0.5);
        itemStand = (ArmorStand) location.getWorld().spawnEntity(itemLocation, EntityType.ARMOR_STAND);
        
        // Configurar el ArmorStand para que sea invisible y tenga el item como helmet
        itemStand.setVisible(false);
        itemStand.setGravity(false);
        itemStand.setCanPickupItems(false);
        itemStand.setMarker(true);
        itemStand.setSmall(true);
        itemStand.setArms(false);
        itemStand.setBasePlate(false);
        
        // Establecer el item como helmet
        itemStand.setHelmet(item);
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
            
            @Override
            public void run() {
                if (removed || itemStand == null || itemStand.isDead()) {
                    cancel();
                    return;
                }

                // Solo rotación del ArmorStand, sin movimiento vertical
                Location newLoc = location.clone().add(0.5, 3.0, 0.5);
                newLoc.setYaw(yaw);
                
                itemStand.teleport(newLoc);
                yaw += 5; // Incrementar rotación
                
                // Resetear yaw para evitar overflow
                if (yaw >= 360) {
                    yaw = 0;
                }
            }
        };
        animationTask.runTaskTimer(plugin, 0L, 1L);
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
                    }.runTaskLater(plugin, 100L); // 5 segundos (5 * 20 ticks)
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
        }
        
        // Remover holograma principal
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
        }
        
        // Remover todos los hologramas del lore
        for (ArmorStand loreHologram : loreHolograms) {
            if (loreHologram != null && !loreHologram.isDead()) {
                loreHologram.remove();
            }
        }
        loreHolograms.clear();
        
        // Cleanup adicional por si acaso (mantener como respaldo)
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
