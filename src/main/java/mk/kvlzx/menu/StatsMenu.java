package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.clip.placeholderapi.PlaceholderAPI;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.StatsMenuConfig;
import mk.kvlzx.managers.RankManager;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class StatsMenu extends Menu {
    private final StatsMenuConfig menuConfig;

    public StatsMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getStatsMenuConfig().getMenuTitle(), plugin.getStatsMenuConfig().getMenuSize());
        this.menuConfig = plugin.getStatsMenuConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Configurar elementos del menú
        setupMenuItems(player, inv, stats);
        
        // Llenar espacios vacíos con vidrio decorativo
        fillEmptySlots(inv);
    }

    private void setupMenuItems(Player player, Inventory inv, PlayerStats stats) {
        inv.setItem(menuConfig.getSkullSlot(),
            createConfiguredMenuItem(menuConfig.getSkullMaterial(), player, stats,
                menuConfig.getSkullName(), menuConfig.getSkullLore()));

        inv.setItem(menuConfig.getKillsSlot(),
            createConfiguredMenuItem(menuConfig.getKillsMaterial(), player, stats,
                menuConfig.getKillsName(), menuConfig.getKillsLore()));

        inv.setItem(menuConfig.getDeathsSlot(),
            createConfiguredMenuItem(menuConfig.getDeathsMaterial(), player, stats,
                menuConfig.getDeathsName(), menuConfig.getDeathsLore()));

        inv.setItem(menuConfig.getEloSlot(),
            createConfiguredMenuItem(menuConfig.getEloMaterial(), player, stats,
                menuConfig.getEloName(), menuConfig.getEloLore()));

        inv.setItem(menuConfig.getKgcoinsSlot(),
            createConfiguredMenuItem(menuConfig.getKgcoinsMaterial(), player, stats,
                menuConfig.getKgcoinsName(), menuConfig.getKgcoinsLore()));

        inv.setItem(menuConfig.getPlaytimeSlot(),
            createConfiguredMenuItem(menuConfig.getPlaytimeMaterial(), player, stats,
                menuConfig.getPlaytimeName(), menuConfig.getPlaytimeLore()));

        // Elemento de regreso
        inv.setItem(menuConfig.getBackSlot(),
            createConfiguredMenuItem(menuConfig.getBackMaterial(), player, stats,
                menuConfig.getBackName(), menuConfig.getBackLore()));
    }

    private ItemStack createConfiguredMenuItem(String material, Player player, PlayerStats stats,
                                                String name, List<String> lore) {
        String processedName = processText(player, stats, name);
        List<String> processedLore = processTextList(player, stats, lore);
        
        return menuConfig.createMenuItem(material, player, processedName, processedLore);
    }

    private void fillEmptySlots(Inventory inv) {
        // Crear elementos de relleno
        ItemStack darkGlass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 11);  // Azul oscuro
        ItemStack lightGlass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 3);  // Azul claro

        // Patrón alternado de relleno
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                boolean isDark = ((i / 9) + (i % 9)) % 2 == 0;
                inv.setItem(i, isDark ? darkGlass : lightGlass);
            }
        }
    }

    // ==================== MÉTODOS DE PROCESAMIENTO DE TEXTO ====================

    private String processText(Player player, PlayerStats stats, String text) {
        if (text == null) return null;
        
        String processedText = replaceNativePlaceholders(player, stats, text);
        return applyPlaceholderAPI(player, processedText);
    }

    private List<String> processTextList(Player player, PlayerStats stats, List<String> textList) {
        if (textList == null) return null;
        
        List<String> processedList = replaceNativePlaceholders(player, stats, textList);
        return applyPlaceholderAPI(player, processedList);
    }

    private String replaceNativePlaceholders(Player player, PlayerStats stats, String text) {
        if (text == null) return null;
        
        return text.replace("%player%", player.getName())
                    .replace("%kills%", String.valueOf(stats.getKills()))
                    .replace("%current_streak%", String.valueOf(stats.getCurrentStreak()))
                    .replace("%best_streak%", String.valueOf(stats.getMaxStreak()))
                    .replace("%deaths%", String.valueOf(stats.getDeaths()))
                    .replace("%kdr%", String.format("%.2f", stats.getKDR()))
                    .replace("%elo%", String.valueOf(stats.getElo()))
                    .replace("%rank%", RankManager.getRankPrefix(stats.getElo()))
                    .replace("%kgcoins%", String.valueOf(stats.getKGCoins()))
                    .replace("%playtime%", stats.getFormattedPlayTime());
    }

    private List<String> replaceNativePlaceholders(Player player, PlayerStats stats, List<String> lore) {
        if (lore == null) return null;
        
        List<String> processedLore = new ArrayList<>();
        for (String line : lore) {
            processedLore.add(replaceNativePlaceholders(player, stats, line));
        }
        return processedLore;
    }

    private String applyPlaceholderAPI(Player player, String text) {
        if (text == null) return null;
        
        // Verificar si PlaceholderAPI está habilitado
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    private List<String> applyPlaceholderAPI(Player player, List<String> lore) {
        if (lore == null) return null;
        
        List<String> processedLore = new ArrayList<>();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            for (String line : lore) {
                processedLore.add(PlaceholderAPI.setPlaceholders(player, line));
            }
        } else {
            processedLore.addAll(lore);
        }
        return processedLore;
    }

    // ==================== MANEJO DE EVENTOS ====================

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (event.getSlot() == menuConfig.getBackSlot()) {
            Player player = (Player) event.getWhoClicked();
            plugin.getMenuManager().openMenu(player, "main");
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private ItemStack createItem(Material material, String name, byte data, String... lore) {
        ItemStack item = new ItemStack(material, 1, data);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        
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
