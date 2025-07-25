package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.menu.MainMenu;
import mk.kvlzx.menu.Menu;
import mk.kvlzx.menu.TopKillsMenu;
import mk.kvlzx.menu.TopKDRMenu;
import mk.kvlzx.menu.TopEloMenu;
import mk.kvlzx.menu.TopStreakMenu;
import mk.kvlzx.menu.TopTimeMenu;
import mk.kvlzx.menu.HotbarEditMenu;
import mk.kvlzx.menu.JoinMessageCategoriesMenu;
import mk.kvlzx.menu.JoinMessageShopMenu;
import mk.kvlzx.menu.StatsMenu;
import mk.kvlzx.menu.PlayerListMenu;
import mk.kvlzx.menu.ReportReasonMenu;
import mk.kvlzx.menu.ShopMenu;
import mk.kvlzx.menu.BlockShopMenu;
import mk.kvlzx.menu.KnockerShopMenu;
import mk.kvlzx.menu.DeathMessageCategoriesMenu;
import mk.kvlzx.menu.DeathMessageShopMenu;
import mk.kvlzx.menu.KillMessageCategoriesMenu;
import mk.kvlzx.menu.KillMessageShopMenu;
import mk.kvlzx.menu.ArrowEffectCategoriesMenu;
import mk.kvlzx.menu.ArrowEffectShopMenu;
import mk.kvlzx.menu.MusicCategoriesMenu;
import mk.kvlzx.menu.MusicShopMenu;
import mk.kvlzx.menu.DeathSoundCategoriesMenu;
import mk.kvlzx.menu.DeathSoundShopMenu;
import mk.kvlzx.menu.KillSoundCategoriesMenu;
import mk.kvlzx.menu.KillSoundShopMenu;

public class MenuManager {
    private final MysthicKnockBack plugin;
    private final Map<UUID, Menu> playerMenus;
    private final Map<String, Menu> registeredMenus;

    public MenuManager(MysthicKnockBack plugin) {
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
        registerMenu("player_list", new PlayerListMenu(plugin));
        registerMenu("report_reason", new ReportReasonMenu(plugin));
        registerMenu("shop", new ShopMenu(plugin));
        registerMenu("block_shop", new BlockShopMenu(plugin));
        registerMenu("knocker_shop", new KnockerShopMenu(plugin));
        registerMenu("death_message_categories", new DeathMessageCategoriesMenu(plugin));
        registerMenu("death_message_shop", new DeathMessageShopMenu(plugin));
        registerMenu("kill_message_categories", new KillMessageCategoriesMenu(plugin));
        registerMenu("kill_message_shop", new KillMessageShopMenu(plugin));
        registerMenu("arrow_effect_categories", new ArrowEffectCategoriesMenu(plugin));
        registerMenu("arrow_effect_shop", new ArrowEffectShopMenu(plugin));
        registerMenu("death_sound_categories", new DeathSoundCategoriesMenu(plugin));
        registerMenu("death_sound_shop", new DeathSoundShopMenu(plugin));
        registerMenu("kill_sound_categories", new KillSoundCategoriesMenu(plugin));
        registerMenu("kill_sound_shop", new KillSoundShopMenu(plugin));
        registerMenu("music_categories", new MusicCategoriesMenu(plugin));
        registerMenu("music_shop", new MusicShopMenu(plugin));
        registerMenu("join_message_categories", new JoinMessageCategoriesMenu(plugin));
        registerMenu("join_message_shop", new JoinMessageShopMenu(plugin));
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
