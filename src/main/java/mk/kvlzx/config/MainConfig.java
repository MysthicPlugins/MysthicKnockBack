package mk.kvlzx.config;

import org.bukkit.configuration.file.FileConfiguration;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class MainConfig {
    private final CustomConfig configFile;

    private String prefix;
    private Long autoSaveInterval;
    private Boolean chatEnabled;
    private String chatFormat;

    private Boolean endermiteEnabled;
    private Integer endermiteLimit;
    private String endermiteName;
    private Integer endermiteTime;
    private String endermiteSpawnMessage;
    private String endermiteLimitMessage;

    private String knockerId;
    private String knockerName;
    private String knockerLore;
    private Boolean knockerKnockback;
    private Integer knockerKnockbackLevel;
    private String blocksId;
    private String blocksName;
    private String blocksLore;
    private Boolean blocksKnockback;
    private Integer blocksKnockbackLevel;
    private String bowId;
    private String bowName;
    private String bowLore;
    private Boolean bowKnockback;
    private Integer bowKnockbackLevel;
    private String plateId;
    private String plateName;
    private String plateLore;
    private Boolean plateKnockback;
    private Integer plateKnockbackLevel;
    private String featherId;
    private String featherName;
    private String featherLore;
    private Boolean featherKnockback;
    private Integer featherKnockbackLevel;
    private String pearlId;
    private String pearlName;
    private String pearlLore;
    private Boolean pearlKnockback;
    private Integer pearlKnockbackLevel;
    private String arrowId;
    private String arrowName;
    private String arrowLore;
    private Boolean arrowKnockback;
    private Integer arrowKnockbackLevel;

    private Integer defaultElo;
    private Long combatLog;

    public MainConfig(MysthicKnockBack plugin) {
        configFile = new CustomConfig("config.yml", "config/global", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        prefix = config.getString("config.prefix", "&b[&3KBFFA&b] "); // Valor por defecto
        autoSaveInterval = config.getLong("config.auto-save-interval", 5); // Valor por defecto 5 minutos

        chatEnabled = config.getBoolean("config.chat.enabled");
        chatFormat = config.getString("config.chat.format");

        endermiteEnabled = config.getBoolean("config.endermite.enabled");
        endermiteLimit = config.getInt("config.endermite.limit");
        endermiteName = config.getString("config.endermite.name");
        endermiteTime = config.getInt("config.endermite.time");
        endermiteSpawnMessage = config.getString("config.endermite.messages.spawn");
        endermiteLimitMessage = config.getString("config.endermite.messages.limit");

        knockerId = config.getString("config.hotbar.default.items.knocker.id");
        knockerName = config.getString("config.hotbar.default.items.knocker.name");
        knockerLore = config.getString("config.hotbar.default.items.knocker.lore");
        knockerKnockback = config.getBoolean("config.hotbar.default.items.knocker.knockback.enabled");
        knockerKnockbackLevel = config.getInt("config.hotbar.default.items.knocker.knockback.level");
        blocksId = config.getString("config.hotbar.default.items.blocks.id");
        blocksName = config.getString("config.hotbar.default.items.blocks.name");
        blocksLore = config.getString("config.hotbar.default.items.blocks.lore");
        blocksKnockback = config.getBoolean("config.hotbar.default.items.blocks.knockback.enabled");
        blocksKnockbackLevel = config.getInt("config.hotbar.default.items.blocks.knockback.level");
        bowId = config.getString("config.hotbar.default.items.bow.id");
        bowName = config.getString("config.hotbar.default.items.bow.name");
        bowLore = config.getString("config.hotbar.default.items.bow.lore");
        bowKnockback = config.getBoolean("config.hotbar.default.items.bow.knockback.enabled");
        bowKnockbackLevel = config.getInt("config.hotbar.default.items.bow.knockback.level");
        plateId = config.getString("config.hotbar.default.items.plate.id");
        plateName = config.getString("config.hotbar.default.items.plate.name");
        plateLore = config.getString("config.hotbar.default.items.plate.lore");
        plateKnockback = config.getBoolean("config.hotbar.default.items.plate.knockback.enabled");
        plateKnockbackLevel = config.getInt("config.hotbar.default.items.plate.knockback.level");
        featherId = config.getString("config.hotbar.default.items.feather.id");
        featherName = config.getString("config.hotbar.default.items.feather.name");
        featherLore = config.getString("config.hotbar.default.items.feather.lore");
        featherKnockback = config.getBoolean("config.hotbar.default.items.feather.knockback.enabled");
        featherKnockbackLevel = config.getInt("config.hotbar.default.items.feather.knockback.level");
        pearlId = config.getString("config.hotbar.default.items.pearl.id");
        pearlName = config.getString("config.hotbar.default.items.pearl.name");
        pearlLore = config.getString("config.hotbar.default.items.pearl.lore");
        pearlKnockback = config.getBoolean("config.hotbar.default.items.pearl.knockback.enabled");
        pearlKnockbackLevel = config.getInt("config.hotbar.default.items.pearl.knockback.level");
        arrowId = config.getString("config.hotbar.default.items.arrow.id");
        arrowName = config.getString("config.hotbar.default.items.arrow.name");
        arrowLore = config.getString("config.hotbar.default.items.arrow.lore");
        arrowKnockback = config.getBoolean("config.hotbar.default.items.arrow.knockback.enabled");
        arrowKnockbackLevel = config.getInt("config.hotbar.default.items.arrow.knockback.level");

        defaultElo = config.getInt("config.combat.default-elo");
        combatLog = config.getLong("config.combat.combat-log");
    }

    public void reload() {
        configFile.reloadConfig();
        loadConfig();
    }

    public String getPrefix() { return prefix;  }
    public Long getAutoSaveInterval() { return autoSaveInterval; }
    public Boolean getChatEnabled() { return chatEnabled; }
    public String getChatFormat() { return chatFormat; }

    public Boolean getEndermiteEnabled() { return endermiteEnabled; }
    public Integer getEndermiteLimit() { return endermiteLimit; }
    public String getEndermiteName() { return endermiteName; }
    public Integer getEndermiteTime() { return endermiteTime; }
    public String getEndermiteSpawnMessage() { return endermiteSpawnMessage; }
    public String getEndermiteLimitMessage() { return endermiteLimitMessage; }
    
    public String getKnockerId() { return knockerId; }
    public String getKnockerName() { return knockerName; }
    public String getKnockerLore() { return knockerLore; }
    public Boolean getKnockerKnockback() { return knockerKnockback; }
    public Integer getKnockerKnockbackLevel() { return knockerKnockbackLevel; }
    public String getBlocksId() { return blocksId; }
    public String getBlocksName() { return blocksName; }
    public String getBlocksLore() { return blocksLore; }
    public Boolean getBlocksKnockback() { return blocksKnockback; }
    public Integer getBlocksKnockbackLevel() { return blocksKnockbackLevel; }
    public String getBowId() { return bowId; }
    public String getBowName() { return bowName; }
    public String getBowLore() { return bowLore; }
    public Boolean getBowKnockback() { return bowKnockback; }
    public Integer getBowKnockbackLevel() { return bowKnockbackLevel; }
    public String getPlateId() { return plateId; }
    public String getPlateName() { return plateName; }
    public String getPlateLore() { return plateLore; }
    public Boolean getPlateKnockback() { return plateKnockback; }
    public Integer getPlateKnockbackLevel() { return plateKnockbackLevel; }
    public String getFeatherId() { return featherId; }
    public String getFeatherName() { return featherName; }
    public String getFeatherLore() { return featherLore; }
    public Boolean getFeatherKnockback() { return featherKnockback; }
    public Integer getFeatherKnockbackLevel() { return featherKnockbackLevel; }
    public String getPearlId() { return pearlId; }
    public String getPearlName() { return pearlName; }
    public String getPearlLore() { return pearlLore; }
    public Boolean getPearlKnockback() { return pearlKnockback; }
    public Integer getPearlKnockbackLevel() { return pearlKnockbackLevel; }
    public String getArrowId() { return arrowId; }
    public String getArrowName() { return arrowName; }
    public String getArrowLore() { return arrowLore; }
    public Boolean getArrowKnockback() { return arrowKnockback; }
    public Integer getArrowKnockbackLevel() { return arrowKnockbackLevel; }

    public Integer getDefaultElo() { return defaultElo; }
    public Long getCombatLog() { return combatLog; }
}
