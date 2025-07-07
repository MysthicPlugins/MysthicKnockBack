package mk.kvlzx.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.MainConfig;
import mk.kvlzx.data.StatsData;
import mk.kvlzx.managers.RankManager;
import mk.kvlzx.managers.StreakManager;
import mk.kvlzx.utils.MessageUtils;

public class PlayerStats {
    private static Map<UUID, PlayerStats> stats = new HashMap<>();
    private final UUID uuid;
    private int kills;
    private int deaths;
    private int elo;
    private long playTime;
    private long lastJoin;
    private long lastDeathTime = 0;
    private static final long DEATH_COOLDOWN = 500;
    private static StatsData statsData;
    private int kgCoins;

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
        this.kills = 0;
        this.deaths = 0;
        this.elo = MysthicKnockBack.getInstance().getMainConfig().getDefaultElo(); // Valor por defecto
        this.playTime = 0;
        this.lastJoin = System.currentTimeMillis();
        this.kgCoins = 0;
    }

    public static PlayerStats getStats(UUID uuid) {
        return stats.computeIfAbsent(uuid, k -> new PlayerStats(k));
    }

    public static Set<UUID> getAllStats() {
        return stats.keySet();
    }

    public static void initializeStatsData(MysthicKnockBack plugin) {
        statsData = new StatsData(plugin);
    }

    public static void loadAllStats() {
        if (statsData == null) return;
        
        // Cargar datos de todos los jugadores almacenados
        ConfigurationSection statsSection = statsData.getConfig().getConfigurationSection("stats");
        if (statsSection != null) {
            for (String uuidStr : statsSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    PlayerStats playerStats = new PlayerStats(uuid);
                    statsData.loadStats(uuid, playerStats);
                    stats.put(uuid, playerStats);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveAllStats() {
        if (statsData == null) return;
        stats.forEach((uuid, playerStats) -> {
            try {
                statsData.saveStats(uuid, playerStats);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // Limpiar los ArmorStands al guardar
        StreakManager.cleanup();
    }

    public void saveStats() {
        if (statsData != null) {
            statsData.saveStats(uuid, this);
        }
    }

    public void addKill() {
        this.kills++;
        StreakManager.addStreak(uuid);
        
        // Usar valores de configuración para ELO ganado
        MainConfig config = MysthicKnockBack.getInstance().getMainConfig();
        int eloGained = (int)(Math.random() * (config.getEloMaxGained() - config.getEloMinGained() + 1)) + config.getEloMinGained();
        this.elo += eloGained;

        // Notificar al jugador con mensaje personalizado
        Player player = Bukkit.getPlayer(uuid);
        if (config.getEloGainedMessageEnabled()) {
            if (player != null && player.isOnline()) {
                String eloMessage = config.getEloGainedMessage().replace("%elo%", String.valueOf(eloGained));
                player.sendMessage(MessageUtils.getColor(eloMessage));
            }
        }

        // Usar valores de configuración para KGCoins
        int coinsGained = (int)(Math.random() * (config.getKgCoinsGainedMax() - config.getKgCoinsGainedMin() + 1)) + config.getKgCoinsGainedMin();
        this.kgCoins += coinsGained;
        
        // Notificar al jugador con mensaje personalizado
        if (config.getKgCoinsGainedMessageEnabled()) {
            if (player != null && player.isOnline()) {
                String coinsMessage = config.getKgCoinsGainedMessage().replace("%coins%", String.valueOf(coinsGained));
                player.sendMessage(MessageUtils.getColor(coinsMessage));
            }
        }

        // Actualizar el rango del jugador
        if (player != null && player.isOnline()) {
            RankManager.updatePlayerRank(player, this.elo);
        }
    }

    public void resetStreak() {
        StreakManager.resetStreak(uuid);
    }

    public int getCurrentStreak() {
        return StreakManager.getStreak(uuid);
    }

    public int getMaxStreak() {
        return StreakManager.getMaxStreak(uuid);
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
        
        // Usar valores de configuración para ELO perdido
        MainConfig config = MysthicKnockBack.getInstance().getMainConfig();
        int eloLost = (int)(Math.random() * (config.getEloMaxLost() - config.getEloMinLost() + 1)) + config.getEloMinLost();
        this.elo = Math.max(0, this.elo - eloLost);
        
        // Notificar al jugador con mensaje personalizado
        Player player = Bukkit.getPlayer(uuid);
        if (config.getEloLostMessageEnabled()) {
            if (player != null && player.isOnline()) {
                String eloLostMessage = config.getEloLostMessage().replace("%elo%", String.valueOf(eloLost));
                player.sendMessage(MessageUtils.getColor(eloLostMessage));
            }
        }
        
        // Actualizar rango si el jugador está online
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
        long timePassed = (now - lastJoin) / (1000 * 60); // Convertir a minutos
        playTime += timePassed;
        lastJoin = now;
    }

    public long getPlayTime() {
        return playTime; // Retorna el tiempo en minutos directamente
    }

    public String getFormattedPlayTime() {
        long hours = playTime / 60;
        long minutes = playTime % 60;
        return String.format("%dh %02dm", hours, minutes);
    }

    public void loadStats(StatsData data) {
        String path = "stats." + uuid.toString();
        ConfigurationSection section = data.getConfig().getConfigurationSection(path);
        if (section != null) {
            this.kills = section.getInt("kills", 0);
            this.deaths = section.getInt("deaths", 0);
            this.elo = section.getInt("elo", 500);
            StreakManager.setMaxStreak(uuid, section.getInt("maxStreak", 0));
            this.playTime = section.getLong("playTime", 0);
            this.kgCoins = section.getInt("kgcoins", 0);
        }
    }

    public int getKGCoins() {
        return kgCoins;
    }

    public void setKGCoins(int amount) {
        this.kgCoins = Math.max(0, amount);
    }

    public void addKGCoins(int amount) {
        this.kgCoins = Math.max(0, this.kgCoins + amount);
    }

    public boolean removeKGCoins(int amount) {
        if (this.kgCoins >= amount) {
            this.kgCoins -= amount;
            return true;
        }
        return false;
    }

    public UUID getUUID() {
        return uuid;
    }
}
