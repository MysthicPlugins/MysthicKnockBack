package mk.kvlzx.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.MysthicKnockBack;

public class RankManager {
    public enum Rank {
        DIVINE(MysthicKnockBack.getInstance().getMainConfig().getDivineElo(), MysthicKnockBack.getInstance().getMainConfig().getDivineDisplay()),
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

    public static void updatePlayerRank(Player player, int elo) {
        if (player == null) return;
        
        // Agregar un pequeño delay para asegurar que el jugador esté completamente cargado
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Rank rank = Rank.getRankByElo(elo);
                    if (rank != null) {
                        String displayName = rank.getDisplayName();
                        // Validar que displayName no sea null
                        if (displayName != null && !displayName.trim().isEmpty()) {
                            String formattedDisplayName = MessageUtils.getColor(displayName + " &r" + player.getName());
                            player.setPlayerListName(formattedDisplayName);
                            player.setDisplayName(formattedDisplayName);
                        } else {
                            // Fallback si displayName es null o vacío
                            String fallbackDisplayName = MessageUtils.getColor("&7[Unknown] &r" + player.getName());
                            player.setPlayerListName(fallbackDisplayName);
                            player.setDisplayName(fallbackDisplayName);
                        }
                    }
                } catch (Exception e) {
                    // En caso de error, asignar un nombre por defecto
                    try {
                        String fallbackDisplayName = MessageUtils.getColor("&7[Error] &r" + player.getName());
                        player.setPlayerListName(fallbackDisplayName);
                        player.setDisplayName(fallbackDisplayName);
                    } catch (Exception ignored) {
                        // Si incluso el fallback falla, no hacer nada
                    }
                    
                    MysthicKnockBack.getInstance().getLogger().severe(
                        "Error updating player rank for " + player.getName() + " (ELO: " + elo + "): " + e.getMessage()
                    );
                    e.printStackTrace();
                }
            }
        }.runTaskLater(MysthicKnockBack.getInstance(), 2L);
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