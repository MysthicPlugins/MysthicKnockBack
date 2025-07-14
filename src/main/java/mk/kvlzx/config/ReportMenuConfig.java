package mk.kvlzx.config;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class ReportMenuConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    private String menuPlayerListTitle;
    private Integer menuPlayerListSize;
    private String menuPlayerListItemSkullName;
    private List<String> menuPlayerListItemSkullLore;
    private String menuPlayerListItemBackId;
    private String menuPlayerListItemBackName;
    private List<String> menuPlayerListItemBackLore;
    private Integer menuPlayerListItemBackSlot;
    private List<Integer> menuPlayerListFillerSlots;

    private String menuReportReasonTitle;
    private Integer menuReportReasonSize;
    private String menuReportReasonItemSkullName;
    private List<String> menuReportReasonItemSkullLore;
    private Integer menuReportReasonItemSkullSlot;
    private List<String> menuReportReasonItemReasonLore;
    private List<Integer> menuReportReasonItemReasonSlots;
    private String menuReportReasonItemBackId;
    private String menuReportReasonItemBackName;
    private List<String> menuReportReasonItemBackLore;
    private Integer menuReportReasonItemBackSlot;
    private String menuReportReasonMessageError;

    private String reasonHacksId;
    private String reasonHacksName;
    private String reasonHacksLore;
    private String reasonToxicId;
    private String reasonToxicName;
    private String reasonToxicLore;
    private String reasonTeamingId;
    private String reasonTeamingName;
    private String reasonTeamingLore;
    private String reasonBugAbuseId;
    private String reasonBugAbuseName;
    private String reasonBugAbuseLore;
    private String reasonInappropriateSkinId;
    private String reasonInappropriateSkinName;
    private String reasonInappropriateSkinLore;
    private String reasonOtherId;
    private String reasonOtherName;
    private String reasonOtherLore;


    public ReportMenuConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("report.yml", "config/menus", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        menuPlayerListTitle = config.getString("report-menus.player-list.title");
        menuPlayerListSize = config.getInt("report-menus.player-list.size");
        menuPlayerListItemSkullName = config.getString("report-menus.player-list.items.player-skulls.name");
        menuPlayerListItemSkullLore = config.getStringList("report-menus.player-list.items.player-skulls.lore");
        menuPlayerListItemBackId = validateAndGetMaterial(config, "report-menus.player-list.items.back.id", "ARROW");
        menuPlayerListItemBackName = config.getString("report-menus.player-list.items.back.name");
        menuPlayerListItemBackLore = config.getStringList("report-menus.player-list.items.back.lore");
        menuPlayerListItemBackSlot = config.getInt("report-menus.player-list.items.back.slot");
        menuPlayerListFillerSlots = config.getIntegerList("report-menus.player-list.items.fill.slots");

        menuReportReasonTitle = config.getString("report-menus.report-reasons.title");
        menuReportReasonSize = config.getInt("report-menus.report-reasons.size");
        menuReportReasonItemSkullName = config.getString("report-menus.report-reasons.items.target-skull.name");
        menuReportReasonItemSkullLore = config.getStringList("report-menus.report-reasons.items.target-skull.lore");
        menuReportReasonItemSkullSlot = config.getInt("report-menus.report-reasons.items.target-skull.slot");
        menuReportReasonItemReasonLore = config.getStringList("report-menus.report-reasons.items.report-reasons.lore");
        menuReportReasonItemReasonSlots = config.getIntegerList("report-menus.report-reasons.items.report-reasons.slots");
        menuReportReasonItemBackId = validateAndGetMaterial(config, "report-menus.report-reasons.items.back.id", "ARROW");
        menuReportReasonItemBackName = config.getString("report-menus.report-reasons.items.back.name");
        menuReportReasonItemBackLore = config.getStringList("report-menus.report-reasons.items.back.lore");
        menuReportReasonItemBackSlot = config.getInt("report-menus.report-reasons.items.back.slot");
        menuReportReasonMessageError = config.getString("report-menus.messages.player-not-found");

        reasonHacksId = config.getString("reasons.hacks.id");
        reasonHacksName = config.getString("reasons.hacks.name");
        reasonHacksLore = config.getString("reasons.hacks.lore");
        reasonToxicId = config.getString("reasons.toxic.id");
        reasonToxicName = config.getString("reasons.toxic.name");
        reasonToxicLore = config.getString("reasons.toxic.lore");
        reasonTeamingId = config.getString("reasons.teaming.id");
        reasonTeamingName = config.getString("reasons.teaming.name");
        reasonTeamingLore = config.getString("reasons.teaming.lore");
        reasonBugAbuseId = config.getString("reasons.bug-abuse.id");
        reasonBugAbuseName = config.getString("reasons.bug-abuse.name");
        reasonBugAbuseLore = config.getString("reasons.bug-abuse.lore");
        reasonInappropriateSkinId = config.getString("reasons.inappropriate-skin.id");
        reasonInappropriateSkinName = config.getString("reasons.inappropriate-skin.name");
        reasonInappropriateSkinLore = config.getString("reasons.inappropriate-skin.lore");
        reasonOtherId = config.getString("reasons.other.id");
        reasonOtherName = config.getString("reasons.other.name");
        reasonOtherLore = config.getString("reasons.other.lore");

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
            "report-menus.player-list.items.back.id",
            "report-menus.report-reasons.items.back.id"
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

    public String getMenuPlayerListTitle() { return menuPlayerListTitle; }
    public Integer getMenuPlayerListSize() { return menuPlayerListSize; }
    public String getMenuPlayerListItemSkullName() { return menuPlayerListItemSkullName; }
    public List<String> getMenuPlayerListItemSkullLore() { return menuPlayerListItemSkullLore; }
    public String getMenuPlayerListItemBackId() { return menuPlayerListItemBackId; }
    public String getMenuPlayerListItemBackName() { return menuPlayerListItemBackName; }
    public List<String> getMenuPlayerListItemBackLore() { return menuPlayerListItemBackLore; }
    public Integer getMenuPlayerListItemBackSlot() { return menuPlayerListItemBackSlot; }
    public List<Integer> getMenuPlayerListFillerSlots() { return menuPlayerListFillerSlots; }

    public String getMenuReportReasonTitle() { return menuReportReasonTitle; }
    public Integer getMenuReportReasonSize() { return menuReportReasonSize; }
    public String getMenuReportReasonItemSkullName() { return menuReportReasonItemSkullName; }
    public List<String> getMenuReportReasonItemSkullLore() { return menuReportReasonItemSkullLore; }
    public Integer getMenuReportReasonItemSkullSlot() { return menuReportReasonItemSkullSlot; }
    public List<String> getMenuReportReasonItemReasonLore() { return menuReportReasonItemReasonLore; }
    public List<Integer> getMenuReportReasonItemReasonSlots() { return menuReportReasonItemReasonSlots; }
    public String getMenuReportReasonItemBackId() { return menuReportReasonItemBackId; }
    public String getMenuReportReasonItemBackName() { return menuReportReasonItemBackName; }
    public List<String> getMenuReportReasonItemBackLore() { return menuReportReasonItemBackLore; }
    public Integer getMenuReportReasonItemBackSlot() { return menuReportReasonItemBackSlot; }
    public String getMenuReportReasonMessageError() { return menuReportReasonMessageError; }

    public String getReasonHacksId() { return reasonHacksId; }
    public String getReasonHacksName() { return reasonHacksName; }
    public String getReasonHacksLore() { return reasonHacksLore; }
    public String getReasonToxicId() { return reasonToxicId; }
    public String getReasonToxicName() { return reasonToxicName; }
    public String getReasonToxicLore() { return reasonToxicLore; }
    public String getReasonTeamingId() { return reasonTeamingId; }
    public String getReasonTeamingName() { return reasonTeamingName; }
    public String getReasonTeamingLore() { return reasonTeamingLore; }
    public String getReasonBugAbuseId() { return reasonBugAbuseId; }
    public String getReasonBugAbuseName() { return reasonBugAbuseName; }
    public String getReasonBugAbuseLore() { return reasonBugAbuseLore; }
    public String getReasonInappropriateSkinId() { return reasonInappropriateSkinId; }
    public String getReasonInappropriateSkinName() { return reasonInappropriateSkinName; }
    public String getReasonInappropriateSkinLore() { return reasonInappropriateSkinLore; }
    public String getReasonOtherId() { return reasonOtherId; }
    public String getReasonOtherName() { return reasonOtherName; }
    public String getReasonOtherLore() { return reasonOtherLore; }

}

