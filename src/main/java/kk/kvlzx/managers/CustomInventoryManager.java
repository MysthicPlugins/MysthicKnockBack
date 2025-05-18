package kk.kvlzx.managers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.items.CustomItem;
import kk.kvlzx.items.CustomItem.ItemType;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.utils.config.CustomConfig;

public class CustomInventoryManager {
    private final KvKnockback plugin;
    private final Map<UUID, ItemStack[]> customInventories = new HashMap<>();
    private static final String EDITOR_TITLE = "&8• &6⚒ Editor de Hotbar &8•";
    private CustomConfig inventoryConfig;

    private static final ItemStack[] DEFAULT_ITEMS = new ItemStack[] {
        CustomItem.create(ItemType.KNOCKER),
        CustomItem.create(ItemType.BLOCKS),
        CustomItem.create(ItemType.BOW),
        CustomItem.create(ItemType.PEARL),
        CustomItem.create(ItemType.FEATHER),
        CustomItem.create(ItemType.PLATE)
    };

    public CustomInventoryManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.inventoryConfig = new CustomConfig("inventories.yml", "data", plugin);
        this.inventoryConfig.registerConfig();
        loadInventories();
    }

    private void loadInventories() {
        if (inventoryConfig.getConfig().contains("inventories")) {
            for (String uuidStr : inventoryConfig.getConfig().getConfigurationSection("inventories").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    ItemStack[] contents = new ItemStack[9];
                    
                    for (int i = 0; i < 9; i++) {
                        String path = "inventories." + uuidStr + ".slot." + i;
                        if (inventoryConfig.getConfig().contains(path)) {
                            contents[i] = inventoryConfig.getConfig().getItemStack(path);
                        }
                    }
                    
                    customInventories.put(uuid, contents);
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading inventory for UUID " + uuidStr);
                }
            }
        }
    }

    public void openEditor(Player player) {
        Inventory editor = Bukkit.createInventory(null, 27, MessageUtils.getColor(EDITOR_TITLE));
        
        // Primera fila: Items disponibles
        for (int i = 0; i < DEFAULT_ITEMS.length; i++) {
            editor.setItem(i, DEFAULT_ITEMS[i].clone());
        }
        
        // Segunda fila: Separador con instrucciones
        ItemStack glass = createItem(Material.STAINED_GLASS_PANE, "&7▼ Arrastra los items abajo ▼", "&7Click para clonar items");
        for (int i = 9; i < 18; i++) {
            editor.setItem(i, glass);
        }
        
        // Tercera fila: Layout actual del jugador
        ItemStack[] currentLayout = customInventories.getOrDefault(player.getUniqueId(), new ItemStack[9]);
        for (int i = 0; i < 9; i++) {
            if (currentLayout[i] != null) {
                editor.setItem(i + 18, currentLayout[i].clone());
            }
        }
        
        player.openInventory(editor);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1, (byte)7); // Usando el color gris para el cristal
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        if (lore != null && lore.length > 0) {
            meta.setLore(Arrays.stream(lore).map(MessageUtils::getColor).collect(Collectors.toList()));
        }
        item.setItemMeta(meta);
        return item;
    }

    public void saveCustomInventory(UUID playerId, ItemStack[] contents) {
        ItemStack[] layout = new ItemStack[9];
        System.arraycopy(contents, 27, layout, 0, 9);
        customInventories.put(playerId, layout);
    }

    public void saveAllInventories() {
        inventoryConfig.getConfig().set("inventories", null);
        
        for (Map.Entry<UUID, ItemStack[]> entry : customInventories.entrySet()) {
            UUID playerId = entry.getKey();
            ItemStack[] contents = entry.getValue();
            
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    String path = "inventories." + playerId + ".slot." + i;
                    inventoryConfig.getConfig().set(path, contents[i]);
                }
            }
        }

        inventoryConfig.saveConfig();
    }

    public boolean isEditorInventory(Inventory inv) {
        return inv != null && inv.getTitle().equals(MessageUtils.getColor(EDITOR_TITLE));
    }
}
