package mk.kvlzx.powerup;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mk.kvlzx.utils.MessageUtils;

public class PowerUp {
    private final PowerUpType type;
    private final Location location;
    private Item droppedItem;
    private final long spawnTime;
    private boolean collected = false;

    public PowerUp(PowerUpType type, Location location) {
        this.type = type;
        this.location = location.clone();
        this.spawnTime = System.currentTimeMillis();
        spawnItem();
    }

    private void spawnItem() {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type.getDisplayName());
            meta.setLore(type.getLore());
            item.setItemMeta(meta);
        }

        // Hacer que el item flote y rote
        droppedItem = location.getWorld().dropItem(location, item);
        droppedItem.setVelocity(new Vector(0, 0.1, 0));
        droppedItem.setPickupDelay(Integer.MAX_VALUE); // No se puede recoger normalmente
        droppedItem.setCustomName(type.getDisplayName());
        droppedItem.setCustomNameVisible(true);
    }

    public void applyEffect(Player player) {
        if (collected) return;
        
        collected = true;
        
        // Remover el item del mundo
        if (droppedItem != null && !droppedItem.isDead()) {
            droppedItem.remove();
        }

        // Aplicar el efecto según el tipo
        switch (type) {
            case SPEED:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1)); // 30 segundos, nivel 2
                break;
            case JUMP:
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 30, 1)); // 30 segundos, nivel 2
                break;
            case STRENGTH:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 20, 0)); // 20 segundos, nivel 1
                break;
            case HEALTH:
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 1)); // 10 segundos, nivel 2
                break;
            case INVISIBILITY:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 15, 0)); // 15 segundos
                break;
            case KNOCKBACK:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 15, 0)); // 15 segundos
                // Efecto especial de knockback se manejará en el sistema de combate
                break;
        }

        // Mensaje al jugador
        player.sendMessage(MessageUtils.getColor("&aYou have picked up a " + type.getDisplayName() + " &apowerup!"));
    }

    public boolean isNearPlayer(Player player) {
        return player.getLocation().distance(location) <= 1.5; // Radio de 1.5 bloques
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > 60000; // 60 segundos
    }

    public boolean isCollected() {
        return collected;
    }

    public void remove() {
        if (droppedItem != null && !droppedItem.isDead()) {
            droppedItem.remove();
        }
    }

    public PowerUpType getType() {
        return type;
    }

    public Location getLocation() {
        return location.clone();
    }
}
