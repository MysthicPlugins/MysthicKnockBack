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

public class JoinMessageCategoriesMenu extends Menu {

    public JoinMessageCategoriesMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lMessage Categories &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Common messages
        inv.setItem(11, createItem(Material.PAPER, "&7Common Messages",
            "&8▪ &7Price: &f15,000 KGCoins",
            "&8▪ &7Rarity: &7COMMON",
            "",
            "&8➥ Simple and catchy messages",
            "&7Click to view messages"));

        // Epic messages
        inv.setItem(13, createItem(Material.BOOK, "&5Epic Messages",
            "&8▪ &7Price: &f35,000 KGCoins",
            "&8▪ &7Rarity: &5EPIC",
            "",
            "&8➥ More original messages",
            "&7Click to view messages"));

        // Legendary messages
        inv.setItem(15, createItem(Material.BOOK_AND_QUILL, "&6Legendary Messages",
            "&8▪ &7Price: &f75,000 KGCoins",
            "&8▪ &7Rarity: &6LEGENDARY",
            "",
            "&8➥ The most epic messages",
            "&7Click to view messages"));

        // Back button
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Filler
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
        
        Player player = (Player) event.getWhoClicked();

        switch(event.getSlot()) {
            case 11:
                JoinMessageShopMenu.setCurrentCategory("COMMON");
                plugin.getMenuManager().openMenu(player, "join_message_shop");
                break;
            case 13:
                JoinMessageShopMenu.setCurrentCategory("EPIC");
                plugin.getMenuManager().openMenu(player, "join_message_shop");
                break;
            case 15:
                JoinMessageShopMenu.setCurrentCategory("LEGENDARY");
                plugin.getMenuManager().openMenu(player, "join_message_shop");
                break;
            case 40:
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
