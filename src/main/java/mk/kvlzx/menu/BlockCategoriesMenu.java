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
        super(plugin, "&8• &e&lBlock Categories &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Bloques comunes
        inv.setItem(11, createItem(Material.WOOD, "&7Common Blocks",
            "&8▪ &7Price: &f1,000 KGCoins",
            "&8▪ &7Rarity: &7COMMON",
            "",
            "&8➥ Woods, glasses, basic stones",
            "&7Click to see the blocks"));

        // Poco comunes
        inv.setItem(13, createItem(Material.SMOOTH_BRICK, "&aUncommon Blocks",
            "&8▪ &7Price: &f2,500 KGCoins",
            "&8▪ &7Rarity: &aUNCOMMON",
            "",
            "&8➥ Bricks, dyed clays, wools",
            "&7Click to see the blocks"));

        // Raros
        inv.setItem(15, createItem(Material.QUARTZ_BLOCK, "&9Rare Blocks",
            "&8▪ &7Price: &f5,000 KGCoins",
            "&8▪ &7Rarity: &9RARE",
            "",
            "&8➥ Quartz, snow, packed ice",
            "&7Click to see the blocks"));

        // Épicos
        inv.setItem(21, createItem(Material.PRISMARINE, "&5Epic Blocks",
            "&8▪ &7Price: &f7,500 KGCoins",
            "&8▪ &7Rarity: &5EPIC",
            "",
            "&8➥ Prismarine, obsidian, glowing blocks",
            "&7Click to see the blocks"));

        // Bedrock (Especial) - Ahora en el centro
        inv.setItem(22, createItem(Material.BEDROCK, "&4&lBedrock",
            "&8▪ &7Price: &f50,000 KGCoins",
            "&8▪ &7Rarity: &4SPECIAL",
            "",
            "&8➥ &4&lRequires all previous blocks",
            "&7Click to see requirements"));

        // Legendarios
        inv.setItem(23, createItem(Material.ENDER_STONE, "&6Legendary Blocks",
            "&8▪ &7Price: &f10,000 KGCoins",
            "&8▪ &7Rarity: &6LEGENDARY",
            "",
            "&8➥ End stone, mineral blocks, nether",
            "&7Click to see the blocks"));

        // Bloques troll
        inv.setItem(31, createItem(Material.HOPPER, "&d&lTroll Blocks",
            "&8▪ &7Price: &f15,000 KGCoins",
            "&8▪ &7Rarity: &dTROLL",
            "",
            "&8➥ &7Blocks with special effects!",
            "&7Click to see the blocks"));

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7);
        fillEmptySlots(inv, filler);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        switch(event.getSlot()) {
            case 11: // Comunes
                BlockShopMenu.setCurrentCategory("COMÚN");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 13: // Poco comunes
                BlockShopMenu.setCurrentCategory("POCO COMÚN");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 15: // Raros
                BlockShopMenu.setCurrentCategory("RARO");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 21: // Épicos
                BlockShopMenu.setCurrentCategory("ÉPICO");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 22: // Bedrock
                BlockShopMenu.setCurrentCategory("ESPECIAL");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 23: // Legendarios
                BlockShopMenu.setCurrentCategory("LEGENDARIO");
                plugin.getMenuManager().openMenu(player, "block_shop");
                break;
            case 31: // Bloques Troll
                BlockShopMenu.setCurrentCategory("TROLL");
                plugin.getMenuManager().openMenu(player, "block_shop");
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
