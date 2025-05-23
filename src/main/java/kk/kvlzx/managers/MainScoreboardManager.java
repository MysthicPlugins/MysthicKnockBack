package kk.kvlzx.managers;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.arena.Arena;
import kk.kvlzx.arena.ArenaManager;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.stats.Streak;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.utils.TitleUtils;

public class MainScoreboardManager {
    private final KvKnockback plugin;
    private int timeLeft = 120;
    private final int ARENA_TIME = 120;
    private final int[] COUNTDOWN_ALERTS = {60, 30, 10, 5, 4, 3, 2, 1};
    private final ScoreboardManager scoreboardManager;

    public MainScoreboardManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.scoreboardManager = Bukkit.getScoreboardManager();
        setupScoreboard();
        startArenaRotation();
    }

    private void setupScoreboard() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void updatePlayerScoreboard(Player player) {
        Scoreboard board = scoreboardManager.getNewScoreboard();
        Objective obj = board.registerNewObjective("main", "dummy");
        obj.setDisplayName(MessageUtils.getColor("&e☆ &6&lKnockbackFFA"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena == null) currentArena = "Ninguna";
        
        Streak streak = plugin.getStreakManager().getStreak(player.getUniqueId());
        
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String formattedTime = String.format("&a%02d:%02d", minutes, seconds);

        // Las líneas se agregan en orden inverso debido a cómo funciona el sistema de scores
        setScore(obj, "&6&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", 13);
        setScore(obj, "", 12);
        setScore(obj, "&e☆ &fJugador: &b" + player.getName(), 11);
        setScore(obj, "", 10);
        setScore(obj, "&e⚡ &fArena: &a" + currentArena, 9);
        setScore(obj, "&e❋ &fRango: " + RankManager.getRankPrefix(stats.getElo()), 8);
        setScore(obj, "", 7);
        setScore(obj, "&e⚔ &fKills: &a" + stats.getKills(), 6);
        setScore(obj, "&e☠ &fMuertes: &c" + stats.getDeaths(), 5);
        setScore(obj, "&e✦ &fElo: &6" + stats.getElo(), 4);
        setScore(obj, "&e❈ &fKDR: &b" + String.format("%.2f", stats.getKDR()), 3);
        setScore(obj, "&e➜ &fRacha: &d" + streak.getKills() + (streak.getMaxKillstreak() > 0 ? " &7(" + streak.getMaxKillstreak() + ")" : ""), 2);
        setScore(obj, "&e⌚ &fTiempo: " + formattedTime, 1);
        setScore(obj, "&6&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", 0);

        player.setScoreboard(board);
    }

    private void setScore(Objective obj, String text, int score) {
        Score scoreObj = obj.getScore(MessageUtils.getColor(text));
        scoreObj.setScore(score);
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
                            loadingColors[step] + loadingFrames[step] + " &7" + (step * 20 + 20) + "%",
                            10, 30, 10
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
                        TitleUtils.sendTitle(player, 
                            "&a&l¡Arena " + nextArena + "!", 
                            "&e¡Buena suerte!",
                            10, 40, 10
                        );
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    }, 20L);
                }
            }.runTaskLater(plugin, 2L);
        }

        // Actualizar la arena actual
        plugin.getArenaManager().setCurrentArena(nextArena);
    }
}
