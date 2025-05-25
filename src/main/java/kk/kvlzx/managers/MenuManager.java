package kk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import kk.kvlzx.menus.MenuType;
import kk.kvlzx.utils.MessageUtils;

public class MenuManager {
    
    public static boolean isPluginInventory(Inventory inventory) {
        if (inventory == null) return false;
        String title = inventory.getTitle();
        
        for (MenuType type : MenuType.values()) {
            if (MessageUtils.getColor(type.getTitle()).equals(title)) {
                return true;
            }
        }
        return false;
    }

    public static MenuType getMenuType(Inventory inventory) {
        if (inventory == null) return null;
        String title = inventory.getTitle();
        
        // Verificar primero tops ya que tienen títulos dinámicos
        if (title.contains("Top")) {
            return MenuType.TOP_MENU;
        }
        
        // Para los demás menús, verificar título exacto
        for (MenuType type : MenuType.values()) {
            if (MessageUtils.getColor(type.getTitle()).equals(title)) {
                return type;
            }
        }
        return null;
    }

    public static Inventory createInventory(MenuType type, String title) {
        return Bukkit.createInventory(null, type.getSize(), MessageUtils.getColor(title));
    }

    public static Inventory createInventory(MenuType type) {
        return createInventory(type, type.getTitle());
    }
}
