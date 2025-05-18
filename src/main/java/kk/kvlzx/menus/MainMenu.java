package kk.kvlzx.menus;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import kk.kvlzx.utils.MessageUtils;

public class MainMenu {
    public static void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, MessageUtils.getColor("&8• &a&lMenú Principal &8•"));

        // Top Kills (Espada de Diamante)
        menu.setItem(10, createMenuItem(Material.DIAMOND_SWORD, "&b⚔ Top Kills", "&7Click para ver el ranking"));

        // Top KDR (Manzana Dorada)
        menu.setItem(12, createMenuItem(Material.GOLDEN_APPLE, "&6☠ Top KDR", "&7Click para ver el ranking"));

        // Top Rachas (Blaze Powder)
        menu.setItem(14, createMenuItem(Material.BLAZE_POWDER, "&c⚡ Top Rachas", "&7Click para ver el ranking"));

        // Top ELO (Estrella del Nether)
        menu.setItem(16, createMenuItem(Material.NETHER_STAR, "&e✦ Top ELO", "&7Click para ver el ranking"));

        // Tus Stats (Cabeza del jugador)
        ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName(MessageUtils.getColor("&a✪ Tus Estadísticas"));
        meta.setLore(Arrays.asList(MessageUtils.getColor("&7Click para ver tus stats")));
        playerHead.setItemMeta(meta);
        menu.setItem(13, playerHead);

        // Añadir botón de personalización de inventario
        menu.setItem(22, createMenuItem(Material.CHEST, "&6⚒ Personalizar Inventario", "&7Click para personalizar tu inventario de PvP"));

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
