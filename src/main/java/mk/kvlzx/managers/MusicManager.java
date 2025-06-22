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
    private final Map<UUID, BukkitTask> playerLoopTasks = new HashMap<>(); // Nueva: tareas específicas para el loop
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

    private final int MAX_DISTANCE = MysthicKnockBack.getInstance().getMainConfig().getMaxDistanceJukebox();

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
        Location previewLoc = getValidJukeboxLocation(player);
        if (previewLoc == null) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + plugin.getMainConfig().getMusicNonSpacePreview()));
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
        
        stopMusicForPlayer(player);
        
        String simpleName = musicName.replace("records.", "");
        MusicData musicData = MUSIC_DATA.get(simpleName);
        if (musicData == null) {
            return;
        }

        Location jukeboxLoc = getValidJukeboxLocation(player);
        if (jukeboxLoc == null) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + plugin.getMainConfig().getMusicNonSpace()));
            return;
        }

        // Colocar la jukebox
        Block block = jukeboxLoc.getBlock();
        block.setType(Material.JUKEBOX);
        
        if (block.getType() != Material.JUKEBOX) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + plugin.getMainConfig().getJukeboxError()));
            return;
        }

        // Insertar el disco en la jukebox
        if (block.getState() instanceof Jukebox) {
            Jukebox jukebox = (Jukebox) block.getState();
            jukebox.setPlaying(musicData.discMaterial);
            jukebox.update();
        }

        playerJukeboxes.put(player.getUniqueId(), jukeboxLoc);
        currentMusic.put(player.getUniqueId(), simpleName);
        
        // Iniciar efectos de notas
        startNoteEffects(jukeboxLoc);
        
        // Iniciar el ciclo de música
        startMusicLoop(player, simpleName, jukeboxLoc, musicData);
        
        // Tarea para verificar condiciones cada 10 segundos (solo verificaciones, no reinicio de música)
        BukkitTask checkTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || 
                !simpleName.equalsIgnoreCase(plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId()))) {
                stopMusicForPlayer(player);
                return;
            }

            // Verificar distancia
            if (jukeboxLoc.distance(player.getLocation()) > MAX_DISTANCE) {
                player.sendMessage(MessageUtils.getColor(plugin.getMainConfig().getMoveTooFar()));
                stopMusicForPlayer(player);
                return;
            }
            
            // Verificar si la jukebox sigue ahí
            Block jukeboxBlock = jukeboxLoc.getBlock();
            if (jukeboxBlock.getType() != Material.JUKEBOX) {
                stopMusicForPlayer(player);
                return;
            }
            
        }, 0L, 40L); // Verificar cada 2 segundos (40 ticks)

        playerTasks.put(player.getUniqueId(), checkTask);
    }

    private void startMusicLoop(Player player, String simpleName, Location jukeboxLoc, MusicData musicData) {
        // Cancelar el loop anterior si existe
        BukkitTask oldLoopTask = playerLoopTasks.remove(player.getUniqueId());
        if (oldLoopTask != null) {
            oldLoopTask.cancel();
        }

        // Programar el reinicio exactamente cuando termine la canción
        BukkitTask loopTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Verificar que el jugador sigue online y quiere esta música
            if (!player.isOnline() || 
                !simpleName.equalsIgnoreCase(plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId()))) {
                return;
            }

            // Verificar que la jukebox sigue ahí
            Block jukeboxBlock = jukeboxLoc.getBlock();
            if (jukeboxBlock.getType() != Material.JUKEBOX) {
                return;
            }

            // Verificar distancia
            if (jukeboxLoc.distance(player.getLocation()) > MAX_DISTANCE) {
                return;
            }

            // Reiniciar la música
            if (jukeboxBlock.getState() instanceof Jukebox) {
                Jukebox jukebox = (Jukebox) jukeboxBlock.getState();
                jukebox.setPlaying(musicData.discMaterial);
                jukebox.update();
                
                // Programar el siguiente ciclo
                startMusicLoop(player, simpleName, jukeboxLoc, musicData);
            }
            
        }, musicData.duration * 20L); // Convertir segundos a ticks (20 ticks = 1 segundo)

        playerLoopTasks.put(player.getUniqueId(), loopTask);
    }

    private Location getValidJukeboxLocation(Player player) {
        // Obtener la ubicación inicial (3 bloques en la dirección que mira el jugador)
        Location baseLocation = player.getLocation().add(player.getLocation().getDirection().multiply(3));
        
        // Buscar múltiples ubicaciones posibles
        Location[] candidateLocations = {
            baseLocation.clone(),
            baseLocation.clone().add(1, 0, 0),
            baseLocation.clone().add(-1, 0, 0),
            baseLocation.clone().add(0, 0, 1),
            baseLocation.clone().add(0, 0, -1),
            baseLocation.clone().add(1, 0, 1),
            baseLocation.clone().add(-1, 0, -1),
            baseLocation.clone().add(1, 0, -1),
            baseLocation.clone().add(-1, 0, 1),
            // Ubicaciones más cercanas al jugador si las anteriores fallan
            player.getLocation().clone().add(2, 0, 0),
            player.getLocation().clone().add(-2, 0, 0),
            player.getLocation().clone().add(0, 0, 2),
            player.getLocation().clone().add(0, 0, -2)
        };
        
        for (Location candidate : candidateLocations) {
            Location groundLocation = findGroundLocation(candidate);
            if (groundLocation != null && canPlaceJukebox(groundLocation)) {
                return groundLocation;
            }
        }
        
        return null;
    }

    private Location findGroundLocation(Location startLocation) {
        // Buscar el suelo hacia abajo desde la ubicación inicial
        Location searchLocation = startLocation.clone();
        
        // Primero, subir hasta encontrar aire si estamos dentro de un bloque
        while (searchLocation.getBlock().getType() != Material.AIR && searchLocation.getY() < 256) {
            searchLocation.add(0, 1, 0);
        }
        
        // Luego, buscar hacia abajo hasta encontrar un bloque sólido
        for (int y = (int) searchLocation.getY(); y >= 0; y--) {
            searchLocation.setY(y);
            Block currentBlock = searchLocation.getBlock();
            Block belowBlock = searchLocation.clone().add(0, -1, 0).getBlock();
            
            // Si el bloque actual es aire y el de abajo es sólido, esta es una buena ubicación
            if (currentBlock.getType() == Material.AIR && belowBlock.getType().isSolid()) {
                return searchLocation.clone();
            }
        }
        
        return null;
    }

    private boolean canPlaceJukebox(Location location) {
        
        Block block = location.getBlock();
        Block below = block.getRelative(BlockFace.DOWN);
        
        if (block.getType() != Material.AIR && block.getType() != Material.JUKEBOX) {
            return false;
        }
        
        if (!below.getType().isSolid()) {
            return false;
        }

        // Verificar jukeboxes cercanas
        for (Location existingLoc : playerJukeboxes.values()) {
            if (existingLoc.getWorld().equals(location.getWorld()) && 
                existingLoc.distance(location) < 2) {
                return false;
            }
        }

        return true;
    }

    private void startNoteEffects(Location jukeboxLoc) {
        BukkitTask noteTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (jukeboxLoc.getBlock().getType() == Material.JUKEBOX) {
                jukeboxLoc.getWorld().playEffect(
                    jukeboxLoc.clone().add(0.0, 1.2, 0.0), 
                    Effect.NOTE, 
                    1
                );
            }
        }, 0L, 10L); // Cada 0.5 segundos

        noteEffectTasks.put(jukeboxLoc.hashCode(), noteTask);
    }

    public void stopMusicForPlayer(Player player) {
        // Cancelar la tarea de loop específica
        BukkitTask loopTask = playerLoopTasks.remove(player.getUniqueId());
        if (loopTask != null) {
            loopTask.cancel();
        }

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

        // Cancelar la tarea de verificación
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        // Limpiar registros
        currentMusic.remove(player.getUniqueId());
        playerJukeboxes.remove(player.getUniqueId());
    }

    public void onDisable() {
        // Cancelar todas las tareas de loop
        playerLoopTasks.values().forEach(BukkitTask::cancel);
        playerLoopTasks.clear();

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