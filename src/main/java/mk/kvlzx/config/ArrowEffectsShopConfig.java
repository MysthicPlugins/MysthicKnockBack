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

public class ArrowEffectsShopConfig {
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
    
    // Arrow effects configuration
    private List<Integer> arrowEffectSlots;
    private String arrowEffectTitle;
    private List<String> arrowEffectLore;
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
    
    // Arrow effect items
    private Map<String, ArrowEffectItem> arrowEffectItems;
    
    // Status messages
    private Map<String, List<String>> statusMessages;
    
    // Messages
    private String effectSelectedMessage;
    private String effectDeselectedMessage;
    private String effectPurchasedMessage;
    private String insufficientFundsMessage;

    public ArrowEffectsShopConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("arrow-effects-shop.yml", "config/menus", plugin);
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
        
        // Arrow effects configuration
        arrowEffectSlots = config.getIntegerList("menu.items.arrow_effects.slots");
        arrowEffectTitle = config.getString("menu.items.arrow_effects.title");
        arrowEffectLore = config.getStringList("menu.items.arrow_effects.lore");
        materialSelected = validateAndGetMaterial(config, "menu.items.arrow_effects.material_selected", "ARROW");
        materialUnselected = validateAndGetMaterial(config, "menu.items.arrow_effects.material_unselected", "ARROW");
        enchantedIfSelected = config.getBoolean("menu.items.arrow_effects.enchanted_if_selected");
        hideEnchants = config.getBoolean("menu.items.arrow_effects.hide_enchants");
        
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
        
        // Load arrow effect items
        loadArrowEffectItems(config);
        
        // Load status messages
        loadStatusMessages(config);
        
        // Load messages
        effectSelectedMessage = config.getString("messages.effect_selected");
        effectDeselectedMessage = config.getString("messages.effect_deselected");
        effectPurchasedMessage = config.getString("messages.effect_purchased");
        insufficientFundsMessage = config.getString("messages.insufficient_funds");
    }
    
    private void loadArrowEffectItems(FileConfiguration config) {
        arrowEffectItems = new HashMap<>();
        ConfigurationSection arrowEffectItemsSection = config.getConfigurationSection("arrow_effect_items");
        
        if (arrowEffectItemsSection != null) {
            for (String key : arrowEffectItemsSection.getKeys(false)) {
                ConfigurationSection effectSection = arrowEffectItemsSection.getConfigurationSection(key);
                if (effectSection != null) {
                    ArrowEffectItem effectItem = new ArrowEffectItem(
                        key,
                        effectSection.getString("name", key),
                        effectSection.getString("description", ""),
                        effectSection.getInt("price", 0),
                        effectSection.getString("rarity", "COMMON"),
                        effectSection.getString("rarity_color", "&7"),
                        effectSection.getString("effect", "FLAME"),
                        (float) effectSection.getDouble("effect_speed", 0.0),
                        effectSection.getInt("effect_count", 1),
                        (float) effectSection.getDouble("offset_x", 0.0),
                        (float) effectSection.getDouble("offset_y", 0.0),
                        (float) effectSection.getDouble("offset_z", 0.0)
                    );
                    arrowEffectItems.put(key, effectItem);
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
            "menu.items.arrow_effects.material_selected",
            "menu.items.arrow_effects.material_unselected",
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
            plugin.getLogger().warning("Invalid materials detected in arrow effects menu configuration. The menu will be loaded with default values for problematic materials.");
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
    
    // Getters for arrow effects configuration
    public List<Integer> getArrowEffectSlots() { return new ArrayList<>(arrowEffectSlots); }
    public String getArrowEffectTitle() { return arrowEffectTitle; }
    public List<String> getArrowEffectLore() { return new ArrayList<>(arrowEffectLore); }
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
    
    // Getters for arrow effect items and messages
    public Map<String, ArrowEffectItem> getArrowEffectItems() { return new HashMap<>(arrowEffectItems); }
    public Map<String, List<String>> getStatusMessages() { return new HashMap<>(statusMessages); }
    public String getEffectSelectedMessage() { return effectSelectedMessage; }
    public String getEffectDeselectedMessage() { return effectDeselectedMessage; }
    public String getEffectPurchasedMessage() { return effectPurchasedMessage; }
    public String getInsufficientFundsMessage() { return insufficientFundsMessage; }
    
    // Helper methods
    public List<String> getStatusMessage(String statusKey) {
        return statusMessages.getOrDefault(statusKey, new ArrayList<>());
    }
    
    public ArrowEffectItem getArrowEffectItem(String key) {
        return arrowEffectItems.get(key);
    }
    
    public List<ArrowEffectItem> getSortedArrowEffectsByRarity() {
        Map<String, Integer> rarityOrder = new HashMap<>();
        rarityOrder.put("COMMON", 1);
        rarityOrder.put("EPIC", 2);
        rarityOrder.put("LEGENDARY", 3);
        
        List<ArrowEffectItem> sortedList = new ArrayList<>(arrowEffectItems.values());
        
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
    
    // Inner class for arrow effect items
    public static class ArrowEffectItem {
        private final String key;
        private final String name;
        private final String description;
        private final int price;
        private final String rarity;
        private final String rarityColor;
        private final String effect;
        private final float effectSpeed;
        private final int effectCount;
        private final float offsetX;
        private final float offsetY;
        private final float offsetZ;
        
        public ArrowEffectItem(String key, String name, String description, int price, String rarity, 
                                String rarityColor, String effect, float effectSpeed, int effectCount,
                                float offsetX, float offsetY, float offsetZ) {
            this.key = key;
            this.name = name;
            this.description = description;
            this.price = price;
            this.rarity = rarity;
            this.rarityColor = rarityColor;
            this.effect = effect;
            this.effectSpeed = effectSpeed;
            this.effectCount = effectCount;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }
        
        // Getters
        public String getKey() { return key; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getPrice() { return price; }
        public String getRarity() { return rarity; }
        public String getRarityColor() { return rarityColor; }
        public String getEffect() { return effect; }
        public float getEffectSpeed() { return effectSpeed; }
        public int getEffectCount() { return effectCount; }
        public float getOffsetX() { return offsetX; }
        public float getOffsetY() { return offsetY; }
        public float getOffsetZ() { return offsetZ; }
    }
}
