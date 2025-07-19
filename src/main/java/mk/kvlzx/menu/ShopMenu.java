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
import mk.kvlzx.config.ShopMenuConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class ShopMenu extends Menu {
    private final ShopMenuConfig menuConfig;

    public ShopMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getShopMenuConfig().getMenuTitle(), plugin.getShopMenuConfig().getMenuSize());
        this.menuConfig = plugin.getShopMenuConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Mostrar balance
        ItemStack balance = menuConfig.createMenuItem("balance");
        if (balance != null) {
            ItemMeta meta = balance.getItemMeta();
            List<String> lore = new ArrayList<>();
            for (String line : menuConfig.getBalanceLore()) {
                lore.add(MessageUtils.getColor(line.replace("%coins%", String.valueOf(stats.getKGCoins()))));
            }
            meta.setLore(lore);
            balance.setItemMeta(meta);
            inv.setItem(menuConfig.getBalanceSlot(), balance);
        }

        // Configurar items de la tienda
        inv.setItem(menuConfig.getBlocksSlot(), menuConfig.createMenuItem("blocks"));
        inv.setItem(menuConfig.getKnockersSlot(), menuConfig.createMenuItem("knockers"));
        inv.setItem(menuConfig.getKillMessagesSlot(), menuConfig.createMenuItem("kill-messages"));
        inv.setItem(menuConfig.getDeathMessagesSlot(), menuConfig.createMenuItem("death-messages"));
        inv.setItem(menuConfig.getArrowEffectsSlot(), menuConfig.createMenuItem("arrow-effects"));
        inv.setItem(menuConfig.getDeathSoundsSlot(), menuConfig.createMenuItem("death-sounds"));
        inv.setItem(menuConfig.getKillSoundsSlot(), menuConfig.createMenuItem("kill-sounds"));
        inv.setItem(menuConfig.getJoinMessagesSlot(), menuConfig.createMenuItem("join-messages"));
        inv.setItem(menuConfig.getMusicSlot(), menuConfig.createMenuItem("music"));
        inv.setItem(menuConfig.getBackSlot(), menuConfig.createMenuItem("back"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Validar que el click sea en el menú y no en el inventario del jugador
        if (!isValidClick(event)) {
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        
        int slot = event.getSlot();
        
        if (slot == menuConfig.getBlocksSlot()) {
            plugin.getMenuManager().openMenu(player, "block_categories");
        } else if (slot == menuConfig.getKnockersSlot()) {
            plugin.getMenuManager().openMenu(player, "knocker_categories");
        } else if (slot == menuConfig.getKillMessagesSlot()) {
            plugin.getMenuManager().openMenu(player, "kill_message_categories");
        } else if (slot == menuConfig.getDeathMessagesSlot()) {
            plugin.getMenuManager().openMenu(player, "death_message_categories");
        } else if (slot == menuConfig.getArrowEffectsSlot()) {
            plugin.getMenuManager().openMenu(player, "arrow_effect_categories");
        } else if (slot == menuConfig.getDeathSoundsSlot()) {
            plugin.getMenuManager().openMenu(player, "death_sound_categories");
        } else if (slot == menuConfig.getKillSoundsSlot()) {
            plugin.getMenuManager().openMenu(player, "kill_sound_categories");
        } else if (slot == menuConfig.getJoinMessagesSlot()) {
            plugin.getMenuManager().openMenu(player, "join_message_categories");
        } else if (slot == menuConfig.getMusicSlot()) {
            plugin.getMenuManager().openMenu(player, "music_categories");
        } else if (slot == menuConfig.getBackSlot()) {
            plugin.getMenuManager().openMenu(player, "main");
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
