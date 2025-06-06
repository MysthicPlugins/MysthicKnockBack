package mk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;

import mk.kvlzx.MysthicKnockBack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MusicManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();

    public MusicManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    public void startMusicForPlayer(Player player, String music) {
        stopMusicForPlayer(player);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    playerTasks.remove(player.getUniqueId());
                    return;
                }

                playMusic(player, music);
            }
        }.runTaskTimer(plugin, 0L, 2400L); // 2400 ticks = 2 minutos

        playerTasks.put(player.getUniqueId(), task);
    }

    public void stopMusicForPlayer(Player player) {
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        // Detener la música usando NMS
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(
            new PacketPlayOutNamedSoundEffect("", 0, 0, 0, 0, 0)
        );
    }

    private void playMusic(Player player, String music) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(
            music,
            player.getLocation().getX(),
            player.getLocation().getY(),
            player.getLocation().getZ(),
            1.0F,  // volumen
            1.0F   // pitch
        );
        craftPlayer.getHandle().playerConnection.sendPacket(packet);
    }

    public void playPreviewMusic(Player player, String nmsSound) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(
            nmsSound,
            player.getLocation().getX(),
            player.getLocation().getY(),
            player.getLocation().getZ(),
            1.0F,  // volumen para preview
            1.0F   // pitch para preview
        );
        craftPlayer.getHandle().playerConnection.sendPacket(packet);
    }

    public void onDisable() {
        playerTasks.values().forEach(BukkitTask::cancel);
        playerTasks.clear();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            stopMusicForPlayer(player);
        }
    }
}
