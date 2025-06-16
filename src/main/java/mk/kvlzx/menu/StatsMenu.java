package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.managers.RankManager;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class StatsMenu extends Menu {

    public StatsMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &b&lMy Statistics &8•", 36);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Crear los items de relleno
        ItemStack darkGlass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 11); // Azul oscuro
        ItemStack lightGlass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 3);  // Celeste

        // Cabeza del jugador (centrada)
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(player.getName());
        skullMeta.setDisplayName(MessageUtils.getColor("&b" + player.getName()));
        List<String> skullLore = new ArrayList<>();
        skullLore.add(MessageUtils.getColor("&7These are your statistics"));
        skullLore.add(MessageUtils.getColor("&7Keep improving!"));
        skullMeta.setLore(skullLore);
        skull.setItemMeta(skullMeta);
        inv.setItem(13, skull);

        // Stats alrededor de la cabeza
        inv.setItem(10, createItem(Material.DIAMOND_SWORD, "&a&lKills",
            "&8▪ &7Total kills: &a" + stats.getKills(),
            "&8▪ &7Current streak: &a" + stats.getCurrentStreak(),
            "&8▪ &7Best streak: &a" + stats.getMaxStreak()));

        inv.setItem(12, createItem(Material.SKULL_ITEM, "&c&lDeaths", (byte) 0,
            "&8▪ &7Total deaths: &c" + stats.getDeaths(),
            "&8▪ &7KDR: &b" + String.format("%.2f", stats.getKDR())));

        inv.setItem(14, createItem(Material.NETHER_STAR, "&6&lELO",
            "&8▪ &7Current ELO: &6" + stats.getElo(),
            "&8▪ &7Rank: " + getRankLine(stats.getElo())));

        inv.setItem(16, createItem(Material.GOLD_INGOT, "&e&lKGCoins",
            "&8▪ &7Current balance: &e" + stats.getKGCoins(),
            "&8▪ &7Server currency"));

        inv.setItem(22, createItem(Material.WATCH, "&e&lPlay Time",
            "&8▪ &7Total time: &e" + stats.getFormattedPlayTime()));

        // Botón de volver al menú principal
        inv.setItem(31, createItem(Material.ARROW, "&c← Back", 
            "&8▪ &7Click to return to main menu"));

        // Patrón de relleno alternado
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                // Alternar entre vidrio oscuro y claro basado en posición
                boolean isDark = ((i / 9) + (i % 9)) % 2 == 0;
                inv.setItem(i, isDark ? darkGlass : lightGlass);
            }
        }
    }

    private String getRankLine(int elo) {
        String rankPrefix = RankManager.getRankPrefix(elo);
        return rankPrefix + " &7(" + elo + " ELO)";
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getSlot() == 31) {
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
