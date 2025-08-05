package mk.kvlzx.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.clip.placeholderapi.PlaceholderAPI;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class MainMenuConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    private String menuTitle;
    private Integer menuSize;

    private String menuTopKillsId;
    private String menuTopKillsName;
    private List<String> menuTopKillsLore;
    private Integer menuTopKillsSlot;
    private String menuTopEloId;
    private String menuTopEloName;
    private List<String> menuTopEloLore;
    private Integer menuTopEloSlot;
    private String menuTopStreaksId;
    private String menuTopStreaksName;
    private List<String> menuTopStreaksLore;
    private Integer menuTopStreaksSlot;
    private String menuTopKdrId;
    private String menuTopKdrName;
    private List<String> menuTopKdrLore;
    private Integer menuTopKdrSlot;
    private String menuTopTimeId;
    private String menuTopTimeName;
    private List<String> menuTopTimeLore;
    private Integer menuTopTimeSlot;
    private String menuArenaVoteId;
    private String menuArenaVoteName;
    private List<String> menuArenaVoteLore;
    private Integer menuArenaVoteSlot;
    private String menuEditHotbarId;
    private String menuEditHotbarName;
    private List<String> menuEditHotbarLore;
    private Integer menuEditHotbarSlot;
    private String menuMyStatsId;
    private String menuMyStatsName;
    private List<String> menuMyStatsLore;
    private Integer menuMyStatsSlot;
    private String menuReportPlayerId;
    private String menuReportPlayerName;
    private List<String> menuReportPlayerLore;
    private Integer menuReportPlayerSlot;
    private String menuShopId;
    private String menuShopName;
    private List<String> menuShopLore;
    private Integer menuShopSlot;

    public MainMenuConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("main.yml", "config/menus", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        menuTitle = config.getString("menu.title");
        menuSize = config.getInt("menu.size");

        // Validar y cargar materiales con valores por defecto
        menuTopKillsId = validateAndGetMaterial(config, "menu.items.top-kills.id", "DIAMOND_SWORD");
        menuTopKillsName = config.getString("menu.items.top-kills.name");
        menuTopKillsLore = config.getStringList("menu.items.top-kills.lore");
        menuTopKillsSlot = config.getInt("menu.items.top-kills.slot");
        
        menuTopEloId = validateAndGetMaterial(config, "menu.items.top-elo.id", "NETHER_STAR");
        menuTopEloName = config.getString("menu.items.top-elo.name");
        menuTopEloLore = config.getStringList("menu.items.top-elo.lore");
        menuTopEloSlot = config.getInt("menu.items.top-elo.slot");
        
        menuTopStreaksId = validateAndGetMaterial(config, "menu.items.top-streaks.id", "DIAMOND");
        menuTopStreaksName = config.getString("menu.items.top-streaks.name");
        menuTopStreaksLore = config.getStringList("menu.items.top-streaks.lore");
        menuTopStreaksSlot = config.getInt("menu.items.top-streaks.slot");
        
        menuTopKdrId = validateAndGetMaterial(config, "menu.items.top-kdr.id", "GOLDEN_APPLE");
        menuTopKdrName = config.getString("menu.items.top-kdr.name");
        menuTopKdrLore = config.getStringList("menu.items.top-kdr.lore");
        menuTopKdrSlot = config.getInt("menu.items.top-kdr.slot");
        
        menuTopTimeId = validateAndGetMaterial(config, "menu.items.top-time.id", "WATCH");
        menuTopTimeName = config.getString("menu.items.top-time.name");
        menuTopTimeLore = config.getStringList("menu.items.top-time.lore");
        menuTopTimeSlot = config.getInt("menu.items.top-time.slot");

        menuArenaVoteId = validateAndGetMaterial(config, "menu.items.arena-vote.id", "BEACON");
        menuArenaVoteName = config.getString("menu.items.arena-vote.name");
        menuArenaVoteLore = config.getStringList("menu.items.arena-vote.lore");
        menuArenaVoteSlot = config.getInt("menu.items.arena-vote.slot");
        
        menuEditHotbarId = validateAndGetMaterial(config, "menu.items.edit-hotbar.id", "DIAMOND_SWORD");
        menuEditHotbarName = config.getString("menu.items.edit-hotbar.name");
        menuEditHotbarLore = config.getStringList("menu.items.edit-hotbar.lore");
        menuEditHotbarSlot = config.getInt("menu.items.edit-hotbar.slot");
        
        // PLAYER_SKULL se maneja especialmente - se mantiene como está para procesamiento posterior
        menuMyStatsId = config.getString("menu.items.my-stats.id", "PLAYER_SKULL");
        menuMyStatsName = config.getString("menu.items.my-stats.name");
        menuMyStatsLore = config.getStringList("menu.items.my-stats.lore");
        menuMyStatsSlot = config.getInt("menu.items.my-stats.slot");
        
        menuReportPlayerId = validateAndGetMaterial(config, "menu.items.report-player.id", "BOOK_AND_QUILL");
        menuReportPlayerName = config.getString("menu.items.report-player.name");
        menuReportPlayerLore = config.getStringList("menu.items.report-player.lore");
        menuReportPlayerSlot = config.getInt("menu.items.report-player.slot");
        
        menuShopId = validateAndGetMaterial(config, "menu.items.shop.id", "EMERALD");
        menuShopName = config.getString("menu.items.shop.name");
        menuShopLore = config.getStringList("menu.items.shop.lore");
        menuShopSlot = config.getInt("menu.items.shop.slot");
    }

    private String validateAndGetMaterial(FileConfiguration config, String path, String defaultMaterial) {
        String materialName = config.getString(path);

        if (materialName == null || materialName.trim().isEmpty()) {
            plugin.getLogger().warning("Material at '" + path + "' is empty. Using default value: " + defaultMaterial);
            return defaultMaterial;
        }

        // Si es PLAYER_SKULL, lo mantenemos como está (se procesará especialmente)
        if (materialName.equalsIgnoreCase("PLAYER_SKULL")) {
            return materialName;
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
            "menu.items.top-kills.id",
            "menu.items.top-elo.id",
            "menu.items.top-streaks.id",
            "menu.items.top-kdr.id",
            "menu.items.top-time.id",
            "menu.items.edit-hotbar.id",
            "menu.items.my-stats.id",
            "menu.items.report-player.id",
            "menu.items.shop.id",
            "menu.items.arena-vote.id"
        };
        
        for (String path : materialPaths) {
            String materialName = config.getString(path);
            if (materialName != null && !materialName.trim().isEmpty()) {
                // Skipear validacion para PLAYER_SKULL, es un material custom
                if (materialName.equalsIgnoreCase("PLAYER_SKULL")) {
                    continue;
                }
                
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

    public ItemStack createMenuItem(String materialId, Player player, String name, List<String> lore) {
        ItemStack item;
        
        if (materialId.equalsIgnoreCase("PLAYER_SKULL")) {
            // Crear la cabeza del jugador
            item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 3 = Player skull
            ItemMeta meta = item.getItemMeta();
            
            if (meta instanceof SkullMeta && player != null) {
                SkullMeta skullMeta = (SkullMeta) meta;
                skullMeta.setOwner(player.getName());
            }
            
            if (name != null) {
                meta.setDisplayName(MessageUtils.getColor(name));
            }
            if (lore != null && !lore.isEmpty()) {
                List<String> finalLore = new ArrayList<>();
                for (String line : lore) {
                    String processedLine = processPlaceholders(line, player);
                        finalLore.add(MessageUtils.getColor(processedLine));
                }
                meta.setLore(finalLore);
            }
            item.setItemMeta(meta);
        } else {
            // Crear el item normal
            try {
                Material material = Material.valueOf(materialId.toUpperCase());
                item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                
                if (name != null) {
                    meta.setDisplayName(MessageUtils.getColor(name));
                }
                if (lore != null && !lore.isEmpty()) {
                    List<String> coloredLore = new ArrayList<>();
                    for (String line : lore) {
                        coloredLore.add(MessageUtils.getColor(line));
                    }
                    meta.setLore(coloredLore);
                }
                item.setItemMeta(meta);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Failed to create item with material: " + materialId + ". Using STONE as fallback.");
                item = new ItemStack(Material.STONE);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§cInvalid Material");
                item.setItemMeta(meta);
            }
        }
        
        return item;
    }

    private String processPlaceholders(String text, Player player) {
        if (text == null) return "";
        
        // Placeholders personalizados
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena == null) {
            currentArena = plugin.getTabConfig().getScoreNullArena();
        }
        
        text = text.replace("%current_arena%", currentArena);
        text = text.replace("%player_name%", player.getName());
        
        // PlaceholderAPI si está disponible
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                text = PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                // Si PlaceholderAPI falla, continuar sin error
            }
        }
        
        return text;
    }

    public void reload() {
        configFile.reloadConfig();
        
        // Validar materiales después de recargar la configuración
        if (!validateAllMaterials()) {
            plugin.getLogger().warning("Invalid materials detected in menu configuration. The menu will be loaded with default values for problematic materials.");
        }
        
        loadConfig();
    }

    // Getters
    public String getMenuTitle() { return menuTitle; }
    public Integer getMenuSize() { return menuSize; }
    public String getMenuTopKillsId() { return menuTopKillsId; }
    public String getMenuTopKillsName() { return menuTopKillsName; }
    public List<String> getMenuTopKillsLore() { return menuTopKillsLore; }
    public Integer getMenuTopKillsSlot() { return menuTopKillsSlot; }
    public String getMenuTopEloId() { return menuTopEloId; }
    public String getMenuTopEloName() { return menuTopEloName; }
    public List<String> getMenuTopEloLore() { return menuTopEloLore; }
    public Integer getMenuTopEloSlot() { return menuTopEloSlot; }
    public String getMenuTopStreaksId() { return menuTopStreaksId; }
    public String getMenuTopStreaksName() { return menuTopStreaksName; }
    public List<String> getMenuTopStreaksLore() { return menuTopStreaksLore; }
    public Integer getMenuTopStreaksSlot() { return menuTopStreaksSlot; }
    public String getMenuTopKdrId() { return menuTopKdrId; }
    public String getMenuTopKdrName() { return menuTopKdrName; }
    public List<String> getMenuTopKdrLore() { return menuTopKdrLore; }
    public Integer getMenuTopKdrSlot() { return menuTopKdrSlot; }
    public String getMenuTopTimeId() { return menuTopTimeId; }
    public String getMenuTopTimeName() { return menuTopTimeName; }
    public List<String> getMenuTopTimeLore() { return menuTopTimeLore; }
    public Integer getMenuTopTimeSlot() { return menuTopTimeSlot; }
    public String getMenuEditHotbarId() { return menuEditHotbarId; }
    public String getMenuEditHotbarName() { return menuEditHotbarName; }
    public List<String> getMenuEditHotbarLore() { return menuEditHotbarLore; }
    public Integer getMenuEditHotbarSlot() { return menuEditHotbarSlot; }
    public String getMenuMyStatsId() { return menuMyStatsId; }
    public String getMenuMyStatsName() { return menuMyStatsName; }
    public List<String> getMenuMyStatsLore() { return menuMyStatsLore; }
    public Integer getMenuMyStatsSlot() { return menuMyStatsSlot; }
    public String getMenuReportPlayerId() { return menuReportPlayerId; }
    public String getMenuReportPlayerName() { return menuReportPlayerName; }
    public List<String> getMenuReportPlayerLore() { return menuReportPlayerLore; }
    public Integer getMenuReportPlayerSlot() { return menuReportPlayerSlot; }
    public String getMenuShopId() { return menuShopId; }
    public String getMenuShopName() { return menuShopName; }
    public List<String> getMenuShopLore() { return menuShopLore; }
    public Integer getMenuShopSlot() { return menuShopSlot; }
    public String getMenuArenaVoteId() { return menuArenaVoteId; }
    public String getMenuArenaVoteName() { return menuArenaVoteName; }
    public List<String> getMenuArenaVoteLore() { return menuArenaVoteLore; }
    public Integer getMenuArenaVoteSlot() { return menuArenaVoteSlot; }

}
