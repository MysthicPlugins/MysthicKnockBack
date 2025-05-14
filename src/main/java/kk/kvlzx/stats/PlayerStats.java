package kk.kvlzx.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats {
    private static Map<UUID, PlayerStats> stats = new HashMap<>();
    private int kills;
    private int deaths;
    private int elo;

    public PlayerStats() {
        this.kills = 0;
        this.deaths = 0;
        this.elo = 1000; // ELO inicial
    }

    public static PlayerStats getStats(UUID uuid) {
        return stats.computeIfAbsent(uuid, k -> new PlayerStats());
    }

    public void addKill() {
        this.kills++;
        int eloGained = (int)(Math.random() * 10) + 6; // Random entre 6-15
        this.elo += eloGained;
    }

    public void addDeath() {
        this.deaths++;
    }

    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getElo() { return elo; }
}
