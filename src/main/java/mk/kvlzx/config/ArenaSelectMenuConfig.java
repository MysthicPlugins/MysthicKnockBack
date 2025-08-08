package mk.kvlzx.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class ArenaSelectMenuConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    private String menuTitle;
    private Integer menuSize;

    // Arena items configuration
    private String arenaCurrentId;
    private String arenaCurrentName;
    private List<String> arenaCurrentLore;
    private String arenaAvailableId;
    private String arenaAvailableName;
    private List<String> arenaAvailableLore;
    private String arenaBlockedId;
    private String arenaBlockedName;
    private List<String> arenaBlockedLore;

    // Special items configuration
    private String infoItemId;
    private String infoItemName;
    private List<String> infoItemLore;
    private Integer infoItemSlot;
    private String backButtonId;
    private String backButtonName;
    private List<String> backButtonLore;
    private Integer backButtonSlot;

    // Decoration configuration
    private String decorationId;
    private Byte decorationData;

    // Arena grid configuration
    private Integer startSlot;
    private Integer slotsPerRow;
    private Integer skipSlots;
    private Integer maxSlot;

    private String cannotVoteTimeMessage;
    private String noPermissionMessage;
    private String alreadyInArenaMessage;
    private String voteUnavailableMessage;
    private String arenaNotFoundMessage;

    public ArenaSelectMenuConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("arena-select.yml", "config/menus", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        menuTitle = config.getString("menu.title");
        menuSize = config.getInt("menu.size");

        // Arena items configuration
        arenaCurrentId = validateAndGetMaterial(config, "arena-items.current.id", "EMERALD_BLOCK");
        arenaCurrentName = config.getString("arena-items.current.name");
        arenaCurrentLore = config.getStringList("arena-items.current.lore");

        arenaAvailableId = validateAndGetMaterial(config, "arena-items.available.id", "DIAMOND_BLOCK");
        arenaAvailableName = config.getString("arena-items.available.name");
        arenaAvailableLore = config.getStringList("arena-items.available.lore");

        arenaBlockedId = validateAndGetMaterial(config, "arena-items.blocked.id", "REDSTONE_BLOCK");
        arenaBlockedName = config.getString("arena-items.blocked.name");
        arenaBlockedLore = config.getStringList("arena-items.blocked.lore");

        // Special items configuration
        infoItemId = validateAndGetMaterial(config, "special-items.info.id", "PAPER");
        infoItemName = config.getString("special-items.info.name");
        infoItemLore = config.getStringList("special-items.info.lore");
        infoItemSlot = config.getInt("special-items.info.slot");

        backButtonId = validateAndGetMaterial(config, "special-items.back-button.id", "ARROW");
        backButtonName = config.getString("special-items.back-button.name");
        backButtonLore = config.getStringList("special-items.back-button.lore");
        backButtonSlot = config.getInt("special-items.back-button.slot");

        // Decoration configuration
        decorationId = validateAndGetMaterial(config, "decoration.id", "STAINED_GLASS_PANE");
        decorationData = (byte) config.getInt("decoration.data");

        // Arena grid configuration
        startSlot = config.getInt("arena-grid.start-slot");
        slotsPerRow = config.getInt("arena-grid.slots-per-row");
        skipSlots = config.getInt("arena-grid.skip-slots");
        maxSlot = config.getInt("arena-grid.max-slot");

        cannotVoteTimeMessage = config.getString("messages.cannot-vote-time");
        noPermissionMessage = config.getString("messages.no-permission");
        alreadyInArenaMessage = config.getString("messages.already-in-arena");
        voteUnavailableMessage = config.getString("messages.vote-unavailable");
        arenaNotFoundMessage = config.getString("messages.arena-not-found");
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
            "arena-items.current.id",
            "arena-items.available.id",
            "arena-items.blocked.id",
            "special-items.info.id",
            "special-items.back-button.id",
            "decoration.id"
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

    public ItemStack createArenaItem(String arenaName, ArenaItemType type, String timeLeft) {
        String materialId;
        String name;
        List<String> lore;

        switch (type) {
            case CURRENT:
                materialId = arenaCurrentId;
                name = arenaCurrentName;
                lore = new ArrayList<>(arenaCurrentLore);
                break;
            case AVAILABLE:
                materialId = arenaAvailableId;
                name = arenaAvailableName;
                lore = new ArrayList<>(arenaAvailableLore);
                break;
            case BLOCKED:
                materialId = arenaBlockedId;
                name = arenaBlockedName;
                lore = new ArrayList<>(arenaBlockedLore);
                break;
            default:
                materialId = "STONE";
                name = "&cError";
                lore = new ArrayList<>();
        }

        // Replace placeholders
        if (name != null) {
            name = name.replace("%arena_name%", arenaName);
        }

        List<String> processedLore = new ArrayList<>();
        if (lore != null) {
            for (String line : lore) {
                String processedLine = line.replace("%arena_name%", arenaName)
                                            .replace("%time_left%", timeLeft != null ? timeLeft : "N/A");
                processedLore.add(processedLine);
            }
        }

        return createMenuItem(materialId, name, processedLore);
    }

    public ItemStack createInfoItem(String currentArena, int totalArenas, String timeLeft) {
        String name = infoItemName;
        List<String> lore = new ArrayList<>();

        if (infoItemLore != null) {
            for (String line : infoItemLore) {
                String processedLine = line.replace("%current_arena%", currentArena != null ? currentArena : plugin.getTabConfig().getScoreNullArena())
                                            .replace("%total_arenas%", String.valueOf(totalArenas))
                                            .replace("%time_left%", timeLeft != null ? timeLeft : "N/A");
                lore.add(processedLine);
            }
        }

        return createMenuItem(infoItemId, name, lore);
    }

    public ItemStack createBackButton() {
        return createMenuItem(backButtonId, backButtonName, backButtonLore);
    }

    public ItemStack createDecorationItem() {
        try {
            Material material = Material.valueOf(decorationId.toUpperCase());
            ItemStack item = new ItemStack(material, 1, decorationData);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
            return item;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Failed to create decoration item with material: " + decorationId + ". Using STAINED_GLASS_PANE as fallback.");
            ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 9);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
            return item;
        }
    }

    private ItemStack createMenuItem(String materialId, String name, List<String> lore) {
        ItemStack item;
        
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
        
        return item;
    }

    public int calculateNextSlot(int currentSlot) {
        int nextSlot = currentSlot + 1;
        int position = nextSlot - startSlot;
        
        if (position % slotsPerRow == 0) {
            nextSlot += skipSlots;
        }
        
        return nextSlot;
    }

    public void reload() {
        configFile.reloadConfig();
        
        if (!validateAllMaterials()) {
            plugin.getLogger().warning("Invalid materials detected in arena select menu configuration. The menu will be loaded with default values for problematic materials.");
        }
        
        loadConfig();
    }

    // Getters
    public String getMenuTitle() { return menuTitle; }
    public Integer getMenuSize() { return menuSize; }
    public String getArenaCurrentId() { return arenaCurrentId; }
    public String getArenaAvailableId() { return arenaAvailableId; }
    public String getArenaBlockedId() { return arenaBlockedId; }
    public String getInfoItemId() { return infoItemId; }
    public String getBackButtonId() { return backButtonId; }
    public String getDecorationId() { return decorationId; }
    public Integer getInfoItemSlot() { return infoItemSlot; }
    public Integer getBackButtonSlot() { return backButtonSlot; }
    public Integer getStartSlot() { return startSlot; }
    public Integer getSlotsPerRow() { return slotsPerRow; }
    public Integer getSkipSlots() { return skipSlots; }
    public Integer getMaxSlot() { return maxSlot; }

    public String getCannotVoteTimeMessage() { return cannotVoteTimeMessage; }
    public String getNoPermissionMessage() { return noPermissionMessage; }
    public String getAlreadyInArenaMessage() { return alreadyInArenaMessage; }
    public String getVoteUnavailableMessage() { return voteUnavailableMessage; }
    public String getArenaNotFoundMessage() { return arenaNotFoundMessage; }

    public enum ArenaItemType {
        CURRENT, AVAILABLE, BLOCKED
    }

}