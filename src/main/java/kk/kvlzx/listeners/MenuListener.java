package kk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.menus.MainMenu;
import kk.kvlzx.menus.TopMenu;
import kk.kvlzx.menus.StatsMenu;

public class MenuListener implements Listener {
    private final KvKnockback plugin;

    public MenuListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.SKULL_ITEM) {
            MainMenu.openMenu(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getInventory().getTitle();

        if (title.contains("Menú Principal") || title.contains("Top ")) {
            for (int slot : event.getRawSlots()) {
                if (slot < event.getView().getTopInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        String title = event.getInventory().getTitle();
        if (title.contains("Menú Principal")) {
            cancelEvents(event);
            
            switch (clicked.getType()) {
                case DIAMOND_SWORD:
                    TopMenu.openKillsMenu(player);
                    break;
                case GOLDEN_APPLE:
                    TopMenu.openKDRMenu(player);
                    break;
                case BLAZE_POWDER:
                    TopMenu.openStreaksMenu(player);
                    break;
                case NETHER_STAR:
                    TopMenu.openELOMenu(player);
                    break;
                case SKULL_ITEM:
                    player.closeInventory();
                    StatsMenu.openMenu(player);
                    break;
                default:
                    break;
            }
        } else if (title.contains("Top ")) {
            cancelEvents(event);
        } else if (title.contains("Estadísticas")) {
            cancelEvents(event);
        }
    }

    private void cancelEvents(InventoryClickEvent event) {
            // Prevenir mover items entre inventarios
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                event.setCancelled(true);
                return;
            }
            
            // Prevenir usar hotkeys de números (1-9)
            if (event.getHotbarButton() != -1) {
                event.setCancelled(true);
                return;
            }
            
            // Permitir solo click izquierdo en el editor
            if (event.isRightClick()) {
                event.setCancelled(true);
                return;
            }

            // Prevenir shift+click que mueve items al inventario del jugador
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(false);
    }
}
