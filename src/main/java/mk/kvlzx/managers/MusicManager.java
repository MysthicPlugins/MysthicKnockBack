package mk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
        // Debug
        plugin.getLogger().info("Iniciando música para " + player.getName() + ": " + musicName);
        
        stopMusicForPlayer(player);
        
        String simpleName = musicName.replace("records.", "");
        MusicData musicData = MUSIC_DATA.get(simpleName);
        if (musicData == null) {
            plugin.getLogger().warning("No se encontró la música: " + simpleName);
            return;
        }

        final Location[] jukeboxLoc = new Location[1]; // Array para hacer la variable "efectivamente final"
        jukeboxLoc[0] = getJukeboxLocation(player);

        // Debug de ubicación
        plugin.getLogger().info("Intentando colocar jukebox en: " + jukeboxLoc[0]);

        if (!canPlaceJukebox(jukeboxLoc[0])) {
            plugin.getLogger().info("Buscando ubicación alternativa...");
            // Intentar posiciones alternativas
            Location[] alternativeLocations = {
                jukeboxLoc[0].clone().add(0, 1, 0),
                jukeboxLoc[0].clone().add(1, 0, 0),
                jukeboxLoc[0].clone().add(-1, 0, 0),
                jukeboxLoc[0].clone().add(0, 0, 1),
                jukeboxLoc[0].clone().add(0, 0, -1)
            };

            boolean found = false;
            for (Location alt : alternativeLocations) {
                if (canPlaceJukebox(alt)) {
                    jukeboxLoc[0] = alt;
                    found = true;
                    plugin.getLogger().info("Ubicación alternativa encontrada: " + alt);
                    break;
                }
            }

            if (!found) {
                plugin.getLogger().warning("No se encontró ubicación válida para la jukebox");
                player.sendMessage(MessageUtils.getColor("&cNo hay espacio suficiente para colocar la jukebox."));
                return;
            }
        }

        // Asegurarse de que el bloque se coloque correctamente
        Block block = jukeboxLoc[0].getBlock();
        block.setType(Material.JUKEBOX);
        
        // Debug de colocación
        plugin.getLogger().info("Jukebox colocada en: " + jukeboxLoc[0]);
        
        if (block.getType() != Material.JUKEBOX) {
            plugin.getLogger().warning("Error al colocar la jukebox en: " + jukeboxLoc[0]);
            player.sendMessage(MessageUtils.getColor("&cError al colocar la jukebox."));
            return;
        }

        playerJukeboxes.put(player.getUniqueId(), jukeboxLoc[0]);
        currentMusic.put(player.getUniqueId(), simpleName);
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || 
                !simpleName.equals(plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId()))) {
                plugin.getLogger().info("Deteniendo música por desconexión o cambio: " + player.getName());
                stopMusicForPlayer(player);
                return;
            }

            // Verificar distancia
            if (jukeboxLoc[0].distance(player.getLocation()) > MAX_DISTANCE) {
                plugin.getLogger().info(player.getName() + " se alejó demasiado de la jukebox");
                player.sendMessage(MessageUtils.getColor("&cTe has alejado demasiado de tu jukebox."));
                stopMusicForPlayer(player);
                return;
            }
            
            playNMSSound(player, musicData, jukeboxLoc[0]);
            startNoteEffects(jukeboxLoc[0]);
            
        }, 0L, musicData.duration * 20L);

        playerTasks.put(player.getUniqueId(), task);
        plugin.getLogger().info("Música iniciada exitosamente para " + player.getName());
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
        // Debug del método canPlaceJukebox
        plugin.getLogger().info("Verificando ubicación para jukebox: " + location);
        
        Block block = location.getBlock();
        Block below = block.getRelative(BlockFace.DOWN);
        
        if (block.getType() != Material.AIR && block.getType() != Material.JUKEBOX) {
            plugin.getLogger().info("Bloque ocupado: " + block.getType());
            return false;
        }
        
        if (!below.getType().isSolid()) {
            plugin.getLogger().info("No hay bloque sólido debajo: " + below.getType());
            return false;
        }

        // Verificar jukeboxes cercanas
        for (Location existingLoc : playerJukeboxes.values()) {
            if (existingLoc.getWorld().equals(location.getWorld()) && 
                existingLoc.distance(location) < 2) {
                plugin.getLogger().info("Jukebox demasiado cerca en: " + existingLoc);
                return false;
            }
        }

        plugin.getLogger().info("Ubicación válida para jukebox");
        return true;
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
        // Primero detener las tareas y efectos
        Location jukeboxLoc = playerJukeboxes.get(player.getUniqueId());
        if (jukeboxLoc != null) {
            BukkitTask noteTask = noteEffectTasks.remove(jukeboxLoc.hashCode());
            if (noteTask != null) {
                noteTask.cancel();
            }

            // Asegurarse de que el bloque exista antes de intentar removerlo
            Block block = jukeboxLoc.getBlock();
            if (block != null && block.getType() == Material.JUKEBOX) {
                block.setType(Material.AIR);
                jukeboxLoc.getWorld().playEffect(jukeboxLoc, Effect.STEP_SOUND, Material.JUKEBOX);
            }
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
