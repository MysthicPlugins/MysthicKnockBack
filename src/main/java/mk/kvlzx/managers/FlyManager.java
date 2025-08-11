package mk.kvlzx.managers;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.arena.Zone;

public class FlyManager {
    private final MysthicKnockBack plugin;
    private final Set<UUID> flyingPlayers = ConcurrentHashMap.newKeySet();
    
    public FlyManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        startFlyChecker();
    }
    
    /**
     * Añade un jugador a la lista de vuelo
     */
    public void addFlyingPlayer(UUID playerId) {
        flyingPlayers.add(playerId);
    }
    
    /**
     * Remueve un jugador de la lista de vuelo
     */
    public void removeFlyingPlayer(UUID playerId) {
        flyingPlayers.remove(playerId);
    }
    
    /**
     * Verifica si un jugador está volando
     */
    public boolean isFlyingPlayer(UUID playerId) {
        return flyingPlayers.contains(playerId);
    }
    
    /**
     * Desactiva el vuelo de un jugador
     */
    public void disableFly(Player player) {
        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            removeFlyingPlayer(player.getUniqueId());
        }
    }
    
    /**
     * Verifica si el jugador está en una zona de spawn
     */
    private boolean isPlayerInSpawnZone(Player player) {
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena == null) {
            return false;
        }

        Arena arena = plugin.getArenaManager().getArena(currentArena);
        if (arena == null) {
            return false;
        }

        Zone spawnZone = arena.getZone("spawn");
        if (spawnZone == null) {
            return false;
        }

        return spawnZone.isInside(player.getLocation());
    }
    
    /**
     * Inicia el verificador de vuelo que se ejecuta cada 2 segundos
     */
    private void startFlyChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Solo verificar jugadores que tienen fly activado y no están en creativo
                    if (player.getAllowFlight() && player.getGameMode() != GameMode.CREATIVE) {
                        // Si no está en zona de spawn, desactivar fly
                        if (!isPlayerInSpawnZone(player)) {
                            disableFly(player);
                        } else {
                            // Si está en zona de spawn, añadirlo a la lista de seguimiento
                            addFlyingPlayer(player.getUniqueId());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Cada segundo (20 ticks)
    }
    
    /**
     * Limpia el fly de todos los jugadores (para uso en shutdown o cambio de arena)
     */
    public void disableAllFly() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getAllowFlight()) {
                if (player.getGameMode() == GameMode.CREATIVE) return; 
                disableFly(player);
            }
        }
        flyingPlayers.clear();
    }
    
    /**
     * Verifica un jugador específico inmediatamente
     */
    public void checkPlayer(Player player) {
        if (player.getAllowFlight() && !isPlayerInSpawnZone(player)) {
            if (player.getGameMode() == GameMode.CREATIVE) return; 
            disableFly(player);
        }
    }
}
