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

public class KillMessagesShopConfig {
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
    
    // Kill messages configuration
    private List<Integer> killMessageSlots;
    private String killMessageTitle;
    private List<String> killMessageLore;
    private String materialSelected;
    private String materialUnselected;
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
    
    // Kill message items
    private Map<String, KillMessageItem> killMessageItems;
    
    // Status messages
    private Map<String, List<String>> statusMessages;
    
    // Messages
    private String messageSelectedMessage;
    private String messageDeselectedMessage;
    private String messagePurchasedMessage;
    private String insufficientFundsMessage;
    private String defaultVictimName;

    public KillMessagesShopConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("kill-messages-shop.yml", "config/menus", plugin);
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
        
        // Kill messages configuration
        killMessageSlots = config.getIntegerList("menu.items.kill_messages.slots");
        killMessageTitle = config.getString("menu.items.kill_messages.title");
        killMessageLore = config.getStringList("menu.items.kill_messages.lore");
        materialSelected = validateAndGetMaterial(config, "menu.items.kill_messages.material_selected", "ENCHANTED_BOOK");
        materialUnselected = validateAndGetMaterial(config, "menu.items.kill_messages.material_unselected", "PAPER");
        enchantedIfSelected = config.getBoolean("menu.items.kill_messages.enchanted_if_selected");
        hideEnchants = config.getBoolean("menu.items.kill_messages.hide_enchants");
        
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
        
        // Load kill message items
        loadKillMessageItems(config);
        
        // Load status messages
        loadStatusMessages(config);
        
        // Load messages
        messageSelectedMessage = config.getString("messages.message_selected");
        messageDeselectedMessage = config.getString("messages.message_deselected");
        messagePurchasedMessage = config.getString("messages.message_purchased");
        insufficientFundsMessage = config.getString("messages.insufficient_funds");
        defaultVictimName = config.getString("messages.default_victim_name");
    }
    
    private void loadKillMessageItems(FileConfiguration config) {
        killMessageItems = new HashMap<>();
        ConfigurationSection killMessageItemsSection = config.getConfigurationSection("kill_message_items");
        
        if (killMessageItemsSection != null) {
            for (String key : killMessageItemsSection.getKeys(false)) {
                ConfigurationSection messageSection = killMessageItemsSection.getConfigurationSection(key);
                if (messageSection != null) {
                    KillMessageItem messageItem = new KillMessageItem(
                        key,
                        messageSection.getString("name", key),
                        messageSection.getString("message", ""),
                        messageSection.getInt("price", 0),
                        messageSection.getString("rarity", "COMMON"),
                        messageSection.getString("rarity_color", "&7"),
                        messageSection.getString("description", "")
                    );
                    killMessageItems.put(key, messageItem);
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
            "menu.items.kill_messages.material_selected",
            "menu.items.kill_messages.material_unselected",
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
        
        if (!validateAllMaterials()) {
            plugin.getLogger().warning("Invalid materials detected in kill messages menu configuration. The menu will be loaded with default values for problematic materials.");
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
    
    // Getters for kill messages configuration
    public List<Integer> getKillMessageSlots() { return new ArrayList<>(killMessageSlots); }
    public String getKillMessageTitle() { return killMessageTitle; }
    public List<String> getKillMessageLore() { return new ArrayList<>(killMessageLore); }
    public String getMaterialSelected() { return materialSelected; }
    public String getMaterialUnselected() { return materialUnselected; }
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
    
    // Getters for kill message items and messages
    public Map<String, KillMessageItem> getKillMessageItems() { return new HashMap<>(killMessageItems); }
    public Map<String, List<String>> getStatusMessages() { return new HashMap<>(statusMessages); }
    public String getMessageSelectedMessage() { return messageSelectedMessage; }
    public String getMessageDeselectedMessage() { return messageDeselectedMessage; }
    public String getMessagePurchasedMessage() { return messagePurchasedMessage; }
    public String getInsufficientFundsMessage() { return insufficientFundsMessage; }
    public String getDefaultVictimName() { return defaultVictimName; }
    
    // Helper methods
    public List<String> getStatusMessage(String statusKey) {
        return statusMessages.getOrDefault(statusKey, new ArrayList<>());
    }
    
    public KillMessageItem getKillMessageItem(String key) {
        return killMessageItems.get(key);
    }
    
    public List<KillMessageItem> getSortedKillMessagesByRarity() {
        Map<String, Integer> rarityOrder = new HashMap<>();
        rarityOrder.put("COMMON", 1);
        rarityOrder.put("EPIC", 2);
        rarityOrder.put("LEGENDARY", 3);
        
        List<KillMessageItem> sortedList = new ArrayList<>(killMessageItems.values());
        
        sortedList.sort((item1, item2) -> {
            // First compare by rarity
            int rarity1 = rarityOrder.getOrDefault(item1.getRarity(), 999);
            int rarity2 = rarityOrder.getOrDefault(item2.getRarity(), 999);
            
            if (rarity1 != rarity2) {
                return Integer.compare(rarity1, rarity2);
            }
            
            // If rarities are the same, compare by price
            if (item1.getPrice() != item2.getPrice()) {
                return Integer.compare(item1.getPrice(), item2.getPrice());
            }
            
            // If both rarity and price are the same, compare by name
            return item1.getName().compareToIgnoreCase(item2.getName());
        });
        
        return sortedList;
    }
    
    // Inner class for kill message items
    public static class KillMessageItem {
        private final String key;
        private final String name;
        private final String message;
        private final int price;
        private final String rarity;
        private final String rarityColor;
        private final String description;
        
        public KillMessageItem(String key, String name, String message, int price, String rarity, 
                                String rarityColor, String description) {
            this.key = key;
            this.name = name;
            this.message = message;
            this.price = price;
            this.rarity = rarity;
            this.rarityColor = rarityColor;
            this.description = description;
        }
        
        // Getters
        public String getKey() { return key; }
        public String getName() { return name; }
        public String getMessage() { return message; }
        public int getPrice() { return price; }
        public String getRarity() { return rarity; }
        public String getRarityColor() { return rarityColor; }
        public String getDescription() { return description; }
    }
}
