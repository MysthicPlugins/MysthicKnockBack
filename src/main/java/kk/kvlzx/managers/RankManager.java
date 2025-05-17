package kk.kvlzx.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.KvKnockback;

public class RankManager {
    public enum Rank {
        DIVINO(30500, "&d&lDivino"),
        GRAN_MAESTRO(25500, "&4&lGran Maestro"),
        DIOS(20500, "&b&lDios"),
        TITAN(18500, "&3&lTitán"),
        INMORTAL(16500, "&5&lInmortal"),
        SUPREMO(14500, "&c&lSupremo"),
        MITICO(12500, "&6&lMítico"),
        LEYENDA(9500, "&e&lLeyenda"),
        HEROE(8500, "&a&lHéroe"),
        CAMPEON(7500, "&2&lCampeón"),
        MAESTRO(6500, "&9&lMaestro"),
        ELITE(5500, "&1&lÉlite"),
        VETERANO(4500, "&8&lVeterano"),
        COMPETIDOR(3500, "&7&lCompetidor"),
        APRENDIZ(2000, "&f&lAprendiz"),
        NOVATO(1000, "&7Novato"),
        RANDOM(500, "&8Random");

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
        }.runTaskLater(KvKnockback.getInstance(), 2L);
    }

    public static String getRankPrefix(int elo) {
        return MessageUtils.getColor(Rank.getRankByElo(elo).getDisplayName());
    }
}
