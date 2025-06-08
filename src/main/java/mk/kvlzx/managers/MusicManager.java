package mk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

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
        final Material discMaterial;
        final int duration;

        MusicData(Material discMaterial, int duration) {
            this.discMaterial = discMaterial;
            this.duration = duration;
        }
    }

    private final int MAX_DISTANCE = 30;

    public MusicManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        initializeMusicData();
    }

    private void initializeMusicData() {
        MUSIC_DATA.put("far", new MusicData(Material.RECORD_5, 174));      // Far
        MUSIC_DATA.put("mall", new MusicData(Material.RECORD_6, 197));     // Mall
        MUSIC_DATA.put("strad", new MusicData(Material.RECORD_9, 188));    // Strad
        MUSIC_DATA.put("cat", new MusicData(Material.GREEN_RECORD, 185));  // Cat
        MUSIC_DATA.put("chirp", new MusicData(Material.RECORD_4, 185));    // Chirp
        MUSIC_DATA.put("mellohi", new MusicData(Material.RECORD_7, 96));   // Mellohi
        MUSIC_DATA.put("stal", new MusicData(Material.RECORD_8, 150));     // Stal
    }

    public void playPreviewMusic(Player player, String musicName) {
        stopMusicForPlayer(player);
        
        String simpleName = musicName.replace("records.", "");
        MusicData musicData = MUSIC_DATA.get(simpleName);
        if (musicData == null) return;

        // Para preview, crear una jukebox temporal solo por 10 segundos
        Location previewLoc = getJukeboxLocation(player);
        if (!canPlaceJukebox(previewLoc)) {
            player.sendMessage(MessageUtils.getColor("&cNo hay espacio para la preview de música."));
            return;
        }

        // Colocar jukebox temporal
        Block block = previewLoc.getBlock();
        block.setType(Material.JUKEBOX);

        // Insertar el disco
        if (block.getState() instanceof Jukebox) {
            Jukebox jukebox = (Jukebox) block.getState();
            jukebox.setPlaying(musicData.discMaterial);
            jukebox.update();
        }

        currentMusic.put(player.getUniqueId(), simpleName);
        playerJukeboxes.put(player.getUniqueId(), previewLoc);
        
        // Detener después de 10 segundos
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            stopMusicForPlayer(player);
        }, 200L);
        
        playerTasks.put(player.getUniqueId(), task);
        startNoteEffects(previewLoc);
    }

    public void startMusicForPlayer(Player player, String musicName) {
        plugin.getLogger().info("Iniciando música para " + player.getName() + ": " + musicName);
        
        stopMusicForPlayer(player);
        
        String simpleName = musicName.replace("records.", "");
        MusicData musicData = MUSIC_DATA.get(simpleName);
        if (musicData == null) {
            plugin.getLogger().warning("No se encontró la música: " + simpleName);
            return;
        }

        final Location[] jukeboxLoc = new Location[1];
        jukeboxLoc[0] = getJukeboxLocation(player);

        plugin.getLogger().info("Intentando colocar jukebox en: " + jukeboxLoc[0]);

        if (!canPlaceJukebox(jukeboxLoc[0])) {
            plugin.getLogger().info("Buscando ubicación alternativa...");
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

        // Colocar la jukebox
        Block block = jukeboxLoc[0].getBlock();
        block.setType(Material.JUKEBOX);
        
        plugin.getLogger().info("Jukebox colocada en: " + jukeboxLoc[0]);
        
        if (block.getType() != Material.JUKEBOX) {
            plugin.getLogger().warning("Error al colocar la jukebox en: " + jukeboxLoc[0]);
            player.sendMessage(MessageUtils.getColor("&cError al colocar la jukebox."));
            return;
        }

        // Insertar el disco en la jukebox
        if (block.getState() instanceof Jukebox) {
            Jukebox jukebox = (Jukebox) block.getState();
            jukebox.setPlaying(musicData.discMaterial);
            jukebox.update();
        }

        playerJukeboxes.put(player.getUniqueId(), jukeboxLoc[0]);
        currentMusic.put(player.getUniqueId(), simpleName);
        
        // Iniciar efectos de notas
        startNoteEffects(jukeboxLoc[0]);
        
        // Tarea para reproducir la música en loop y verificar condiciones
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || 
                !simpleName.equalsIgnoreCase(plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId()))) {
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
            
            // Verificar si la jukebox sigue ahí y reproducir
            Block jukeboxBlock = jukeboxLoc[0].getBlock();
            if (jukeboxBlock.getType() == Material.JUKEBOX && jukeboxBlock.getState() instanceof Jukebox) {
                Jukebox jukebox = (Jukebox) jukeboxBlock.getState();
                
                // Si no está reproduciendo, reiniciar
                if (!jukebox.isPlaying()) {
                    jukebox.setPlaying(musicData.discMaterial);
                    jukebox.update();
                    plugin.getLogger().info("Reproduciendo música en jukebox para " + player.getName());
                }
            }
            
        }, 20L, 40L); // Verificar cada 2 segundos

        playerTasks.put(player.getUniqueId(), task);
        plugin.getLogger().info("Música iniciada exitosamente para " + player.getName());
    }

    private Location getJukeboxLocation(Player player) {
        return player.getLocation().add(player.getLocation().getDirection().multiply(3));
    }

    private boolean canPlaceJukebox(Location location) {
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
        // Detener efectos de notas
        Location jukeboxLoc = playerJukeboxes.get(player.getUniqueId());
        if (jukeboxLoc != null) {
            BukkitTask noteTask = noteEffectTasks.remove(jukeboxLoc.hashCode());
            if (noteTask != null) {
                noteTask.cancel();
            }

            // Remover el disco de la jukebox y luego la jukebox
            Block block = jukeboxLoc.getBlock();
            if (block != null && block.getType() == Material.JUKEBOX) {
                if (block.getState() instanceof Jukebox) {
                    Jukebox jukebox = (Jukebox) block.getState();
                    
                    // Detener la reproducción
                    jukebox.setPlaying(null);
                    jukebox.update();
                    
                    // Esperar un tick antes de remover la jukebox para asegurar que se actualice
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (block.getType() == Material.JUKEBOX) {
                            block.setType(Material.AIR);
                            jukeboxLoc.getWorld().playEffect(jukeboxLoc, Effect.STEP_SOUND, Material.JUKEBOX);
                        }
                    }, 1L);
                } else {
                    // Si por alguna razón no es una Jukebox, remover directamente
                    block.setType(Material.AIR);
                    jukeboxLoc.getWorld().playEffect(jukeboxLoc, Effect.STEP_SOUND, Material.JUKEBOX);
                }
            }
        }

        // Cancelar la tarea de reproducción
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        // Limpiar registros
        currentMusic.remove(player.getUniqueId());
        playerJukeboxes.remove(player.getUniqueId());
    }

    public void onDisable() {
        // Cancelar todas las tareas de efectos de notas
        noteEffectTasks.values().forEach(BukkitTask::cancel);
        noteEffectTasks.clear();
        
        // Asegurar que todas las jukeboxes sean removidas correctamente
        for (Location loc : playerJukeboxes.values()) {
            Block block = loc.getBlock();
            if (block.getType() == Material.JUKEBOX) {
                if (block.getState() instanceof Jukebox) {
                    Jukebox jukebox = (Jukebox) block.getState();
                    jukebox.setPlaying(null);
                    jukebox.update();
                }
                block.setType(Material.AIR);
            }
        }
        playerJukeboxes.clear();
        
        // Detener música para todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            stopMusicForPlayer(player);
        }
        playerTasks.clear();
        currentMusic.clear();
    }

    // Getter para playerJukeboxes
    public Map<UUID, Location> getPlayerJukeboxes() {
        return playerJukeboxes;
    }
}
