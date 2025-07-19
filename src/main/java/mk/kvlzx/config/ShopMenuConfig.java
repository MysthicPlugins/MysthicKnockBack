package mk.kvlzx.config;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.config.CustomConfig;

public class ShopMenuConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    private String menuTitle;
    private int menuSize;

    // Balance
    private String balanceId;
    private String balanceName;
    private List<String> balanceLore;
    private int balanceSlot;

    // Blocks
    private String blocksId;
    private String blocksName;
    private List<String> blocksLore;
    private int blocksSlot;

    // Knockers
    private String knockersId;
    private String knockersName;
    private List<String> knockersLore;
    private int knockersSlot;

    // Kill Messages
    private String killMessagesId;
    private String killMessagesName;
    private List<String> killMessagesLore;
    private int killMessagesSlot;

    // Death Messages
    private String deathMessagesId;
    private String deathMessagesName;
    private List<String> deathMessagesLore;
    private int deathMessagesSlot;

    // Arrow Effects
    private String arrowEffectsId;
    private String arrowEffectsName;
    private List<String> arrowEffectsLore;
    private int arrowEffectsSlot;

    // Death Sounds
    private String deathSoundsId;
    private String deathSoundsName;
    private List<String> deathSoundsLore;
    private int deathSoundsSlot;

    // Kill Sounds
    private String killSoundsId;
    private String killSoundsName;
    private List<String> killSoundsLore;
    private int killSoundsSlot;

    // Join Messages
    private String joinMessagesId;
    private String joinMessagesName;
    private List<String> joinMessagesLore;
    private int joinMessagesSlot;

    // Music
    private String musicId;
    private String musicName;
    private List<String> musicLore;
    private int musicSlot;

    // Back Button
    private String backId;
    private String backName;
    private List<String> backLore;
    private int backSlot;

    public ShopMenuConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.configFile = new CustomConfig("main-shop.yml", "config/menus", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();
        
        menuTitle = config.getString("menu.title");
        menuSize = config.getInt("menu.size");

        // Balance
        balanceId = validateAndGetMaterial(config, "menu.items.balance.id", "EMERALD");
        balanceName = config.getString("menu.items.balance.name");
        balanceLore = config.getStringList("menu.items.balance.lore");
        balanceSlot = config.getInt("menu.items.balance.slot");

        // Blocks
        blocksId = validateAndGetMaterial(config, "menu.items.blocks.id", "SANDSTONE");
        blocksName = config.getString("menu.items.blocks.name");
        blocksLore = config.getStringList("menu.items.blocks.lore");
        blocksSlot = config.getInt("menu.items.blocks.slot");

        // Knockers
        knockersId = validateAndGetMaterial(config, "menu.items.knockers.id", "STICK");
        knockersName = config.getString("menu.items.knockers.name");
        knockersLore = config.getStringList("menu.items.knockers.lore");
        knockersSlot = config.getInt("menu.items.knockers.slot");

        // Kill Messages
        killMessagesId = validateAndGetMaterial(config, "menu.items.kill-messages.id", "PAPER");
        killMessagesName = config.getString("menu.items.kill-messages.name");
        killMessagesLore = config.getStringList("menu.items.kill-messages.lore");
        killMessagesSlot = config.getInt("menu.items.kill-messages.slot");

        // Death Messages
        deathMessagesId = validateAndGetMaterial(config, "menu.items.death-messages.id", "BOOK_AND_QUILL");
        deathMessagesName = config.getString("menu.items.death-messages.name");
        deathMessagesLore = config.getStringList("menu.items.death-messages.lore");
        deathMessagesSlot = config.getInt("menu.items.death-messages.slot");

        // Arrow Effects
        arrowEffectsId = validateAndGetMaterial(config, "menu.items.arrow-effects.id", "ARROW");
        arrowEffectsName = config.getString("menu.items.arrow-effects.name");
        arrowEffectsLore = config.getStringList("menu.items.arrow-effects.lore");
        arrowEffectsSlot = config.getInt("menu.items.arrow-effects.slot");

        // Death Sounds
        deathSoundsId = validateAndGetMaterial(config, "menu.items.death-sounds.id", "NOTE_BLOCK");
        deathSoundsName = config.getString("menu.items.death-sounds.name");
        deathSoundsLore = config.getStringList("menu.items.death-sounds.lore");
        deathSoundsSlot = config.getInt("menu.items.death-sounds.slot");

        // Kill Sounds
        killSoundsId = validateAndGetMaterial(config, "menu.items.kill-sounds.id", "DIAMOND_SWORD");
        killSoundsName = config.getString("menu.items.kill-sounds.name");
        killSoundsLore = config.getStringList("menu.items.kill-sounds.lore");
        killSoundsSlot = config.getInt("menu.items.kill-sounds.slot");

        // Join Messages
        joinMessagesId = validateAndGetMaterial(config, "menu.items.join-messages.id", "ANVIL");
        joinMessagesName = config.getString("menu.items.join-messages.name");
        joinMessagesLore = config.getStringList("menu.items.join-messages.lore");
        joinMessagesSlot = config.getInt("menu.items.join-messages.slot");

        // Music
        musicId = validateAndGetMaterial(config, "menu.items.music.id", "GOLD_RECORD");
        musicName = config.getString("menu.items.music.name");
        musicLore = config.getStringList("menu.items.music.lore");
        musicSlot = config.getInt("menu.items.music.slot");

        // Back Button
        backId = validateAndGetMaterial(config, "menu.items.back.id", "ARROW");
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
            "menu.items.balance.id",
            "menu.items.blocks.id",
            "menu.items.knockers.id",
            "menu.items.kill-messages.id",
            "menu.items.death-messages.id",
            "menu.items.arrow-effects.id",
            "menu.items.death-sounds.id",
            "menu.items.kill-sounds.id",
            "menu.items.join-messages.id",
            "menu.items.music.id",
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

    public ItemStack createMenuItem(String itemName) {
        switch(itemName) {
            case "blocks":
                return createCustomItem(blocksId, blocksName, blocksLore);
            case "knockers":
                return createCustomItem(knockersId, knockersName, knockersLore);
            case "kill-messages":
                return createCustomItem(killMessagesId, killMessagesName, killMessagesLore);
            case "death-messages":
                return createCustomItem(deathMessagesId, deathMessagesName, deathMessagesLore);
            case "arrow-effects":
                return createCustomItem(arrowEffectsId, arrowEffectsName, arrowEffectsLore);
            case "death-sounds":
                return createCustomItem(deathSoundsId, deathSoundsName, deathSoundsLore);
            case "kill-sounds":
                return createCustomItem(killSoundsId, killSoundsName, killSoundsLore);
            case "join-messages":
                return createCustomItem(joinMessagesId, joinMessagesName, joinMessagesLore);
            case "music":
                return createCustomItem(musicId, musicName, musicLore);
            case "back":
                return createCustomItem(backId, backName, backLore);
            case "balance":
                return createCustomItem(balanceId, balanceName, balanceLore);
            default:
                return null;
        }
    }

    private ItemStack createCustomItem(String materialId, String name, List<String> lore) {
        try {
            Material material = Material.valueOf(materialId.toUpperCase());
            ItemStack item = new ItemStack(material);
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
            return item;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Failed to create item with material: " + materialId + ". Using STONE as fallback.");
            ItemStack item = new ItemStack(Material.STONE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§cInvalid Material");
            item.setItemMeta(meta);
            return item;
        }
    }

    public void reload() {
        configFile.reloadConfig();
        
        // Validar materiales después de recargar la configuración
        if (!validateAllMaterials()) {
            plugin.getLogger().warning("Invalid materials detected in menu configuration. The menu will be loaded with default values for problematic materials.");
        }
        
        loadConfig();
    }

    // Getters
    public String getMenuTitle() { return menuTitle; }
    public int getMenuSize() { return menuSize; }

    // Balance
    public String getBalanceId() { return balanceId; }
    public String getBalanceName() { return balanceName; }
    public List<String> getBalanceLore() { return balanceLore; }
    public int getBalanceSlot() { return balanceSlot; }

    // Blocks
    public String getBlocksId() { return blocksId; }
    public String getBlocksName() { return blocksName; }
    public List<String> getBlocksLore() { return blocksLore; }
    public int getBlocksSlot() { return blocksSlot; }

    // Knockers
    public String getKnockersId() { return knockersId; }
    public String getKnockersName() { return knockersName; }
    public List<String> getKnockersLore() { return knockersLore; }
    public int getKnockersSlot() { return knockersSlot; }

    // Kill Messages
    public String getKillMessagesId() { return killMessagesId; }
    public String getKillMessagesName() { return killMessagesName; }
    public List<String> getKillMessagesLore() { return killMessagesLore; }
    public int getKillMessagesSlot() { return killMessagesSlot; }

    // Death Messages
    public String getDeathMessagesId() { return deathMessagesId; }
    public String getDeathMessagesName() { return deathMessagesName; }
    public List<String> getDeathMessagesLore() { return deathMessagesLore; }
    public int getDeathMessagesSlot() { return deathMessagesSlot; }

    // Arrow Effects
    public String getArrowEffectsId() { return arrowEffectsId; }
    public String getArrowEffectsName() { return arrowEffectsName; }
    public List<String> getArrowEffectsLore() { return arrowEffectsLore; }
    public int getArrowEffectsSlot() { return arrowEffectsSlot; }

    // Death Sounds
    public String getDeathSoundsId() { return deathSoundsId; }
    public String getDeathSoundsName() { return deathSoundsName; }
    public List<String> getDeathSoundsLore() { return deathSoundsLore; }
    public int getDeathSoundsSlot() { return deathSoundsSlot; }

    // Kill Sounds
    public String getKillSoundsId() { return killSoundsId; }
    public String getKillSoundsName() { return killSoundsName; }
    public List<String> getKillSoundsLore() { return killSoundsLore; }
    public int getKillSoundsSlot() { return killSoundsSlot; }

    // Join Messages
    public String getJoinMessagesId() { return joinMessagesId; }
    public String getJoinMessagesName() { return joinMessagesName; }
    public List<String> getJoinMessagesLore() { return joinMessagesLore; }
    public int getJoinMessagesSlot() { return joinMessagesSlot; }

    // Music
    public String getMusicId() { return musicId; }
    public String getMusicName() { return musicName; }
    public List<String> getMusicLore() { return musicLore; }
    public int getMusicSlot() { return musicSlot; }

    // Back Button
    public String getBackId() { return backId; }
    public String getBackName() { return backName; }
    public List<String> getBackLore() { return backLore; }
    public int getBackSlot() { return backSlot; }
}
