package kk.kvlzx.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.util.Map;
import java.util.UUID;
import java.util.List;

import kk.kvlzx.managers.MenuManager;
import kk.kvlzx.managers.TopManager;

public class TopMenu {
    private static final int[] TOP_SLOTS = {13, 21, 23, 29, 31, 33, 37, 39, 41, 43};

    public static void openMenu(Player player, TopType type) {
        Inventory menu = MenuManager.createInventory(MenuType.TOP_MENU);
        List<Map.Entry<UUID, Integer>> top = TopManager.getTop(type, 10);

        for (int i = 0; i < top.size(); i++) {
            Map.Entry<UUID, Integer> entry = top.get(i);
            menu.setItem(TOP_SLOTS[i], 
                TopManager.createTopSkull(i + 1, entry.getKey(), entry.getValue(), type));
        }

        player.openInventory(menu);
    }
}
