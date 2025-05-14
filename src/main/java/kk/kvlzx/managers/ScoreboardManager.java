package kk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

public class ScoreboardManager {
    private final KvKnockback plugin;

    public ScoreboardManager(KvKnockback plugin) {
        this.plugin = plugin;
        startScoreboardUpdate();
    }

    private void startScoreboardUpdate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L); // Actualizar cada medio segundo
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("stats", "dummy");
        obj.setDisplayName(MessageUtils.getColor("&b&lKvKnockback"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        
        Score empty1 = obj.getScore("§1");
        empty1.setScore(7);
        
        Score kills = obj.getScore(MessageUtils.getColor("&fKills: &a" + stats.getKills()));
        kills.setScore(6);
        
        Score deaths = obj.getScore(MessageUtils.getColor("&fMuertes: &c" + stats.getDeaths()));
        deaths.setScore(5);
        
        Score empty2 = obj.getScore("§2");
        empty2.setScore(4);
        
        Score elo = obj.getScore(MessageUtils.getColor("&fELO: &6" + stats.getElo()));
        elo.setScore(3);
        
        Score empty3 = obj.getScore("§3");
        empty3.setScore(2);
        
        Score timeLeft = obj.getScore(MessageUtils.getColor("&fTiempo: &e5:00"));
        timeLeft.setScore(1);

        player.setScoreboard(board);
    }
}
