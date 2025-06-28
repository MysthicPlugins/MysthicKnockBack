package mk.kvlzx.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class StatsMenuConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;
    
    // Configuración del menú
    private String menuTitle;
    private int menuSize;
    
    // Configuración de items
    private String skullMaterial;
    private String skullName;
    private List<String> skullLore;
    private int skullSlot;
    
    private String killsMaterial;
    private String killsName;
    private List<String> killsLore;
    private int killsSlot;
    
    private String deathsMaterial;
    private String deathsName;
    private List<String> deathsLore;
    private int deathsSlot;
    
    private String eloMaterial;
    private String eloName;
    private List<String> eloLore;
    private int eloSlot;
    
    private String kgcoinsMaterial;
    private String kgcoinsName;
    private List<String> kgcoinsLore;
    private int kgcoinsSlot;
    
    private String playtimeMaterial;
    private String playtimeName;
    private List<String> playtimeLore;
    private int playtimeSlot;
    
    private String backMaterial;
    private String backName;
    private List<String> backLore;
    private int backSlot;

    public StatsMenuConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("stats.yml", "config/menus", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        // Configuración del menú
        menuTitle = config.getString("menu.title");
        menuSize = config.getInt("menu.size");
        
        // Configuración del skull
        skullMaterial = validateAndGetMaterial(config, "menu.items.skull.id", "PLAYER_SKULL");
        skullName = config.getString("menu.items.skull.name");
        skullLore = config.getStringList("menu.items.skull.lore");
        skullSlot = config.getInt("menu.items.skull.slot");
        
        // Configuración de kills
        killsMaterial = validateAndGetMaterial(config, "menu.items.kills.id", "DIAMOND_SWORD");
        killsName = config.getString("menu.items.kills.name");
        killsLore = config.getStringList("menu.items.kills.lore");
        killsSlot = config.getInt("menu.items.kills.slot");
        
        // Configuración de deaths
        deathsMaterial = validateAndGetMaterial(config, "menu.items.deaths.id", "PLAYER_SKULL");
        deathsName = config.getString("menu.items.deaths.name");
        deathsLore = config.getStringList("menu.items.deaths.lore");
        deathsSlot = config.getInt("menu.items.deaths.slot");
        
        // Configuración de ELO
        eloMaterial = validateAndGetMaterial(config, "menu.items.elo.id", "NETHER_STAR");
        eloName = config.getString("menu.items.elo.name");
        eloLore = config.getStringList("menu.items.elo.lore");
        eloSlot = config.getInt("menu.items.elo.slot");
        
        // Configuración de KGCoins
        kgcoinsMaterial = validateAndGetMaterial(config, "menu.items.kgcoins.id", "GOLD_INGOT");
        kgcoinsName = config.getString("menu.items.kgcoins.name");
        kgcoinsLore = config.getStringList("menu.items.kgcoins.lore");
        kgcoinsSlot = config.getInt("menu.items.kgcoins.slot");
        
        // Configuración de playtime
        playtimeMaterial = validateAndGetMaterial(config, "menu.items.playtime.id", "WATCH");
        playtimeName = config.getString("menu.items.playtime.name");
        playtimeLore = config.getStringList("menu.items.playtime.lore");
        playtimeSlot = config.getInt("menu.items.playtime.slot");
        
        // Configuración del botón back
        backMaterial = validateAndGetMaterial(config, "menu.items.back.id", "ARROW");
        backName = config.getString("menu.items.back.name");
        backLore = config.getStringList("menu.items.back.lore");
        backSlot = config.getInt("menu.items.back.slot");
    }

    private String validateAndGetMaterial(FileConfiguration config, String path, String defaultMaterial) {
        String materialName = config.getString(path);

        if (materialName == null || materialName.trim().isEmpty()) {
            plugin.getLogger().warning("Material at '" + path + "' is empty. Using default value: " + defaultMaterial);
            return defaultMaterial;
        }

        // Si es PLAYER_SKULL, lo mantenemos como está (se procesará especialmente)
        if (materialName.equalsIgnoreCase("PLAYER_SKULL")) {
            return materialName;
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
            "menu.items.skull.id",
            "menu.items.kills.id",
            "menu.items.deaths.id",
            "menu.items.elo.id",
            "menu.items.kgcoins.id",
            "menu.items.playtime.id",
            "menu.items.back.id"
        };
        
        for (String path : materialPaths) {
            String materialName = config.getString(path);
            if (materialName != null && !materialName.trim().isEmpty()) {
                // Skipear validacion para PLAYER_SKULL, es un material custom
                if (materialName.equalsIgnoreCase("PLAYER_SKULL")) {
                    continue;
                }
                
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
        
        if (materialId.equalsIgnoreCase("PLAYER_SKULL")) {
            // Crear la cabeza del jugador
            item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 3 = Player skull
            ItemMeta meta = item.getItemMeta();
            
            if (meta instanceof SkullMeta && player != null) {
                SkullMeta skullMeta = (SkullMeta) meta;
                skullMeta.setOwner(player.getName());
            }
            
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
        } else {
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

    public String getMenuTitle() { return menuTitle; }
    public int getMenuSize() { return menuSize; }
    public String getSkullMaterial() { return skullMaterial; }
    public String getSkullName() { return skullName; }
    public List<String> getSkullLore() { return skullLore; }
    public int getSkullSlot() { return skullSlot; }
    public String getKillsMaterial() { return killsMaterial; }
    public String getKillsName() { return killsName; }
    public List<String> getKillsLore() { return killsLore; }
    public int getKillsSlot() { return killsSlot; }
    public String getDeathsMaterial() { return deathsMaterial; }
    public String getDeathsName() { return deathsName; }
    public List<String> getDeathsLore() { return deathsLore; }
    public int getDeathsSlot() { return deathsSlot; }
    public String getEloMaterial() { return eloMaterial; }
    public String getEloName() { return eloName; }
    public List<String> getEloLore() { return eloLore; }
    public int getEloSlot() { return eloSlot; }
    public String getKgcoinsMaterial() { return kgcoinsMaterial; }
    public String getKgcoinsName() { return kgcoinsName; }
    public List<String> getKgcoinsLore() { return kgcoinsLore; }
    public int getKgcoinsSlot() { return kgcoinsSlot; }
    public String getPlaytimeMaterial() { return playtimeMaterial; }
    public String getPlaytimeName() { return playtimeName; }
    public List<String> getPlaytimeLore() { return playtimeLore; }
    public int getPlaytimeSlot() { return playtimeSlot; }
    public String getBackMaterial() { return backMaterial; }
    public String getBackName() { return backName; }
    public List<String> getBackLore() { return backLore; }
    public int getBackSlot() { return backSlot; }
}
