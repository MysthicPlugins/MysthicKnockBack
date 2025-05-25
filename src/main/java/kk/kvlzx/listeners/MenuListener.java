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
import org.bukkit.inventory.Inventory;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.items.ItemsManager;
import kk.kvlzx.managers.MenuManager;
import kk.kvlzx.menus.MainMenu;
import kk.kvlzx.menus.MenuType;
import kk.kvlzx.menus.TopMenu;
import kk.kvlzx.menus.StatsMenu;
import kk.kvlzx.menus.InventoryEditorMenu;
import kk.kvlzx.utils.MessageUtils;

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
        
        Player player = (Player) event.getWhoClicked();
        if (MenuManager.isInMenu(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        MenuManager.debugPlayerMenu(player); // Debug antes de cerrar el menú
        
        if (MenuManager.isInMenuType(player, MenuType.INVENTORY_EDITOR)) {
            ItemStack[] newLayout = new ItemStack[9];
            Inventory inv = event.getInventory();
            for (int i = 0; i < 9; i++) {
                newLayout[i] = inv.getItem(i);
            }
            ItemsManager.savePvPLayout(newLayout);
            player.sendMessage(MessageUtils.getColor("&aInventorio guardado correctamente!"));
        }
        
        MenuManager.removePlayer(player);
        MenuManager.debugPlayerMenu(player); // Debug después de cerrar el menú
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!MenuManager.isInMenu(player)) return;

        // Prevenir clicks en el inventario del jugador
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }

        // Si no hay item clickeado, retornar
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        MenuType menuType = MenuManager.getPlayerMenuType(player);
        if (menuType == null) return;

        // Manejar el botón de retorno para todos los menús excepto el principal
        if (menuType != MenuType.MAIN && 
            clicked.getType() == Material.ARROW && 
            clicked.hasItemMeta() && 
            clicked.getItemMeta().hasDisplayName() &&
            clicked.getItemMeta().getDisplayName().contains("Volver")) {
            MainMenu.openMenu(player);
            return;
        }

        // Manejar clicks según el tipo de menú
        switch (menuType) {
            case MAIN:
                handleMainMenuClick(clicked.getType(), player);
                break;
            case INVENTORY_EDITOR:
                event.setCancelled(false);
                break;
            case STATS:
                event.setCancelled(true);
                break;
            case TOP_ELO:
                event.setCancelled(true);
                break;
            case TOP_KDR:
                event.setCancelled(true);
                break;
            case TOP_KILLS:
                event.setCancelled(true);
                break;
            case TOP_PLAYTIME:
                event.setCancelled(true);
                break;
            case TOP_STREAK:
                event.setCancelled(true);
                break;
        }
    }

    private void handleMainMenuClick(Material type, Player player) {
        switch (type) {
            case SKULL_ITEM:
                StatsMenu.openMenu(player);
                break;
            case DIAMOND_SWORD:
                TopMenu.openMenu(player, MenuType.TOP_KILLS);
                break;
            case GOLDEN_APPLE:
                TopMenu.openMenu(player, MenuType.TOP_KDR);
                break;
            case BLAZE_POWDER:
                TopMenu.openMenu(player, MenuType.TOP_STREAK);
                break;
            case NETHER_STAR:
                TopMenu.openMenu(player, MenuType.TOP_ELO);
                break;
            case WATCH:
                TopMenu.openMenu(player, MenuType.TOP_PLAYTIME);
                break;
            case REDSTONE:
                InventoryEditorMenu.openMenu(player);
                break;
            default:
                break;
        }
        MenuManager.debugPlayerMenu(player); // Debug después del cambio
    }
}
