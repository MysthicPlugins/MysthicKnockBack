package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class MusicCategoriesMenu extends Menu {

    public MusicCategoriesMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &d&lBackground Music &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Common music
        inv.setItem(11, createItem(Material.GOLD_RECORD, "&7Common Music",
            "&8▪ &7Price: &f15,000 KGCoins",
            "&8▪ &7Rarity: &7COMMON",
            "",
            "&8➥ Soothing melodies",
            "&7Click to view songs"));

        // Epic music
        inv.setItem(13, createItem(Material.RECORD_3, "&5Epic Music",
            "&8▪ &7Price: &f35,000 KGCoins",
            "&8▪ &7Rarity: &5EPIC",
            "",
            "&8➥ More intense melodies",
            "&7Click to view songs"));

        // Legendary music
        inv.setItem(15, createItem(Material.RECORD_12, "&6Legendary Music",
            "&8▪ &7Price: &f75,000 KGCoins",
            "&8▪ &7Rarity: &6LEGENDARY",
            "",
            "&8➥ The best melodies",
            "&7Click to view songs"));

        // Back button
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Filler
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch(event.getSlot()) {
            case 11: // Common
                MusicShopMenu.setCurrentCategory("COMMON");
                plugin.getMenuManager().openMenu(player, "music_shop");
                break;
            case 13: // Epic
                MusicShopMenu.setCurrentCategory("EPIC");
                plugin.getMenuManager().openMenu(player, "music_shop");
                break;
            case 15: // Legendary
                MusicShopMenu.setCurrentCategory("LEGENDARY");
                plugin.getMenuManager().openMenu(player, "music_shop");
                break;
            case 40: // Back
                plugin.getMenuManager().openMenu(player, "shop");
                break;
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
