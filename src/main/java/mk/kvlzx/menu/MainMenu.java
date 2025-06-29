package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.MainMenuConfig;
import mk.kvlzx.utils.MessageUtils;

public class MainMenu extends Menu {
    private final MainMenuConfig menuConfig;

    public MainMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getMainMenuConfig().getMenuTitle(), plugin.getMainMenuConfig().getMenuSize());
        this.menuConfig = plugin.getMainMenuConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Crear los items usando la configuracion
        inv.setItem(menuConfig.getMenuTopKillsSlot(), 
            menuConfig.createMenuItem(menuConfig.getMenuTopKillsId(), player, 
                menuConfig.getMenuTopKillsName(), menuConfig.getMenuTopKillsLore()));
        
        inv.setItem(menuConfig.getMenuTopEloSlot(), 
            menuConfig.createMenuItem(menuConfig.getMenuTopEloId(), player, 
                menuConfig.getMenuTopEloName(), menuConfig.getMenuTopEloLore()));
        
        inv.setItem(menuConfig.getMenuTopStreaksSlot(), 
            menuConfig.createMenuItem(menuConfig.getMenuTopStreaksId(), player, 
                menuConfig.getMenuTopStreaksName(), menuConfig.getMenuTopStreaksLore()));

        inv.setItem(menuConfig.getMenuTopKdrSlot(), 
            menuConfig.createMenuItem(menuConfig.getMenuTopKdrId(), player, 
                menuConfig.getMenuTopKdrName(), menuConfig.getMenuTopKdrLore()));
            
        inv.setItem(menuConfig.getMenuTopTimeSlot(), 
            menuConfig.createMenuItem(menuConfig.getMenuTopTimeId(), player, 
                menuConfig.getMenuTopTimeName(), menuConfig.getMenuTopTimeLore()));

        inv.setItem(menuConfig.getMenuEditHotbarSlot(), 
            menuConfig.createMenuItem(menuConfig.getMenuEditHotbarId(), player, 
                menuConfig.getMenuEditHotbarName(), menuConfig.getMenuEditHotbarLore()));

        // Manejo especial para la cabeza del jugador
        inv.setItem(menuConfig.getMenuMyStatsSlot(), 
        menuConfig.createMenuItem(menuConfig.getMenuMyStatsId(), player, 
            menuConfig.getMenuMyStatsName(), menuConfig.getMenuMyStatsLore()));

        inv.setItem(menuConfig.getMenuReportPlayerSlot(), 
            menuConfig.createMenuItem(menuConfig.getMenuReportPlayerId(), player, 
                menuConfig.getMenuReportPlayerName(), menuConfig.getMenuReportPlayerLore()));

        inv.setItem(menuConfig.getMenuShopSlot(), 
            menuConfig.createMenuItem(menuConfig.getMenuShopId(), player, 
                menuConfig.getMenuShopName(), menuConfig.getMenuShopLore()));

        // Relleno con diseño
        ItemStack darkGlass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15);
        ItemStack lightGlass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7);
        
        // Patrón de relleno alternado
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, (i % 2 == 0) ? darkGlass : lightGlass);
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!isValidClick(event)) {
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        int slot = event.getSlot();
        
        if (slot == menuConfig.getMenuTopKillsSlot()) {
            plugin.getMenuManager().openMenu(player, "top_kills");
        } else if (slot == menuConfig.getMenuTopEloSlot()) {
            plugin.getMenuManager().openMenu(player, "top_elo");
        } else if (slot == menuConfig.getMenuTopStreaksSlot()) {
            plugin.getMenuManager().openMenu(player, "top_streak");
        } else if (slot == menuConfig.getMenuTopKdrSlot()) {
            plugin.getMenuManager().openMenu(player, "top_kdr");
        } else if (slot == menuConfig.getMenuTopTimeSlot()) {
            plugin.getMenuManager().openMenu(player, "top_time");
        } else if (slot == menuConfig.getMenuEditHotbarSlot()) {
            plugin.getMenuManager().openMenu(player, "hotbar_edit");
        } else if (slot == menuConfig.getMenuMyStatsSlot()) {
            plugin.getMenuManager().openMenu(player, "stats");
        } else if (slot == menuConfig.getMenuReportPlayerSlot()) {
            plugin.getMenuManager().openMenu(player, "player_list");
        } else if (slot == menuConfig.getMenuShopSlot()) {
            plugin.getMenuManager().openMenu(player, "shop");
        }
    }

    private ItemStack createItem(Material material, String name, byte data, String... lore) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        if (lore.length > 0) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(MessageUtils.getColor(line));
            }
            meta.setLore(coloredLore);
        }
        item.setItemMeta(meta);
        return item;
    }
}