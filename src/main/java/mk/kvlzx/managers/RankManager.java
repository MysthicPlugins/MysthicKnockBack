package mk.kvlzx.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.MysthicKnockBack;

public class RankManager {
    public enum Rank {
        DIVINE(30500, "&d&l[Divine]"),
        GRAND_MASTER(25500, "&4&l[Grand Master]"),
        GOD(20500, "&b&l[God]"),
        TITAN(18500, "&3&l[Titan]"),
        IMMORTAL(16500, "&5&l[Immortal]"),
        SUPREME(14500, "&c&l[Supreme]"),
        MYTHIC(12500, "&6&l[Mythic]"),
        LEGEND(9500, "&e&l[Legend]"),
        HERO(8500, "&a&l[Hero]"),
        CHAMPION(7500, "&2&l[Champion]"),
        MASTER(6500, "&9&l[Master]"),
        ELITE(5500, "&1&l[Elite]"),
        VETERAN(4500, "&8&l[Veteran]"),
        COMPETITOR(3500, "&7&l[Competitor]"),
        APPRENTICE(2000, "&f&l[Apprentice]"),
        NOVICE(1000, "&7[Novice]"),
        RANDOM(500, "&8[Random]");

        private final int minElo;
        private final String displayName;

        Rank(int minElo, String displayName) {
            this.minElo = minElo;
            this.displayName = displayName;
        }

        public int getMinElo() {
            return minElo;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Rank getRankByElo(int elo) {
            for (Rank rank : values()) {
                if (elo >= rank.getMinElo()) {
                    return rank;
                }
            }
            return RANDOM;
        }
    }

    public static void updatePlayerRank(Player player, int elo) {
        // Agregar un pequeño delay para asegurar que el jugador esté completamente cargado
        new BukkitRunnable() {
            @Override
            public void run() {
                Rank rank = Rank.getRankByElo(elo);
                String displayName = MessageUtils.getColor(rank.getDisplayName() + " &r" + player.getName());
                player.setPlayerListName(displayName);
                player.setDisplayName(displayName);
            }
        }.runTaskLater(MysthicKnockBack.getInstance(), 2L);
    }

    public static String getRankPrefix(int elo) {
        return MessageUtils.getColor(Rank.getRankByElo(elo).getDisplayName());
    }
}
