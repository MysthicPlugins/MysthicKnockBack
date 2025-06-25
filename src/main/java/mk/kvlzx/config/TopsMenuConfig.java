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

public class TopsMenuConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    private String topKillsTitle;
    private Integer topKillsSize;
    private String topKillsPlayersName;
    private List<String> topKillsPlayersLore;
    private String topKillsNonDataName;
    private List<String> topKillsNonDataLore;
    private List<Integer> topKillsSlots;
    private String topKillsBackId;
    private String topKillsBackName;
    private List<String> topKillsBackLore;
    private Integer topKillsBackSlot;

    private String topEloTitle;
    private Integer topEloSize;
    private String topEloPlayersName;
    private List<String> topEloPlayersLore;
    private String topEloNonDataName;
    private List<String> topEloNonDataLore;
    private List<Integer> topEloSlots;
    private String topEloBackId;
    private String topEloBackName;
    private List<String> topEloBackLore;
    private Integer topEloBackSlot;

    private String topKdrTitle;
    private Integer topKdrSize;
    private String topKdrPlayersName;
    private List<String> topKdrPlayersLore;
    private String topKdrNonDataName;
    private List<String> topKdrNonDataLore;
    private List<Integer> topKdrSlots;
    private String topKdrBackId;
    private String topKdrBackName;
    private List<String> topKdrBackLore;
    private Integer topKdrBackSlot;

    private String topStreaksTitle;
    private Integer topStreaksSize;
    private String topStreaksPlayersName;
    private List<String> topStreaksPlayersLore;
    private String topStreaksNonDataName;
    private List<String> topStreaksNonDataLore;
    private List<Integer> topStreaksSlots;
    private String topStreaksBackId;
    private String topStreaksBackName;
    private List<String> topStreaksBackLore;
    private Integer topStreaksBackSlot;

    private String topTimeTitle;
    private Integer topTimeSize;
    private String topTimePlayersName;
    private List<String> topTimePlayersLore;
    private String topTimeNonDataName;
    private List<String> topTimeNonDataLore;
    private List<Integer> topTimeSlots;
    private String topTimeBackId;
    private String topTimeBackName;
    private List<String> topTimeBackLore;
    private Integer topTimeBackSlot;

    public TopsMenuConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("top-menus.yml", "config/menus", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        topKillsTitle = config.getString("top-menus.top-kills.title");
        topKillsSize = config.getInt("top-menus.top-kills.size");
        topKillsPlayersName = config.getString("top-menus.top-kills.items.tops.name");
        topKillsPlayersLore = config.getStringList("top-menus.top-kills.items.tops.lore");
        topKillsNonDataName = config.getString("top-menus.top-kills.items.tops.non-data.name");
        topKillsNonDataLore = config.getStringList("top-menus.top-kills.items.tops.non-data.lore");
        topKillsSlots = config.getIntegerList("top-menus.top-kills.items.tops.slots");
        topKillsBackId = validateAndGetMaterial(config, "top-menus.top-kills.items.back.id", "ARROW");
        topKillsBackName = config.getString("top-menus.top-kills.items.back.name");
        topKillsBackLore = config.getStringList("top-menus.top-kills.items.back.lore");
        topKillsBackSlot = config.getInt("top-menus.top-kills.items.back.slot");

        topEloTitle = config.getString("top-menus.top-elo.title");
        topEloSize = config.getInt("top-menus.top-elo.size");
        topEloPlayersName = config.getString("top-menus.top-elo.items.tops.name");
        topEloPlayersLore = config.getStringList("top-menus.top-elo.items.tops.lore");
        topEloNonDataName = config.getString("top-menus.top-elo.items.tops.non-data.name");
        topEloNonDataLore = config.getStringList("top-menus.top-elo.items.tops.non-data.lore");
        topEloSlots = config.getIntegerList("top-menus.top-elo.items.tops.slots");
        topEloBackId = validateAndGetMaterial(config, "top-menus.top-elo.items.back.id", "ARROW");
        topEloBackName = config.getString("top-menus.top-elo.items.back.name");
        topEloBackLore = config.getStringList("top-menus.top-elo.items.back.lore");
        topEloBackSlot = config.getInt("top-menus.top-elo.items.back.slot");

        topKdrTitle = config.getString("top-menus.top-kdr.title");
        topKdrSize = config.getInt("top-menus.top-kdr.size");
        topKdrPlayersName = config.getString("top-menus.top-kdr.items.tops.name");
        topKdrPlayersLore = config.getStringList("top-menus.top-kdr.items.tops.lore");
        topKdrNonDataName = config.getString("top-menus.top-kdr.items.tops.non-data.name");
        topKdrNonDataLore = config.getStringList("top-menus.top-kdr.items.tops.non-data.lore");
        topKdrSlots = config.getIntegerList("top-menus.top-kdr.items.tops.slots");
        topKdrBackId = validateAndGetMaterial(config, "top-menus.top-kdr.items.back.id", "ARROW");
        topKdrBackName = config.getString("top-menus.top-kdr.items.back.name");
        topKdrBackLore = config.getStringList("top-menus.top-kdr.items.back.lore");
        topKdrBackSlot = config.getInt("top-menus.top-kdr.items.back.slot");

        topStreaksTitle = config.getString("top-menus.top-streaks.title");
        topStreaksSize = config.getInt("top-menus.top-streaks.size");
        topStreaksPlayersName = config.getString("top-menus.top-streaks.items.tops.name");
        topStreaksPlayersLore = config.getStringList("top-menus.top-streaks.items.tops.lore");
        topStreaksNonDataName = config.getString("top-menus.top-streaks.items.tops.non-data.name");
        topStreaksNonDataLore = config.getStringList("top-menus.top-streaks.items.tops.non-data.lore");
        topStreaksSlots = config.getIntegerList("top-menus.top-streaks.items.tops.slots");
        topStreaksBackId = validateAndGetMaterial(config, "top-menus.top-streaks.items.back.id", "ARROW");
        topStreaksBackName = config.getString("top-menus.top-streaks.items.back.name");
        topStreaksBackLore = config.getStringList("top-menus.top-streaks.items.back.lore");
        topStreaksBackSlot = config.getInt("top-menus.top-streaks.items.back.slot");

        topTimeTitle = config.getString("top-menus.top-time.title");
        topTimeSize = config.getInt("top-menus.top-time.size");
        topTimePlayersName = config.getString("top-menus.top-time.items.tops.name");
        topTimePlayersLore = config.getStringList("top-menus.top-time.items.tops.lore");
        topTimeNonDataName = config.getString("top-menus.top-time.items.tops.non-data.name");
        topTimeNonDataLore = config.getStringList("top-menus.top-time.items.tops.non-data.lore");
        topTimeSlots = config.getIntegerList("top-menus.top-time.items.tops.slots");
        topTimeBackId = validateAndGetMaterial(config, "top-menus.top-time.items.back.id", "ARROW");
        topTimeBackName = config.getString("top-menus.top-time.items.back.name");
        topTimeBackLore = config.getStringList("top-menus.top-time.items.back.lore");
        topTimeBackSlot = config.getInt("top-menus.top-time.items.back.slot");
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

    public String getTopKillsTitle() { return topKillsTitle; }
    public Integer getTopKillsSize() { return topKillsSize; }
    public String getTopKillsPlayersName() { return topKillsPlayersName; }
    public List<String> getTopKillsPlayersLore() { return topKillsPlayersLore; }
    public String getTopKillsNonDataName() { return topKillsNonDataName; }
    public List<String> getTopKillsNonDataLore() { return topKillsNonDataLore; }
    public List<Integer> getTopKillsSlots() { return topKillsSlots; }
    public String getTopKillsBackId() { return topKillsBackId; }
    public String getTopKillsBackName() { return topKillsBackName; }
    public List<String> getTopKillsBackLore() { return topKillsBackLore; }
    public Integer getTopKillsBackSlot() { return topKillsBackSlot; }

    public String getTopEloTitle() { return topEloTitle; }
    public Integer getTopEloSize() { return topEloSize; }
    public String getTopEloPlayersName() { return topEloPlayersName; }
    public List<String> getTopEloPlayersLore() { return topEloPlayersLore; }
    public String getTopEloNonDataName() { return topEloNonDataName; }
    public List<String> getTopEloNonDataLore() { return topEloNonDataLore; }
    public List<Integer> getTopEloSlots() { return topEloSlots; }
    public String getTopEloBackId() { return topEloBackId; }
    public String getTopEloBackName() { return topEloBackName; }
    public List<String> getTopEloBackLore() { return topEloBackLore; }
    public Integer getTopEloBackSlot() { return topEloBackSlot; }

    public String getTopKdrTitle() { return topKdrTitle; }
    public Integer getTopKdrSize() { return topKdrSize; }
    public String getTopKdrPlayersName() { return topKdrPlayersName; }
    public List<String> getTopKdrPlayersLore() { return topKdrPlayersLore; }
    public String getTopKdrNonDataName() { return topKdrNonDataName; }
    public List<String> getTopKdrNonDataLore() { return topKdrNonDataLore; }
    public List<Integer> getTopKdrSlots() { return topKdrSlots; }
    public String getTopKdrBackId() { return topKdrBackId; }
    public String getTopKdrBackName() { return topKdrBackName; }
    public List<String> getTopKdrBackLore() { return topKdrBackLore; }
    public Integer getTopKdrBackSlot() { return topKdrBackSlot; }

    public String getTopStreaksTitle() { return topStreaksTitle; }
    public Integer getTopStreaksSize() { return topStreaksSize; }
    public String getTopStreaksPlayersName() { return topStreaksPlayersName; }
    public List<String> getTopStreaksPlayersLore() { return topStreaksPlayersLore; }
    public String getTopStreaksNonDataName() { return topStreaksNonDataName; }
    public List<String> getTopStreaksNonDataLore() { return topStreaksNonDataLore; }
    public List<Integer> getTopStreaksSlots() { return topStreaksSlots; }
    public String getTopStreaksBackId() { return topStreaksBackId; }
    public String getTopStreaksBackName() { return topStreaksBackName; }
    public List<String> getTopStreaksBackLore() { return topStreaksBackLore; }
    public Integer getTopStreaksBackSlot() { return topStreaksBackSlot; }

    public String getTopTimeTitle() { return topTimeTitle; }
    public Integer getTopTimeSize() { return topTimeSize; }
    public String getTopTimePlayersName() { return topTimePlayersName; }
    public List<String> getTopTimePlayersLore() { return topTimePlayersLore; }
    public String getTopTimeNonDataName() { return topTimeNonDataName; }
    public List<String> getTopTimeNonDataLore() { return topTimeNonDataLore; }
    public List<Integer> getTopTimeSlots() { return topTimeSlots; }
    public String getTopTimeBackId() { return topTimeBackId; }
    public String getTopTimeBackName() { return topTimeBackName; }
    public List<String> getTopTimeBackLore() { return topTimeBackLore; }
    public Integer getTopTimeBackSlot() { return topTimeBackSlot; }

}
