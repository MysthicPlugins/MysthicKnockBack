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

public class BlockCategoriesMenu extends Menu {

    public BlockCategoriesMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lCategorías de Bloques &8•", 36);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Bloques comunes
        inv.setItem(10, createItem(Material.WOOD, "&7Bloques Comunes",
            "&8▪ &7Precio: &f1,000 KGCoins",
            "&8▪ &7Rareza: &7COMÚN",
            "",
            "&8➥ Maderas, vidrios, piedras básicas",
            "&7Click para ver los bloques"));

        // Poco comunes
        inv.setItem(12, createItem(Material.SMOOTH_BRICK, "&aBloques Poco Comunes",
            "&8▪ &7Precio: &f2,500 KGCoins",
            "&8▪ &7Rareza: &aPOCO COMÚN",
            "",
            "&8➥ Ladrillos, arcillas teñidas, lanas",
            "&7Click para ver los bloques"));

        // Raros
        inv.setItem(14, createItem(Material.QUARTZ_BLOCK, "&9Bloques Raros",
            "&8▪ &7Precio: &f5,000 KGCoins",
            "&8▪ &7Rareza: &9RARO",
            "",
            "&8➥ Cuarzo, nieve, hielo compacto",
            "&7Click para ver los bloques"));

        // Épicos
        inv.setItem(16, createItem(Material.PRISMARINE, "&5Bloques Épicos",
            "&8▪ &7Precio: &f7,500 KGCoins",
            "&8▪ &7Rareza: &5ÉPICO",
            "",
            "&8➥ Prismarina, obsidiana, bloques brillantes",
            "&7Click para ver los bloques"));

        // Legendarios
        inv.setItem(21, createItem(Material.ENDER_STONE, "&6Bloques Legendarios",
            "&8▪ &7Precio: &f10,000 KGCoins",
            "&8▪ &7Rareza: &6LEGENDARIO",
            "",
            "&8➥ End stone, bloques minerales, nether",
            "&7Click para ver los bloques"));

        // Bedrock (Especial)
        inv.setItem(23, createItem(Material.BEDROCK, "&4&lBedrock",
            "&8▪ &7Precio: &f50,000 KGCoins",
            "&8▪ &7Rareza: &4ESPECIAL",
            "",
            "&8➥ &4&lRequiere todos los bloques anteriores",
            "&7Click para ver requisitos"));

        // Añadir el botón de bloques troll (slot 25)
        inv.setItem(25, createItem(Material.HOPPER, "&d&lBloques Troll",
            "&8▪ &7Precio: &f15,000 KGCoins",
            "&8▪ &7Rareza: &dTROLL",
            "",
            "&8➥ &7¡Bloques con efectos especiales!",
            "&7Click para ver los bloques"));

        // Botón para volver
        inv.setItem(31, createItem(Material.ARROW, "&c← Volver", 
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
            case 10: // Comunes
                BlockShopMenu.setCurrentCategory("COMÚN");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 12: // Poco comunes
                BlockShopMenu.setCurrentCategory("POCO COMÚN");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 14: // Raros
                BlockShopMenu.setCurrentCategory("RARO");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 16: // Épicos
                BlockShopMenu.setCurrentCategory("ÉPICO");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 21: // Legendarios
                BlockShopMenu.setCurrentCategory("LEGENDARIO");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 23: // Bedrock
                BlockShopMenu.setCurrentCategory("ESPECIAL");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 25: // Bloques Troll
                BlockShopMenu.setCurrentCategory("TROLL");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 31: // Volver
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
