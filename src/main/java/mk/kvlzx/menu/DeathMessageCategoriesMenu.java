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
        super(plugin, "&8• &e&lMessage Categories &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Mensajes comunes
        inv.setItem(11, createItem(Material.PAPER, "&7Common Messages",
            "&8▪ &7Price: &f10,000 KGCoins",
            "&8▪ &7Rarity: &7COMMON",
            "",
            "&8➥ Funny and simple messages",
            "&7Click to view messages"));

        // Mensajes épicos
        inv.setItem(13, createItem(Material.BOOK, "&5Epic Messages",
            "&8▪ &7Price: &f25,000 KGCoins",
            "&8▪ &7Rarity: &5EPIC",
            "",
            "&8➥ More stylish messages",
            "&7Click to view messages"));

        // Mensajes legendarios
        inv.setItem(15, createItem(Material.BOOK_AND_QUILL, "&6Legendary Messages",
            "&8▪ &7Price: &f50,000 KGCoins",
            "&8▪ &7Rarity: &6LEGENDARY",
            "",
            "&8➥ The most epic messages",
            "&7Click to view messages"));

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7);
        fillEmptySlots(inv, filler);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Validar que el click sea en el menú y no en el inventario del jugador
        if (!isValidClick(event)) {
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch(event.getSlot()) {
            case 11: // Comunes
                DeathMessageShopMenu.setCurrentCategory("COMMON");
                plugin.getMenuManager().openMenu(player, "death_message_shop");
                break;
            case 13: // Épicos
                DeathMessageShopMenu.setCurrentCategory("EPIC");
                plugin.getMenuManager().openMenu(player, "death_message_shop");
                break;
            case 15: // Legendarios
                DeathMessageShopMenu.setCurrentCategory("LEGENDARY");
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
