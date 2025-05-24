package kk.kvlzx.data;

import org.bukkit.configuration.ConfigurationSection;
import java.util.UUID;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.config.CustomConfig;

public class StatsData {
    private final KvKnockback plugin;
    private final CustomConfig statsConfig;

    public StatsData(KvKnockback plugin) {
        this.plugin = plugin;
        this.statsConfig = new CustomConfig("stats.yml", "data", plugin);
        this.statsConfig.registerConfig();
    }

    public void saveStats(UUID uuid, PlayerStats stats) {
        String path = "stats." + uuid.toString();
        ConfigurationSection section = statsConfig.getConfig().createSection(path);
        
        section.set("kills", stats.getKills());
        section.set("deaths", stats.getDeaths());
        section.set("kdr", stats.getKDR());
        section.set("elo", stats.getElo());
        section.set("maxKillstreak", stats.getMaxKillstreak());
        section.set("playTime", stats.getPlayTimeHours());
        
        statsConfig.saveConfig();
    }

    public void loadStats(UUID uuid, PlayerStats stats) {
        String path = "stats." + uuid.toString();
        if (!statsConfig.getConfig().contains(path)) return;
        
        ConfigurationSection section = statsConfig.getConfig().getConfigurationSection(path);
        stats.setKills(section.getInt("kills"));
        stats.setDeaths(section.getInt("deaths"));
        stats.setElo(section.getInt("elo"));
        // No necesitamos cargar KDR ya que se calcula automáticamente
        // El playTime y maxKillstreak se manejan internamente
    }
}
