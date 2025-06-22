package mk.kvlzx.utils;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.managers.RankManager;
import mk.kvlzx.stats.PlayerStats;

public class PlaceholdersUtils extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "kbffa";
    }

    @Override
    public String getAuthor() {
        return "Kvlzx & Gabo";
    }

    @Override
    public String getVersion() {
        return MysthicKnockBack.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        if (identifier.equalsIgnoreCase("rank")) {
            return RankManager.getRankPrefix(stats.getElo());
        } else if (identifier.equalsIgnoreCase("elo")) {
            return String.valueOf(stats.getElo());
        } else if (identifier.equalsIgnoreCase("kills")) {
            return String.valueOf(stats.getKills());
        } else if (identifier.equalsIgnoreCase("deaths")) {
            return String.valueOf(stats.getDeaths());
        } else if (identifier.equalsIgnoreCase("kdr")) {
            return String.valueOf(String.format("%.2f", stats.getKDR()));
        } else if (identifier.equalsIgnoreCase("streak")) {
            return String.valueOf(stats.getCurrentStreak());
        } else if (identifier.equalsIgnoreCase("max_streak")) {
            return String.valueOf(stats.getMaxStreak());
        } else if (identifier.equalsIgnoreCase("coins")) {
            return String.valueOf(stats.getKGCoins());
        } else if (identifier.equalsIgnoreCase("playtime")) {
            return String.valueOf(stats.getFormattedPlayTime());
        } else if (identifier.equalsIgnoreCase("current_arena")) {
            String arenaName = MysthicKnockBack.getInstance().getArenaManager().getCurrentArena();
            return arenaName != null ? arenaName : "None";
        } else if (identifier.equalsIgnoreCase("next_arena")) {
            String nextArenaName = MysthicKnockBack.getInstance().getArenaManager().getNextArena();
            return nextArenaName != null ? nextArenaName : "None";
        } else if (identifier.equalsIgnoreCase("current_zone")) {
            String zone = MysthicKnockBack.getInstance().getArenaManager().getPlayerZone(player);
            return zone != null ? zone : "None";
        }
        return null; // Si no se reconoce el placeholder, retorna null
    }
    
}
