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
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class ShopMenu extends Menu {

    public ShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &a&lTienda Principal &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Mostrar balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lTu Balance",
            "&7Balance actual: &a" + stats.getKGCoins() + " KGCoins"));

        // Categorías de cosméticos reorganizadas
        inv.setItem(11, createItem(Material.SANDSTONE, "&e&lBloques Personalizados",
            "&7Click para ver los bloques disponibles",
            "",
            "&8➥ Bloques exclusivos para construcción",
            "&aDisponible!"));

        inv.setItem(13, createItem(Material.STICK, "&e&lKnockers Personalizados", 
            "&7Click para ver los knockers disponibles",
            "",
            "&8➥ Palos de empuje exclusivos",
            "&aDisponible!"));

        inv.setItem(15, createItem(Material.PAPER, "&e&lMensajes de Kill", 
            "&7Click para ver mensajes de kill",
            "",
            "&8➥ Mensajes al eliminar jugadores",
            "&aDisponible!"));

        inv.setItem(29, createItem(Material.BOOK_AND_QUILL, "&e&lMensajes de Muerte", 
            "&7Click para ver mensajes de muerte",
            "",
            "&8➥ Mensajes personalizados al morir",
            "&aDisponible!"));

        inv.setItem(31, createItem(Material.DIAMOND_SWORD, "&b&lEfectos de Kill",
            "&7Click para ver efectos de kill",
            "",
            "&8➥ Efectos al eliminar jugadores",
            "&cPróximamente"));

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver al menú principal"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch(event.getSlot()) {
            case 11: // Bloques
                plugin.getMenuManager().openMenu(player, "block_categories");
                break;
            case 13: // Knockers
                plugin.getMenuManager().openMenu(player, "knocker_categories");
                break;
            case 15: // Mensajes de Kill
                plugin.getMenuManager().openMenu(player, "kill_message_categories");
                break;
            case 29: // Mensajes de muerte
                plugin.getMenuManager().openMenu(player, "death_message_categories");
                break;
            case 31: // Efectos de kill
                player.sendMessage(MessageUtils.getColor("&cPróximamente disponible."));
                break;
            case 40: // Volver
                plugin.getMenuManager().openMenu(player, "main");
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
