package mk.kvlzx.listeners;

import mk.kvlzx.MysthicKnockBack;
import net.minecraft.server.v1_8_R3.PacketPlayOutExplosion;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.Collections;

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
                    Location loc = arrow.getLocation();
                    PacketPlayOutExplosion packet = new PacketPlayOutExplosion(
                        loc.getX(), 
                        loc.getY(), 
                        loc.getZ(), 
                        (float) plugin.getMainConfig().getPowerUpExplosiveArrowRadius(), 
                        Collections.emptyList(), 
                        null
                    );

                    for (Player p : loc.getWorld().getPlayers()) {
                        if (p.getLocation().distanceSquared(loc) < 4096) { // 64*64
                            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
                        }
                    }
                    
                    // Aplicar knockback a jugadores cercanos
                    for (Entity entity : arrow.getNearbyEntities(
                            plugin.getMainConfig().getPowerUpExplosiveArrowRadius(), 
                            plugin.getMainConfig().getPowerUpExplosiveArrowRadius(), 
                            plugin.getMainConfig().getPowerUpExplosiveArrowRadius())) {
                        
                        if (entity instanceof Player && !entity.equals(shooter)) {
                            Player player = (Player) entity;
                            Vector direction = player.getLocation().toVector()
                                    .subtract(arrow.getLocation().toVector())
                                    .normalize()
                                    .multiply(plugin.getMainConfig().getPowerUpExplosiveArrowPower());
                            // Igualar el knockback vertical al horizontal
                            direction.setY(direction.length());
                            player.setVelocity(direction);
                        }
                    }
                    
                    // Remover la flecha
                    arrow.remove();
                }
            }
        }
    }
}
