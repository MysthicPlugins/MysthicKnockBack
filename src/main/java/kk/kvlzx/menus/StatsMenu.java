package kk.kvlzx.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import kk.kvlzx.managers.MenuManager;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

import java.util.Arrays;

public class StatsMenu {
    public static void openMenu(Player player) {
        Inventory menu = MenuManager.createInventory(MenuType.STATS);
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Decoración de bordes
        ItemStack border = createMenuItem(Material.STAINED_GLASS_PANE, "&7", "");
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i % 9 == 0 || i % 9 == 8) {
                menu.setItem(i, border);
            }
        }

        // Cabeza del jugador en el centro
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwner(player.getName());
        headMeta.setDisplayName(MessageUtils.getColor("&b" + player.getName()));
        head.setItemMeta(headMeta);
        menu.setItem(13, head);

        // Estadísticas alrededor
        menu.setItem(10, createStatItem(Material.DIAMOND_SWORD, "&b⚔ Kills", stats.getKills()));
        menu.setItem(11, createStatItem(Material.SKULL_ITEM, "&c☠ Muertes", stats.getDeaths()));
        menu.setItem(12, createStatItem(Material.GOLDEN_APPLE, "&6⚖ KDR", String.format("%.2f", stats.getKDR())));
        menu.setItem(14, createStatItem(Material.NETHER_STAR, "&e✦ ELO", stats.getElo()));
        menu.setItem(15, createStatItem(Material.BLAZE_POWDER, "&c⚡ Racha Máxima", stats.getMaxStreak()));
        menu.setItem(16, createStatItem(Material.WATCH, "&a⌚ Tiempo Jugado", stats.getFormattedPlayTime()));

        // Botón de retorno
        ItemStack backButton = createMenuItem(Material.ARROW, "&c← Volver al menú principal");
        menu.setItem(18, backButton);

        MenuManager.openMenu(player, MenuType.STATS);
    }

    private static ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore).stream()
                    .map(MessageUtils::getColor)
                    .collect(java.util.stream.Collectors.toList()));
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createStatItem(Material material, String name, Object value) {
        String loreText;
        
        if (material == Material.DIAMOND_SWORD) {
            loreText = "&fKills totales: &a" + value;
        } else if (material == Material.SKULL_ITEM) {
            loreText = "&fMuertes totales: &c" + value;
        } else if (material == Material.GOLDEN_APPLE) {
            loreText = "&fRelación K/D: &6" + value;
        } else if (material == Material.NETHER_STAR) {
            loreText = "&fPuntos de ELO: &e" + value;
        } else if (material == Material.BLAZE_POWDER) {
            loreText = "&fMejor racha: &c" + value + " &7kills";
        } else if (material == Material.WATCH) {
            loreText = "&fTiempo jugado: &a" + value;
        } else {
            loreText = "&7Valor: &f" + value;
        }

        return createMenuItem(material, name, loreText);
    }
}
