package mk.kvlzx.config;

import java.util.List;

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

    private Integer blocksTime;

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

    private double featherSpeed;
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

    private double powerUpPickupRadius;
    private Integer powerUpTime;
    private Integer powerUpSpawnInterval;
    private Integer powerUpMaxPowerUp;
    private double powerUpMinDistance;
    private String powerUpJump1Id;
    private String powerUpJump1Name;
    private List<String> powerUpJump1Lore;
    private String powerUpJump1EffectId;
    private Integer powerUpJump1EffectLevel;
    private Integer powerUpJump1EffectDuration;
    private String powerUpJump2Id;
    private String powerUpJump2Name;
    private List<String> powerUpJump2Lore;
    private String powerUpJump2EffectId;
    private Integer powerUpJump2EffectLevel;
    private Integer powerUpJump2EffectDuration;
    private String powerUpJump3Id;
    private String powerUpJump3Name;
    private List<String> powerUpJump3Lore;
    private String powerUpJump3EffectId;
    private Integer powerUpJump3EffectLevel;
    private Integer powerUpJump3EffectDuration;
    private String powerUpJump4Id;
    private String powerUpJump4Name;
    private List<String> powerUpJump4Lore;
    private String powerUpJump4EffectId;
    private Integer powerUpJump4EffectLevel;
    private Integer powerUpJump4EffectDuration;
    private String powerUpInvisibilityId;
    private String powerUpInvisibilityName;
    private List<String> powerUpInvisibilityLore;
    private String powerUpInvisibilityEffectId;
    private Integer powerUpInvisibilityEffectLevel;
    private Integer powerUpInvisibilityEffectDuration;
    private String powerUpKnockbackId;
    private String powerUpKnockbackName;
    private List<String> powerUpKnockbackLore;
    private Integer powerUpKnockbackEffectDuration;

    private String powerUpExplosiveArrowId;
    private String powerUpExplosiveArrowName;
    private List<String> powerUpExplosiveArrowLore;
    private int powerUpExplosiveArrowEffectDuration;
    private double powerUpExplosiveArrowRadius;
    private double powerUpExplosiveArrowPower;

    private String powerUpBlackHoleId;
    private String powerUpBlackHoleName;
    private List<String> powerUpBlackHoleLore;
    private String powerUpBlackHoleItemId;
    private String powerUpBlackHoleItemName;
    private List<String> powerUpBlackHoleItemLore;
    private Integer powerUpBlackHoleMaxThrowDistance;
    private Double powerUpBlackHoleAttractionRadius;
    private Double powerUpBlackHoleAttractionForce;
    private Integer powerUpBlackHoleAttractionDuration;
    private Double powerUpBlackHoleRepulsionForce;
    private Integer powerUpBlackHoleRepulsionDuration;

    private String powerUpDoublePearlId;
    private String powerUpDoublePearlName;
    private List<String> powerUpDoublePearlLore;
    private Integer powerUpDoublePearlEffectDuration;

    private String powerUpMessageAppeared;
    private String powerUpMessagePickup;
    private String powerUpBlackHoleItemPickupMessage;
    private String powerUpDoublePearlActivationMessage;
    private String powerUpDoublePearlExpiredMessage;

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

        blocksTime = config.getInt("config.blocks.time");

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
        slimeBallId = validateAndGetMaterial(config, "config.hotbar.default.items.slime_ball.id", "SLIME_BALL");
        slimeBallName = config.getString("config.hotbar.default.items.slime_ball.name");
        slimeBallLore = config.getString("config.hotbar.default.items.slime_ball.lore");
        slimeBallKnockback = config.getBoolean("config.hotbar.default.items.slime_ball.knockback.enabled");
        slimeBallKnockbackLevel = config.getInt("config.hotbar.default.items.slime_ball.knockback.level");

        featherSpeed = config.getDouble("config.combat.speed");
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


        powerUpPickupRadius = config.getDouble("config.powerups.pickup-radius");
        powerUpTime = config.getInt("config.powerups.time");
        powerUpSpawnInterval = config.getInt("config.powerups.spawn-interval");
        powerUpMaxPowerUp = config.getInt("config.powerups.max-powerups");
        powerUpMinDistance = config.getDouble("config.powerups.min-distance");
        powerUpJump1Id = validateAndGetMaterial(config, "config.powerups.powerups.jump-1.id", "RABBIT_FOOT");
        powerUpJump1Name = config.getString("config.powerups.powerups.jump-1.name");
        powerUpJump1Lore = config.getStringList("config.powerups.powerups.jump-1.lore");
        powerUpJump1EffectId = config.getString("config.powerups.powerups.jump-1.effect.id");
        powerUpJump1EffectLevel = config.getInt("config.powerups.powerups.jump-1.effect.level");
        powerUpJump1EffectDuration = config.getInt("config.powerups.powerups.jump-1.effect.duration");
        powerUpJump2Id = validateAndGetMaterial(config, "config.powerups.powerups.jump-2.id", "RABBIT_FOOT");
        powerUpJump2Name = config.getString("config.powerups.powerups.jump-2.name");
        powerUpJump2Lore = config.getStringList("config.powerups.powerups.jump-2.lore");
        powerUpJump2EffectId = config.getString("config.powerups.powerups.jump-2.effect.id");
        powerUpJump2EffectLevel = config.getInt("config.powerups.powerups.jump-2.effect.level");
        powerUpJump2EffectDuration = config.getInt("config.powerups.powerups.jump-2.effect.duration");
        powerUpJump3Id = validateAndGetMaterial(config, "config.powerups.powerups.jump-3.id", "RABBIT_FOOT");
        powerUpJump3Name = config.getString("config.powerups.powerups.jump-3.name");
        powerUpJump3Lore = config.getStringList("config.powerups.powerups.jump-3.lore");
        powerUpJump3EffectId = config.getString("config.powerups.powerups.jump-3.effect.id");
        powerUpJump3EffectLevel = config.getInt("config.powerups.powerups.jump-3.effect.level");
        powerUpJump3EffectDuration = config.getInt("config.powerups.powerups.jump-3.effect.duration");
        powerUpJump4Id = validateAndGetMaterial(config, "config.powerups.powerups.jump-4.id", "RABBIT_FOOT");
        powerUpJump4Name = config.getString("config.powerups.powerups.jump-4.name");
        powerUpJump4Lore = config.getStringList("config.powerups.powerups.jump-4.lore");
        powerUpJump4EffectId = config.getString("config.powerups.powerups.jump-4.effect.id");
        powerUpJump4EffectLevel = config.getInt("config.powerups.powerups.jump-4.effect.level");
        powerUpJump4EffectDuration = config.getInt("config.powerups.powerups.jump-4.effect.duration");
        powerUpInvisibilityId = validateAndGetMaterial(config, "config.powerups.powerups.invisibility.id", "FERMENTED_SPIDER_EYE");
        powerUpInvisibilityName = config.getString("config.powerups.powerups.invisibility.name");
        powerUpInvisibilityLore = config.getStringList("config.powerups.powerups.invisibility.lore");
        powerUpInvisibilityEffectId = config.getString("config.powerups.powerups.invisibility.effect.id");
        powerUpInvisibilityEffectLevel = config.getInt("config.powerups.powerups.invisibility.effect.level");
        powerUpInvisibilityEffectDuration = config.getInt("config.powerups.powerups.invisibility.effect.duration");
        powerUpKnockbackId = validateAndGetMaterial(config, "config.powerups.powerups.knockback.id", "STICK");
        powerUpKnockbackName = config.getString("config.powerups.powerups.knockback.name");
        powerUpKnockbackLore = config.getStringList("config.powerups.powerups.knockback.lore");
        powerUpKnockbackEffectDuration = config.getInt("config.powerups.powerups.knockback.effect.duration");

        powerUpExplosiveArrowId = validateAndGetMaterial(config, "config.powerups.powerups.explosive-arrow.id", "TNT");
        powerUpExplosiveArrowName = config.getString("config.powerups.powerups.explosive-arrow.name");
        powerUpExplosiveArrowLore = config.getStringList("config.powerups.powerups.explosive-arrow.lore");
        powerUpExplosiveArrowEffectDuration = config.getInt("config.powerups.powerups.explosive-arrow.effect.duration");
        powerUpExplosiveArrowRadius = config.getDouble("config.powerups.powerups.explosive-arrow.effect.radius");
        powerUpExplosiveArrowPower = config.getDouble("config.powerups.powerups.explosive-arrow.effect.power");

        powerUpBlackHoleId = validateAndGetMaterial(config, "config.powerups.powerups.black-hole.id", "OBSIDIAN");
        powerUpBlackHoleName = config.getString("config.powerups.powerups.black-hole.name");
        powerUpBlackHoleLore = config.getStringList("config.powerups.powerups.black-hole.lore");
        powerUpBlackHoleItemId = validateAndGetMaterial(config, "config.powerups.powerups.black-hole.item.id", "OBSIDIAN");
        powerUpBlackHoleItemName = config.getString("config.powerups.powerups.black-hole.item.name");
        powerUpBlackHoleItemLore = config.getStringList("config.powerups.powerups.black-hole.item.lore");
        powerUpBlackHoleMaxThrowDistance = config.getInt("config.powerups.powerups.black-hole.effect.max-throw-distance");
        powerUpBlackHoleAttractionRadius = config.getDouble("config.powerups.powerups.black-hole.effect.attraction-radius");
        powerUpBlackHoleAttractionForce = config.getDouble("config.powerups.powerups.black-hole.effect.attraction-force");
        powerUpBlackHoleAttractionDuration = config.getInt("config.powerups.powerups.black-hole.effect.attraction-duration");
        powerUpBlackHoleRepulsionForce = config.getDouble("config.powerups.powerups.black-hole.effect.repulsion-force");
        powerUpBlackHoleRepulsionDuration = config.getInt("config.powerups.powerups.black-hole.effect.repulsion-duration");

        powerUpDoublePearlId = validateAndGetMaterial(config, "config.powerups.powerups.double-pearl.id", "ENDER_PEARL");
        powerUpDoublePearlName = config.getString("config.powerups.powerups.double-pearl.name");
        powerUpDoublePearlLore = config.getStringList("config.powerups.powerups.double-pearl.lore");
        powerUpDoublePearlEffectDuration = config.getInt("config.powerups.powerups.double-pearl.effect.duration");

        powerUpMessageAppeared = config.getString("config.powerups.messages.appeared");
        powerUpMessagePickup = config.getString("config.powerups.messages.pickup");
        powerUpBlackHoleItemPickupMessage = config.getString("config.powerups.messages.black-hole-item-pickup");
        powerUpDoublePearlActivationMessage = config.getString("config.powerups.messages.double-pearl-activation");
        powerUpDoublePearlExpiredMessage = config.getString("config.powerups.messages.double-pearl-expired");
        

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
            "config.hotbar.default.items.arrow.id",
            "config.hotbar.default.items.slime_ball.id",
            "config.powerups.jump-1.id",
            "config.powerups.jump-2.id",
            "config.powerups.jump-3.id",
            "config.powerups.jump-4.id",
            "config.powerups.invisibility.id",
            "config.powerups.knockback.id",
            "config.powerups.powerups.explosive-arrow.id",
            "config.powerups.powerups.black-hole.id",
            "config.powerups.powerups.black-hole.item.id",
            "config.powerups.powerups.double-pearl.id"
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

    public Integer getBlocksTime() { return blocksTime; }

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

    public double getFeatherSpeed() { return featherSpeed; }
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

    public double getPowerUpPickupRadius() { return powerUpPickupRadius; }
    public Integer getPowerUpTime() { return powerUpTime; }
    public Integer getPowerUpSpawnInterval() { return powerUpSpawnInterval; }
    public Integer getPowerUpMaxPowerUp() { return powerUpMaxPowerUp; }
    public double getPowerUpMinDistance() { return powerUpMinDistance; }
    public String getPowerUpJump1Id() { return powerUpJump1Id; }
    public String getPowerUpJump1Name() { return powerUpJump1Name; }
    public List<String> getPowerUpJump1Lore() { return powerUpJump1Lore; }
    public String getPowerUpJump1EffectId() { return powerUpJump1EffectId; }
    public Integer getPowerUpJump1EffectLevel() { return powerUpJump1EffectLevel; }
    public Integer getPowerUpJump1EffectDuration() { return powerUpJump1EffectDuration; }
    public String getPowerUpJump2Id() { return powerUpJump2Id; }
    public String getPowerUpJump2Name() { return powerUpJump2Name; }
    public List<String> getPowerUpJump2Lore() { return powerUpJump2Lore; }
    public String getPowerUpJump2EffectId() { return powerUpJump2EffectId; }
    public Integer getPowerUpJump2EffectLevel() { return powerUpJump2EffectLevel; }
    public Integer getPowerUpJump2EffectDuration() { return powerUpJump2EffectDuration; }
    public String getPowerUpJump3Id() { return powerUpJump3Id; }
    public String getPowerUpJump3Name() { return powerUpJump3Name; }
    public List<String> getPowerUpJump3Lore() { return powerUpJump3Lore; }
    public String getPowerUpJump3EffectId() { return powerUpJump3EffectId; }
    public Integer getPowerUpJump3EffectLevel() { return powerUpJump3EffectLevel; }
    public Integer getPowerUpJump3EffectDuration() { return powerUpJump3EffectDuration; }
    public String getPowerUpJump4Id() { return powerUpJump4Id; }
    public String getPowerUpJump4Name() { return powerUpJump4Name; }
    public List<String> getPowerUpJump4Lore() { return powerUpJump4Lore; }
    public String getPowerUpJump4EffectId() { return powerUpJump4EffectId; }
    public Integer getPowerUpJump4EffectLevel() { return powerUpJump4EffectLevel; }
    public Integer getPowerUpJump4EffectDuration() { return powerUpJump4EffectDuration; }
    public String getPowerUpInvisibilityId() { return powerUpInvisibilityId; }
    public String getPowerUpInvisibilityName() { return powerUpInvisibilityName; }
    public List<String> getPowerUpInvisibilityLore() { return powerUpInvisibilityLore; }
    public String getPowerUpInvisibilityEffectId() { return powerUpInvisibilityEffectId; }
    public Integer getPowerUpInvisibilityEffectLevel() { return powerUpInvisibilityEffectLevel; }
    public Integer getPowerUpInvisibilityEffectDuration() { return powerUpInvisibilityEffectDuration; }
    public String getPowerUpKnockbackId() { return powerUpKnockbackId; }
    public String getPowerUpKnockbackName() { return powerUpKnockbackName; }
    public List<String> getPowerUpKnockbackLore() { return powerUpKnockbackLore; }
    public Integer getPowerUpKnockbackEffectDuration() { return powerUpKnockbackEffectDuration; }

    public String getPowerUpExplosiveArrowId() { return powerUpExplosiveArrowId; }
    public String getPowerUpExplosiveArrowName() { return powerUpExplosiveArrowName; }
    public List<String> getPowerUpExplosiveArrowLore() { return powerUpExplosiveArrowLore; }
    public int getPowerUpExplosiveArrowEffectDuration() { return powerUpExplosiveArrowEffectDuration; }
    public double getPowerUpExplosiveArrowRadius() { return powerUpExplosiveArrowRadius; }
    public double getPowerUpExplosiveArrowPower() { return powerUpExplosiveArrowPower; }

    public String getPowerUpBlackHoleId() { return powerUpBlackHoleId; }
    public String getPowerUpBlackHoleName() { return powerUpBlackHoleName; }
    public List<String> getPowerUpBlackHoleLore() { return powerUpBlackHoleLore; }
    public String getPowerUpBlackHoleItemId() { return powerUpBlackHoleItemId; }
    public String getPowerUpBlackHoleItemName() { return powerUpBlackHoleItemName; }
    public List<String> getPowerUpBlackHoleItemLore() { return powerUpBlackHoleItemLore; }
    public Integer getPowerUpBlackHoleMaxThrowDistance() { return powerUpBlackHoleMaxThrowDistance; }
    public double getPowerUpBlackHoleAttractionRadius() { return powerUpBlackHoleAttractionRadius; }
    public double getPowerUpBlackHoleAttractionForce() { return powerUpBlackHoleAttractionForce; }
    public Integer getPowerUpBlackHoleAttractionDuration() { return powerUpBlackHoleAttractionDuration; }
    public double getPowerUpBlackHoleRepulsionForce() { return powerUpBlackHoleRepulsionForce; }
    public Integer getPowerUpBlackHoleRepulsionDuration() { return powerUpBlackHoleRepulsionDuration; }

    public String getPowerUpDoublePearlId() { return powerUpDoublePearlId; }
    public String getPowerUpDoublePearlName() { return powerUpDoublePearlName; }
    public List<String> getPowerUpDoublePearlLore() { return powerUpDoublePearlLore; }
    public Integer getPowerUpDoublePearlEffectDuration() { return powerUpDoublePearlEffectDuration; }

    public String getPowerUpMessageAppeared() { return powerUpMessageAppeared; }
    public String getPowerUpMessagePickup() { return powerUpMessagePickup; }
    public String getPowerUpBlackHoleItemPickupMessage() { return powerUpBlackHoleItemPickupMessage; }
    public String getPowerUpDoublePearlActivationMessage() { return powerUpDoublePearlActivationMessage; }
    public String getPowerUpDoublePearlExpiredMessage() { return powerUpDoublePearlExpiredMessage; }

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
