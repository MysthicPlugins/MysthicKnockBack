package mk.kvlzx.config;

import org.bukkit.configuration.file.FileConfiguration;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class MainConfig {
    private final CustomConfig configFile;

    private String prefix;
    private Long autoSaveInterval;

    public MainConfig(MysthicKnockBack plugin) {
        configFile = new CustomConfig("config.yml", "config/global", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        prefix = config.getString("config.prefix", "&b[&3KBFFA&b] "); // Valor por defecto
        autoSaveInterval = config.getLong("config.auto-save-interval", 5); // Valor por defecto 5 minutos
    }

    public void reload() {
        configFile.reloadConfig();
        loadConfig();
    }

    public String getPrefix() { 
        return prefix; 
    }
    
    public Long getAutoSaveInterval() { 
        return autoSaveInterval; 
    }
}
