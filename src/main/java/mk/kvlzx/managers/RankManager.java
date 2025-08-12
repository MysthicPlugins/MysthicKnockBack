package mk.kvlzx.managers;

import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.MysthicKnockBack;

public class RankManager {
    public enum Rank {
        DIVINE(MysthicKnockBack.getInstance().getCombatConfig().getDivineElo(), MysthicKnockBack.getInstance().getCombatConfig().getDivineDisplay()),
        GRAND_MASTER(MysthicKnockBack.getInstance().getCombatConfig().getGrandMasterElo(), MysthicKnockBack.getInstance().getCombatConfig().getGrandMasterDisplay()),
        GOD(MysthicKnockBack.getInstance().getCombatConfig().getGodElo(), MysthicKnockBack.getInstance().getCombatConfig().getGodDisplay()),
        TITAN(MysthicKnockBack.getInstance().getCombatConfig().getTitanElo(), MysthicKnockBack.getInstance().getCombatConfig().getTitanDisplay()),
        IMMORTAL(MysthicKnockBack.getInstance().getCombatConfig().getImmortalElo(), MysthicKnockBack.getInstance().getCombatConfig().getImmortalDisplay()),
        SUPREME(MysthicKnockBack.getInstance().getCombatConfig().getSupremeElo(), MysthicKnockBack.getInstance().getCombatConfig().getSupremeDisplay()),
        MYTHIC(MysthicKnockBack.getInstance().getCombatConfig().getMythicElo(), MysthicKnockBack.getInstance().getCombatConfig().getMythicDisplay()),
        LEGEND(MysthicKnockBack.getInstance().getCombatConfig().getLegendElo(), MysthicKnockBack.getInstance().getCombatConfig().getLegendDisplay()),
        HERO(MysthicKnockBack.getInstance().getCombatConfig().getHeroElo(), MysthicKnockBack.getInstance().getCombatConfig().getHeroDisplay()),
        CHAMPION(MysthicKnockBack.getInstance().getCombatConfig().getChampionElo(), MysthicKnockBack.getInstance().getCombatConfig().getChampionDisplay()),
        MASTER(MysthicKnockBack.getInstance().getCombatConfig().getMasterElo(), MysthicKnockBack.getInstance().getCombatConfig().getMasterDisplay()),
        ELITE(MysthicKnockBack.getInstance().getCombatConfig().getEliteElo(), MysthicKnockBack.getInstance().getCombatConfig().getEliteDisplay()),
        VETERAN(MysthicKnockBack.getInstance().getCombatConfig().getVeteranElo(), MysthicKnockBack.getInstance().getCombatConfig().getVeteranDisplay()),
        COMPETITOR(MysthicKnockBack.getInstance().getCombatConfig().getCompetitorElo(), MysthicKnockBack.getInstance().getCombatConfig().getCompetitorDisplay()),
        APPRENTICE(MysthicKnockBack.getInstance().getCombatConfig().getApprenticeElo(), MysthicKnockBack.getInstance().getCombatConfig().getApprenticeDisplay()),
        NOVICE(MysthicKnockBack.getInstance().getCombatConfig().getNoviceElo(), MysthicKnockBack.getInstance().getCombatConfig().getNoviceDisplay()),
        RANDOM(MysthicKnockBack.getInstance().getCombatConfig().getRandomElo(), MysthicKnockBack.getInstance().getCombatConfig().getRandomDisplay());

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
            // Crear una lista de rangos ordenada por ELO de mayor a menor
            Rank[] ranks = {DIVINE, GRAND_MASTER, GOD, TITAN, IMMORTAL, SUPREME, 
                            MYTHIC, LEGEND, HERO, CHAMPION, MASTER, ELITE, 
                            VETERAN, COMPETITOR, APPRENTICE, NOVICE, RANDOM};
            
            // Buscar el rango apropiado
            for (Rank rank : ranks) {
                if (elo >= rank.getMinElo()) {
                    return rank;
                }
            }
            
            // Si no se encuentra ningún rango, devolver RANDOM como fallback
            return RANDOM;
        }
    }

    public static String getRankPrefix(int elo) {
        try {
            Rank rank = Rank.getRankByElo(elo);
            if (rank != null) {
                String displayName = rank.getDisplayName();
                // Validar que displayName no sea null antes de procesarlo
                if (displayName != null && !displayName.trim().isEmpty()) {
                    return MessageUtils.getColor(displayName);
                } else {
                    return MessageUtils.getColor("&7[Unknown]");
                }
            } else {
                return MessageUtils.getColor("&7[Unknown]");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "&7[Error]"; // Return sin procesar por MessageUtils en caso de error crítico
        }
    }
}