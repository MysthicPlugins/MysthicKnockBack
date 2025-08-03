package mk.kvlzx.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class KillSoundsShopConfig {
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
    
    // Kill sounds configuration
    private List<Integer> killSoundSlots;
    private String killSoundTitle;
    private List<String> killSoundLore;
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
    
    // Kill sound items
    private Map<String, KillSoundItem> killSoundItems;
    
    // Status messages
    private Map<String, List<String>> statusMessages;
    
    // Messages
    private String soundSelectedMessage;
    private String soundDeselectedMessage;
    private String soundPurchasedMessage;
    private String insufficientFundsMessage;

    public KillSoundsShopConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("kill-sounds-shop.yml", "config/menus", plugin);
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
        
        // Kill sounds configuration
        killSoundSlots = config.getIntegerList("menu.items.kill_sounds.slots");
        killSoundTitle = config.getString("menu.items.kill_sounds.title");
        killSoundLore = config.getStringList("menu.items.kill_sounds.lore");
        enchantedIfSelected = config.getBoolean("menu.items.kill_sounds.enchanted_if_selected");
        hideEnchants = config.getBoolean("menu.items.kill_sounds.hide_enchants");
        
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
        
        // Load kill sound items
        loadKillSoundItems(config);
        
        // Load status messages
        loadStatusMessages(config);
        
        // Load messages
        soundSelectedMessage = config.getString("messages.sound_selected");
        soundDeselectedMessage = config.getString("messages.sound_deselected");
        soundPurchasedMessage = config.getString("messages.sound_purchased");
        insufficientFundsMessage = config.getString("messages.insufficient_funds");
    }
    
    private void loadRarityMaterials(FileConfiguration config) {
        rarityMaterials = new HashMap<>();
        ConfigurationSection materialsSection = config.getConfigurationSection("menu.items.kill_sounds.materials");
        
        if (materialsSection != null) {
            for (String rarity : materialsSection.getKeys(false)) {
                ConfigurationSection raritySection = materialsSection.getConfigurationSection(rarity);
                if (raritySection != null) {
                    String selected = validateAndGetMaterial(config, 
                        "menu.items.kill_sounds.materials." + rarity + ".selected", "JUKEBOX");
                    String unselected = validateAndGetMaterial(config, 
                        "menu.items.kill_sounds.materials." + rarity + ".unselected", "NOTE_BLOCK");
                    
                    rarityMaterials.put(rarity.toUpperCase(), new MaterialPair(selected, unselected));
                }
            }
        }
        
        // Ensure default materials exist
        if (!rarityMaterials.containsKey("COMMON")) {
            rarityMaterials.put("COMMON", new MaterialPair("JUKEBOX", "NOTE_BLOCK"));
        }
        if (!rarityMaterials.containsKey("EPIC")) {
            rarityMaterials.put("EPIC", new MaterialPair("JUKEBOX", "RECORD_11"));
        }
        if (!rarityMaterials.containsKey("LEGENDARY")) {
            rarityMaterials.put("LEGENDARY", new MaterialPair("JUKEBOX", "RECORD_12"));
        }
    }
    
    private void loadKillSoundItems(FileConfiguration config) {
        killSoundItems = new HashMap<>();
        ConfigurationSection killSoundItemsSection = config.getConfigurationSection("kill_sound_items");
        
        if (killSoundItemsSection != null) {
            for (String key : killSoundItemsSection.getKeys(false)) {
                ConfigurationSection soundSection = killSoundItemsSection.getConfigurationSection(key);
                if (soundSection != null) {
                    // Validate sound
                    String soundName = soundSection.getString("sound", "");
                    Sound sound = validateAndGetSound(soundName);
                    
                    KillSoundItem soundItem = new KillSoundItem(
                        key,
                        soundSection.getString("name", key),
                        sound,
                        (float) soundSection.getDouble("volume", 1.0),
                        (float) soundSection.getDouble("pitch", 1.0),
                        soundSection.getInt("price", 0),
                        soundSection.getString("rarity", "COMMON"),
                        soundSection.getString("rarity_color", "&7"),
                        soundSection.getString("description", "")
                    );
                    killSoundItems.put(key, soundItem);
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
    
    private Sound validateAndGetSound(String soundName) {
        if (soundName == null || soundName.trim().isEmpty()) {
            plugin.getLogger().warning("Sound name is empty. Using default sound: LEVEL_UP");
            return Sound.LEVEL_UP;
        }

        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound '" + soundName + "'. Using default sound: LEVEL_UP");
            return Sound.LEVEL_UP;
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
        ConfigurationSection materialsSection = config.getConfigurationSection("menu.items.kill_sounds.materials");
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
                                    "' at 'menu.items.kill_sounds.materials." + rarity + "." + key + "'");
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
            plugin.getLogger().warning("Invalid materials detected in kill sounds menu configuration. The menu will be loaded with default values for problematic materials.");
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
    
    // Getters for kill sounds configuration
    public List<Integer> getKillSoundSlots() { return new ArrayList<>(killSoundSlots); }
    public String getKillSoundTitle() { return killSoundTitle; }
    public List<String> getKillSoundLore() { return new ArrayList<>(killSoundLore); }
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
    
    // Getters for kill sound items and messages
    public Map<String, KillSoundItem> getKillSoundItems() { return new HashMap<>(killSoundItems); }
    public Map<String, List<String>> getStatusMessages() { return new HashMap<>(statusMessages); }
    public String getSoundSelectedMessage() { return soundSelectedMessage; }
    public String getSoundDeselectedMessage() { return soundDeselectedMessage; }
    public String getSoundPurchasedMessage() { return soundPurchasedMessage; }
    public String getInsufficientFundsMessage() { return insufficientFundsMessage; }
    
    // Helper methods
    public List<String> getStatusMessage(String statusKey) {
        return statusMessages.getOrDefault(statusKey, new ArrayList<>());
    }
    
    public KillSoundItem getKillSoundItem(String key) {
        return killSoundItems.get(key);
    }
    
    public String getMaterialForRarity(String rarity, boolean isSelected) {
        MaterialPair materials = rarityMaterials.get(rarity.toUpperCase());
        if (materials == null) {
            materials = rarityMaterials.get("COMMON");
        }
        return isSelected ? materials.getSelected() : materials.getUnselected();
    }
    
    public List<KillSoundItem> getSortedKillSoundsByRarity() {
        Map<String, Integer> rarityOrder = new HashMap<>();
        rarityOrder.put("COMMON", 1);
        rarityOrder.put("EPIC", 2);
        rarityOrder.put("LEGENDARY", 3);
        
        List<KillSoundItem> sortedList = new ArrayList<>(killSoundItems.values());
        
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
    public static class KillSoundItem {
        private final String key;
        private final String name;
        private final Sound sound;
        private final float volume;
        private final float pitch;
        private final int price;
        private final String rarity;
        private final String rarityColor;
        private final String description;
        
        public KillSoundItem(String key, String name, Sound sound, float volume, float pitch, 
                                int price, String rarity, String rarityColor, String description) {
            this.key = key;
            this.name = name;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.price = price;
            this.rarity = rarity;
            this.rarityColor = rarityColor;
            this.description = description;
        }
        
        // Getters
        public String getKey() { return key; }
        public String getName() { return name; }
        public Sound getSound() { return sound; }
        public float getVolume() { return volume; }
        public float getPitch() { return pitch; }
        public int getPrice() { return price; }
        public String getRarity() { return rarity; }
        public String getRarityColor() { return rarityColor; }
        public String getDescription() { return description; }
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
