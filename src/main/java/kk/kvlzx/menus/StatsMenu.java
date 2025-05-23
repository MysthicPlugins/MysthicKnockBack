package kk.kvlzx.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class StatsMenu {
    
    public static void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, MessageUtils.getColor("&8• &b&lEstadísticas &8•"));
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        
        // Kills
        menu.setItem(10, createStatItem(Material.DIAMOND_SWORD, "&e⚔ Kills",
            Arrays.asList(
                "&7Total: &a" + stats.getKills(),
                "&7Máxima racha: &6" + stats.getMaxKillstreak(),
                "",
                "&8Click para ver el top"
            )));

        // Muertes
        menu.setItem(12, createStatItem(Material.SKULL_ITEM, "&e☠ Muertes",
            Arrays.asList(
                "&7Total: &c" + stats.getDeaths(),
                "&7KDR: &b" + String.format("%.2f", stats.getKDR()),
                "",
                "&8Click para ver el top"
            )));

        // ELO
        menu.setItem(14, createStatItem(Material.NETHER_STAR, "&e✦ ELO",
            Arrays.asList(
                "&7Actual: &6" + stats.getElo(),
                "",
                "&8Click para ver el top"
            )));

        // Tiempo Jugado
        menu.setItem(16, createStatItem(Material.WATCH, "&e⌚ Tiempo Jugado",
            Arrays.asList(
                "&7Total: &a" + stats.getFormattedPlayTime(),
                "",
                "&8Click para ver el top"
            )));

        player.openInventory(menu);
    }

    private static ItemStack createStatItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(MessageUtils.getColor(line));
        }
        meta.setLore(coloredLore);
        
        item.setItemMeta(meta);
        return item;
    }
}
