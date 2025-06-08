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

public class KnockerCategoriesMenu extends Menu {

    public KnockerCategoriesMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lKnocker Categories &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Comunes
        inv.setItem(11, createItem(Material.BONE, "&7Common Knockers",
            "&8▪ &7Price: &f5,000 KGCoins",
            "&8▪ &7Rarity: &7COMMON",
            "",
            "&8➥ Basic but effective weapons",
            "&7Click to view knockers"));

        // Poco comunes
        inv.setItem(13, createItem(Material.BOOK, "&aUncommon",
            "&8▪ &7Price: &f15,000 KGCoins",
            "&8▪ &7Rarity: &aUNCOMMON",
            "",
            "&8➥ Weapons with special effects",
            "&7Click to view knockers"));

        // Raros
        inv.setItem(15, createItem(Material.BLAZE_ROD, "&9Rare Knockers",
            "&8▪ &7Price: &f50,000 KGCoins",
            "&8▪ &7Rarity: &9RARE",
            "",
            "&8➥ High-powered weapons",
            "&7Click to view knockers"));

        // Épicos
        inv.setItem(21, createItem(Material.DIAMOND, "&5Epic Knockers",
            "&8▪ &7Price: &f500,000 KGCoins",
            "&8▪ &7Rarity: &5EPIC",
            "",
            "&8➥ Legendary weapons",
            "&7Click to view knockers"));

        // Legendarios
        inv.setItem(23, createItem(Material.GOLDEN_APPLE, "&6Legendary Knockers",
            "&8▪ &7Price: &f1,000,000 KGCoins",
            "&8▪ &7Rarity: &6LEGENDARY",
            "",
            "&8➥ The most powerful weapons",
            "&7Click to view knockers"));

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7);
        fillEmptySlots(inv, filler);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch(event.getSlot()) {
            case 11: // Comunes
                KnockerShopMenu.setCurrentCategory("COMMON");
                plugin.getMenuManager().openMenu(player, "knocker_shop");
                break;
            case 13: // Poco comunes
                KnockerShopMenu.setCurrentCategory("UNCOMMON");
                plugin.getMenuManager().openMenu(player, "knocker_shop");
                break;
            case 15: // Raros
                KnockerShopMenu.setCurrentCategory("RARE");
                plugin.getMenuManager().openMenu(player, "knocker_shop");
                break;
            case 21: // Épicos
                KnockerShopMenu.setCurrentCategory("EPIC");
                plugin.getMenuManager().openMenu(player, "knocker_shop");
                break;
            case 23: // Legendarios
                KnockerShopMenu.setCurrentCategory("LEGENDARY");
                plugin.getMenuManager().openMenu(player, "knocker_shop");
                break;
            case 40: // Volver
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