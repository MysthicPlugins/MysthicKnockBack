package mk.kvlzx.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder.EnumWorldBorderAction;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

public class WorldBorder {
    private final Location center;
    private final double size;
    private final net.minecraft.server.v1_8_R3.WorldBorder border;

    public WorldBorder(Location center, double size) {
        this.center = center;
        this.size = size;
        this.border = new net.minecraft.server.v1_8_R3.WorldBorder();
        this.border.setCenter(center.getX(), center.getZ());
        this.border.setSize(size);
        this.border.setWarningDistance(10);
    }

    public void show(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        
        // Inicializar el borde
        craftPlayer.getHandle().playerConnection.sendPacket(
            new PacketPlayOutWorldBorder(border, EnumWorldBorderAction.INITIALIZE)
        );
    }

    public static void removeBorder(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        net.minecraft.server.v1_8_R3.WorldBorder border = new net.minecraft.server.v1_8_R3.WorldBorder();
        border.setSize(6.0E7D); // Tamaño máximo para "eliminar" el borde
        
        craftPlayer.getHandle().playerConnection.sendPacket(
            new PacketPlayOutWorldBorder(border, EnumWorldBorderAction.INITIALIZE)
        );
    }
}
