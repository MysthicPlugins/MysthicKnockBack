package mk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mk.kvlzx.MysthicKnockBack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MusicManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();
    
    // Duraciones de los discos en ticks (20 ticks = 1 segundo)
    private final Map<String, Integer> RECORD_DURATIONS = new HashMap<String, Integer>() {{
        put("records.far", 3480);    // 2:54 (174 segundos)
        put("records.mall", 3940);   // 3:17 (197 segundos)
        put("records.strad", 3760);  // 3:08 (188 segundos)
        put("records.cat", 3700);    // 3:05 (185 segundos)
        put("records.chirp", 3700);  // 3:05 (185 segundos)
        put("records.mellohi", 1920);// 1:36 (96 segundos)
        put("records.stal", 3000);   // 2:30 (150 segundos)
    }};

    private static final int PREVIEW_DURATION = 200; // 10 segundos (200 ticks)

    public MusicManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    public void startMusicForPlayer(Player player, String recordName) {
        stopMusicForPlayer(player);
        
        // Obtener la duración del disco (o usar un valor por defecto de 200 segundos)
        int duration = RECORD_DURATIONS.getOrDefault(recordName, 4000);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    playerTasks.remove(player.getUniqueId());
                    return;
                }

                // Obtener solo el nombre de la música sin el "records."
                String musicName = recordName.substring(recordName.lastIndexOf('.') + 1);
                // Obtener la música actual del jugador
                String currentMusic = plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId());
                
                // Comparar los nombres correctamente
                if (!currentMusic.equalsIgnoreCase(musicName)) {
                    this.cancel();
                    playerTasks.remove(player.getUniqueId());
                    return;
                }

                player.playSound(player.getLocation(), recordName, 1.0f, 1.0f);
            }
        }.runTaskTimer(plugin, 0L, duration); // Reproducir al inicio y luego cada vez que termine

        playerTasks.put(player.getUniqueId(), task);
    }

    public void stopMusicForPlayer(Player player) {
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        // Detener todos los sonidos
        player.playSound(player.getLocation(), "", 1.0f, 1.0f); // Sonido vacío para detener
    }

    public void playPreviewMusic(Player player, String sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        
        // Programar que se detenga el sonido después de 10 segundos
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.playSound(player.getLocation(), "", 1.0f, 1.0f); // Detener el sonido
        }, PREVIEW_DURATION);
    }

    public void onDisable() {
        playerTasks.values().forEach(BukkitTask::cancel);
        playerTasks.clear();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            stopMusicForPlayer(player);
        }
    }
}
