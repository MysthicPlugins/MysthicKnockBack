package kk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.items.CustomItem;
import kk.kvlzx.utils.MessageUtils;

public class MainMenu extends Menu {

    public MainMenu(KvKnockback plugin) {
        super(plugin, "&8• &b&lMenú Principal &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // TOP ROW - Tops más importantes
        inv.setItem(12, createItem(Material.DIAMOND_SWORD, "&a&lTop Kills",
            "&7Click para ver el top 10 de kills"));
        
        inv.setItem(13, createItem(Material.NETHER_STAR, "&6&lTop ELO",
            "&7Click para ver el top 10 de ELO"));
        
        inv.setItem(14, createItem(Material.DIAMOND, "&d&lTop Rachas",
            "&7Click para ver el top 10 de rachas"));

        // MIDDLE ROW - Tops secundarios
        inv.setItem(21, createItem(Material.GOLDEN_APPLE, "&b&lTop KDR",
            "&7Click para ver el top 10 de KDR"));
            
        inv.setItem(23, createItem(Material.WATCH, "&e&lTop Tiempo",
            "&7Click para ver el top 10 de tiempo jugado"));

        // BOTTOM ROW - Acciones personales
        inv.setItem(37, createItem(Material.DIAMOND_SWORD, "&e&lEditar Hotbar",
            "&7Click para personalizar tu hotbar",
            "",
            "&8➥ Personaliza la posición de tus items"));

        inv.setItem(39, CustomItem.createSkull(player, "&a&lMis Estadísticas",
            "&7Click para ver tus estadísticas"));

        inv.setItem(41, createItem(Material.BOOK_AND_QUILL, "&c&lReportar Jugador",
            "&7Click para reportar a un jugador",
            "",
            "&8➥ Reporta comportamiento inadecuado"));

        inv.setItem(43, createItem(Material.EMERALD, "&a&lTienda", 
            "&7Click para abrir la tienda",
            "",
            "&8➥ ¡Compra cosméticos y más!"));

        // Relleno con diseño
        ItemStack darkGlass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15);
        ItemStack lightGlass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7);
        
        // Patrón de relleno alternado
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, (i % 2 == 0) ? darkGlass : lightGlass);
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch (event.getSlot()) {
            case 12: // Top Kills
                plugin.getMenuManager().openMenu(player, "top_kills");
                break;
            case 13: // Top Elo
                plugin.getMenuManager().openMenu(player, "top_elo");
                break;
            case 14: // Top Rachas
                plugin.getMenuManager().openMenu(player, "top_streak");
                break;
            case 21: // Top KDR
                plugin.getMenuManager().openMenu(player, "top_kdr");
                break;
            case 23: // Top Tiempo
                plugin.getMenuManager().openMenu(player, "top_time");
                break;
            case 37: // Editar Hotbar
                plugin.getMenuManager().openMenu(player, "hotbar_edit");
                break;
            case 39: // Mis Estadísticas
                plugin.getMenuManager().openMenu(player, "stats");
                break;
            case 41: // Reportar Jugador
                plugin.getMenuManager().openMenu(player, "player_list");
                break;
            case 43: // Tienda
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
