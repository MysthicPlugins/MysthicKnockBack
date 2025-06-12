package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class PlayerListMenu extends Menu {

    public PlayerListMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &c&lOnline Players &8•", 54);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player); // Remover al jugador que está reportando

        // Place player heads
        int slot = 10;
        for (Player target : onlinePlayers) {
            if (slot > 43) break; // Slot limit

            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(target.getName());
            meta.setDisplayName(MessageUtils.getColor("&c" + target.getName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.getColor("&7Click to report this player"));
            meta.setLore(lore);
            
            skull.setItemMeta(meta);
            inv.setItem(slot, skull);

            // Increment slot, skipping borders
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        // Back button
        inv.setItem(49, createItem(Material.ARROW, "&c← Back", "&7Click to return to the menu"));

        // Borders with wither skulls and redstone, alternating with two empty spaces
        ItemStack witherSkull = createItem(Material.SKULL_ITEM, "&7", (byte) 1); // Wither skull
        ItemStack redstone = createItem(Material.REDSTONE, "&7"); // Redstone block

        // Define border slots
        int[] borderSlots = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,     // Top row
            9, 18, 27, 36,                   // Left column
            17, 26, 35, 44,                  // Right column
            45, 46, 47, 48, 50, 51, 52, 53  // Bottom row (excludes slot 49)
        };

        // Place items with pattern: wither, space, space, redstone, space, space
        for (int i = 0; i < borderSlots.length; i++) {
            if (borderSlots[i] == 49) continue; // Avoid overwriting the back button
            if (i % 6 == 0) {
                inv.setItem(borderSlots[i], witherSkull); // Wither skull
            } else if (i % 6 == 3) {
                inv.setItem(borderSlots[i], redstone); // Redstone
            } // Slots i % 6 == 1, 2, 4, 5 remain empty (null)
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() != null) {
            // Ignore clicks on decorative items (wither skull and redstone block)
            if ((event.getCurrentItem().getType() == Material.SKULL_ITEM && 
                event.getCurrentItem().getDurability() == 1) ||
                event.getCurrentItem().getType() == Material.REDSTONE) {
                return;
            }

            if (event.getSlot() == 49) {
                plugin.getMenuManager().openMenu(player, "main");
                return;
            }

            if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                String targetName = MessageUtils.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                plugin.getReportManager().setReportTarget(player.getUniqueId(), targetName);
                plugin.getMenuManager().openMenu(player, "report_reason");
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
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

    private ItemStack createItem(Material material, String name, byte data, String... lore) {
        ItemStack item = createItem(material, name, lore);
        item.setDurability(data);
        return item;
    }
}
