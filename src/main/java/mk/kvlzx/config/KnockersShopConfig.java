package mk.kvlzx.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.items.CustomItem.ItemType;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class KnockersShopConfig {
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
    
    // Knockers configuration
    private List<Integer> knockerSlots;
    private String knockerTitle;
    private List<String> knockerLore;
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
    
    // Knocker items
    private Map<String, KnockerItem> knockerItems;
    
    // Status messages
    private Map<String, List<String>> statusMessages;
    
    // Messages
    private String knockerSelectedMessage;
    private String knockerPurchasedMessage;
    private String insufficientFundsMessage;

    // Hardcoded knocker materials mapping
    private static final Map<String, KnockerMaterial> KNOCKER_MATERIALS = new HashMap<>();
    
    static {
        // COMMON knockers
        KNOCKER_MATERIALS.put("stick", new KnockerMaterial(Material.STICK, 0));
        KNOCKER_MATERIALS.put("bone", new KnockerMaterial(Material.BONE, 0));
        KNOCKER_MATERIALS.put("blaze_rod", new KnockerMaterial(Material.BLAZE_ROD, 0));
        KNOCKER_MATERIALS.put("carrot_stick", new KnockerMaterial(Material.CARROT_STICK, 0));
        
        // UNCOMMON knockers
        KNOCKER_MATERIALS.put("coal", new KnockerMaterial(Material.COAL, 0));
        KNOCKER_MATERIALS.put("apple", new KnockerMaterial(Material.APPLE, 0));
        KNOCKER_MATERIALS.put("saddle", new KnockerMaterial(Material.SADDLE, 0));
        KNOCKER_MATERIALS.put("name_tag", new KnockerMaterial(Material.NAME_TAG, 0));
        
        // RARE knockers
        KNOCKER_MATERIALS.put("paper", new KnockerMaterial(Material.PAPER, 0));
        KNOCKER_MATERIALS.put("enchanted_book", new KnockerMaterial(Material.ENCHANTED_BOOK, 0));
        KNOCKER_MATERIALS.put("rabbit_foot", new KnockerMaterial(Material.RABBIT_FOOT, 0));
        KNOCKER_MATERIALS.put("iron_sword", new KnockerMaterial(Material.IRON_SWORD, 0));
        
        // EPIC knockers
        KNOCKER_MATERIALS.put("diamond_sword", new KnockerMaterial(Material.DIAMOND_SWORD, 0));
        KNOCKER_MATERIALS.put("iron_axe", new KnockerMaterial(Material.IRON_AXE, 0));
        KNOCKER_MATERIALS.put("diamond_axe", new KnockerMaterial(Material.DIAMOND_AXE, 0));
        KNOCKER_MATERIALS.put("diamond_pickaxe", new KnockerMaterial(Material.DIAMOND_PICKAXE, 0));
        
        // LEGENDARY knockers
        KNOCKER_MATERIALS.put("nether_star", new KnockerMaterial(Material.NETHER_STAR, 0));
        KNOCKER_MATERIALS.put("ghast_tear", new KnockerMaterial(Material.GHAST_TEAR, 0));
        KNOCKER_MATERIALS.put("prismarine_shard", new KnockerMaterial(Material.PRISMARINE_SHARD, 0));
        KNOCKER_MATERIALS.put("emerald", new KnockerMaterial(Material.EMERALD, 0));
    }

    public KnockersShopConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("knockers-shop.yml", "config/menus", plugin);
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
        
        // Knockers configuration
        knockerSlots = config.getIntegerList("menu.items.knockers.slots");
        knockerTitle = config.getString("menu.items.knockers.title");
        knockerLore = config.getStringList("menu.items.knockers.lore");
        enchantedIfSelected = config.getBoolean("menu.items.knockers.enchanted_if_selected");
        hideEnchants = config.getBoolean("menu.items.knockers.hide_enchants");
        
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
        
        // Load knocker items
        loadKnockerItems(config);
        
        // Load status messages
        loadStatusMessages(config);
        
        // Load messages
        knockerSelectedMessage = config.getString("messages.knocker_selected");
        knockerPurchasedMessage = config.getString("messages.knocker_purchased");
        insufficientFundsMessage = config.getString("messages.insufficient_funds");
    }
    
    private void loadKnockerItems(FileConfiguration config) {
        knockerItems = new HashMap<>();
        ConfigurationSection knockerItemsSection = config.getConfigurationSection("knocker_items");
        
        if (knockerItemsSection != null) {
            for (String key : knockerItemsSection.getKeys(false)) {
                ConfigurationSection knockerSection = knockerItemsSection.getConfigurationSection(key);
                if (knockerSection != null && KNOCKER_MATERIALS.containsKey(key)) {
                    KnockerMaterial knockerMaterial = KNOCKER_MATERIALS.get(key);
                    KnockerItem knockerItem = new KnockerItem(
                        key,
                        knockerSection.getString("name", key),
                        knockerMaterial.getMaterial(),
                        knockerMaterial.getData(),
                        knockerSection.getInt("price", 0),
                        knockerSection.getString("rarity", "COMMON"),
                        knockerSection.getString("rarity_color", "&7"),
                        knockerSection.getString("lore", ""),
                        knockerSection.getBoolean("default", false)
                    );
                    knockerItems.put(key, knockerItem);
                } else if (!KNOCKER_MATERIALS.containsKey(key)) {
                    plugin.getLogger().warning("Unknown knocker key '" + key + "' in configuration. Skipping...");
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
        return createMenuItem(materialId, name, 0, lore);
    }

    public ItemStack createMenuItem(String materialId, String name, int data, List<String> lore) {
        ItemStack item;
        
        try {
            Material material = Material.valueOf(materialId.toUpperCase());
            item = new ItemStack(material, 1, (short) data);
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
            plugin.getLogger().warning("Failed to create item with material: " + materialId + ". Using STICK as fallback.");
            item = new ItemStack(Material.STICK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§cInvalid Material");
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createKnockerItem(Material knockerType) {
        // Create knocker
        KnockersShopConfig.KnockerItem knockerItem = getKnockerItem(knockerType.name().toLowerCase());
        if (knockerItem != null) {
            ItemStack item = new ItemStack(knockerItem.getMaterial(), 1, (short) knockerItem.getData());
            ItemMeta meta = item.getItemMeta();
            String displayName = knockerItem.getRarityColor() + knockerItem.getName();
            meta.setDisplayName(MessageUtils.getColor(displayName));
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.getColor(knockerItem.getRarityColor() + knockerItem.getRarity()));
            lore.add(MessageUtils.getColor(knockerItem.getLore()));
            meta.setLore(lore);
            item.setItemMeta(meta);
            item.addUnsafeEnchantment(Enchantment.KNOCKBACK, plugin.getMainConfig().getKnockerKnockbackLevel());
            return item;
        } else {
            return CustomItem.create(ItemType.KNOCKER);
        }
    }

    public void reload() {
        configFile.reloadConfig();
        
        // Validar materiales después de recargar la configuración
        if (!validateAllMaterials()) {
            plugin.getLogger().warning("Invalid materials detected in knocker menu configuration. The menu will be loaded with default values for problematic materials.");
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
    
    // Getters for knockers configuration
    public List<Integer> getKnockerSlots() { return new ArrayList<>(knockerSlots); }
    public String getKnockerTitle() { return knockerTitle; }
    public List<String> getKnockerLore() { return new ArrayList<>(knockerLore); }
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
    
    // Getters for knocker items and messages
    public Map<String, KnockerItem> getKnockerItems() { return new HashMap<>(knockerItems); }
    public Map<String, List<String>> getStatusMessages() { return new HashMap<>(statusMessages); }
    public String getKnockerSelectedMessage() { return knockerSelectedMessage; }
    public String getKnockerPurchasedMessage() { return knockerPurchasedMessage; }
    public String getInsufficientFundsMessage() { return insufficientFundsMessage; }
    
    // Helper methods
    public List<String> getStatusMessage(String statusKey) {
        return statusMessages.getOrDefault(statusKey, new ArrayList<>());
    }
    
    public KnockerItem getKnockerItem(String key) {
        return knockerItems.get(key);
    }
    
    public static KnockerMaterial getKnockerMaterial(String key) {
        return KNOCKER_MATERIALS.get(key);
    }
    
    // Inner class for knocker materials
    public static class KnockerMaterial {
        private final Material material;
        private final int data;
        
        public KnockerMaterial(Material material, int data) {
            this.material = material;
            this.data = data;
        }
        
        public Material getMaterial() { return material; }
        public int getData() { return data; }
    }
    
    // Inner class for knocker items
    public static class KnockerItem {
        private final String key;
        private final String name;
        private final Material material;
        private final int data;
        private final int price;
        private final String rarity;
        private final String rarityColor;
        private final String lore;
        private final boolean isDefault;
        
        public KnockerItem(String key, String name, Material material, int data, int price, String rarity, 
                            String rarityColor, String lore, boolean isDefault) {
            this.key = key;
            this.name = name;
            this.material = material;
            this.data = data;
            this.price = price;
            this.rarity = rarity;
            this.rarityColor = rarityColor;
            this.lore = lore;
            this.isDefault = isDefault;
        }
        
        // Getters
        public String getKey() { return key; }
        public String getName() { return name; }
        public Material getMaterial() { return material; }
        public int getData() { return data; }
        public int getPrice() { return price; }
        public String getRarity() { return rarity; }
        public String getRarityColor() { return rarityColor; }
        public String getLore() { return lore; }
        public boolean isDefault() { return isDefault; }
    }
}
