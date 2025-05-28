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
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.utils.TitleUtils;

public class MainScoreboardManager {
    private final KvKnockback plugin;
    private int timeLeft = 120;
    private final int ARENA_TIME = 120;
    private final int[] COUNTDOWN_ALERTS = {60, 30, 10, 5, 4, 3, 2, 1};
    private final ScoreboardManager scoreboardManager;
    private boolean arenaChanging = false; // Nueva variable

    public MainScoreboardManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.scoreboardManager = Bukkit.getScoreboardManager();
        setupScoreboard();
        startArenaRotation();
    }

    private void setupScoreboard() {
        // Cambiar el intervalo de actualización a 2L (0.1 segundos)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    private void updatePlayerScoreboard(Player player) {
        Scoreboard board = scoreboardManager.getNewScoreboard();
        Objective obj = board.registerNewObjective("main", "dummy");
        obj.setDisplayName(MessageUtils.getColor("&b&lKnockback&3&lFFA"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena == null) currentArena = "Ninguna";
        
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String formattedTime = String.format("&e%02d:%02d", minutes, seconds);

        // Las líneas se agregan en orden inverso
        setScore(obj, "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", 16);
        setScore(obj, "", 15);
        setScore(obj, "&b❈ &fJugador: &b" + player.getName(), 14);
        setScore(obj, "", 13);
        setScore(obj, "&b☁ &fArena: &b" + currentArena, 12);
        setScore(obj, "&b⏳ &fTiempo: " + formattedTime, 11);
        setScore(obj, "&b✧ &fRango: " + RankManager.getRankPrefix(stats.getElo()), 10);
        setScore(obj, "", 9);
        setScore(obj, "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", 8);
        setScore(obj, "&b⚔ &fKills: &a" + stats.getKills(), 7);
        setScore(obj, "&b☠ &fMuertes: &c" + stats.getDeaths(), 6);
        setScore(obj, "&b⭐ &fElo: &6" + stats.getElo(), 5);
        setScore(obj, "&b⚖ &fKDR: &b" + String.format("%.2f", stats.getKDR()), 4);
        setScore(obj, "&b⚡ &fRacha: &d" + stats.getCurrentStreak(), 3);
        setScore(obj, "&b$ &fCoins: &e" + stats.getKGCoins(), 2);
        setScore(obj, "", 1);
        setScore(obj, "&f&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", 0);

        player.setScoreboard(board);
    }

    private void setScore(Objective obj, String text, int score) {
        Score scoreObj = obj.getScore(MessageUtils.getColor(text));
        scoreObj.setScore(score);
    }

    private void startArenaRotation() {
        new BukkitRunnable() {
            private long lastSecond = System.currentTimeMillis() / 1000;

            @Override
            public void run() {
                long currentSecond = System.currentTimeMillis() / 1000;
                
                // Solo actualizar cuando realmente haya pasado un segundo
                if (currentSecond > lastSecond) {
                    timeLeft--;
                    lastSecond = currentSecond;
                    
                    // Alertas de tiempo
                    if (Arrays.stream(COUNTDOWN_ALERTS).anyMatch(t -> t == timeLeft)) {
                        Bukkit.broadcastMessage(MessageUtils.getColor("&a¡La arena cambiará en &c" + timeLeft + " &asegundos!"));
                    }

                    if (timeLeft <= 0) {
                        rotateArena();
                        timeLeft = ARENA_TIME;
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 1L); // Actualizar cada tick pero controlar el tiempo con currentTimeMillis
    }

    public boolean isArenaChanging() {
        return arenaChanging;
    }

    private void rotateArena() {
        // Activar el estado de cambio de arena ANTES de cualquier otra operación
        arenaChanging = true;

        ArenaManager arenaManager = plugin.getArenaManager();
        String currentArena = arenaManager.getCurrentArena();
        String nextArena = arenaManager.getNextArena();
        
        if (nextArena == null || currentArena == null) {
            arenaChanging = false;
            return;
        }

        Arena nextArenaObj = arenaManager.getArena(nextArena);
        Location nextSpawn = nextArenaObj.getSpawnLocation();
        
        if (nextSpawn == null) {
            Bukkit.broadcastMessage(MessageUtils.getColor("&cError: La arena " + nextArena + " no tiene un punto de spawn configurado."));
            arenaChanging = false;
            return;
        }

        // Congelar y preparar a todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setWalkSpeed(0.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 128, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1, false, false));
            player.playSound(player.getLocation(), Sound.PORTAL_TRIGGER, 1.0f, 1.0f);
            // Dar invulnerabilidad temporal
            player.setNoDamageTicks(100);
        }

        // Activar el estado de cambio de arena
        arenaChanging = true;

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
                    arenaChanging = false; // Desactivar el estado de cambio cuando termina
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
                    player.setNoDamageTicks(60); // Asegurar invulnerabilidad por 3 segundos después del teleport
                    
                    // Restaurar movimiento después de 1.5 segundos
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.setWalkSpeed(0.2f);
                        arenaChanging = false; // Desactivar el estado de cambio solo después de restaurar todo
                        TitleUtils.sendTitle(player, 
                            "&a&l¡Arena " + nextArena + "!", 
                            "&e¡Buena suerte!",
                            10, 40, 10
                        );
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    }, 30L);
                }
            }.runTaskLater(plugin, 2L);
        }

        // Actualizar la arena actual
        plugin.getArenaManager().setCurrentArena(nextArena);
    }
}
