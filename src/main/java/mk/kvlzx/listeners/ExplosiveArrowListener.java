package mk.kvlzx.listeners;

import mk.kvlzx.MysthicKnockBack;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

public class ExplosiveArrowListener implements Listener {

    private final MysthicKnockBack plugin;

    public ExplosiveArrowListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                
                // Verificar si el jugador tiene el metadata de flecha explosiva
                if (shooter.hasMetadata("explosive_arrow")) {
                    // Remover el metadata después de usar la flecha explosiva
                    shooter.removeMetadata("explosive_arrow", plugin);
                    
                    // Crear explosión visual sin daño al terreno
                    arrow.getWorld().createExplosion(arrow.getLocation(), 0.0F);
                    
                    // Aplicar knockback a jugadores cercanos
                    for (Entity entity : arrow.getNearbyEntities(
                            plugin.getMainConfig().getPowerUpExplosiveArrowRadius(), 
                            plugin.getMainConfig().getPowerUpExplosiveArrowRadius(), 
                            plugin.getMainConfig().getPowerUpExplosiveArrowRadius())) {
                        
                        if (entity instanceof Player && !entity.equals(shooter)) {
                            Player player = (Player) entity;
                            Vector direction = player.getLocation().toVector()
                                    .subtract(arrow.getLocation().toVector())
                                    .normalize();
                            player.setVelocity(direction.multiply(plugin.getMainConfig().getPowerUpExplosiveArrowPower()));
                        }
                    }
                    
                    // Remover la flecha
                    arrow.remove();
                }
            }
        }
    }
}
