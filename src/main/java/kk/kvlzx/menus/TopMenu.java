package kk.kvlzx.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;
import java.util.List;

import kk.kvlzx.managers.MenuManager;
import kk.kvlzx.managers.TopManager;
import kk.kvlzx.utils.MessageUtils;

public class TopMenu {
    private static final int[] TOP_SLOTS = {10, 11, 12, 13, 14, 15, 16, // Primera fila (7 slots)
                                          21, 22, 23}; // Segunda fila centrada (3 slots)

    public static void openMenu(Player player, TopType type) {
        Inventory menu = MenuManager.createInventory(MenuType.TOP_MENU, type.getMenuTitle());
        List<Map.Entry<UUID, Integer>> top = TopManager.getTop(type, 10);

        // Decoración de bordes
        ItemStack border = createBorderItem(type);
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                menu.setItem(i, border);
            }
        }

        // Colocar los tops
        for (int i = 0; i < Math.min(top.size(), 10); i++) {
            Map.Entry<UUID, Integer> entry = top.get(i);
            menu.setItem(TOP_SLOTS[i], 
                TopManager.createTopSkull(i + 1, entry.getKey(), entry.getValue(), type));
        }

        // Agregar botón de retorno
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(MessageUtils.getColor("&c← Volver al menú principal"));
        backButton.setItemMeta(backMeta);
        menu.setItem(18, backButton);

        player.openInventory(menu);
    }

    private static ItemStack createBorderItem(TopType type) {
        ItemStack border = new ItemStack(Material.STAINED_GLASS_PANE, 1, getBorderColor(type));
        ItemMeta meta = border.getItemMeta();
        meta.setDisplayName(" ");
        border.setItemMeta(meta);
        return border;
    }

    private static byte getBorderColor(TopType type) {
        switch(type) {
            case KILLS: return 11; // Azul
            case KDR: return 1; // Naranja
            case STREAK: return 14; // Rojo
            case ELO: return 4; // Amarillo
            case PLAYTIME: return 5; // Verde
            default: return 15; // Negro
        }
    }
}
