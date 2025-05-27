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
        // Tops - Primera fila
        inv.setItem(11, createItem(Material.DIAMOND_SWORD, "&a&lTop Kills",
            "&7Click para ver el top 10 de kills"));
        
        inv.setItem(13, createItem(Material.GOLDEN_APPLE, "&b&lTop KDR",
            "&7Click para ver el top 10 de KDR"));
        
        inv.setItem(15, createItem(Material.NETHER_STAR, "&6&lTop ELO",
            "&7Click para ver el top 10 de ELO"));
            
        // Tops - Segunda fila
        inv.setItem(20, createItem(Material.DIAMOND, "&d&lTop Rachas",
            "&7Click para ver el top 10 de rachas"));
            
        inv.setItem(24, createItem(Material.WATCH, "&e&lTop Tiempo",
            "&7Click para ver el top 10 de tiempo jugado"));

        // Hotbar edit - Cuarta fila
        inv.setItem(39, createItem(Material.DIAMOND_SWORD, "&e&lEditar Hotbar",
            "&7Click para personalizar tu hotbar",
            "",
            "&8➥ Personaliza la posición de tus items"));

        // Mis Estadísticas
        inv.setItem(40, CustomItem.createSkull(player, "&a&lMis Estadísticas",
            "&7Click para ver tus estadísticas"));

        // Añadir botón de reportes
        inv.setItem(41, createItem(Material.BOOK_AND_QUILL, "&c&lReportar Jugador",
            "&7Click para reportar a un jugador",
            "",
            "&8➥ Reporta comportamiento inadecuado"));

        // Añadir botón de tienda antes del relleno
        inv.setItem(42, createItem(Material.EMERALD, "&a&lTienda", 
            "&7Click para abrir la tienda",
            "",
            "&8➥ ¡Compra cosméticos y más!"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15);
        fillEmptySlots(inv, filler);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch (event.getSlot()) {
            case 11: // Top Kills
                plugin.getMenuManager().openMenu(player, "top_kills");
                break;
            case 13: // Top KDR
                plugin.getMenuManager().openMenu(player, "top_kdr");
                break;
            case 15: // Top Elo
                plugin.getMenuManager().openMenu(player, "top_elo");
                break;
            case 20: // Top Rachas
                plugin.getMenuManager().openMenu(player, "top_streak");
                break;
            case 24: // Top Tiempo
                plugin.getMenuManager().openMenu(player, "top_time");
                break;
            case 39: // Editar Hotbar
                plugin.getMenuManager().openMenu(player, "hotbar_edit");
                break;
            case 40: // Mis Estadísticas
                plugin.getMenuManager().openMenu(player, "stats");
                break;
            case 41: // Reportar Jugador
                plugin.getMenuManager().openMenu(player, "player_list");
                break;
            case 42: // Tienda
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
