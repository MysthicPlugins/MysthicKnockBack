package mk.kvlzx.data;

import org.bukkit.configuration.ConfigurationSection;
import java.util.UUID;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.config.CustomConfig;

public class StatsData {
    private final MysthicKnockBack plugin;
    private final CustomConfig statsConfig;

    public StatsData(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.statsConfig = new CustomConfig("stats.yml", "data", plugin);
        this.statsConfig.registerConfig();
    }

    public void saveStats(UUID uuid, PlayerStats stats) {
        String path = "stats." + uuid.toString();
        ConfigurationSection section = statsConfig.getConfig().createSection(path);
        
        section.set("kills", stats.getKills());
        section.set("deaths", stats.getDeaths());
        section.set("elo", stats.getElo());
        section.set("maxStreak", stats.getMaxStreak());
        section.set("currentStreak", stats.getCurrentStreak());
        section.set("playTime", stats.getPlayTime()); // Guardamos los minutos directamente
        section.set("kgcoins", stats.getKGCoins());
        
        statsConfig.saveConfig();
    }

    public void loadStats(UUID uuid, PlayerStats stats) {
        String path = "stats." + uuid.toString();
        if (!statsConfig.getConfig().contains(path)) return;
        
        ConfigurationSection section = statsConfig.getConfig().getConfigurationSection(path);
        if (section != null) {
            stats.loadStats(this);
        }
    }

    public ConfigurationSection getConfig() {
        return statsConfig.getConfig();
    }
}
