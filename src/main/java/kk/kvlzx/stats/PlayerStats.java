package kk.kvlzx.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.data.StatsData;
import kk.kvlzx.managers.RankManager;
import kk.kvlzx.utils.MessageUtils;

public class PlayerStats {
    private static Map<UUID, PlayerStats> stats = new HashMap<>();
    private static final Map<UUID, ScoreboardTeam> teams = new HashMap<>();
    private static final Scoreboard scoreboard = new Scoreboard();
    private final UUID uuid;
    private int kills;
    private int deaths;
    private int elo;
    private int currentStreak;
    private int maxStreak;
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

    public String getMvpTag() {
        if (currentStreak < 5) return null; // No mostrar tag si tiene menos de 5 kills
        if (currentStreak >= 500) return "&5MVP+";
        else if (currentStreak >= 300) return "&cMVP";
        else if (currentStreak >= 250) return "&6MVP";
        else if (currentStreak >= 200) return "&eMVP";
        else if (currentStreak >= 150) return "&bMVP";
        else if (currentStreak >= 100) return "&aMVP";
        else if (currentStreak >= 80) return "&9MVP";
        else if (currentStreak >= 60) return "&dMVP";
        else if (currentStreak >= 40) return "&7MVP";
        return "&8MVP"; // Tag básico para rachas entre 5 y 39
    }

    private void updateNametag(Player player) {
        if (player == null) return;
        
        String teamName = player.getName().substring(0, Math.min(player.getName().length(), 12));
        ScoreboardTeam team = teams.computeIfAbsent(uuid, k -> {
            ScoreboardTeam newTeam = new ScoreboardTeam(scoreboard, teamName);
            newTeam.setNameTagVisibility(ScoreboardTeam.EnumNameTagVisibility.ALWAYS);
            return newTeam;
        });

        // Solo mostrar tag y racha si tiene 5 o más kills
        if (currentStreak >= 5) {
            String mvpTag = getMvpTag();
            String belowName = MessageUtils.getColor(mvpTag + " " + currentStreak + " &7⚔");
            team.setPrefix("");
            team.setSuffix(belowName);
        } else {
            team.setPrefix("");
            team.setSuffix("");
        }

        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(team, 2);
        for (Player online : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void addKill() {
        this.kills++;
        this.currentStreak++;
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
        }
        
        int eloGained = (int)(Math.random() * 10) + 6; // Random entre 6-15
        this.elo += eloGained;

        // Notificar racha si es múltiplo de 5
        if (currentStreak > 0 && currentStreak % 5 == 0) {
            Bukkit.broadcastMessage(MessageUtils.getColor("&e" + Bukkit.getPlayer(uuid).getName() + 
                " &fha alcanzado una racha de &a" + currentStreak + " &akills!"));
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getWorld().playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
            }
        }

        // Actualizar nametag
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            RankManager.updatePlayerRank(player, this.elo);
            updateNametag(player);
        }
    }

    public void resetStreak() {
        if (currentStreak >= 5) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Bukkit.broadcastMessage(MessageUtils.getColor("&c☠ &f" + player.getName() + 
                    " &7perdió su racha de &c" + currentStreak + " &7kills! &c☠"));
                player.playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 1.0f, 1.0f);
            }
        }
        currentStreak = 0;
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            updateNametag(player);
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

    public int getMaxStreak() {
        return maxStreak;
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

    public int getCurrentStreak() {
        return currentStreak;
    }

    public static void cleanup() {
        // Limpiar todos los teams al desactivar
        for (ScoreboardTeam team : teams.values()) {
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(team, 1);
            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        }
        teams.clear();
    }
}
