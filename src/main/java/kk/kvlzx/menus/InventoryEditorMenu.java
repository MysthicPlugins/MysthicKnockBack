package kk.kvlzx.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.items.ItemsManager;
import kk.kvlzx.managers.MenuManager;

public class InventoryEditorMenu {
    public static void openMenu(Player player) {
        Inventory menu = MenuManager.createInventory(MenuType.INVENTORY_EDITOR);
        ItemStack[] currentLayout = ItemsManager.getPvPLayout();

        // Colocar el layout actual
        for (int i = 0; i < 9; i++) {
            if (currentLayout[i] != null) {
                menu.setItem(i, currentLayout[i].clone());
            }
        }

        MenuManager.openMenu(player, MenuType.INVENTORY_EDITOR, menu);
    }
}
