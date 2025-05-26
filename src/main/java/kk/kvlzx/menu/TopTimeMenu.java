package kk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.items.CustomItem;

public class TopTimeMenu extends Menu {

    public TopTimeMenu(KvKnockback plugin) {
        super(plugin, "&8• &b&lTop Tiempo Jugado &8•", 27);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        List<UUID> topPlayers = new ArrayList<>(PlayerStats.getAllStats());
        topPlayers.sort((uuid1, uuid2) -> {
            PlayerStats stats1 = PlayerStats.getStats(uuid1);
            PlayerStats stats2 = PlayerStats.getStats(uuid2);
            return Long.compare(stats2.getPlayTime(), stats1.getPlayTime());
        });

        for (int i = 0; i < Math.min(10, topPlayers.size()); i++) {
            UUID uuid = topPlayers.get(i);
            PlayerStats stats = PlayerStats.getStats(uuid);
            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            
            List<String> lore = new ArrayList<>();
            lore.add("&7Posición: &f#" + (i + 1));
            lore.add("&7Tiempo: &b" + stats.getFormattedPlayTime());
            
            ItemStack skull = CustomItem.createSkullFromUUID(uuid, 
                "&b" + playerName,
                lore.toArray(new String[0]));
            
            inv.setItem(10 + i, skull);
        }

        ItemStack backButton = createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver al menú principal");
        inv.setItem(22, backButton);

        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15);
        fillEmptySlots(inv, filler);
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
        ItemMeta meta = item.getItemMeta();
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
