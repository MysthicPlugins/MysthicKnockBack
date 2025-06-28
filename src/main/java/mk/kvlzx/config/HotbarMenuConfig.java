package mk.kvlzx.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class HotbarMenuConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    private String menuTitle;
    private int menuSize;

    private int knockerSlot;
    private int blocksSlot;
    private int bowSlot;
    private int plateSlot;
    private int featherSlot;
    private int pearlSlot;

    private String separatorMaterial;
    private String separatorName;
    private List<String> separatorLore;
    private int separatorData;
    private List<Integer> separatorSlots;

    private String saveMaterial;
    private String saveName;
    private List<String> saveLore;
    private int saveSlot;

    private String resetMaterial;
    private String resetName;
    private List<String> resetLore;
    private int resetSlot;

    private String backMaterial;
    private String backName;
    private List<String> backLore;
    private int backSlot;

    private String hotbarSavedMessage;
    private String hotbarResetMessage;

    public HotbarMenuConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("hotbar.yml", "config/menus", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        menuTitle = config.getString("menu.title");
        menuSize = config.getInt("menu.size");

        knockerSlot = config.getInt("menu.items.knocker.slot");
        blocksSlot = config.getInt("menu.items.blocks.slot");
        bowSlot = config.getInt("menu.items.bow.slot");
        plateSlot = config.getInt("menu.items.plate.slot");
        featherSlot = config.getInt("menu.items.feather.slot");
        pearlSlot = config.getInt("menu.items.pearl.slot");

        // Separator item
        separatorMaterial = validateAndGetMaterial(config, "menu.items.separator.id", "STAINED_GLASS_PANE");
        separatorName = config.getString("menu.items.separator.name");
        separatorLore = config.getStringList("menu.items.separator.lore");
        separatorData = config.getInt("menu.items.separator.data");
        separatorSlots = config.getIntegerList("menu.items.separator.slots");
        if (separatorSlots.isEmpty()) {
            separatorSlots = new ArrayList<>();
            for (int i = 27; i < 36; i++) {
                separatorSlots.add(i);
            }
        }

        // Save button
        saveMaterial = validateAndGetMaterial(config, "menu.items.save.id", "EMERALD_BLOCK");
        saveName = config.getString("menu.items.save.name");
        saveLore = config.getStringList("menu.items.save.lore");
        saveSlot = config.getInt("menu.items.save.slot");

        // Reset button
        resetMaterial = validateAndGetMaterial(config, "menu.items.reset.id", "REDSTONE_BLOCK");
        resetName = config.getString("menu.items.reset.name");
        resetLore = config.getStringList("menu.items.reset.lore");
        resetSlot = config.getInt("menu.items.reset.slot");

        // Back button
        backMaterial = validateAndGetMaterial(config, "menu.items.back.id", "ARROW");
        backName = config.getString("menu.items.back.name");
        backLore = config.getStringList("menu.items.back.lore");
        backSlot = config.getInt("menu.items.back.slot");

        // Messages
        hotbarSavedMessage = config.getString("menu.messages.hotbar-saved");
        hotbarResetMessage = config.getString("menu.messages.hotbar-reset");
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
            "menu.items.separator.id",
            "menu.items.save.id",
            "menu.items.reset.id",
            "menu.items.back.id"
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

    public ItemStack createMenuItem(String materialId, Player player, String name, List<String> lore) {
        ItemStack item;
        try {
            Material material = Material.valueOf(materialId.toUpperCase());
            item = new ItemStack(material);
            
            // Aplicar data específica para materiales que lo requieren
            if (material == Material.STAINED_GLASS_PANE && materialId.equals(separatorMaterial)) {
                item.setDurability((short) separatorData);
            }
            
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
            plugin.getLogger().warning("Invalid materials detected in menu configuration. The menu will be loaded with default values for problematic materials.");
        }
        loadConfig();
    }

    public String getMenuTitle() { return menuTitle; }
    public int getMenuSize() { return menuSize; }
    
    public int getKnockerSlot() { return knockerSlot; }
    public int getBlocksSlot() { return blocksSlot; }
    public int getBowSlot() { return bowSlot; }
    public int getPlateSlot() { return plateSlot; }
    public int getFeatherSlot() { return featherSlot; }
    public int getPearlSlot() { return pearlSlot; }
    public String getSeparatorMaterial() { return separatorMaterial; }
    public String getSeparatorName() { return separatorName; }
    public List<String> getSeparatorLore() { return separatorLore; }
    public int getSeparatorData() { return separatorData; }
    public List<Integer> getSeparatorSlots() { return separatorSlots; }
    public String getSaveMaterial() { return saveMaterial; }
    public String getSaveName() { return saveName; }
    public List<String> getSaveLore() { return saveLore; }
    public int getSaveSlot() { return saveSlot; }
    public String getResetMaterial() { return resetMaterial; }
    public String getResetName() { return resetName; }
    public List<String> getResetLore() { return resetLore; }
    public int getResetSlot() { return resetSlot; }
    public String getBackMaterial() { return backMaterial; }
    public String getBackName() { return backName; }
    public List<String> getBackLore() { return backLore; }
    public int getBackSlot() { return backSlot; }

    public String getHotbarSavedMessage() { return hotbarSavedMessage; }
    public String getHotbarResetMessage() { return hotbarResetMessage; }
}
