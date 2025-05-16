package kk.kvlzx.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import kk.kvlzx.KvKnockback;

public class InventoryListener implements Listener{

    private final KvKnockback plugin;

    public InventoryListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Evento para futuros menu, cosmeticos, duels, bots
    }
}
