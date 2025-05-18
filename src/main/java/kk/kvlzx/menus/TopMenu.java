package kk.kvlzx.menus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.ToDoubleFunction;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

public class TopMenu {

    private static class PlayerTopData {
        final UUID uuid;
        final String name;
        final double value;

        PlayerTopData(UUID uuid, String name, double value) {
            this.uuid = uuid;
            this.name = name;
            this.value = value;
        }
    }

    public static void openKillsMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, MessageUtils.getColor("&8• &b⚔ Top Kills &8•"));
        List<PlayerTopData> top = getTopPlayers(data -> data.getKills());
        fillTopMenu(menu, top, "&b⚔ Kills: &f", false);
        player.openInventory(menu);
    }

    public static void openKDRMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, MessageUtils.getColor("&8• &6☠ Top KDR &8•"));
        List<PlayerTopData> top = getTopPlayers(data -> data.getKDR());
        fillTopMenu(menu, top, "&6☠ KDR: &f", false);
        player.openInventory(menu);
    }

    public static void openStreaksMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, MessageUtils.getColor("&8• &c⚡ Top Rachas &8•"));
        List<PlayerTopData> top = getTopPlayers(data -> data.getMaxKillstreak());
        fillTopMenu(menu, top, "&c⚡ Racha: &f", false);
        player.openInventory(menu);
    }

    public static void openELOMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, MessageUtils.getColor("&8• &e✦ Top ELO &8•"));
        List<PlayerTopData> top = getTopPlayers(data -> data.getElo());
        fillTopMenu(menu, top, "&e✦ ELO: &f", false);
        player.openInventory(menu);
    }

    public static void openPlayTimeMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, MessageUtils.getColor("&8• &d⌚ Top Tiempo Jugado &8•"));
        List<PlayerTopData> top = getTopPlayers(PlayerStats::getPlayTimeHours);
        fillTopMenu(menu, top, "&d⌚ Tiempo: &f", true);
        player.openInventory(menu);
    }

    private static List<PlayerTopData> getTopPlayers(ToDoubleFunction<PlayerStats> valueExtractor) {
        List<PlayerTopData> topPlayers = new ArrayList<>();
        
        for (UUID uuid : PlayerStats.getAllStats()) {
            PlayerStats stats = PlayerStats.getStats(uuid);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            topPlayers.add(new PlayerTopData(uuid, offlinePlayer.getName(), valueExtractor.applyAsDouble(stats)));
        }

        topPlayers.sort(Comparator.comparingDouble(data -> -data.value)); // Orden descendente
        return topPlayers.subList(0, Math.min(10, topPlayers.size()));
    }

    private static void fillTopMenu(Inventory menu, List<PlayerTopData> topPlayers, String statPrefix, boolean isPlayTime) {
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21}; // Posiciones de los items

        for (int i = 0; i < Math.min(topPlayers.size(), slots.length); i++) {
            PlayerTopData data = topPlayers.get(i);
            ItemStack skull = createPlayerHead(data, i + 1, statPrefix, isPlayTime);
            menu.setItem(slots[i], skull);
        }

        // Decoración del menú
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte)7);
        for (int i = 0; i < menu.getSize(); i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, glass);
            }
        }
    }

    private static ItemStack createPlayerHead(PlayerTopData data, int position, String statPrefix, boolean isPlayTime) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        meta.setOwner(data.name);
        meta.setDisplayName(MessageUtils.getColor(getPositionPrefix(position) + " &f" + data.name));
        
        List<String> lore = new ArrayList<>();
        if (isPlayTime) {
            long hours = (long) data.value;
            long minutes = (long) ((data.value - hours) * 60);
            lore.add(MessageUtils.getColor(statPrefix + hours + "h " + minutes + "m"));
        } else if (statPrefix.contains("KDR")) {
            // Solo KDR tiene 2 decimales
            lore.add(MessageUtils.getColor(statPrefix + String.format("%.2f", data.value)));
        } else {
            // El resto de stats se muestran como enteros
            lore.add(MessageUtils.getColor(statPrefix + String.format("%.0f", data.value)));
        }
        lore.add(MessageUtils.getColor("&7Posición: #" + position));
        meta.setLore(lore);
        
        head.setItemMeta(meta);
        return head;
    }

    private static String getPositionPrefix(int position) {
        switch (position) {
            case 1: return "&e&l1er";
            case 2: return "&7&l2do";
            case 3: return "&6&l3er";
            default: return "&f" + position + "°";
        }
    }
}
