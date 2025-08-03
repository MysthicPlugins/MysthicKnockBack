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

public class MusicShopConfig {
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
    
    // Background music configuration
    private List<Integer> musicSlots;
    private String musicTitle;
    private List<String> musicLore;
    private Map<String, MaterialPair> rarityMaterials;
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
    
    // Background music items
    private Map<String, BackgroundMusicItem> musicItems;
    
    // Status messages
    private Map<String, List<String>> statusMessages;
    
    // Messages
    private String musicSelectedMessage;
    private String musicDeselectedMessage;
    private String musicPurchasedMessage;
    private String insufficientFundsMessage;

    public MusicShopConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("music-shop.yml", "config/menus", plugin);
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
        
        // Background music configuration
        musicSlots = config.getIntegerList("menu.items.background_music.slots");
        musicTitle = config.getString("menu.items.background_music.title");
        musicLore = config.getStringList("menu.items.background_music.lore");
        enchantedIfSelected = config.getBoolean("menu.items.background_music.enchanted_if_selected");
        hideEnchants = config.getBoolean("menu.items.background_music.hide_enchants");
        
        // Load rarity materials
        loadRarityMaterials(config);
        
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
        
        // Load background music items
        loadMusicItems(config);
        
        // Load status messages
        loadStatusMessages(config);
        
        // Load messages
        musicSelectedMessage = config.getString("messages.music_selected");
        musicDeselectedMessage = config.getString("messages.music_deselected");
        musicPurchasedMessage = config.getString("messages.music_purchased");
        insufficientFundsMessage = config.getString("messages.insufficient_funds");
    }
    
    private void loadRarityMaterials(FileConfiguration config) {
        rarityMaterials = new HashMap<>();
        ConfigurationSection materialsSection = config.getConfigurationSection("menu.items.background_music.materials");
        
        if (materialsSection != null) {
            for (String rarity : materialsSection.getKeys(false)) {
                ConfigurationSection raritySection = materialsSection.getConfigurationSection(rarity);
                if (raritySection != null) {
                    String selected = validateAndGetMaterial(config, 
                        "menu.items.background_music.materials." + rarity + ".selected", "JUKEBOX");
                    String unselected = validateAndGetMaterial(config, 
                        "menu.items.background_music.materials." + rarity + ".unselected", "RECORD_12");
                    
                    rarityMaterials.put(rarity.toUpperCase(), new MaterialPair(selected, unselected));
                }
            }
        }
        
        // Ensure default materials exist
        if (!rarityMaterials.containsKey("COMMON")) {
            rarityMaterials.put("COMMON", new MaterialPair("JUKEBOX", "RECORD_12"));
        }
        if (!rarityMaterials.containsKey("EPIC")) {
            rarityMaterials.put("EPIC", new MaterialPair("JUKEBOX", "RECORD_11"));
        }
        if (!rarityMaterials.containsKey("LEGENDARY")) {
            rarityMaterials.put("LEGENDARY", new MaterialPair("JUKEBOX", "RECORD_MELLOHI"));
        }
    }
    
    private void loadMusicItems(FileConfiguration config) {
        musicItems = new HashMap<>();
        ConfigurationSection musicItemsSection = config.getConfigurationSection("background_music_items");
        
        if (musicItemsSection != null) {
            for (String key : musicItemsSection.getKeys(false)) {
                ConfigurationSection musicSection = musicItemsSection.getConfigurationSection(key);
                if (musicSection != null) {
                    BackgroundMusicItem musicItem = new BackgroundMusicItem(
                        key,
                        musicSection.getString("name", key),
                        musicSection.getInt("price", 0),
                        musicSection.getString("rarity", "COMMON"),
                        musicSection.getString("rarity_color", "&7"),
                        musicSection.getString("description", ""),
                        musicSection.getString("sound", "records.cat"),
                        (float) musicSection.getDouble("volume", 1.0),
                        (float) musicSection.getDouble("pitch", 1.0)
                    );
                    musicItems.put(key, musicItem);
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
        
        // Validate basic materials
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
        
        // Validate rarity materials
        ConfigurationSection materialsSection = config.getConfigurationSection("menu.items.background_music.materials");
        if (materialsSection != null) {
            for (String rarity : materialsSection.getKeys(false)) {
                ConfigurationSection raritySection = materialsSection.getConfigurationSection(rarity);
                if (raritySection != null) {
                    String[] rarityMaterialKeys = {"selected", "unselected"};
                    for (String key : rarityMaterialKeys) {
                        String materialName = raritySection.getString(key);
                        if (materialName != null && !materialName.trim().isEmpty()) {
                            try {
                                Material.valueOf(materialName.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().severe("Invalid material detected: '" + materialName + 
                                    "' at 'menu.items.background_music.materials." + rarity + "." + key + "'");
                                allValid = false;
                            }
                        }
                    }
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
            plugin.getLogger().warning("Invalid materials detected in music shop configuration. The menu will be loaded with default values for problematic materials.");
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
    
    // Getters for background music configuration
    public List<Integer> getMusicSlots() { return new ArrayList<>(musicSlots); }
    public String getMusicTitle() { return musicTitle; }
    public List<String> getMusicLore() { return new ArrayList<>(musicLore); }
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
    
    // Getters for background music items and messages
    public Map<String, BackgroundMusicItem> getMusicItems() { return new HashMap<>(musicItems); }
    public Map<String, List<String>> getStatusMessages() { return new HashMap<>(statusMessages); }
    public String getMusicSelectedMessage() { return musicSelectedMessage; }
    public String getMusicDeselectedMessage() { return musicDeselectedMessage; }
    public String getMusicPurchasedMessage() { return musicPurchasedMessage; }
    public String getInsufficientFundsMessage() { return insufficientFundsMessage; }
    
    // Helper methods
    public List<String> getStatusMessage(String statusKey) {
        return statusMessages.getOrDefault(statusKey, new ArrayList<>());
    }
    
    public BackgroundMusicItem getMusicItem(String key) {
        return musicItems.get(key);
    }
    
    public String getMaterialForRarity(String rarity, boolean isSelected) {
        MaterialPair materials = rarityMaterials.get(rarity.toUpperCase());
        if (materials == null) {
            materials = rarityMaterials.get("COMMON");
        }
        return isSelected ? materials.getSelected() : materials.getUnselected();
    }
    
    public List<BackgroundMusicItem> getSortedMusicByRarity() {
        Map<String, Integer> rarityOrder = new HashMap<>();
        rarityOrder.put("COMMON", 1);
        rarityOrder.put("EPIC", 2);
        rarityOrder.put("LEGENDARY", 3);
        
        List<BackgroundMusicItem> sortedList = new ArrayList<>(musicItems.values());
        
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
    
    // Inner classes
    public static class BackgroundMusicItem {
        private final String key;
        private final String name;
        private final int price;
        private final String rarity;
        private final String rarityColor;
        private final String description;
        private final String sound;
        private final float volume;
        private final float pitch;
        
        public BackgroundMusicItem(String key, String name, int price, String rarity, 
                                    String rarityColor, String description, String sound, 
                                    float volume, float pitch) {
            this.key = key;
            this.name = name;
            this.price = price;
            this.rarity = rarity;
            this.rarityColor = rarityColor;
            this.description = description;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
        
        // Getters
        public String getKey() { return key; }
        public String getName() { return name; }
        public int getPrice() { return price; }
        public String getRarity() { return rarity; }
        public String getRarityColor() { return rarityColor; }
        public String getDescription() { return description; }
        public String getSound() { return sound; }
        public float getVolume() { return volume; }
        public float getPitch() { return pitch; }
    }
    
    private static class MaterialPair {
        private final String selected;
        private final String unselected;
        
        public MaterialPair(String selected, String unselected) {
            this.selected = selected;
            this.unselected = unselected;
        }
        
        public String getSelected() { return selected; }
        public String getUnselected() { return unselected; }
    }
}
