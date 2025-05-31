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
        super(plugin, "&8• &e&lCategorías de Knockers &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Comunes
        inv.setItem(11, createItem(Material.BONE, "&7Knockers Comunes",
            "&8▪ &7Precio: &f5,000 KGCoins",
            "&8▪ &7Rareza: &7COMÚN",
            "",
            "&8➥ Armas básicas pero efectivas",
            "&7Click para ver los knockers"));

        // Poco comunes
        inv.setItem(13, createItem(Material.BOOK, "&aPoco Comunes",
            "&8▪ &7Precio: &f15,000 KGCoins",
            "&8▪ &7Rareza: &aPOCO COMÚN",
            "",
            "&8➥ Armas con efectos especiales",
            "&7Click para ver los knockers"));

        // Raros
        inv.setItem(15, createItem(Material.BLAZE_ROD, "&9Knockers Raros",
            "&8▪ &7Precio: &f50,000 KGCoins",
            "&8▪ &7Rareza: &9RARO",
            "",
            "&8➥ Armas de gran poder",
            "&7Click para ver los knockers"));

        // Épicos
        inv.setItem(21, createItem(Material.DIAMOND, "&5Knockers Épicos",
            "&8▪ &7Precio: &f500,000 KGCoins",
            "&8▪ &7Rareza: &5ÉPICO",
            "",
            "&8➥ Armas legendarias",
            "&7Click para ver los knockers"));

        // Legendarios
        inv.setItem(23, createItem(Material.GOLDEN_APPLE, "&6Knockers Legendarios",
            "&8▪ &7Precio: &f1,000,000 KGCoins",
            "&8▪ &7Rareza: &6LEGENDARIO",
            "",
            "&8➥ Las armas más poderosas",
            "&7Click para ver los knockers"));

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver a la tienda"));

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
                KnockerShopMenu.setCurrentCategory("COMÚN");
                plugin.getMenuManager().openMenu(player, "knocker_shop");
                break;
            case 13: // Poco comunes
                KnockerShopMenu.setCurrentCategory("POCO COMÚN");
                plugin.getMenuManager().openMenu(player, "knocker_shop");
                break;
            case 15: // Raros
                KnockerShopMenu.setCurrentCategory("RARO");
                plugin.getMenuManager().openMenu(player, "knocker_shop");
                break;
            case 21: // Épicos
                KnockerShopMenu.setCurrentCategory("ÉPICO");
                plugin.getMenuManager().openMenu(player, "knocker_shop");
                break;
            case 23: // Legendarios
                KnockerShopMenu.setCurrentCategory("LEGENDARIO");
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
