package kk.kvlzx.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.event.block.Action;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.menu.HotbarEditMenu;
import kk.kvlzx.menu.Menu;

public class MenuListener implements Listener {
    private final KvKnockback plugin;

    public MenuListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Menu menu = plugin.getMenuManager().getOpenMenu(player);
        
        if (menu != null) {
            menu.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Menu menu = plugin.getMenuManager().getOpenMenu(player);
        
        // Verificar si tenía un menú abierto
        if (menu != null) {
            // Verificar específicamente si es el menú de hotbar y tiene un item en el cursor
            if (menu instanceof HotbarEditMenu && player.getItemOnCursor() != null && 
                player.getItemOnCursor().getType() != Material.AIR) {
                
                // Eliminar el item del cursor
                player.setItemOnCursor(null);
                
                // Reabrir el inventario en el siguiente tick para evitar problemas
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getMenuManager().openMenu(player, "hotbar_edit");
                });
                return;
            }
            
            plugin.getMenuManager().closeMenu(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (item != null && item.getType() == Material.SKULL_ITEM) {
                String zone = plugin.getArenaManager().getPlayerZone(player);
                if (zone != null && zone.equalsIgnoreCase("spawn")) {
                    event.setCancelled(true);
                    plugin.getMenuManager().openMenu(player, "main");
                }
            }
        }
    }
}
