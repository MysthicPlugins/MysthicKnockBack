package kk.kvlzx.arena;

import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_8_R3.WorldBorder;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class VirtualBorder {
    private final WorldBorder border;
    private final Location center;
    private final double size;

    public VirtualBorder(Location center, double size) {
        this.center = center;
        this.size = size;
        this.border = new WorldBorder();
        this.border.world = ((CraftWorld) center.getWorld()).getHandle();
        
        updateBorder();
    }

    private void updateBorder() {
        border.setCenter(center.getX(), center.getZ());
        border.setSize(size);
        border.setWarningDistance(0);
        border.setWarningTime(0);
    }

    public void show(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        
        // Enviar todos los paquetes necesarios para mostrar el borde
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE));
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER));
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS));
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(border, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_TIME));
    }

    public void hide(Player player) {
        WorldBorder emptyBorder = new WorldBorder();
        emptyBorder.world = ((CraftWorld) center.getWorld()).getHandle();
        emptyBorder.setSize(6.0E7D); // Tamaño default de Minecraft
        
        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(emptyBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE));
    }

    public boolean isInside(Location location) {
        double x = location.getX();
        double z = location.getZ();
        double radius = size / 2;
        
        return Math.abs(x - center.getX()) <= radius && 
               Math.abs(z - center.getZ()) <= radius;
    }
}
