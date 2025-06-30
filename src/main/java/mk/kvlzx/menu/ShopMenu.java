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
        super(plugin, "&8• &a&lMain Shop &8•", 54);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Mostrar balance en el centro superior
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current balance: &a" + stats.getKGCoins() + " KGCoins"));

        // Primera fila de items (slots 19-25)
        inv.setItem(19, createItem(Material.SANDSTONE, "&e&lCustom Blocks",
            "&7Click to view available blocks",
            "",
            "&8▪ &7Special decorative blocks",
            "&8▪ &7Perfect for building",
            "",
            "&8➥ Includes +30 unique blocks",
            "&aAvailable!"));

        inv.setItem(22, createItem(Material.STICK, "&e&lCustom Knockers", 
            "&7Click to view available knockers",
            "",
            "&8▪ &7Special knockback sticks",
            "&8▪ &7Visual effects when hitting",
            "",
            "&8➥ Includes +20 unique knockers",
            "&aAvailable!"));

        inv.setItem(25, createItem(Material.PAPER, "&e&lKill Messages", 
            "&7Click to view kill messages",
            "",
            "&8▪ &7Messages when eliminating players",
            "&8▪ &7Show your style",
            "",
            "&8➥ Includes +10 unique messages",
            "&aAvailable!"));

        // Segunda fila de items (slots 28-34)
        inv.setItem(28, createItem(Material.BOOK_AND_QUILL, "&e&lDeath Messages", 
            "&7Click to view death messages",
            "",
            "&8▪ &7Custom messages when dying",
            "&8▪ &7Die in style",
            "",
            "&8➥ Includes +20 unique messages",
            "&aAvailable!"));

        inv.setItem(31, createItem(Material.ARROW, "&e&lArrow Effects", 
            "&7Click to view arrow effects",
            "",
            "&8▪ &7Special effects on your arrows",
            "&8▪ &7Particles and animations",
            "",
            "&8➥ Includes +5 unique effects",
            "&aAvailable!"));

        inv.setItem(34, createItem(Material.NOTE_BLOCK, "&d&lDeath Sounds", 
            "&7Click to view death sounds",
            "",
            "&8▪ &7Special sounds when dying",
            "&8▪ &7Unique audio effects",
            "",
            "&8➥ Includes +5 unique sounds",
            "&aAvailable!"));

        // Tercera fila de items (slots 37-43)
        inv.setItem(37, createItem(Material.DIAMOND_SWORD, "&e&lKill Sounds", 
            "&7Click to view kill sounds",
            "",
            "&8▪ &7Special sounds when eliminating players",
            "&8▪ &7Unique audio effects",
            "",
            "&8➥ Includes +5 unique sounds",
            "&aAvailable!"));

        inv.setItem(40, createItem(Material.ANVIL, "&e&lJoin Messages", 
            "&7Click to view join messages",
            "",
            "&8▪ &7Messages when joining the game",
            "&8▪ &7Show your style",
            "",
            "&8➥ Includes +10 unique messages",
            "&aAvailable!"));

        inv.setItem(43, createItem(Material.GOLD_RECORD, "&5&lMusic",
            "&7Click to view music",
            "",
            "&8▪ &7Custom music",
            "&8▪ &7Exclusive themes",
            "",
            "&8➥ Includes +5 unique music",
            "&aAvailable!"));

        // Botón para volver
        inv.setItem(49, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to main menu"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
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
            case 40: // Mensajes de entrada
                plugin.getMenuManager().openMenu(player, "join_message_categories");
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
