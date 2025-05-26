package kk.kvlzx.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.utils.MessageUtils;

public abstract class Menu {
    protected final KvKnockback plugin;
    protected final String title;
    protected final int size;

    public Menu(KvKnockback plugin, String title, int size) {
        this.plugin = plugin;
        this.title = title;
        this.size = size;
    }

    public Inventory getInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, size, MessageUtils.getColor(title));
        setupItems(player, inv);
        return inv;
    }

    protected abstract void setupItems(Player player, Inventory inv);
    
    public abstract void handleClick(InventoryClickEvent event);

    protected void fillEmptySlots(Inventory inv, ItemStack item) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, item);
            }
        }
    }
}
