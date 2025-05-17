package kk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.arena.Arena;
import kk.kvlzx.arena.ArenaManager;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.stats.Streak;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.utils.TitleUtils;

import java.util.Arrays;

public class ScoreboardManager {
    private final KvKnockback plugin;
    private int timeLeft = 120; // 2 minutos en segundos
    private final int ARENA_TIME = 120; // 2 minutos en segundos
    private final int[] COUNTDOWN_ALERTS = {60, 30, 10, 5, 4, 3, 2, 1}; // Alertas en segundos

    public ScoreboardManager(KvKnockback plugin) {
        this.plugin = plugin;
        startScoreboardUpdate();
        startArenaRotation();
    }

    private void startScoreboardUpdate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Actualizar cada segundo
    }

    private void startArenaRotation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                timeLeft--;
                
                // Alertas de tiempo solo en chat
                if (Arrays.stream(COUNTDOWN_ALERTS).anyMatch(t -> t == timeLeft)) {
                    Bukkit.broadcastMessage(MessageUtils.getColor("&a¡La arena cambiará en &c" + timeLeft + " &asegundos!"));
                }

                if (timeLeft <= 0) {
                    rotateArena();
                    timeLeft = ARENA_TIME;
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void rotateArena() {
        ArenaManager arenaManager = plugin.getArenaManager();
        String currentArena = arenaManager.getCurrentArena();
        String nextArena = arenaManager.getNextArena();
        
        if (nextArena == null || currentArena == null) return;

        Arena nextArenaObj = arenaManager.getArena(nextArena);
        Location nextSpawn = nextArenaObj.getSpawnLocation();
        
        if (nextSpawn == null) {
            Bukkit.broadcastMessage(MessageUtils.getColor("&cError: La arena " + nextArena + " no tiene un punto de spawn configurado."));
            return;
        }

        // Congelar y preparar a todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setWalkSpeed(0.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 128, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false));
            player.playSound(player.getLocation(), Sound.PORTAL_TRIGGER, 1.0f, 1.0f);
        }

        // Secuencia de animación
        new BukkitRunnable() {
            int step = 0;
            String[] loadingFrames = {"▌", "▌▌", "▌▌▌", "▌▌▌▌", "▌▌▌▌▌"};
            String[] loadingColors = {"&c", "&6", "&e", "&a", "&2"};
            
            @Override
            public void run() {
                if (step >= loadingFrames.length + 2) {
                    this.cancel();
                    teleportPlayers(currentArena, nextArena, nextSpawn);
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (step < loadingFrames.length) {
                        TitleUtils.sendTitle(
                            player,
                            "&b&lCambiando de Arena",
                            loadingColors[step] + loadingFrames[step] + " &7" + (step * 20 + 20) + "%"
                        );
                        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                    }
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void teleportPlayers(String currentArena, String nextArena, Location nextSpawn) {
        // Mensaje de transición
        Bukkit.broadcastMessage(MessageUtils.getColor("&b&l=-=-=-=-=-=-=-=-=-="));
        Bukkit.broadcastMessage(MessageUtils.getColor("&e¡Teletransportando jugadores!"));
        Bukkit.broadcastMessage(MessageUtils.getColor("&bDe: &f" + currentArena + " &bA: &f" + nextArena));
        Bukkit.broadcastMessage(MessageUtils.getColor("&b&l=-=-=-=-=-=-=-=-=-="));

        // Teletransportar jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (currentArena != null) {
                plugin.getArenaManager().removePlayerFromArena(player, currentArena);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(nextSpawn);
                    plugin.getArenaManager().addPlayerToArena(player, nextArena);
                    
                    // Efectos visuales y sonoros
                    player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));
                    
                    // Restaurar movimiento después de 1 segundo
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.setWalkSpeed(0.2f);
                        TitleUtils.sendTitle(player, "&a&l¡Arena " + nextArena + "!", "&e¡Buena suerte!");
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    }, 20L);
                }
            }.runTaskLater(plugin, 2L);
        }

        // Actualizar la arena actual
        plugin.getArenaManager().setCurrentArena(nextArena);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("stats", "dummy");
        obj.setDisplayName(MessageUtils.getColor("&e&l⚔ KnockbackFFA ⚔"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentArena = plugin.getArenaManager().getCurrentArena();
        Streak streak = plugin.getStreakManager().getStreak(player);
        
        int score = 13; // Aumentamos en 1 para agregar la línea del KDR
        
        obj.getScore(MessageUtils.getColor("&7&m----------------")).setScore(score--);
        obj.getScore(MessageUtils.getColor("&b● " + player.getName())).setScore(score--);
        obj.getScore("").setScore(score--);
        obj.getScore(MessageUtils.getColor("&6● Arena: &f" + currentArena)).setScore(score--);
        obj.getScore(MessageUtils.getColor("&c● Rank: " + RankManager.getRankPrefix(stats.getElo()))).setScore(score--);
        obj.getScore(" ").setScore(score--);
        obj.getScore(MessageUtils.getColor("&a● Kills: &f" + stats.getKills())).setScore(score--);
        obj.getScore(MessageUtils.getColor("&4● Deaths: &f" + stats.getDeaths())).setScore(score--);
        obj.getScore(MessageUtils.getColor("&e● Elo: &f" + stats.getElo())).setScore(score--);
        obj.getScore(MessageUtils.getColor("&b● KDR: &f" + String.format("%.2f", stats.getKDR()))).setScore(score--);
        
        String streakText = "&5● Racha: &f" + streak.getKills();
        if (streak.getMaxKillstreak() > 0) {
            streakText += " &7(" + streak.getMaxKillstreak() + ")";
        }
        obj.getScore(MessageUtils.getColor(streakText)).setScore(score--);
        
        String timeFormatted = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
        obj.getScore(MessageUtils.getColor("&d● Tiempo: &f" + timeFormatted)).setScore(score--);
        obj.getScore(MessageUtils.getColor("&7&m----------------")).setScore(score);

        player.setScoreboard(board);
    }
}
