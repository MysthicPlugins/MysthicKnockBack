package kk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

public class StatsMenu extends Menu {

    public StatsMenu(KvKnockback plugin) {
        super(plugin, "&8• &b&lMis Estadísticas &8•", 27);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Cabeza del jugador en el centro
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(player.getName());
        skullMeta.setDisplayName(MessageUtils.getColor("&b" + player.getName()));
        List<String> skullLore = new ArrayList<>();
        skullLore.add(MessageUtils.getColor("&7Estas son tus estadísticas generales"));
        skullLore.add(MessageUtils.getColor("&7¡Sigue mejorando!"));
        skullMeta.setLore(skullLore);
        skull.setItemMeta(skullMeta);
        inv.setItem(13, skull);

        // Kills
        inv.setItem(10, createItem(Material.DIAMOND_SWORD, "&a&lKills",
            "&7Kills totales: &a" + stats.getKills(),
            "&7Racha actual: &a" + stats.getCurrentStreak(),
            "&7Mejor racha: &a" + stats.getMaxStreak()));

        // Muertes
        inv.setItem(12, createItem(Material.SKULL_ITEM, "&c&lMuertes",
            "&7Muertes totales: &c" + stats.getDeaths(),
            "&7KDR: &b" + String.format("%.2f", stats.getKDR())));

        // ELO
        inv.setItem(14, createItem(Material.NETHER_STAR, "&6&lELO",
            "&7ELO actual: &6" + stats.getElo(),
            "&7Rango: " + getRankLine(stats.getElo())));

        // Tiempo jugado
        inv.setItem(16, createItem(Material.WATCH, "&e&lTiempo Jugado",
            "&7Tiempo total: &e" + stats.getFormattedPlayTime()));

        // Botón para volver
        inv.setItem(22, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver al menú principal"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15);
        fillEmptySlots(inv, filler);
    }

    private String getRankLine(int elo) {
        String rankPrefix = kk.kvlzx.managers.RankManager.getRankPrefix(elo);
        return rankPrefix + " &7(" + elo + " ELO)";
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getSlot() == 22) {
            Player player = (Player) event.getWhoClicked();
            plugin.getMenuManager().openMenu(player, "main");
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, name, (byte) 0, lore);
    }

    private ItemStack createItem(Material material, String name, byte data, String... lore) {
        ItemStack item = new ItemStack(material, 1, data);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        
        if (lore.length > 0) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(MessageUtils.getColor(line));
            }
            meta.setLore(coloredLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
}
