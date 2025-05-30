package mk.kvlzx.data;

import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class InventoryData {
    private final CustomConfig inventoryConfig;

    public InventoryData(MysthicKnockBack plugin) {
        this.inventoryConfig = new CustomConfig("inventories.yml", "data", plugin);
        this.inventoryConfig.registerConfig();
    }

    public void saveLayout(UUID uuid, ItemStack[] layout) {
        String basePath = "inventories." + uuid + ".items";
        
        // Limpiar la sección actual
        inventoryConfig.getConfig().set(basePath, null);
        
        // Guardar solo los slots que tienen items
        for (int i = 0; i < layout.length; i++) {
            if (layout[i] != null) {
                inventoryConfig.getConfig().set(basePath + "." + i, layout[i]);
            }
        }
        
        inventoryConfig.saveConfig();
    }

    public ItemStack[] loadLayout(UUID uuid) {
        ItemStack[] layout = new ItemStack[9];
        String path = "inventories." + uuid + ".items";
        
        ConfigurationSection section = inventoryConfig.getConfig().getConfigurationSection(path);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    if (slot >= 0 && slot < 9) {
                        layout[slot] = section.getItemStack(key);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        return layout;
    }

    public boolean hasLayout(UUID uuid) {
        return inventoryConfig.getConfig().contains("inventories." + uuid);
    }

    public void removeLayout(UUID uuid) {
        // Eliminar toda la sección del jugador
        inventoryConfig.getConfig().set("inventories." + uuid, null);
        inventoryConfig.saveConfig();
    }
}
