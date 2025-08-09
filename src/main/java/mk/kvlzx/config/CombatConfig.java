package mk.kvlzx.config;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class CombatConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    // Combat basic configuration
    private Integer defaultElo;
    private Long combatLog;
    private double featherSpeed;

    // ELO configuration
    private Integer eloMinGained;
    private Integer eloMaxGained;
    private Boolean eloGainedMessageEnabled;
    private String eloGainedMessage;
    private Integer eloMinLost;
    private Integer eloMaxLost;
    private Boolean eloLostMessageEnabled;
    private String eloLostMessage;

    // KGCoins configuration
    private Integer kgcoinsGainedMin;
    private Integer kgcoinsGainedMax;
    private Boolean kgcoinsGainedMessageEnabled;
    private String kgcoinsGainedMessage;

    // Rank multipliers
    private Map<String, RankMultiplier> rankMultipliers;

    // Knockback configuration
    private Double horizontalKnockback;
    private Double verticalKnockback;
    private Double knockbackReduction;
    private Integer knockbackSprintMultiplier;
    private Double horizontalKnockbackArrow;
    private Double sprintKnockbackArrow;
    private Double knockbackHorizontalEndermite;
    private Double knockbackVerticalEndermite;
    private Integer knockbackLevelEndermite;
    private Double maxKnockbackHorizontal;
    private Double maxKnockbackHorizontalArrow;
    private Double maxKnockbackVertical;

    // Ranks configuration
    private Map<String, RankInfo> ranks;

    public CombatConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("combat.yml", "config/global", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        // Basic combat configuration
        defaultElo = config.getInt("combat.default-elo");
        combatLog = config.getLong("combat.combat-log");
        featherSpeed = config.getDouble("combat.speed");

        // ELO configuration
        eloMinGained = config.getInt("combat.elo.gained.min");
        eloMaxGained = config.getInt("combat.elo.gained.max");
        eloGainedMessageEnabled = config.getBoolean("combat.elo.gained.message-enabled");
        eloGainedMessage = config.getString("combat.elo.gained.message");
        eloMinLost = config.getInt("combat.elo.lost.min");
        eloMaxLost = config.getInt("combat.elo.lost.max");
        eloLostMessageEnabled = config.getBoolean("combat.elo.lost.message-enabled");
        eloLostMessage = config.getString("combat.elo.lost.message");

        // KGCoins configuration
        kgcoinsGainedMin = config.getInt("combat.kgcoins.gained.min");
        kgcoinsGainedMax = config.getInt("combat.kgcoins.gained.max");
        kgcoinsGainedMessageEnabled = config.getBoolean("combat.kgcoins.gained.message-enabled");
        kgcoinsGainedMessage = config.getString("combat.kgcoins.gained.message");

        // Load rank multipliers
        loadRankMultipliers(config);

        // Knockback configuration
        horizontalKnockback = config.getDouble("combat.knockback.hit.horizontal");
        verticalKnockback = config.getDouble("combat.knockback.hit.vertical");
        knockbackReduction = config.getDouble("combat.knockback.hit.resistance-reduction");
        knockbackSprintMultiplier = config.getInt("combat.knockback.hit.sprint-multiplier");
        horizontalKnockbackArrow = config.getDouble("combat.knockback.arrow.horizontal");
        sprintKnockbackArrow = config.getDouble("combat.knockback.arrow.sprint-multiplier");
        knockbackHorizontalEndermite = config.getDouble("combat.knockback.endermite.horizontal");
        knockbackVerticalEndermite = config.getDouble("combat.knockback.endermite.vertical");
        knockbackLevelEndermite = config.getInt("combat.knockback.endermite.level");
        maxKnockbackHorizontal = config.getDouble("combat.knockback.limits.max-horizontal");
        maxKnockbackHorizontalArrow = config.getDouble("combat.knockback.limits.max-horizontal-arrow");
        maxKnockbackVertical = config.getDouble("combat.knockback.limits.max-vertical");

        // Load ranks
        loadRanks(config);
    }

    private void loadRankMultipliers(FileConfiguration config) {
        rankMultipliers = new HashMap<>();

        if (config.contains("combat.kgcoins.rank-multipliers")) {
            ConfigurationSection multipliersSection = config.getConfigurationSection("combat.kgcoins.rank-multipliers");
            if (multipliersSection != null) {
                for (String groupName : multipliersSection.getKeys(false)) {
                    ConfigurationSection groupSection = multipliersSection.getConfigurationSection(groupName);
                    if (groupSection != null) {
                        double multiplier = groupSection.getDouble("multiplier", 1.0);
                        boolean messageEnabled = groupSection.getBoolean("message-enabled", false);
                        String message = groupSection.getString("message", "");

                        if (multiplier > 1.0) { // Only add if there's actually a bonus
                            rankMultipliers.put(groupName.toLowerCase(), 
                                new RankMultiplier(multiplier, messageEnabled, message));
                        }
                    }
                }
            }
        }
    }

    private void loadRanks(FileConfiguration config) {
        ranks = new HashMap<>();

        if (config.contains("combat.ranks")) {
            ConfigurationSection ranksSection = config.getConfigurationSection("combat.ranks");
            if (ranksSection != null) {
                for (String rankName : ranksSection.getKeys(false)) {
                    ConfigurationSection rankSection = ranksSection.getConfigurationSection(rankName);
                    if (rankSection != null) {
                        String display = rankSection.getString("display", "");
                        int elo = rankSection.getInt("elo", 0);
                        ranks.put(rankName.toLowerCase(), new RankInfo(display, elo));
                    }
                }
            }
        }
    }

    public void reload() {
        configFile.reloadConfig();
        
        // Clear existing data
        if (rankMultipliers != null) {
            rankMultipliers.clear();
        }
        if (ranks != null) {
            ranks.clear();
        }
        
        loadConfig();
    }

    // ======== BASIC GETTERS ========
    public Integer getDefaultElo() { return defaultElo; }
    public Long getCombatLog() { return combatLog; }
    public double getFeatherSpeed() { return featherSpeed; }
    
    // ======== ELO GETTERS ========
    public Integer getEloMinGained() { return eloMinGained; }
    public Integer getEloMaxGained() { return eloMaxGained; }
    public Boolean getEloGainedMessageEnabled() { return eloGainedMessageEnabled; }
    public String getEloGainedMessage() { return eloGainedMessage; }
    public Integer getEloMinLost() { return eloMinLost; }
    public Integer getEloMaxLost() { return eloMaxLost; }
    public Boolean getEloLostMessageEnabled() { return eloLostMessageEnabled; }
    public String getEloLostMessage() { return eloLostMessage; }

    // ======== KGCOINS GETTERS ========
    public Integer getKgCoinsGainedMin() { return kgcoinsGainedMin; }
    public Integer getKgCoinsGainedMax() { return kgcoinsGainedMax; }
    public Boolean getKgCoinsGainedMessageEnabled() { return kgcoinsGainedMessageEnabled; }
    public String getKgCoinsGainedMessage() { return kgcoinsGainedMessage; }

    // ======== RANK MULTIPLIER METHODS ========
    public RankMultiplier getRankMultiplier(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return null;
        }
        return rankMultipliers.get(groupName.toLowerCase());
    }

    public boolean hasRankMultiplier(String groupName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            return false;
        }
        return rankMultipliers.containsKey(groupName.toLowerCase());
    }

    public Map<String, RankMultiplier> getAllRankMultipliers() {
        return new HashMap<>(rankMultipliers);
    }

    // ======== KNOCKBACK GETTERS ========
    public Double getHorizontalKnockback() { return horizontalKnockback; }
    public Double getVerticalKnockback() { return verticalKnockback; }
    public Double getKnockbackReduction() { return knockbackReduction; }
    public Integer getKnockbackSprintMultiplier() { return knockbackSprintMultiplier; }
    public Double getHorizontalKnockbackArrow() { return horizontalKnockbackArrow; }
    public Double getSprintKnockbackArrow() { return sprintKnockbackArrow; }
    public Double getKnockbackHorizontalEndermite() { return knockbackHorizontalEndermite; }
    public Double getKnockbackVerticalEndermite() { return knockbackVerticalEndermite; }
    public Integer getKnockbackLevelEndermite() { return knockbackLevelEndermite; }
    public Double getMaxKnockbackHorizontal() { return maxKnockbackHorizontal; }
    public Double getMaxKnockbackHorizontalArrow() { return maxKnockbackHorizontalArrow; }
    public Double getMaxKnockbackVertical() { return maxKnockbackVertical; }

    // ======== RANKS GETTERS ========
    public RankInfo getRank(String rankName) {
        if (rankName == null || rankName.trim().isEmpty()) {
            return null;
        }
        return ranks.get(rankName.toLowerCase());
    }

    public Map<String, RankInfo> getAllRanks() {
        return new HashMap<>(ranks);
    }

    public String getDivineDisplay() { 
        RankInfo rank = getRank("divine");
        return rank != null ? rank.getDisplay() : "&d&l[Divine]";
    }
    public Integer getDivineElo() { 
        RankInfo rank = getRank("divine");
        return rank != null ? rank.getElo() : 30500;
    }
    public String getGrandMasterDisplay() { 
        RankInfo rank = getRank("grand_master");
        return rank != null ? rank.getDisplay() : "&4&l[Grand Master]";
    }
    public Integer getGrandMasterElo() { 
        RankInfo rank = getRank("grand_master");
        return rank != null ? rank.getElo() : 25500;
    }
    public String getGodDisplay() { 
        RankInfo rank = getRank("god");
        return rank != null ? rank.getDisplay() : "&b&l[God]";
    }
    public Integer getGodElo() { 
        RankInfo rank = getRank("god");
        return rank != null ? rank.getElo() : 20500;
    }
    public String getTitanDisplay() { 
        RankInfo rank = getRank("titan");
        return rank != null ? rank.getDisplay() : "&6&l[Titan]";
    }
    public Integer getTitanElo() { 
        RankInfo rank = getRank("titan");
        return rank != null ? rank.getElo() : 18500;
    }
    public String getImmortalDisplay() { 
        RankInfo rank = getRank("immortal");
        return rank != null ? rank.getDisplay() : "&5&l[Immortal]";
    }
    public Integer getImmortalElo() { 
        RankInfo rank = getRank("immortal");
        return rank != null ? rank.getElo() : 16500;
    }
    public String getSupremeDisplay() { 
        RankInfo rank = getRank("supreme");
        return rank != null ? rank.getDisplay() : "&c&l[Supreme]";
    }
    public Integer getSupremeElo() { 
        RankInfo rank = getRank("supreme");
        return rank != null ? rank.getElo() : 14500;
    }
    public String getMythicDisplay() { 
        RankInfo rank = getRank("mythic");
        return rank != null ? rank.getDisplay() : "&6&l[Mythic]";
    }
    public Integer getMythicElo() { 
        RankInfo rank = getRank("mythic");
        return rank != null ? rank.getElo() : 12500;
    }
    public String getLegendDisplay() { 
        RankInfo rank = getRank("legend");
        return rank != null ? rank.getDisplay() : "&e&l[Legend]";
    }
    public Integer getLegendElo() { 
        RankInfo rank = getRank("legend");
        return rank != null ? rank.getElo() : 9500;
    }
    public String getHeroDisplay() { 
        RankInfo rank = getRank("hero");
        return rank != null ? rank.getDisplay() : "&a&l[Hero]";
    }
    public Integer getHeroElo() { 
        RankInfo rank = getRank("hero");
        return rank != null ? rank.getElo() : 8500;
    }
    public String getChampionDisplay() { 
        RankInfo rank = getRank("champion");
        return rank != null ? rank.getDisplay() : "&2&l[Champion]";
    }
    public Integer getChampionElo() { 
        RankInfo rank = getRank("champion");
        return rank != null ? rank.getElo() : 7500;
    }
    public String getMasterDisplay() { 
        RankInfo rank = getRank("master");
        return rank != null ? rank.getDisplay() : "&9&l[Master]";
    }
    public Integer getMasterElo() { 
        RankInfo rank = getRank("master");
        return rank != null ? rank.getElo() : 6500;
    }
    public String getEliteDisplay() { 
        RankInfo rank = getRank("elite");
        return rank != null ? rank.getDisplay() : "&1&l[Elite]";
    }
    public Integer getEliteElo() { 
        RankInfo rank = getRank("elite");
        return rank != null ? rank.getElo() : 5500;
    }
    public String getVeteranDisplay() { 
        RankInfo rank = getRank("veteran");
        return rank != null ? rank.getDisplay() : "&8&l[Veteran]";
    }
    public Integer getVeteranElo() { 
        RankInfo rank = getRank("veteran");
        return rank != null ? rank.getElo() : 4500;
    }
    public String getCompetitorDisplay() { 
        RankInfo rank = getRank("competitor");
        return rank != null ? rank.getDisplay() : "&7&l[Competitor]";
    }
    public Integer getCompetitorElo() { 
        RankInfo rank = getRank("competitor");
        return rank != null ? rank.getElo() : 3500;
    }
    public String getApprenticeDisplay() { 
        RankInfo rank = getRank("apprentice");
        return rank != null ? rank.getDisplay() : "&f&l[Apprentice]";
    }
    public Integer getApprenticeElo() { 
        RankInfo rank = getRank("apprentice");
        return rank != null ? rank.getElo() : 2000;
    }
    public String getNoviceDisplay() { 
        RankInfo rank = getRank("novice");
        return rank != null ? rank.getDisplay() : "&7[Novice]";
    }
    public Integer getNoviceElo() { 
        RankInfo rank = getRank("novice");
        return rank != null ? rank.getElo() : 1000;
    }
    public String getRandomDisplay() { 
        RankInfo rank = getRank("random");
        return rank != null ? rank.getDisplay() : "&8[Random]";
    }
    public Integer getRandomElo() { 
        RankInfo rank = getRank("random");
        return rank != null ? rank.getElo() : 500;
    }

    // ======== UTILITY METHODS ========
    public int calculateCoinsWithMultiplier(String playerGroup, int baseCoins) {
        RankMultiplier multiplier = getRankMultiplier(playerGroup);
        if (multiplier != null) {
            return (int) Math.round(baseCoins * multiplier.getMultiplier());
        }
        return baseCoins;
    }

    public int getBonusCoins(String playerGroup, int baseCoins) {
        RankMultiplier multiplier = getRankMultiplier(playerGroup);
        if (multiplier != null) {
            int totalCoins = calculateCoinsWithMultiplier(playerGroup, baseCoins);
            return totalCoins - baseCoins;
        }
        return 0;
    }

    // ======== INNER CLASSES ========
    public static class RankMultiplier {
        private final double multiplier;
        private final boolean messageEnabled;
        private final String message;

        public RankMultiplier(double multiplier, boolean messageEnabled, String message) {
            this.multiplier = multiplier;
            this.messageEnabled = messageEnabled;
            this.message = message != null ? message : "";
        }

        public double getMultiplier() { return multiplier; }
        public boolean isMessageEnabled() { return messageEnabled; }
        public String getMessage() { return message; }
    }

    public static class RankInfo {
        private final String display;
        private final int elo;

        public RankInfo(String display, int elo) {
            this.display = display != null ? display : "";
            this.elo = elo;
        }

        public String getDisplay() { return display; }
        public int getElo() { return elo; }
    }
}
