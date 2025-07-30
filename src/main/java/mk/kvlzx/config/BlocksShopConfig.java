package mk.kvlzx.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class BlocksShopConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;
    
    // Menu configuration
    private String menuTitle;
    private int menuSize;
    
    // Balance item configuration
    private int balanceSlot;
    private String balanceMaterial;
    private String balanceTitle;
    private List<String> balanceLore;
    
    // Blocks configuration
    private List<Integer> blockSlots;
    private String blockTitle;
    private List<String> blockLore;
    private boolean enchantedIfSelected;
    private boolean hideEnchants;
    
    // Back button configuration
    private int backButtonSlot;
    private String backButtonMaterial;
    private String backButtonTitle;
    private List<String> backButtonLore;
    
    // Filler configuration
    private String fillerMaterial;
    private int fillerData;
    private String fillerTitle;
    private boolean fillEmptySlots;
    
    // Block items
    private Map<String, BlockItem> blockItems;
    
    // Status messages
    private Map<String, List<String>> statusMessages;
    
    // Messages
    private String blockSelectedMessage;
    private String blockPurchasedMessage;
    private String insufficientFundsMessage;
    private String bedrockLockedMessage;

    public BlocksShopConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("blocks-shop.yml", "config/menus", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();
        
        // Menu configuration
        menuTitle = config.getString("menu.title");
        menuSize = config.getInt("menu.size");
        
        // Balance item configuration
        balanceSlot = config.getInt("menu.items.balance.slot");
        balanceMaterial = validateAndGetMaterial(config, "menu.items.balance.material", "EMERALD");
        balanceTitle = config.getString("menu.items.balance.title");
        balanceLore = config.getStringList("menu.items.balance.lore");
        
        // Blocks configuration
        blockSlots = config.getIntegerList("menu.items.blocks.slots");
        blockTitle = config.getString("menu.items.blocks.title");
        blockLore = config.getStringList("menu.items.blocks.lore");
        enchantedIfSelected = config.getBoolean("menu.items.blocks.enchanted_if_selected");
        hideEnchants = config.getBoolean("menu.items.blocks.hide_enchants");
        
        // Back button configuration
        backButtonSlot = config.getInt("menu.items.back_button.slot");
        backButtonMaterial = validateAndGetMaterial(config, "menu.items.back_button.material", "ARROW");
        backButtonTitle = config.getString("menu.items.back_button.title");
        backButtonLore = config.getStringList("menu.items.back_button.lore");
        
        // Filler configuration
        fillerMaterial = validateAndGetMaterial(config, "menu.items.filler.material", "STAINED_GLASS_PANE");
        fillerData = config.getInt("menu.items.filler.data");
        fillerTitle = config.getString("menu.items.filler.title");
        fillEmptySlots = config.getBoolean("menu.items.filler.fill_empty_slots");
        
        // Load block items
        loadBlockItems(config);
        
        // Load status messages
        loadStatusMessages(config);
        
        // Load messages
        blockSelectedMessage = config.getString("messages.block_selected");
        blockPurchasedMessage = config.getString("messages.block_purchased");
        insufficientFundsMessage = config.getString("messages.insufficient_funds");
        bedrockLockedMessage = config.getString("messages.bedrock_locked");
    }
    
    private void loadBlockItems(FileConfiguration config) {
        blockItems = new HashMap<>();
        ConfigurationSection blockItemsSection = config.getConfigurationSection("block_items");
        
        if (blockItemsSection != null) {
            for (String key : blockItemsSection.getKeys(false)) {
                ConfigurationSection blockSection = blockItemsSection.getConfigurationSection(key);
                if (blockSection != null) {
                    BlockItem blockItem = new BlockItem(
                        key,
                        blockSection.getString("name", key),
                        blockSection.getInt("price", 0),
                        blockSection.getString("rarity", "COMMON"),
                        blockSection.getString("rarity_color", "&7"),
                        blockSection.getString("lore", ""),
                        blockSection.getBoolean("default", false),
                        blockSection.getString("special_requirement", null)
                    );
                    blockItems.put(key, blockItem);
                }
            }
        }
    }
    
    private void loadStatusMessages(FileConfiguration config) {
        statusMessages = new HashMap<>();
        ConfigurationSection statusSection = config.getConfigurationSection("status_messages");
        
        if (statusSection != null) {
            for (String key : statusSection.getKeys(false)) {
                List<String> messages = statusSection.getStringList(key);
                statusMessages.put(key, messages);
            }
        }
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
            "menu.items.balance.material",
            "menu.items.back_button.material",
            "menu.items.filler.material"
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

    public ItemStack createMenuItem(String materialId, String name, List<String> lore) {
        ItemStack item;
        
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
        return item;
    }

    public void reload() {
        configFile.reloadConfig();
        
        // Validar materiales después de recargar la configuración
        if (!validateAllMaterials()) {
            plugin.getLogger().warning("Invalid materials detected in menu configuration. The menu will be loaded with default values for problematic materials.");
        }
        
        loadConfig();
    }

    // Getters for menu configuration
    public String getMenuTitle() { return menuTitle; }
    public int getMenuSize() { return menuSize; }
    
    // Getters for balance item
    public int getBalanceSlot() { return balanceSlot; }
    public String getBalanceMaterial() { return balanceMaterial; }
    public String getBalanceTitle() { return balanceTitle; }
    public List<String> getBalanceLore() { return new ArrayList<>(balanceLore); }
    
    // Getters for blocks configuration
    public List<Integer> getBlockSlots() { return new ArrayList<>(blockSlots); }
    public String getBlockTitle() { return blockTitle; }
    public List<String> getBlockLore() { return new ArrayList<>(blockLore); }
    public boolean isEnchantedIfSelected() { return enchantedIfSelected; }
    public boolean isHideEnchants() { return hideEnchants; }
    
    // Getters for back button
    public int getBackButtonSlot() { return backButtonSlot; }
    public String getBackButtonMaterial() { return backButtonMaterial; }
    public String getBackButtonTitle() { return backButtonTitle; }
    public List<String> getBackButtonLore() { return new ArrayList<>(backButtonLore); }
    
    // Getters for filler
    public String getFillerMaterial() { return fillerMaterial; }
    public int getFillerData() { return fillerData; }
    public String getFillerTitle() { return fillerTitle; }
    public boolean isFillEmptySlots() { return fillEmptySlots; }
    
    // Getters for block items and messages
    public Map<String, BlockItem> getBlockItems() { return new HashMap<>(blockItems); }
    public Map<String, List<String>> getStatusMessages() { return new HashMap<>(statusMessages); }
    public String getBlockSelectedMessage() { return blockSelectedMessage; }
    public String getBlockPurchasedMessage() { return blockPurchasedMessage; }
    public String getInsufficientFundsMessage() { return insufficientFundsMessage; }
    public String getBedrockLockedMessage() { return bedrockLockedMessage; }
    
    // Helper methods
    public List<String> getStatusMessage(String statusKey) {
        return statusMessages.getOrDefault(statusKey, new ArrayList<>());
    }
    
    public BlockItem getBlockItem(String key) {
        return blockItems.get(key);
    }
    
    // Inner class for block items
    public static class BlockItem {
        private final String key;
        private final String name;
        private final int price;
        private final String rarity;
        private final String rarityColor;
        private final String lore;
        private final boolean isDefault;
        private final String specialRequirement;
        
        public BlockItem(String key, String name, int price, String rarity, String rarityColor, 
                        String lore, boolean isDefault, String specialRequirement) {
            this.key = key;
            this.name = name;
            this.price = price;
            this.rarity = rarity;
            this.rarityColor = rarityColor;
            this.lore = lore;
            this.isDefault = isDefault;
            this.specialRequirement = specialRequirement;
        }
        
        // Getters
        public String getKey() { return key; }
        public String getName() { return name; }
        public int getPrice() { return price; }
        public String getRarity() { return rarity; }
        public String getRarityColor() { return rarityColor; }
        public String getLore() { return lore; }
        public boolean isDefault() { return isDefault; }
        public String getSpecialRequirement() { return specialRequirement; }
        public boolean hasSpecialRequirement() { return specialRequirement != null; }
    }
}