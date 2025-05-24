package kk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.managers.MenuManager;
import kk.kvlzx.menus.MainMenu;
import kk.kvlzx.menus.MenuType;
import kk.kvlzx.menus.TopMenu;
import kk.kvlzx.menus.TopType;
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

        if (MenuManager.isPluginInventory(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        // Aquí puedes manejar el cierre de inventarios específicos si es necesario
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (MenuManager.isPluginInventory(event.getInventory())) {
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

            // Permitir solo click izquierdo
            if (event.isRightClick()) {
                event.setCancelled(true);
                return;
            }

            // Prevenir shift+click
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);
            // Acá podemos manejar los clicks específicos según el tipo de menú
            handleMenuClick(event);
        }
    }

    private void handleMenuClick(InventoryClickEvent event) {
        MenuType menuType = MenuManager.getMenuType(event.getInventory());
        if (menuType == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        Player player = (Player) event.getWhoClicked();

        switch (menuType) {
            case MAIN_MENU:
                handleMainMenuClick(clicked, player);
                break;
            case TOP_MENU:
                // Los tops son solo visuales, no necesitan manejo de clicks
                break;
        }
    }

    private void handleMainMenuClick(ItemStack clicked, Player player) {
        Material type = clicked.getType();
        switch (type) {
            case SKULL_ITEM:
                StatsMenu.openMenu(player);
                break;
            case DIAMOND_SWORD:
                TopMenu.openMenu(player, TopType.KILLS);
                break;
            case GOLDEN_APPLE:
                TopMenu.openMenu(player, TopType.KDR);
                break;
            case BLAZE_POWDER:
                TopMenu.openMenu(player, TopType.STREAK);
                break;
            case NETHER_STAR:
                TopMenu.openMenu(player, TopType.ELO);
                break;
            case WATCH:
                TopMenu.openMenu(player, TopType.PLAYTIME);
                break;
            default:
                break;
        }
    }
}
