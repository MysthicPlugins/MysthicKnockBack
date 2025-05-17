package kk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.arena.Arena;
import kk.kvlzx.arena.ArenaManager;
import kk.kvlzx.stats.PlayerStats;
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
                
                // Alertas de tiempo
                if (Arrays.stream(COUNTDOWN_ALERTS).anyMatch(t -> t == timeLeft)) {
                    Bukkit.broadcastMessage(MessageUtils.getColor("&a¡La arena cambiará en &c" + timeLeft + " &asegundos!"));
                }
                
                // Últimos 5 segundos con títulos
                if (timeLeft <= 5 && timeLeft > 0) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        TitleUtils.sendTitle(
                            player,
                            "&c" + timeLeft,
                            "&esegundos para cambiar de arena"
                        );
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                    }
                }

                if (timeLeft <= 0) {
                    rotateArena();
                    timeLeft = ARENA_TIME; // Reinicia el contador
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void rotateArena() {
        ArenaManager arenaManager = plugin.getArenaManager();
        String currentArena = arenaManager.getCurrentArena();
        String nextArena = arenaManager.getNextArena();
        
        if (nextArena == null) return;

        Arena nextArenaObj = arenaManager.getArena(nextArena);
        Location nextSpawn = nextArenaObj.getSpawnLocation();
        
        if (nextSpawn == null) {
            Bukkit.broadcastMessage(MessageUtils.getColor("&cError: La arena " + nextArena + " no tiene un punto de spawn configurado."));
            return;
        }

        // Mensaje de transición
        Bukkit.broadcastMessage(MessageUtils.getColor("&b&l=-=-=-=-=-=-=-=-=-="));
        Bukkit.broadcastMessage(MessageUtils.getColor("&e¡Cambiando de arena!"));
        Bukkit.broadcastMessage(MessageUtils.getColor("&bDe: &f" + currentArena + " &bA: &f" + nextArena));
        Bukkit.broadcastMessage(MessageUtils.getColor("&b&l=-=-=-=-=-=-=-=-=-="));

        // Teletransportar a TODOS los jugadores online
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Remover del registro de la arena anterior
            if (currentArena != null) {
                arenaManager.removePlayerFromArena(player, currentArena);
            }

            // Teletransportar y registrar en la nueva arena
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(nextSpawn);
                    arenaManager.addPlayerToArena(player, nextArena);
                    
                    // Efectos visuales y sonoros
                    player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
            }.runTaskLater(plugin, 2L); // Pequeño delay para asegurar sincronización
        }

        // Actualizar la arena actual
        arenaManager.setCurrentArena(nextArena);
        Bukkit.broadcastMessage(MessageUtils.getColor("&a¡Todos los jugadores han sido teletransportados a la arena " + nextArena + "!"));
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("stats", "dummy");
        obj.setDisplayName(MessageUtils.getColor("&e&lKnockbackFFA"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentArena = plugin.getArenaManager().getCurrentArena();
        
        int score = 9;
        
        obj.getScore(MessageUtils.getColor("&7&m----------------")).setScore(score--);
        obj.getScore(MessageUtils.getColor("&fArena: &b" + currentArena)).setScore(score--);
        obj.getScore("").setScore(score--);
        obj.getScore(MessageUtils.getColor("&fKills: &a" + stats.getKills())).setScore(score--);
        obj.getScore(MessageUtils.getColor("&fMuertes: &c" + stats.getDeaths())).setScore(score--);
        obj.getScore(MessageUtils.getColor("&fELO: &6" + stats.getElo())).setScore(score--);
        obj.getScore(" ").setScore(score--);
        obj.getScore(MessageUtils.getColor("&f User: &f" + player.getName())).setScore(score--);
        obj.getScore(" ").setScore(score--);

        
        String timeFormatted = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
        obj.getScore(MessageUtils.getColor("&fTiempo: &e" + timeFormatted)).setScore(score--);
        obj.getScore(MessageUtils.getColor("&7&m----------------")).setScore(score);

        player.setScoreboard(board);
    }
}
