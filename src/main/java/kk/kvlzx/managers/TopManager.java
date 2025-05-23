package kk.kvlzx.managers;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.menus.TopType;
import kk.kvlzx.items.CustomItem;

public class TopManager {
    
    public static List<Map.Entry<UUID, Integer>> getTop(TopType type, int limit) {
        Map<UUID, Integer> values = new HashMap<>();
        
        for (UUID uuid : PlayerStats.getAllStats()) {
            PlayerStats stats = PlayerStats.getStats(uuid);
            switch (type) {
                case KILLS:
                    values.put(uuid, stats.getKills());
                    break;
                case KDR:
                    values.put(uuid, (int)(stats.getKDR() * 100)); // Multiplicamos por 100 para ordenar
                    break;
                case STREAK:
                    values.put(uuid, stats.getMaxKillstreak());
                    break;
                case ELO:
                    values.put(uuid, stats.getElo());
                    break;
                case PLAYTIME:
                    values.put(uuid, (int)stats.getPlayTimeHours());
                    break;
            }
        }
        
        return values.entrySet()
                    .stream()
                    .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
    }

    public static ItemStack createTopSkull(int position, UUID uuid, int value, TopType type) {
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        String posStr;
        switch (position) {
            case 1:
                posStr = "&6[1°]";
                break;
            case 2:
                posStr = "&7[2°]";
                break;
            case 3:
                posStr = "&c[3°]";
                break;
            default:
                posStr = "&7[" + position + "°]";
                break;
        }
        
        String valueStr;
        switch (type) {
            case KDR:
                valueStr = String.format("%.2f", value / 100.0);
                break;
            case PLAYTIME:
                valueStr = value + "h";
                break;
            default:
                valueStr = String.valueOf(value);
                break;
        }

        return CustomItem.createSkullFromUUID(uuid, 
            posStr + " &f" + playerName,
            "&7" + type.getTitle() + ": &e" + valueStr);
    }
}
