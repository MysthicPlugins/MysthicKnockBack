package mk.kvlzx.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.MysthicKnockBack;

public class RankManager {
    public enum Rank {
        DIVINO(30500, "&d&l[Divino]"),
        GRAN_MAESTRO(25500, "&4&l[Gran Maestro]"),
        DIOS(20500, "&b&l[Dios]"),
        TITAN(18500, "&3&l[Titán]"),
        INMORTAL(16500, "&5&l[Inmortal]"),
        SUPREMO(14500, "&c&l[Supremo]"),
        MITICO(12500, "&6&l[Mítico]"),
        LEYENDA(9500, "&e&l[Leyenda]"),
        HEROE(8500, "&a&l[Héroe]"),
        CAMPEON(7500, "&2&l[Campeón]"),
        MAESTRO(6500, "&9&l[Maestro]"),
        ELITE(5500, "&1&l[Élite]"),
        VETERANO(4500, "&8&l[Veterano]"),
        COMPETIDOR(3500, "&7&l[Competidor]"),
        APRENDIZ(2000, "&f&l[Aprendiz]"),
        NOVATO(1000, "&7[Novato]"),
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
        // Agregar un pequeño delay para asegurar que el jugador esté completamente spawneado
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
