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

public class DeathMessageCategoriesMenu extends Menu {

    public DeathMessageCategoriesMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lCategorías de Mensajes &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Mensajes comunes
        inv.setItem(11, createItem(Material.PAPER, "&7Mensajes Comunes",
            "&8▪ &7Precio: &f10,000 KGCoins",
            "&8▪ &7Rareza: &7COMÚN",
            "",
            "&8➥ Mensajes graciosos y simples",
            "&7Click para ver los mensajes"));

        // Mensajes épicos
        inv.setItem(13, createItem(Material.BOOK, "&5Mensajes Épicos",
            "&8▪ &7Precio: &f25,000 KGCoins",
            "&8▪ &7Rareza: &5ÉPICO",
            "",
            "&8➥ Mensajes con más estilo",
            "&7Click para ver los mensajes"));

        // Mensajes legendarios
        inv.setItem(15, createItem(Material.BOOK_AND_QUILL, "&6Mensajes Legendarios",
            "&8▪ &7Precio: &f50,000 KGCoins",
            "&8▪ &7Rareza: &6LEGENDARIO",
            "",
            "&8➥ Los mensajes más épicos",
            "&7Click para ver los mensajes"));

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
                DeathMessageShopMenu.setCurrentCategory("COMÚN");
                plugin.getMenuManager().openMenu(player, "death_message_shop");
                break;
            case 13: // Épicos
                DeathMessageShopMenu.setCurrentCategory("ÉPICO");
                plugin.getMenuManager().openMenu(player, "death_message_shop");
                break;
            case 15: // Legendarios
                DeathMessageShopMenu.setCurrentCategory("LEGENDARIO");
                plugin.getMenuManager().openMenu(player, "death_message_shop");
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
