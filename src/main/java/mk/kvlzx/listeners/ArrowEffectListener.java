package mk.kvlzx.listeners;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.ArrowEffectsShopConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArrowEffectListener implements Listener {
    private final MysthicKnockBack plugin;
    private final Map<UUID, ArrowEffectsShopConfig.ArrowEffectItem> activeArrows = new HashMap<>();

    public ArrowEffectListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;
        
        Player shooter = (Player) arrow.getShooter();
        String effectName = plugin.getCosmeticManager().getPlayerArrowEffect(shooter.getUniqueId());
        
        if (effectName.equals("none")) return;
        
        ArrowEffectsShopConfig.ArrowEffectItem effect = getEffectByName(effectName);
        if (effect == null) return;

        activeArrows.put(arrow.getUniqueId(), effect);
        
        // Schedule a task to play the effect periodically
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isDead() || arrow.isOnGround() || !activeArrows.containsKey(arrow.getUniqueId())) {
                    activeArrows.remove(arrow.getUniqueId());
                    this.cancel();
                    return;
                }
                playEffect(effect, arrow.getLocation());
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();
        
        ArrowEffectsShopConfig.ArrowEffectItem effect = activeArrows.remove(arrow.getUniqueId());
        if (effect != null) {
            // Final effect when the arrow hits
            playEffect(effect, arrow.getLocation());
        }
    }

    /**
     * Search for an ArrowEffectItem by its name
     */
    private ArrowEffectsShopConfig.ArrowEffectItem getEffectByName(String effectName) {
        ArrowEffectsShopConfig config = plugin.getArrowEffectsShopConfig();
        if (config == null) return null;
        
        return config.getArrowEffectItems().values().stream()
            .filter(item -> item.getName().equals(effectName))
            .findFirst()
            .orElse(null);
    }

    /**
     * Play the specified effect at the given location
     */
    private void playEffect(ArrowEffectsShopConfig.ArrowEffectItem effectItem, Location location) {
        try {
            Effect effect = Effect.valueOf(effectItem.getEffect().toUpperCase());
            World world = location.getWorld();
            
            if (world == null) return;

            // Play the effect multiple times if specified
            for (int i = 0; i < effectItem.getEffectCount(); i++) {
                Location effectLocation = location.clone();
                if (effectItem.getOffsetX() != 0 || effectItem.getOffsetY() != 0 || effectItem.getOffsetZ() != 0) {
                    effectLocation.add(
                        (Math.random() - 0.5) * effectItem.getOffsetX() * 2,
                        (Math.random() - 0.5) * effectItem.getOffsetY() * 2,
                        (Math.random() - 0.5) * effectItem.getOffsetZ() * 2
                    );
                }

                // Determine the data value for the effect
                int data = getEffectData(effect);
                
                // Reproducir el efecto
                world.playEffect(effectLocation, effect, data, 50);
                
                // Little delay between effects
                if (i < effectItem.getEffectCount() - 1 && effectItem.getEffectCount() > 1) {
                    try {
                        Thread.sleep(5); // 5 milliseconds delay
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
        } catch (IllegalArgumentException e) {
            // If the effect is not valid, log a warning and play a default effect
            plugin.getLogger().warning("Invalid effect: " + effectItem.getEffect() + " for item: " + effectItem.getName());
            location.getWorld().playEffect(location, Effect.FLAME, 0, 50);
        }
    }

    /**
     * Get the data value for the effect based on its type.
     */
    private int getEffectData(Effect effect) {
        switch (effect) {
            case COLOURED_DUST:
                // For COLOURED_DUST, we return a random color value
                // En 1.8, the data value is a color index (0-15)
                return (int) (Math.random() * 16);
                
            case SMOKE:
                // Direction of the smoke effect
                return (int) (Math.random() * 9);
                
            case STEP_SOUND:
                // ID of the block for the step sound
                return 1; // Stone
                
            default:
                return 0; // Value for other effects
        }
    }
}
