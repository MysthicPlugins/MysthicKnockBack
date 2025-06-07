package mk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import mk.kvlzx.MysthicKnockBack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MusicManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();
    private final Map<UUID, String> currentMusic = new HashMap<>();
    
    private final Map<String, MusicData> MUSIC_DATA = new HashMap<>();

    private static class MusicData {
        final String nmsName;
        final int duration;
        final float volume;
        final float pitch;

        MusicData(String nmsName, int duration, float volume, float pitch) {
            this.nmsName = nmsName;
            this.duration = duration;
            this.volume = volume;
            this.pitch = pitch;
        }
    }

    public MusicManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        initializeMusicData();
    }

    private void initializeMusicData() {
        // Registrar datos de música con nombres NMS
        MUSIC_DATA.put("far", new MusicData("records.far", 174, 1.0f, 1.0f));
        MUSIC_DATA.put("mall", new MusicData("records.mall", 197, 1.0f, 1.0f));
        MUSIC_DATA.put("strad", new MusicData("records.strad", 188, 1.0f, 1.0f));
        MUSIC_DATA.put("cat", new MusicData("records.cat", 185, 1.0f, 1.0f));
        MUSIC_DATA.put("chirp", new MusicData("records.chirp", 185, 1.0f, 1.0f));
        MUSIC_DATA.put("mellohi", new MusicData("records.mellohi", 96, 1.0f, 1.0f));
        MUSIC_DATA.put("stal", new MusicData("records.stal", 150, 1.0f, 1.0f));
    }

    public void startMusicForPlayer(Player player, String musicName) {
        stopMusicForPlayer(player);
        
        String simpleName = musicName.replace("records.", "");
        MusicData musicData = MUSIC_DATA.get(simpleName);
        if (musicData == null) return;

        currentMusic.put(player.getUniqueId(), simpleName);
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || 
                !simpleName.equals(plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId()))) {
                stopMusicForPlayer(player);
                return;
            }
            
            playNMSSound(player, musicData);
            
        }, 0L, musicData.duration * 20L);

        playerTasks.put(player.getUniqueId(), task);
    }

    private void playNMSSound(Player player, MusicData musicData) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(
            musicData.nmsName,
            player.getLocation().getX(),
            player.getLocation().getY(),
            player.getLocation().getZ(),
            musicData.volume,
            musicData.pitch
        );
        craftPlayer.getHandle().playerConnection.sendPacket(packet);
    }

    public void playPreviewMusic(Player player, String musicName) {
        String simpleName = musicName.replace("records.", "");
        MusicData musicData = MUSIC_DATA.get(simpleName);
        if (musicData == null) return;

        playNMSSound(player, musicData);
        
        // Detener después de 10 segundos
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            stopMusicForPlayer(player);
        }, 200L);
    }

    public void stopMusicForPlayer(Player player) {
        // Cancelar la tarea de reproducción
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        // Detener el sonido actual usando NMS
        String current = currentMusic.remove(player.getUniqueId());
        if (current != null) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(
                "system.stop_record", // Packet especial para detener música
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                1.0f,
                1.0f
            );
            craftPlayer.getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            stopMusicForPlayer(player);
        }
        playerTasks.clear();
        currentMusic.clear();
    }
}
