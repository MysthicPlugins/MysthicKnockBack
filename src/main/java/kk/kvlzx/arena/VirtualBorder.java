package kk.kvlzx.arena;

import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_8_R3.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kk.kvlzx.KvKnockback;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class VirtualBorder {
    // Añadir una tarea programada para reenviar el borde
    private BukkitRunnable refreshTask;
    private final Set<UUID> playersWithBorder = new HashSet<>();
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
        sendBorderPackets(craftPlayer);
        playersWithBorder.add(player.getUniqueId());
        
        // Iniciar tarea de refresco si aún no existe
        if (refreshTask == null) {
            startRefreshTask();
        }
    }

    private void sendBorderPackets(CraftPlayer craftPlayer) {
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
        playersWithBorder.remove(player.getUniqueId());
    }

    private void startRefreshTask() {
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<UUID> iterator = playersWithBorder.iterator();
                while (iterator.hasNext()) {
                    UUID uuid = iterator.next();
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        sendBorderPackets((CraftPlayer) player);
                    } else {
                        iterator.remove();
                    }
                }
                
                // Si no hay jugadores, detener la tarea
                if (playersWithBorder.isEmpty()) {
                    refreshTask.cancel();
                    refreshTask = null;
                }
            }
        };
        refreshTask.runTaskTimer(KvKnockback.getInstance(), 100L, 100L); // Refrescar cada 5 segundos
    }

    public void cleanup() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        playersWithBorder.clear();
    }
}
