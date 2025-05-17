package kk.kvlzx.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.KvKnockback;

public class RankManager {
    public enum Rank {
        DIVINO(30500, "&d&l[Divino] "),
        GRAN_MAESTRO(25500, "&4&l[Gran Maestro] "),
        DIOS(20500, "&b&l[Dios] "),
        TITAN(18500, "&3&l[Titán] "),
        INMORTAL(16500, "&5&l[Inmortal] "),
        SUPREMO(14500, "&c&l[Supremo] "),
        MITICO(12500, "&6&l[Mítico] "),
        LEYENDA(9500, "&e&l[Leyenda] "),
        HEROE(8500, "&a&l[Héroe] "),
        CAMPEON(7500, "&2&l[Campeón] "),
        MAESTRO(6500, "&9&l[Maestro] "),
        ELITE(5500, "&1&l[Élite] "),
        VETERANO(4500, "&8&l[Veterano] "),
        COMPETIDOR(3500, "&7&l[Competidor] "),
        APRENDIZ(2000, "&f&l[Aprendiz] "),
        NOVATO(1000, "&7[Novato] "),
        RANDOM(500, "&8[Random] ");

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

        public static Rank getRankByExactElo(int elo) {
            for (Rank rank : values()) {
                if (elo == rank.getMinElo()) {
                    return rank;
                }
            }
            return null; // Retorna null si no coincide con ningún umbral exacto
        }
    }

    public static void updatePlayerRank(Player player, int elo) {
        // Obtiene el rango actual del jugador desde config.yml
        String currentRankName = KvKnockback.getInstance().getConfig().getString("players." + player.getUniqueId() + ".rank", "RANDOM");
        Rank currentRank = Rank.valueOf(currentRankName);

        // Verifica si el Elo coincide exactamente con un umbral
        Rank newRank = Rank.getRankByExactElo(elo);

        // Si el Elo coincide con un umbral, actualiza el rango
        if (newRank != null) {
            currentRank = newRank;
            KvKnockback.getInstance().getConfig().set("players." + player.getUniqueId() + ".rank", currentRank.name());
            KvKnockback.getInstance().saveConfig();
        } else {
            // Si el Elo baja por debajo del umbral actual, regresa a RANDOM
            if (elo < currentRank.getMinElo() && currentRank != Rank.RANDOM) {
                currentRank = Rank.RANDOM;
                KvKnockback.getInstance().getConfig().set("players." + player.getUniqueId() + ".rank", "RANDOM");
                KvKnockback.getInstance().saveConfig();
            }
        }

        // Actualiza el displayName y playerListName
        final Rank finalCurrentRank = currentRank;
        new BukkitRunnable() {
            @Override
            public void run() {
                String displayName = MessageUtils.getColor(finalCurrentRank.getDisplayName() + "&r" + player.getName());
                player.setPlayerListName(displayName);
                player.setDisplayName(displayName);
                MessageUtils.sendMsg(player, "Tu rango ha sido actualizado a: " + finalCurrentRank.getDisplayName());
            }
        }.runTaskLater(KvKnockback.getInstance(), 2L);
    }

    public static String getRankPrefix(Player player) {
        String rankName = KvKnockback.getInstance().getConfig().getString("players." + player.getUniqueId() + ".rank", "RANDOM");
        Rank rank = Rank.valueOf(rankName);
        return MessageUtils.getColor(rank.getDisplayName());
    }
}
