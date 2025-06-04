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
        super(plugin, "&8• &e&lSonidos de Muerte &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Sonidos comunes
        inv.setItem(11, createItem(Material.NOTE_BLOCK, "&7Sonidos Comunes",
            "&8▪ &7Precio: &f15,000 KGCoins",
            "&8▪ &7Rareza: &7COMÚN",
            "",
            "&8➥ Sonidos básicos pero efectivos",
            "&7Click para ver los sonidos"));

        // Sonidos épicos
        inv.setItem(13, createItem(Material.JUKEBOX, "&5Sonidos Épicos",
            "&8▪ &7Precio: &f35,000 KGCoins",
            "&8▪ &7Rareza: &5ÉPICO",
            "",
            "&8➥ Sonidos más elaborados",
            "&7Click para ver los sonidos"));

        // Sonidos legendarios
        inv.setItem(15, createItem(Material.GOLD_RECORD, "&6Sonidos Legendarios",
            "&8▪ &7Precio: &f75,000 KGCoins",
            "&8▪ &7Rareza: &6LEGENDARIO",
            "",
            "&8➥ Los sonidos más impresionantes",
            "&7Click para ver los sonidos"));

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver a la tienda"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch(event.getSlot()) {
            case 11: // Comunes
                DeathSoundShopMenu.setCurrentCategory("COMÚN");
                plugin.getMenuManager().openMenu(player, "death_sound_shop");
                break;
            case 13: // Épicos
                DeathSoundShopMenu.setCurrentCategory("ÉPICO");
                plugin.getMenuManager().openMenu(player, "death_sound_shop");
                break;
            case 15: // Legendarios
                DeathSoundShopMenu.setCurrentCategory("LEGENDARIO");
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
