package mk.kvlzx.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class MainConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

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

    private String skullName;
    private String skullLore;
    private Integer skullSlot;

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
    private String slimeBallId;
    private String slimeBallName;
    private String slimeBallLore;
    private Boolean slimeBallKnockback;
    private Integer slimeBallKnockbackLevel;

    private Integer defaultElo;
    private Long combatLog;
    private Integer eloMinGained;
    private Integer eloMaxGained;
    private Boolean eloGainedMessageEnabled;
    private String eloGainedMessage;
    private Integer eloMinLost;
    private Integer eloMaxLost;
    private Boolean eloLostMessageEnabled;
    private String eloLostMessage;
    private Integer kgcoinsGainedMin;
    private Integer kgcoinsGainedMax;
    private Boolean kgcoinsGainedMessageEnabled;
    private String kgcoinsGainedMessage;
    private double horizontalKnockback;
    private double verticalKnockback;
    private double knockbackReduction;
    private Integer knockbackSprintMultiplier;
    private double horizontalKnockbackArrow;
    private double sprintKnockbackArrow;
    private double knockbackHorizontalEndermite;
    private double knockbackVerticalEndermite;
    private Integer knockbackLevelEndermite;
    private double maxKnockbackHorizontal;
    private double maxKnockbackHorizontalArrow;
    private double maxKnockbackVertical;
    private String divineDisplay;
    private Integer divineElo;
    private String grandMasterDisplay;
    private Integer grandMasterElo;
    private String godDisplay;
    private Integer godElo;
    private String titanDisplay;
    private Integer titanElo;
    private String immortalDisplay;
    private Integer immortalElo;
    private String supremeDisplay;
    private Integer supremeElo;
    private String mythicDisplay;
    private Integer mythicElo;
    private String legendDisplay;
    private Integer legendElo;
    private String heroDisplay;
    private Integer heroElo;
    private String championDisplay;
    private Integer championElo;
    private String masterDisplay;
    private Integer masterElo;
    private String eliteDisplay;
    private Integer eliteElo;
    private String veteranDisplay;
    private Integer veteranElo;
    private String competitorDisplay;
    private Integer competitorElo;
    private String apprenticeDisplay;
    private Integer apprenticeElo;
    private String noviceDisplay;
    private Integer noviceElo;
    private String randomDisplay;
    private Integer randomElo;

    private Boolean joinTitleEnabled;
    private String joinTitleTitle;
    private String joinTitleSubtitle;
    private Integer joinTitleFadeIn;
    private Integer joinTitleStay;
    private Integer joinTitleFadeOut;

    private String musicNonSpacePreview;
    private String musicNonSpace;
    private String jukeboxError;
    private String moveTooFar;
    private Integer maxDistanceJukebox;

    private Integer reportCooldownTime;
    private String reportCooldownMessage;
    private String reportMessage;
    private String reportStaffMessage;

    private String streakTag40;
    private String streakTag60;
    private String streakTag80;
    private String streakTag100;
    private String streakTag150;
    private String streakTag200;
    private String streakTag250;
    private String streakTag300;
    private String streakTag500;
    private String streakArmorStandName;
    private double streakArmorStandX;
    private double streakArmorStandY;
    private double streakArmorStandZ;
    private String streakMessageReached;
    private String streakMessageLost;
    private Boolean streakTitleEnabled;
    private String streakTitleTitle;
    private String streakTitleSubtitle;
    private Integer streakTitleFadeIn;
    private Integer streakTitleStay;
    private Integer streakTitleFadeOut;

    public MainConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("config.yml", "config/global", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        prefix = config.getString("config.prefix", "&b[&3KBFFA&b] ");
        autoSaveInterval = config.getLong("config.auto-save-interval", 5);

        chatEnabled = config.getBoolean("config.chat.enabled");
        chatFormat = config.getString("config.chat.format");

        endermiteEnabled = config.getBoolean("config.endermite.enabled");
        endermiteLimit = config.getInt("config.endermite.limit");
        endermiteName = config.getString("config.endermite.name");
        endermiteTime = config.getInt("config.endermite.time");
        endermiteSpawnMessage = config.getString("config.endermite.messages.spawn");
        endermiteLimitMessage = config.getString("config.endermite.messages.limit");

        skullName = config.getString("config.items.spawn-items.skull.name");
        skullLore = config.getString("config.items.spawn-items.skull.lore");
        skullSlot = config.getInt("config.items.spawn-items.skull.slot");

        // Validar y cargar IDs de materiales con valores por defecto
        knockerId = validateAndGetMaterial(config, "config.hotbar.default.items.knocker.id", "STICK");
        knockerName = config.getString("config.hotbar.default.items.knocker.name");
        knockerLore = config.getString("config.hotbar.default.items.knocker.lore");
        knockerKnockback = config.getBoolean("config.hotbar.default.items.knocker.knockback.enabled");
        knockerKnockbackLevel = config.getInt("config.hotbar.default.items.knocker.knockback.level");
        
        blocksId = validateAndGetMaterial(config, "config.hotbar.default.items.blocks.id", "SANDSTONE");
        blocksName = config.getString("config.hotbar.default.items.blocks.name");
        blocksLore = config.getString("config.hotbar.default.items.blocks.lore");
        blocksKnockback = config.getBoolean("config.hotbar.default.items.blocks.knockback.enabled");
        blocksKnockbackLevel = config.getInt("config.hotbar.default.items.blocks.knockback.level");
        
        bowId = validateAndGetMaterial(config, "config.hotbar.default.items.bow.id", "BOW");
        bowName = config.getString("config.hotbar.default.items.bow.name");
        bowLore = config.getString("config.hotbar.default.items.bow.lore");
        bowKnockback = config.getBoolean("config.hotbar.default.items.bow.knockback.enabled");
        bowKnockbackLevel = config.getInt("config.hotbar.default.items.bow.knockback.level");
        
        plateId = validateAndGetMaterial(config, "config.hotbar.default.items.plate.id", "GOLD_PLATE");
        plateName = config.getString("config.hotbar.default.items.plate.name");
        plateLore = config.getString("config.hotbar.default.items.plate.lore");
        plateKnockback = config.getBoolean("config.hotbar.default.items.plate.knockback.enabled");
        plateKnockbackLevel = config.getInt("config.hotbar.default.items.plate.knockback.level");
        
        featherId = validateAndGetMaterial(config, "config.hotbar.default.items.feather.id", "FEATHER");
        featherName = config.getString("config.hotbar.default.items.feather.name");
        featherLore = config.getString("config.hotbar.default.items.feather.lore");
        featherKnockback = config.getBoolean("config.hotbar.default.items.feather.knockback.enabled");
        featherKnockbackLevel = config.getInt("config.hotbar.default.items.feather.knockback.level");
        
        pearlId = validateAndGetMaterial(config, "config.hotbar.default.items.pearl.id", "ENDER_PEARL");
        pearlName = config.getString("config.hotbar.default.items.pearl.name");
        pearlLore = config.getString("config.hotbar.default.items.pearl.lore");
        pearlKnockback = config.getBoolean("config.hotbar.default.items.pearl.knockback.enabled");
        pearlKnockbackLevel = config.getInt("config.hotbar.default.items.pearl.knockback.level");
        
        arrowId = validateAndGetMaterial(config, "config.hotbar.default.items.arrow.id", "ARROW");
        arrowName = config.getString("config.hotbar.default.items.arrow.name");
        arrowLore = config.getString("config.hotbar.default.items.arrow.lore");
        arrowKnockback = config.getBoolean("config.hotbar.default.items.arrow.knockback.enabled");
        arrowKnockbackLevel = config.getInt("config.hotbar.default.items.arrow.knockback.level");
        slimeBallId = validateAndGetMaterial(config, "config.hotbar.default.items.slimeball.id", "SLIME_BALL");
        slimeBallName = config.getString("config.hotbar.default.items.slimeball.name");
        slimeBallLore = config.getString("config.hotbar.default.items.slimeball.lore");
        slimeBallKnockback = config.getBoolean("config.hotbar.default.items.slimeball.knockback.enabled");
        slimeBallKnockbackLevel = config.getInt("config.hotbar.default.items.slimeball.knockback.level");

        defaultElo = config.getInt("config.combat.default-elo");
        combatLog = config.getLong("config.combat.combat-log");
        eloMinGained = config.getInt("config.combat.elo.min");
        eloMaxGained = config.getInt("config.combat.elo.max");
        eloGainedMessageEnabled = config.getBoolean("config.combat.elo.message-enabled");
        eloGainedMessage = config.getString("config.combat.elo.message");
        eloMinLost = config.getInt("config.combat.elo.death-elo-lost.min");
        eloMaxLost = config.getInt("config.combat.elo.death-elo-lost.max");
        eloLostMessageEnabled = config.getBoolean("config.combat.elo.death-elo-lost.message-enabled");
        eloLostMessage = config.getString("config.combat.elo.death-elo-lost.message");
        kgcoinsGainedMin = config.getInt("config.combat.kgcoins.min");
        kgcoinsGainedMax = config.getInt("config.combat.kgcoins.max");
        kgcoinsGainedMessageEnabled = config.getBoolean("config.combat.kgcoins.message-enabled");
        kgcoinsGainedMessage = config.getString("config.combat.kgcoins.message");
        horizontalKnockback = config.getDouble("config.combat.knockback-hit.horizontal-knockback");
        verticalKnockback = config.getDouble("config.combat.knockback-hit.vertical-knockback");
        knockbackReduction = config.getDouble("config.combat.knockback-hit.knockback-resistance-reduction");
        knockbackSprintMultiplier = config.getInt("config.combat.knockback-hit.sprint-multiplier");
        horizontalKnockbackArrow = config.getDouble("config.combat.knockback-arrow.horizontal-knockback");
        sprintKnockbackArrow = config.getDouble("config.combat.knockback-arrow.sprint-multiplier");
        knockbackHorizontalEndermite = config.getDouble("config.combat.knockback-endermite.horizontal-knockback");
        knockbackVerticalEndermite = config.getDouble("config.combat.knockback-endermite.vertical-knockback");
        knockbackLevelEndermite = config.getInt("config.combat.knockback-endermite.knockback-level");
        maxKnockbackHorizontal = config.getDouble("config.combat.knockback-limits.max-horizontal-knockback");
        maxKnockbackHorizontalArrow = config.getDouble("config.combat.knockback-limits.max-horizontal-knockback-arrow");
        maxKnockbackVertical = config.getDouble("config.combat.knockback-limits.max-vertical-knockback");
        divineDisplay = config.getString("config.combat.ranks.divine.display");
        divineElo = config.getInt("config.combat.ranks.divine.elo");
        grandMasterDisplay = config.getString("config.combat.ranks.grand_master.display");
        grandMasterElo = config.getInt("config.combat.ranks.grand_master.elo");
        godDisplay = config.getString("config.combat.ranks.god.display");
        godElo = config.getInt("config.combat.ranks.god.elo");
        titanDisplay = config.getString("config.combat.ranks.titan.display");
        titanElo = config.getInt("config.combat.ranks.titan.elo");
        immortalDisplay = config.getString("config.combat.ranks.immortal.display");
        immortalElo = config.getInt("config.combat.ranks.immortal.elo");
        supremeDisplay = config.getString("config.combat.ranks.supreme.display");
        supremeElo = config.getInt("config.combat.ranks.supreme.elo");
        mythicDisplay = config.getString("config.combat.ranks.mythic.display");
        mythicElo = config.getInt("config.combat.ranks.mythic.elo");
        legendDisplay = config.getString("config.combat.ranks.legend.display");
        legendElo = config.getInt("config.combat.ranks.legend.elo");
        heroDisplay = config.getString("config.combat.ranks.hero.display");
        heroElo = config.getInt("config.combat.ranks.hero.elo");
        championDisplay = config.getString("config.combat.ranks.champion.display");
        championElo = config.getInt("config.combat.ranks.champion.elo");
        masterDisplay = config.getString("config.combat.ranks.master.display");
        masterElo = config.getInt("config.combat.ranks.master.elo");
        eliteDisplay = config.getString("config.combat.ranks.elite.display");
        eliteElo = config.getInt("config.combat.ranks.elite.elo");
        veteranDisplay = config.getString("config.combat.ranks.veteran.display");
        veteranElo = config.getInt("config.combat.ranks.veteran.elo");
        competitorDisplay = config.getString("config.combat.ranks.competitor.display");
        competitorElo = config.getInt("config.combat.ranks.competitor.elo");
        apprenticeDisplay = config.getString("config.combat.ranks.apprentice.display");
        apprenticeElo = config.getInt("config.combat.ranks.apprentice.elo");
        noviceDisplay = config.getString("config.combat.ranks.novice.display");
        noviceElo = config.getInt("config.combat.ranks.novice.elo");
        randomDisplay = config.getString("config.combat.ranks.random.display");
        randomElo = config.getInt("config.combat.ranks.random.elo");

        joinTitleEnabled = config.getBoolean("config.titles.join-title.enabled");
        joinTitleTitle = config.getString("config.titles.join-title.title");
        joinTitleSubtitle = config.getString("config.titles.join-title.subtitle");
        joinTitleFadeIn = config.getInt("config.titles.join-title.fade-in");
        joinTitleStay = config.getInt("config.titles.join-title.stay");
        joinTitleFadeOut = config.getInt("config.titles.join-title.fade-out");

        musicNonSpacePreview = config.getString("config.music.messages.non-space-preview");
        musicNonSpace = config.getString("config.music.messages.non-space");
        jukeboxError = config.getString("config.music.messages.jukebox-error");
        moveTooFar = config.getString("config.music.messages.move-too-far");
        maxDistanceJukebox = config.getInt("config.music.limit");

        reportCooldownTime = config.getInt("config.report.cooldown");
        reportCooldownMessage = config.getString("config.report.messages.cooldown");
        reportMessage = config.getString("config.report.messages.report-message");
        reportStaffMessage = config.getString("config.report.messages.report-message-staff");

        streakTag40 = config.getString("config.streak.tags.kill-40.tag");
        streakTag60 = config.getString("config.streak.tags.kill-60.tag");
        streakTag80 = config.getString("config.streak.tags.kill-80.tag");
        streakTag100 = config.getString("config.streak.tags.kill-100.tag");
        streakTag150 = config.getString("config.streak.tags.kill-150.tag");
        streakTag200 = config.getString("config.streak.tags.kill-200.tag");
        streakTag250 = config.getString("config.streak.tags.kill-250.tag");
        streakTag300 = config.getString("config.streak.tags.kill-300.tag");
        streakTag500 = config.getString("config.streak.tags.kill-500.tag");
        streakArmorStandName = config.getString("config.streak.armor-stand.name");
        streakArmorStandX = config.getDouble("config.streak.armor-stand.location.location-x");
        streakArmorStandY = config.getDouble("config.streak.armor-stand.location.location-y");
        streakArmorStandZ = config.getDouble("config.streak.armor-stand.location.location-z");
        streakMessageReached = config.getString("config.streak.messages.streak-reached");
        streakMessageLost = config.getString("config.streak.messages.streak-lost");
        streakTitleEnabled = config.getBoolean("config.streak.title.enabled");
        streakTitleTitle = config.getString("config.streak.title.title");
        streakTitleSubtitle = config.getString("config.streak.title.subtitle");
        streakTitleFadeIn = config.getInt("config.streak.title.fade-in");
        streakTitleStay = config.getInt("config.streak.title.stay");
        streakTitleFadeOut = config.getInt("config.streak.title.fade-out");
    }

    private String validateAndGetMaterial(FileConfiguration config, String path, String defaultMaterial) {
        String materialName = config.getString(path);

        if (materialName == null || materialName.trim().isEmpty()) {
            plugin.getLogger().warning("Material at '" + path + "' is empty. Using default value: " + defaultMaterial);
            return defaultMaterial;
        }

        try {
            Material.valueOf(materialName.toUpperCase());
            return materialName.toUpperCase();
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material '" + materialName + "' at '" + path + "'. Using default value: " + defaultMaterial);
            return defaultMaterial;
        }
    }

    public boolean validateAllMaterials() {
        boolean allValid = true;
        FileConfiguration config = configFile.getConfig();
        
        String[] materialPaths = {
            "config.hotbar.default.items.knocker.id",
            "config.hotbar.default.items.blocks.id", 
            "config.hotbar.default.items.bow.id",
            "config.hotbar.default.items.plate.id",
            "config.hotbar.default.items.feather.id",
            "config.hotbar.default.items.pearl.id",
            "config.hotbar.default.items.arrow.id"
        };
        
        for (String path : materialPaths) {
            String materialName = config.getString(path);
            if (materialName != null && !materialName.trim().isEmpty()) {
                try {
                    Material.valueOf(materialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().severe("Invalid material detected: '" + materialName + "' at '" + path + "'");
                    allValid = false;
                }
            }
        }
        
        return allValid;
    }

    public void reload() {
        configFile.reloadConfig();
        
        // Validar materiales después de recargar la configuración
        if (!validateAllMaterials()) {
            plugin.getLogger().warning("Invalid materials detected. The configuration will be loaded with default values for problematic materials.");
        }
        
        loadConfig();
    }

    public String getPrefix() { return prefix; }
    public Long getAutoSaveInterval() { return autoSaveInterval; }
    public Boolean getChatEnabled() { return chatEnabled; }
    public String getChatFormat() { return chatFormat; }

    public Boolean getEndermiteEnabled() { return endermiteEnabled; }
    public Integer getEndermiteLimit() { return endermiteLimit; }
    public String getEndermiteName() { return endermiteName; }
    public Integer getEndermiteTime() { return endermiteTime; }
    public String getEndermiteSpawnMessage() { return endermiteSpawnMessage; }
    public String getEndermiteLimitMessage() { return endermiteLimitMessage; }

    public String getSkullName() { return skullName; }
    public String getSkullLore() { return skullLore; }
    public Integer getSkullSlot() { return skullSlot; }
    
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
    public String getSlimeBallId() { return slimeBallId; }
    public String getSlimeBallName() { return slimeBallName; }
    public String getSlimeBallLore() { return slimeBallLore; }
    public Boolean getSlimeBallKnoback() { return slimeBallKnockback; }
    public Integer getSlimeBallKnockbackLevel() { return slimeBallKnockbackLevel; }

    public Integer getDefaultElo() { return defaultElo; }
    public Long getCombatLog() { return combatLog; }
    public Integer getEloMinGained() { return eloMinGained; }
    public Integer getEloMaxGained() { return eloMaxGained; }
    public Boolean getEloGainedMessageEnabled() {return eloGainedMessageEnabled; }
    public String getEloGainedMessage() { return eloGainedMessage; }
    public Integer getEloMinLost() { return eloMinLost; }
    public Integer getEloMaxLost() { return eloMaxLost; }
    public Boolean getEloLostMessageEnabled() {return eloLostMessageEnabled; }
    public String getEloLostMessage() { return eloLostMessage; }
    public Integer getKgCoinsGainedMin() { return kgcoinsGainedMin; }
    public Integer getKgCoinsGainedMax() { return kgcoinsGainedMax; }
    public Boolean getKgCoinsGainedMessageEnabled() {return kgcoinsGainedMessageEnabled; }
    public String getKgCoinsGainedMessage() { return kgcoinsGainedMessage; }
    public double getHorizontalKnockback() { return horizontalKnockback; }
    public double getVerticalKnockback() { return verticalKnockback; }
    public double getKnockbackReduction() { return knockbackReduction; }
    public Integer getKnockbackSprintMultiplier() { return knockbackSprintMultiplier; }
    public double getHorizontalKnockbackArrow() { return horizontalKnockbackArrow; }
    public double getSprintKnockbackArrow() { return sprintKnockbackArrow; }
    public double getKnockbackHorizontalEndermite() { return knockbackHorizontalEndermite; }
    public double getKnockbackVerticalEndermite() { return knockbackVerticalEndermite; }
    public Integer getKnockbackLevelEndermite() { return knockbackLevelEndermite; }
    public double getMaxKnockbackHorizontal() { return maxKnockbackHorizontal; }
    public double getMaxKnockbackHorizontalArrow() { return maxKnockbackHorizontalArrow; }
    public double getMaxKnockbackVertical() { return maxKnockbackVertical; }
    public String getDivineDisplay() { return divineDisplay; }
    public Integer getDivineElo() {  return divineElo; }
    public String getGrandMasterDisplay() { return grandMasterDisplay; }
    public Integer getGrandMasterElo() { return grandMasterElo; }
    public String getGodDisplay() { return godDisplay; }
    public Integer getGodElo() { return godElo; }
    public String getTitanDisplay() { return titanDisplay; }
    public Integer getTitanElo() { return titanElo; }
    public String getImmortalDisplay() { return immortalDisplay; }
    public Integer getImmortalElo() { return immortalElo; }
    public String getSupremeDisplay() { return supremeDisplay; }
    public Integer getSupremeElo() { return supremeElo; }
    public String getMythicDisplay() { return mythicDisplay; }
    public Integer getMythicElo() { return mythicElo; }
    public String getLegendDisplay() { return legendDisplay; }
    public Integer getLegendElo() { return legendElo; }
    public String getHeroDisplay() { return heroDisplay; }
    public Integer getHeroElo() { return heroElo; }
    public String getChampionDisplay() { return championDisplay; }
    public Integer getChampionElo() { return championElo; }
    public String getMasterDisplay() { return masterDisplay; }
    public Integer getMasterElo() { return masterElo; }
    public String getEliteDisplay() { return eliteDisplay; }
    public Integer getEliteElo() { return eliteElo; }
    public String getVeteranDisplay() { return veteranDisplay; }
    public Integer getVeteranElo() { return veteranElo; }
    public String getCompetitorDisplay() { return competitorDisplay; }
    public Integer getCompetitorElo() { return competitorElo; }
    public String getApprenticeDisplay() { return apprenticeDisplay; }
    public Integer getApprenticeElo() { return apprenticeElo; }
    public String getNoviceDisplay() { return noviceDisplay; }
    public Integer getNoviceElo() { return noviceElo; }
    public String getRandomDisplay() { return randomDisplay; }
    public Integer getRandomElo() { return randomElo; }

    public Boolean getJoinTitleEnabled() { return joinTitleEnabled; }
    public String getJoinTitleTitle() { return joinTitleTitle; }
    public String getJoinTitleSubtitle() { return joinTitleSubtitle; }
    public Integer getJoinTitleFadeIn() { return joinTitleFadeIn; }
    public Integer getJoinTitleStay() { return joinTitleStay; }
    public Integer getJoinTitleFadeOut() { return joinTitleFadeOut; }

    public String getMusicNonSpacePreview() { return musicNonSpacePreview; }
    public String getMusicNonSpace() { return musicNonSpace; }
    public String getJukeboxError() { return jukeboxError; }
    public String getMoveTooFar() { return moveTooFar; }
    public Integer getMaxDistanceJukebox() { return maxDistanceJukebox; }

    public Integer getReportCooldownTime() { return reportCooldownTime; }
    public String getReportCooldownMessage() { return reportCooldownMessage; }
    public String getReportMessage() { return reportMessage; }
    public String getReportStaffMessage() { return reportStaffMessage; }

    public String getStreakTag40() { return streakTag40; }
    public String getStreakTag60() { return streakTag60; }
    public String getStreakTag80() { return streakTag80; }
    public String getStreakTag100() { return streakTag100; }
    public String getStreakTag150() { return streakTag150; }
    public String getStreakTag200() { return streakTag200; }
    public String getStreakTag250() { return streakTag250; }
    public String getStreakTag300() { return streakTag300; }
    public String getStreakTag500() { return streakTag500; }
    public String getStreakArmorStandName() { return streakArmorStandName; }
    public double getStreakArmorStandX() { return streakArmorStandX; }
    public double getStreakArmorStandY() { return streakArmorStandY; }
    public double getStreakArmorStandZ() { return streakArmorStandZ; }
    public String getStreakMessageReached() { return streakMessageReached; }
    public String getStreakMessageLost() { return streakMessageLost; }
    public Boolean getStreakTitleEnabled() { return streakTitleEnabled; }
    public String getStreakTitleTitle() { return streakTitleTitle; }
    public String getStreakTitleSubtitle() { return streakTitleSubtitle; }
    public Integer getStreakTitleFadeIn() { return streakTitleFadeIn; }
    public Integer getStreakTitleStay() { return streakTitleStay; }
    public Integer getStreakTitleFadeOut() { return streakTitleFadeOut; }
}
