package mk.kvlzx.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.MysthicKnockBack;

public class RankManager {
    public enum Rank {
        DIVINE(MysthicKnockBack.getInstance().getMainConfig().getRankDivinoElo(), MysthicKnockBack.getInstance().getMainConfig().getRankDivinoDisplay()),
        GRAND_MASTER(MysthicKnockBack.getInstance().getMainConfig().getGrandMasterElo(), MysthicKnockBack.getInstance().getMainConfig().getGrandMasterDisplay()),
        GOD(MysthicKnockBack.getInstance().getMainConfig().getGodElo(), MysthicKnockBack.getInstance().getMainConfig().getGodDisplay()),
        TITAN(MysthicKnockBack.getInstance().getMainConfig().getTitanElo(), MysthicKnockBack.getInstance().getMainConfig().getTitanDisplay()),
        IMMORTAL(MysthicKnockBack.getInstance().getMainConfig().getImmortalElo(), MysthicKnockBack.getInstance().getMainConfig().getImmortalDisplay()),
        SUPREME(MysthicKnockBack.getInstance().getMainConfig().getSupremeElo(), MysthicKnockBack.getInstance().getMainConfig().getSupremeDisplay()),
        MYTHIC(MysthicKnockBack.getInstance().getMainConfig().getMythicElo(), MysthicKnockBack.getInstance().getMainConfig().getMythicDisplay()),
        LEGEND(MysthicKnockBack.getInstance().getMainConfig().getLegendElo(), MysthicKnockBack.getInstance().getMainConfig().getLegendDisplay()),
        HERO(MysthicKnockBack.getInstance().getMainConfig().getHeroElo(), MysthicKnockBack.getInstance().getMainConfig().getHeroDisplay()),
        CHAMPION(MysthicKnockBack.getInstance().getMainConfig().getChampionElo(), MysthicKnockBack.getInstance().getMainConfig().getChampionDisplay()),
        MASTER(MysthicKnockBack.getInstance().getMainConfig().getMasterElo(), MysthicKnockBack.getInstance().getMainConfig().getMasterDisplay()),
        ELITE(MysthicKnockBack.getInstance().getMainConfig().getEliteElo(), MysthicKnockBack.getInstance().getMainConfig().getEliteDisplay()),
        VETERAN(MysthicKnockBack.getInstance().getMainConfig().getVeteranElo(), MysthicKnockBack.getInstance().getMainConfig().getVeteranDisplay()),
        COMPETITOR(MysthicKnockBack.getInstance().getMainConfig().getCompetitorElo(), MysthicKnockBack.getInstance().getMainConfig().getCompetitorDisplay()),
        APPRENTICE(MysthicKnockBack.getInstance().getMainConfig().getApprenticeElo(), MysthicKnockBack.getInstance().getMainConfig().getApprenticeDisplay()),
        NOVICE(MysthicKnockBack.getInstance().getMainConfig().getNoviceElo(), MysthicKnockBack.getInstance().getMainConfig().getNoviceDisplay()),
        RANDOM(MysthicKnockBack.getInstance().getMainConfig().getRandomElo(), MysthicKnockBack.getInstance().getMainConfig().getRandomDisplay());

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
