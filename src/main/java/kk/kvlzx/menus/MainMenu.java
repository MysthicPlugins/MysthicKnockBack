package kk.kvlzx.menus;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import kk.kvlzx.managers.MenuManager;
import kk.kvlzx.utils.MessageUtils;

public class MainMenu {
    public static void openMenu(Player player) {
        Inventory menu = MenuManager.createInventory(MenuType.MAIN_MENU);

        // Decoración de bordes
        ItemStack border = createMenuItem(Material.STAINED_GLASS_PANE, "&7", "");
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i > 35 || i % 9 == 0 || i % 9 == 8) {
                menu.setItem(i, border);
            }
        }

        // Top Kills (Espada de Diamante)
        menu.setItem(11, createMenuItem(Material.DIAMOND_SWORD, "&b⚔ Top Kills", "&7Click para ver el ranking de kills"));

        // Top KDR (Manzana Dorada)
        menu.setItem(13, createMenuItem(Material.GOLDEN_APPLE, "&6☠ Top KDR", "&7Click para ver el ranking de KDR"));

        // Top Rachas (Blaze Powder)
        menu.setItem(15, createMenuItem(Material.BLAZE_POWDER, "&c⚡ Top Rachas", "&7Click para ver el ranking de rachas"));

        // Top ELO (Estrella del Nether)
        menu.setItem(21, createMenuItem(Material.NETHER_STAR, "&e✦ Top ELO", "&7Click para ver el ranking de ELO"));

        // Top Horas Jugadas (Reloj)
        menu.setItem(23, createMenuItem(Material.WATCH, "&a⌚ Top Horas Jugadas", "&7Click para ver el ranking de tiempo jugado"));

        // Tus Stats (Cabeza del jugador)
        ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName(MessageUtils.getColor("&a✪ Tus Estadísticas"));
        meta.setLore(Arrays.asList(MessageUtils.getColor("&7Click para ver tus stats")));
        playerHead.setItemMeta(meta);
        menu.setItem(31, playerHead);

        // Editor de Inventario
        menu.setItem(40, createMenuItem(Material.REDSTONE, "&c⚙ Editor de Inventario", "&7Click para editar el inventario"));

        player.openInventory(menu);
    }

    private static ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        meta.setLore(Arrays.asList(lore).stream()
                    .map(MessageUtils::getColor)
                    .collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }
}
