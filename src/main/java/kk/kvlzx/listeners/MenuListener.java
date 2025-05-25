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
import kk.kvlzx.menus.TopType;
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

        if (MenuManager.isPluginInventory(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        
        MenuType menuType = MenuManager.getMenuType(event.getInventory());
        if (menuType == MenuType.INVENTORY_EDITOR) {
            ItemStack[] newLayout = new ItemStack[9];
            Inventory inv = event.getInventory();
            for (int i = 0; i < 9; i++) {
                newLayout[i] = inv.getItem(i);
            }
            ItemsManager.savePvPLayout(newLayout);
            player.sendMessage(MessageUtils.getColor("&aInventorio guardado correctamente!"));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!MenuManager.isPluginInventory(event.getInventory())) return;

        // Prevenir clicks en el inventario del jugador
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }

        // Cancelar todo tipo de clicks
        event.setCancelled(true);

        // Si no hay item clickeado, retornar
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        MenuType menuType = MenuManager.getMenuType(event.getInventory());
        if (menuType == null) return;

        // Manejar el botón de retorno para todos los menús
        if (clicked.getType() == Material.ARROW && 
            clicked.hasItemMeta() && 
            clicked.getItemMeta().hasDisplayName() &&
            clicked.getItemMeta().getDisplayName().contains("Volver")) {
            MainMenu.openMenu(player);
            return;
        }

        // Solo procesar clicks en el menú principal
        if (menuType == MenuType.MAIN_MENU) {
            handleMenuInteraction(clicked.getType(), player);
        } else if (menuType == MenuType.INVENTORY_EDITOR) {
            handleInventoryEditorClick(event);
        }
        // Los menús de top son solo visuales, no necesitan manejo de clicks
    }

    private void handleMenuInteraction(Material type, Player player) {
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
            case REDSTONE:
                InventoryEditorMenu.openMenu(player);
                break;
            default:
                break;
        }
    }

    private void handleInventoryEditorClick(InventoryClickEvent event) {
        event.setCancelled(false); // Permitir todos los clicks en el editor
    }
}
