package mk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MusicManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();
    private final Map<UUID, String> currentMusic = new HashMap<>();
    private final Map<UUID, Location> playerJukeboxes = new HashMap<>();
    private final Map<Integer, BukkitTask> noteEffectTasks = new HashMap<>();
    
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

    private final int MAX_DISTANCE = 30;

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

    public void playPreviewMusic(Player player, String musicName) {
        stopMusicForPlayer(player);  // Asegurar que se detenga cualquier música previa
        
        String simpleName = musicName.replace("records.", "");
        MusicData musicData = MUSIC_DATA.get(simpleName);
        if (musicData == null) return;

        // Guardar la música actual temporalmente
        currentMusic.put(player.getUniqueId(), simpleName);

        playNMSSound(player, musicData, player.getLocation());
        
        // Detener después de 10 segundos usando el mismo sistema de tasks
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            stopMusicForPlayer(player);
        }, 200L);
        
        playerTasks.put(player.getUniqueId(), task);
    }

    public void startMusicForPlayer(Player player, String musicName) {
        stopMusicForPlayer(player);
        
        String simpleName = musicName.replace("records.", "");
        MusicData musicData = MUSIC_DATA.get(simpleName);
        if (musicData == null) return;

        // Obtener ubicación 3 bloques frente al jugador
        Location jukeboxLoc = getJukeboxLocation(player);
        if (!canPlaceJukebox(jukeboxLoc)) {
            player.sendMessage(MessageUtils.getColor("&cNo hay espacio suficiente para colocar la jukebox."));
            return;
        }

        // Colocar y guardar la jukebox
        jukeboxLoc.getBlock().setType(Material.JUKEBOX);
        playerJukeboxes.put(player.getUniqueId(), jukeboxLoc);

        currentMusic.put(player.getUniqueId(), simpleName);
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || 
                !simpleName.equals(plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId()))) {
                stopMusicForPlayer(player);
                return;
            }

            // Verificar distancia
            if (jukeboxLoc.distance(player.getLocation()) > MAX_DISTANCE) {
                player.sendMessage(MessageUtils.getColor("&cTe has alejado demasiado de tu jukebox."));
                stopMusicForPlayer(player);
                return;
            }
            
            playNMSSound(player, musicData, jukeboxLoc);
            
            // Iniciar efectos de notas
            startNoteEffects(jukeboxLoc);
            
        }, 0L, musicData.duration * 20L);

        playerTasks.put(player.getUniqueId(), task);
    }

    private void playNMSSound(Player player, MusicData musicData, Location location) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(
            musicData.nmsName,
            location.getX(),
            location.getY(),
            location.getZ(),
            musicData.volume,
            musicData.pitch
        );
        craftPlayer.getHandle().playerConnection.sendPacket(packet);
    }

    private Location getJukeboxLocation(Player player) {
        return player.getLocation().add(player.getLocation().getDirection().multiply(3));
    }

    private boolean canPlaceJukebox(Location location) {
        return location.getBlock().getType() == Material.AIR &&
                location.getBlock().getRelative(BlockFace.DOWN).getType().isSolid();
    }

    private void startNoteEffects(Location jukeboxLoc) {
        BukkitTask noteTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (jukeboxLoc.getBlock().getType() == Material.JUKEBOX) {
                jukeboxLoc.getWorld().playEffect(
                    jukeboxLoc.clone().add(0.5, 1.2, 0.5), 
                    Effect.NOTE, 
                    1
                );
            }
        }, 0L, 10L); // Cada 0.5 segundos

        noteEffectTasks.put(jukeboxLoc.hashCode(), noteTask);
    }

    public void stopMusicForPlayer(Player player) {
        // Detener efectos de notas si existen
        Location jukeboxLoc = playerJukeboxes.get(player.getUniqueId());
        if (jukeboxLoc != null) {
            BukkitTask noteTask = noteEffectTasks.remove(jukeboxLoc.hashCode());
            if (noteTask != null) {
                noteTask.cancel();
            }
        }

        // Remover la jukebox física
        jukeboxLoc = playerJukeboxes.remove(player.getUniqueId());
        if (jukeboxLoc != null && jukeboxLoc.getBlock().getType() == Material.JUKEBOX) {
            jukeboxLoc.getBlock().setType(Material.AIR);
            jukeboxLoc.getWorld().playEffect(jukeboxLoc, Effect.STEP_SOUND, Material.JUKEBOX);
        }

        // Cancelar la tarea de reproducción
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        // Detener el sonido actual
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
        // Cancelar todas las tareas de efectos de notas
        noteEffectTasks.values().forEach(BukkitTask::cancel);
        noteEffectTasks.clear();
        
        // Asegurar que todas las jukeboxes sean removidas
        for (Location loc : playerJukeboxes.values()) {
            if (loc.getBlock().getType() == Material.JUKEBOX) {
                loc.getBlock().setType(Material.AIR);
            }
        }
        playerJukeboxes.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            stopMusicForPlayer(player);
        }
        playerTasks.clear();
        currentMusic.clear();
    }

    // Agregar getter para playerJukeboxes
    public Map<UUID, Location> getPlayerJukeboxes() {
        return playerJukeboxes;
    }
}
