package kk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import kk.kvlzx.menus.MenuType;
import kk.kvlzx.utils.MessageUtils;

public class MenuManager {
    private static final Map<UUID, MenuType> playerMenus = new HashMap<>();
    
    public static Inventory createInventory(MenuType type) {
        return Bukkit.createInventory(null, type.getSize(), MessageUtils.getColor(type.getTitle()));
    }

    public static void openMenu(Player player, MenuType type, Inventory menu) {
        // Registrar al jugador en el mapa de menús
        playerMenus.put(player.getUniqueId(), type);
        
        // Abrir el menú
        player.openInventory(menu);
    }

    public static MenuType getPlayerMenuType(Player player) {
        return playerMenus.get(player.getUniqueId());
    }

    public static void removePlayer(Player player) {
        playerMenus.remove(player.getUniqueId());
    }

    public static boolean isInMenu(Player player) {
        return playerMenus.containsKey(player.getUniqueId());
    }

    public static boolean isInMenuType(Player player, MenuType type) {
        MenuType currentType = playerMenus.get(player.getUniqueId());
        return currentType == type;
    }

    public static void debugPlayerMenu(Player player) {
        MenuType currentMenu = playerMenus.get(player.getUniqueId());
        if (currentMenu != null) {
            player.sendMessage("Estás en el menú: " + currentMenu.name());
        } else {
            player.sendMessage("DEBUG: No estás en ningún menú");
        }
    }
}
