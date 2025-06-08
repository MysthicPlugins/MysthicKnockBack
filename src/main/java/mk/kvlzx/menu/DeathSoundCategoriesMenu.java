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

public class DeathSoundCategoriesMenu extends Menu {

    public DeathSoundCategoriesMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lDeath Sounds &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Sonidos comunes
        inv.setItem(11, createItem(Material.NOTE_BLOCK, "&7Common Sounds",
            "&8▪ &7Price: &f15,000 KGCoins",
            "&8▪ &7Rarity: &7COMMON",
            "",
            "&8➥ Basic but effective sounds",
            "&7Click to view sounds"));

        // Sonidos épicos
        inv.setItem(13, createItem(Material.JUKEBOX, "&5Epic Sounds",
            "&8▪ &7Price: &f35,000 KGCoins",
            "&8▪ &7Rarity: &5EPIC",
            "",
            "&8➥ More elaborate sounds",
            "&7Click to view sounds"));

        // Sonidos legendarios
        inv.setItem(15, createItem(Material.GOLD_RECORD, "&6Legendary Sounds",
            "&8▪ &7Price: &f75,000 KGCoins",
            "&8▪ &7Rarity: &6LEGENDARY",
            "",
            "&8➥ The most impressive sounds",
            "&7Click to view sounds"));

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch(event.getSlot()) {
            case 11: // Comunes
                DeathSoundShopMenu.setCurrentCategory("COMMON");
                plugin.getMenuManager().openMenu(player, "death_sound_shop");
                break;
            case 13: // Épicos
                DeathSoundShopMenu.setCurrentCategory("EPIC");
                plugin.getMenuManager().openMenu(player, "death_sound_shop");
                break;
            case 15: // Legendarios
                DeathSoundShopMenu.setCurrentCategory("LEGENDARY");
                plugin.getMenuManager().openMenu(player, "death_sound_shop");
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
