package kk.kvlzx.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.data.StatsData;
import kk.kvlzx.managers.RankManager;

public class PlayerStats {
    private static Map<UUID, PlayerStats> stats = new HashMap<>();
    private final UUID uuid;
    private int kills;
    private int deaths;
    private int elo;
    private long playTime; // Tiempo en milisegundos
    private long lastJoin;
    private long lastDeathTime = 0;
    private static final long DEATH_COOLDOWN = 500; // 500ms cooldown
    private static StatsData statsData;

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
        this.kills = 0;
        this.deaths = 0;
        this.elo = 500; // ELO inicial modificado a 500
        this.playTime = 0;
        this.lastJoin = System.currentTimeMillis();
    }

    public static PlayerStats getStats(UUID uuid) {
        return stats.computeIfAbsent(uuid, k -> new PlayerStats(k));
    }

    public static Set<UUID> getAllStats() {
        return stats.keySet();
    }

    public static void initializeStatsData(KvKnockback plugin) {
        statsData = new StatsData(plugin);
    }

    public static void loadAllStats() {
        if (statsData == null) return;
        for (UUID uuid : stats.keySet()) {
            statsData.loadStats(uuid, stats.get(uuid));
        }
    }

    public static void saveAllStats() {
        if (statsData == null) return;
        
        // Solo guardar stats de jugadores que han estado online
        stats.forEach((uuid, playerStats) -> {
            try {
                statsData.saveStats(uuid, playerStats);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void saveStats() {
        if (statsData != null) {
            statsData.saveStats(uuid, this);
        }
    }

    public void addKill() {
        this.kills++;
        int eloGained = (int)(Math.random() * 10) + 6; // Random entre 6-15
        this.elo += eloGained;
        
        // Actualizar rango si el jugador está online
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            RankManager.updatePlayerRank(player, this.elo);
        }
    }

    public boolean canDie() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastDeathTime >= DEATH_COOLDOWN;
    }
    
    public void addDeath() {
        long currentTime = System.currentTimeMillis();
        if (!canDie()) {
            return; // Evitar muertes duplicadas
        }
        lastDeathTime = currentTime;
        
        this.deaths++;
        int eloLost = (int)(Math.random() * 10) + 6; // Random entre 6-15
        this.elo = Math.max(0, this.elo - eloLost); // Evita que el ELO sea negativo
        
        // Actualizar rango si el jugador está online
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            RankManager.updatePlayerRank(player, this.elo);
        }
    }

    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getElo() { return elo; }
    public double getKDR() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public int getMaxKillstreak() {
        try {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                return KvKnockback.getInstance().getStreakManager().getStreak(player).getMaxKillstreak();
            }
        } catch (Exception e) {
            // Si hay error, retornar 0
        }
        return 0;
    }

    public void setElo(int elo) {
        this.elo = Math.max(0, elo);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            RankManager.updatePlayerRank(player, this.elo);
        }
    }

    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
    }

    public void setDeaths(int deaths) {
        this.deaths = Math.max(0, deaths);
    }

    public void updatePlayTime() {
        long now = System.currentTimeMillis();
        long timePassed = now - lastJoin;
        playTime += timePassed;
        lastJoin = now;
    }

    public String getFormattedPlayTime() {
        long totalMinutes = playTime / (1000 * 60);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dh %02dm", hours, minutes);
    }

    public double getPlayTimeHours() {
        return playTime / (1000.0 * 60 * 60);
    }
}
