package kk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.menu.MainMenu;
import kk.kvlzx.menu.Menu;
import kk.kvlzx.menu.TopKillsMenu;
import kk.kvlzx.menu.TopKDRMenu;
import kk.kvlzx.menu.TopEloMenu;
import kk.kvlzx.menu.TopStreakMenu;
import kk.kvlzx.menu.TopTimeMenu;
import kk.kvlzx.menu.HotbarEditMenu;
import kk.kvlzx.menu.StatsMenu;
import kk.kvlzx.menu.ReportMenu;
import kk.kvlzx.menu.PlayerListMenu;

public class MenuManager {
    private final KvKnockback plugin;
    private final Map<UUID, Menu> playerMenus;
    private final Map<String, Menu> registeredMenus;

    public MenuManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.playerMenus = new HashMap<>();
        this.registeredMenus = new HashMap<>();
        registerDefaultMenus();
    }

    private void registerDefaultMenus() {
        registerMenu("main", new MainMenu(plugin));
        registerMenu("top_kills", new TopKillsMenu(plugin));
        registerMenu("top_elo", new TopEloMenu(plugin));
        registerMenu("top_streak", new TopStreakMenu(plugin));
        registerMenu("top_kdr", new TopKDRMenu(plugin));
        registerMenu("top_time", new TopTimeMenu(plugin));
        registerMenu("stats", new StatsMenu(plugin));
        registerMenu("hotbar_edit", new HotbarEditMenu(plugin));
        registerMenu("report", new ReportMenu(plugin, null));
        registerMenu("player_list", new PlayerListMenu(plugin));
        // Aquí registraremos los demás menús
    }

    public void registerMenu(String id, Menu menu) {
        registeredMenus.put(id.toLowerCase(), menu);
    }

    public Menu getMenu(String id) {
        return registeredMenus.get(id.toLowerCase());
    }

    public void openMenu(Player player, String menuId) {
        Menu menu = getMenu(menuId);
        if (menu != null) {
            Inventory inv = menu.getInventory(player);
            player.openInventory(inv);
            playerMenus.put(player.getUniqueId(), menu);
        }
    }

    public Menu getOpenMenu(Player player) {
        return playerMenus.get(player.getUniqueId());
    }

    public void closeMenu(Player player) {
        playerMenus.remove(player.getUniqueId());
    }
}
