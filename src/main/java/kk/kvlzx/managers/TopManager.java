package kk.kvlzx.managers;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.items.CustomItem;
import kk.kvlzx.menus.MenuType;

public class TopManager {
    
    public static List<Map.Entry<UUID, Integer>> getTop(MenuType menuType, int limit) {
        if (!menuType.isTopMenu()) return new ArrayList<>();
        Map<UUID, Integer> values = new HashMap<>();
        
        for (UUID uuid : PlayerStats.getAllStats()) {
            PlayerStats stats = PlayerStats.getStats(uuid);
            switch (menuType) {
                case TOP_KILLS:
                    values.put(uuid, stats.getKills());
                    break;
                case TOP_KDR:
                    values.put(uuid, (int)(stats.getKDR() * 100));
                    break;
                case TOP_STREAK:
                    values.put(uuid, stats.getMaxStreak());
                    break;
                case TOP_ELO:
                    values.put(uuid, stats.getElo());
                    break;
                case TOP_PLAYTIME:
                    values.put(uuid, (int)(stats.getPlayTime() / 60)); // Convertir a horas para el ranking
                    break;
                default:
                    break;
            }
        }
        
        return values.entrySet()
                    .stream()
                    .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
    }

    public static ItemStack createTopSkull(int position, UUID uuid, int value, MenuType menuType) {
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        String posStr = getPositionString(position);
        String valueStr = getFormattedValue(value, menuType);
        String statName = getStatName(menuType);

        return CustomItem.createSkullFromUUID(uuid, 
            posStr + " &f" + playerName,
            "&7" + statName + ": &e" + valueStr);
    }

    private static String getStatName(MenuType menuType) {
        switch (menuType) {
            case TOP_KILLS: return "Kills";
            case TOP_KDR: return "KDR";
            case TOP_STREAK: return "Racha";
            case TOP_ELO: return "ELO";
            case TOP_PLAYTIME: return "Horas";
            default: return "";
        }
    }

    private static String getPositionString(int position) {
        switch (position) {
            case 1: return "&6[1°]";
            case 2: return "&7[2°]";
            case 3: return "&c[3°]";
            default: return "&7[" + position + "°]";
        }
    }

    private static String getFormattedValue(int value, MenuType menuType) {
        if (menuType == MenuType.TOP_KDR) {
            return String.format("%.2f", value / 100.0);
        } else if (menuType == MenuType.TOP_PLAYTIME) {
            return value + "h";
        }
        return String.valueOf(value);
    }
}
