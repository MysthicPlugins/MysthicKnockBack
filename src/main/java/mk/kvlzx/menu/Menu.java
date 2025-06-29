package mk.kvlzx.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public abstract class Menu {
    protected final MysthicKnockBack plugin;
    protected final String title;
    protected final int size;

    public Menu(MysthicKnockBack plugin, String title, int size) {
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

    // Método helper para validar si el click es en este menú
    protected boolean isValidClick(InventoryClickEvent event) {
        // Verificar que se haya clickeado en un inventario
        if (event.getClickedInventory() == null) {
            return false;
        }
        
        // Verificar que el click sea en el inventario superior (el menú)
        // y no en el inventario del jugador
        return event.getClickedInventory().equals(event.getView().getTopInventory());
    }

    protected void fillEmptySlots(Inventory inv, ItemStack item) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, item);
            }
        }
    }
}