package kk.kvlzx.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.items.CustomItem;
import kk.kvlzx.items.CustomItem.ItemType;
import kk.kvlzx.items.ItemsManager;
import kk.kvlzx.managers.MenuManager;
import kk.kvlzx.utils.MessageUtils;

public class InventoryEditorMenu {
    public static void openMenu(Player player) {
        Inventory menu = MenuManager.createInventory(MenuType.INVENTORY_EDITOR);

        // Decoración
        ItemStack border = createBorderItem();
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                menu.setItem(i, border);
            }
        }

        // Items disponibles
        for (ItemType type : ItemType.values()) {
            menu.addItem(CustomItem.create(type));
        }

        // Sección de vista previa del inventario
        ItemStack separator = createMenuItem(Material.STAINED_GLASS_PANE, "&e&lInventario PvP", "&7Arrastra los items para ordenarlos", "&7Click para guardar");
        for (int i = 27; i < 36; i++) {
            menu.setItem(i, separator);
        }

        // Cargar inventario actual
        ItemStack[] currentLayout = ItemsManager.getPvPLayout();
        for (int i = 0; i < 9; i++) {
            if (currentLayout[i] != null) {
                menu.setItem(i + 36, currentLayout[i]);
            }
        }

        // Botón de guardar
        ItemStack saveButton = createMenuItem(Material.EMERALD_BLOCK, "&a&lGuardar Cambios", 
            "&7Click para guardar la configuración",
            "&7del inventario PvP");
        menu.setItem(49, saveButton);

        // Botón de volver
        ItemStack backButton = createMenuItem(Material.ARROW, "&c← Volver al menú principal");
        menu.setItem(45, backButton);

        player.openInventory(menu);
    }

    private static ItemStack createBorderItem() {
        ItemStack border = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)7);
        ItemMeta meta = border.getItemMeta();
        meta.setDisplayName(" ");
        border.setItemMeta(meta);
        return border;
    }

    private static ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        if (lore.length > 0) {
            meta.setLore(java.util.Arrays.asList(lore).stream()
                .map(MessageUtils::getColor)
                .collect(java.util.stream.Collectors.toList()));
        }
        item.setItemMeta(meta);
        return item;
    }
}
