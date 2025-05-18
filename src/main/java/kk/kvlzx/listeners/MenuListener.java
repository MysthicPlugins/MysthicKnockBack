package kk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.items.ItemsManager;
import kk.kvlzx.menus.MainMenu;
import kk.kvlzx.menus.TopMenu;
import kk.kvlzx.stats.PlayerStats;
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
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        String title = event.getInventory().getTitle();
        if (title.contains("Menú Principal")) {
            event.setCancelled(true);

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
                    showPlayerStats(player);
                    player.closeInventory();
                    break;
                case CHEST:
                    plugin.getInventoryManager().openEditor(player);
                    break;
                default:
                    break;
            }
        }
    }

    private void showPlayerStats(Player player) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        player.sendMessage(MessageUtils.getColor("&a=== Tus Estadísticas ==="));
        player.sendMessage(MessageUtils.getColor("&fKills: &a" + stats.getKills()));
        player.sendMessage(MessageUtils.getColor("&fMuertes: &c" + stats.getDeaths()));
        player.sendMessage(MessageUtils.getColor("&fELO: &6" + stats.getElo()));
        player.sendMessage(MessageUtils.getColor("&fKDR: &b" + String.format("%.2f", stats.getKDR())));
    }
}
