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
        super(plugin, "&8• &a&lTienda Principal &8•", 54); // Cambiar a 54 slots (6 filas)
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Mostrar balance en el centro superior
        inv.setItem(4, createItem(Material.EMERALD, "&a&lTu Balance",
            "&7Balance actual: &a" + stats.getKGCoins() + " KGCoins"));

        // Primera fila de items (slots 19-25)
        inv.setItem(19, createItem(Material.SANDSTONE, "&e&lBloques Personalizados",
            "&7Click para ver los bloques disponibles",
            "",
            "&8▪ &7Bloques decorativos especiales",
            "&8▪ &7Perfecto para construir",
            "",
            "&8➥ Incluye +30 bloques únicos",
            "&aDisponible!"));

        inv.setItem(22, createItem(Material.STICK, "&e&lKnockers Personalizados", 
            "&7Click para ver los knockers disponibles",
            "",
            "&8▪ &7Palos de empuje especiales",
            "&8▪ &7Efectos visuales al golpear",
            "",
            "&8➥ Incluye +20 knockers únicos",
            "&aDisponible!"));

        inv.setItem(25, createItem(Material.PAPER, "&e&lMensajes de Kill", 
            "&7Click para ver mensajes de kill",
            "",
            "&8▪ &7Mensajes al eliminar jugadores",
            "&8▪ &7Muestra tu estilo",
            "",
            "&8➥ Incluye +15 mensajes únicos",
            "&aDisponible!"));

        // Segunda fila de items (slots 28-34)
        inv.setItem(28, createItem(Material.BOOK_AND_QUILL, "&e&lMensajes de Muerte", 
            "&7Click para ver mensajes de muerte",
            "",
            "&8▪ &7Mensajes personalizados al morir",
            "&8▪ &7Muere con estilo",
            "",
            "&8➥ Incluye +20 mensajes únicos",
            "&aDisponible!"));

        inv.setItem(31, createItem(Material.ARROW, "&e&lEfectos de Flecha", 
            "&7Click para ver efectos de flecha",
            "",
            "&8▪ &7Efectos especiales en tus flechas",
            "&8▪ &7Partículas y animaciones",
            "",
            "&8➥ Incluye +10 efectos únicos",
            "&aDisponible!"));

        inv.setItem(34, createItem(Material.NOTE_BLOCK, "&d&lSonidos de muerte", 
            "&7Click para ver sonidos de muerte",
            "",
            "&8▪ &7Sonidos especiales al morir",
            "&8▪ &7Efectos de audio únicos",
            "",
            "&8➥ Incluye +8 sonidos únicos",
            "&aDisponible!"));

        // Tercera fila de items (slots 37-43)
        inv.setItem(37, createItem(Material.DIAMOND_SWORD, "&e&lSonidos de Kill", 
            "&7Click para ver sonidos de kill",
            "",
            "&8▪ &7Sonidos especiales al eliminar jugadores",
            "&8▪ &7Efectos de audio únicos",
            "",
            "&8➥ Incluye +8 sonidos únicos",
            "&aDisponible!"));

        inv.setItem(43, createItem(Material.GOLD_RECORD, "&5&lMúsica",
            "&7Click para ver música",
            "",
            "&8▪ &7Música personalizada",
            "&8▪ &7Temas exclusivos",
            "",
            "&8➥ Ambienta tus partidas",
            "&aDisponible!"));

        // Botón para volver
        inv.setItem(49, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver al menú principal"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch(event.getSlot()) {
            case 19: // Bloques
                plugin.getMenuManager().openMenu(player, "block_categories");
                break;
            case 22: // Knockers
                plugin.getMenuManager().openMenu(player, "knocker_categories");
                break;
            case 25: // Mensajes de Kill
                plugin.getMenuManager().openMenu(player, "kill_message_categories");
                break;
            case 28: // Mensajes de muerte
                plugin.getMenuManager().openMenu(player, "death_message_categories");
                break;
            case 31: // Efectos de flecha
                plugin.getMenuManager().openMenu(player, "arrow_effect_categories");
                break;
            case 34: // Sonidos de muerte
                plugin.getMenuManager().openMenu(player, "death_sound_categories");
                break;
            case 37: // Sonidos de kill
                plugin.getMenuManager().openMenu(player, "kill_sound_categories");
                break;
            case 43: // Música
                plugin.getMenuManager().openMenu(player, "music_categories");
                break;
            case 49: // Volver
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
