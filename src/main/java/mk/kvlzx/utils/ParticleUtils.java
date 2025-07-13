package mk.kvlzx.utils;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

/**
 * Clase helper para manejar partículas
 */
public class ParticleUtils {
    
    /**
     * Spawna partículas usando NMS con EnumParticle
     */
    public static void spawnParticle(Location location, EnumParticle particle, int count, double spread, double speed) {
        if (location.getWorld() == null) return;
        
        try {
            
            for (int i = 0; i < count; i++) {
                double offsetX = (Math.random() - 0.5) * spread;
                double offsetY = (Math.random() - 0.5) * spread;
                double offsetZ = (Math.random() - 0.5) * spread;
                
                double x = location.getX() + offsetX;
                double y = location.getY() + offsetY;
                double z = location.getZ() + offsetZ;
                
                // Crear paquete de partículas usando EnumParticle
                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                    particle,
                    true, // longDistance
                    (float) x, (float) y, (float) z,
                    (float) offsetX, (float) offsetY, (float) offsetZ,
                    (float) speed,
                    count
                );
                
                // Enviar a jugadores cercanos
                for (Player player : location.getWorld().getPlayers()) {
                    if (player.getLocation().distance(location) <= 48) {
                        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
                        nmsPlayer.playerConnection.sendPacket(packet);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback silencioso si falla
            e.printStackTrace();
        }
    }
    
    /**
     * Spawna partículas en un círculo
     */
    public static void spawnParticleCircle(Location center, EnumParticle particle, int count, double radius, double speed) {
        if (center.getWorld() == null) return;
        
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location particleLoc = new Location(center.getWorld(), x, center.getY(), z);
            spawnParticle(particleLoc, particle, 1, 0.1, speed);
        }
    }
    
    /**
     * Spawna partículas en espiral
     */
    public static void spawnParticleSpiral(Location center, EnumParticle particle, int count, double radius, double height, double speed) {
        if (center.getWorld() == null) return;
        
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count * 3; // 3 vueltas
            double currentRadius = radius * (1 - (double) i / count);
            double currentHeight = height * i / count;
            
            double x = center.getX() + currentRadius * Math.cos(angle);
            double z = center.getZ() + currentRadius * Math.sin(angle);
            double y = center.getY() + currentHeight;
            
            Location particleLoc = new Location(center.getWorld(), x, y, z);
            spawnParticle(particleLoc, particle, 1, 0.05, speed);
        }
    }
}
