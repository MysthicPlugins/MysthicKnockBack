package kk.kvlzx.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.utils.MessageUtils;

public class InventoryListener implements Listener {
    private final KvKnockback plugin;

    public InventoryListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (plugin.getInventoryManager().isEditorInventory(event.getInventory())) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            int slot = event.getRawSlot();
            
            // Primera fila: Items disponibles (0-8)
            if (slot < 9) {
                ItemStack clone = clicked.clone();
                player.getInventory().setItem(event.getHotbarButton(), clone);
                return;
            }
            
            // Segunda fila: Separador (9-17)
            if (slot < 18) return;
            
            // Tercera fila: Área editable (18-26)
            if (slot < 27) {
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    event.setCancelled(false); // Permitir colocar items
                    return;
                }
                if (event.isShiftClick()) {
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(false); // Permitir mover/quitar items
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        if (plugin.getInventoryManager().isEditorInventory(event.getInventory())) {
            // Solo permitir arrastrar en la tercera fila
            for (int slot : event.getRawSlots()) {
                if (slot < 18) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
            
        if (plugin.getInventoryManager().isEditorInventory(event.getInventory())) {
            // Guardar el layout de la tercera fila
            ItemStack[] layout = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                layout[i] = event.getInventory().getItem(i + 18);
            }
            plugin.getInventoryManager().saveCustomInventory(player.getUniqueId(), layout);
            player.sendMessage(MessageUtils.getColor("&aInventario personalizado guardado correctamente!"));
        }
    }
}
