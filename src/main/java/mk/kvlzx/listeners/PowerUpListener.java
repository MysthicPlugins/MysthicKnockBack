package mk.kvlzx.listeners;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.powerup.PowerUpType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

public class PowerUpListener implements Listener {

    private final MysthicKnockBack plugin;

    public PowerUpListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow && event.getEntity().getShooter() instanceof Player) {
            Arrow arrow = (Arrow) event.getEntity();
            Player shooter = (Player) arrow.getShooter();

            if (arrow.hasMetadata("explosive_arrow")) {
                arrow.getWorld().createExplosion(arrow.getLocation(), 0.0F, false);

                double radius = plugin.getMainConfig().getPowerUpExplosiveArrowRadius();
                double power = plugin.getMainConfig().getPowerUpExplosiveArrowPower();

                for (Entity entity : arrow.getNearbyEntities(radius, radius, radius)) {
                    if (entity instanceof Player && !entity.equals(shooter)) {
                        Player nearbyPlayer = (Player) entity;
                        Vector direction = nearbyPlayer.getLocation().toVector().subtract(arrow.getLocation().toVector()).normalize();
                        nearbyPlayer.setVelocity(direction.multiply(power));
                    }
                }
                arrow.remove();
            }
        }
    }
}
