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

public class ReportMenuConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    private String menuPlayerListTitle;
    private Integer menuPlayerListSize;
    private String menuPlayerListItemSkullName;
    private String menuPlayerListItemSkullLore;
    private String menuPlayerListItemBackId;
    private String menuPlayerListItemBackName;
    private List<String> menuPlayerListItemBackLore;
    private Integer menuPlayerListItemBackSlot;
    private List<Integer> menuPlayerListFillerSlots;


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
        menuPlayerListItemSkullName = config.getString("report-menus.player-list-items.player-skulls.name");
        menuPlayerListItemSkullLore = config.getString("report-menus.player-list-items.player-skulls.lore");
        menuPlayerListItemBackId = validateAndGetMaterial(config, "report-menus.player-list-items.back.id", "ARROW");
        menuPlayerListItemBackName = config.getString("report-menus.player-list-items.back.name");
        menuPlayerListItemBackLore = config.getStringList("report-menus.player-list-items.back.lore");
        menuPlayerListItemBackSlot = config.getInt("report-menus.player-list-items.back.slot");
        menuPlayerListFillerSlots = config.getIntegerList("report-menus.player-list.filler-slots");

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
            ""
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
            plugin.getLogger().warning("Invalid materials detected. The configuration will be loaded with default values for problematic materials.");
        }
        
        loadConfig();
    }

    public String getMenuPlayerListTitle() { return menuPlayerListTitle; }
    public Integer getMenuPlayerListSize() { return menuPlayerListSize; }
    public String getMenuPlayerListItemSkullName() { return menuPlayerListItemSkullName; }
    public String getMenuPlayerListItemSkullLore() { return menuPlayerListItemSkullLore; }
    public String getMenuPlayerListItemBackId() { return menuPlayerListItemBackId; }
    public String getMenuPlayerListItemBackName() { return menuPlayerListItemBackName; }
    public List<String> getMenuPlayerListItemBackLore() { return menuPlayerListItemBackLore; }
    public Integer getMenuPlayerListItemBackSlot() { return menuPlayerListItemBackSlot; }
    public List<Integer> getMenuPlayerListFillerSlots() { return menuPlayerListFillerSlots; }

}

