package kk.kvlzx.managers;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.arena.Arena;
import kk.kvlzx.arena.ArenaManager;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.stats.Streak;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.utils.TitleUtils;

import java.util.Arrays;
import java.util.List;

public class TabScoreboardManager {
    private final KvKnockback plugin;
    private final TabAPI tabAPI;
    private final ScoreboardManager scoreboardManager;
    private int timeLeft = 120; // 2 minutos en segundos
    private final int ARENA_TIME = 120; // 2 minutos en segundos
    private final int[] COUNTDOWN_ALERTS = {60, 30, 10, 5, 4, 3, 2, 1}; // Alertas en segundos

    public TabScoreboardManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.tabAPI = TabAPI.getInstance();
        this.scoreboardManager = tabAPI.getScoreboardManager();
        setupScoreboard();
        startArenaRotation();
    }

    private void setupScoreboard() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (TabPlayer player : tabAPI.getOnlinePlayers()) {
                    updatePlayerScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void updatePlayerScoreboard(TabPlayer tabPlayer) {
        if (tabPlayer == null) return;
        
        Player player = plugin.getServer().getPlayer(tabPlayer.getName());
        if (player == null || !player.isOnline()) return;

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena == null) currentArena = "Ninguna";
        
        // No es necesario verificar si streak es null porque getStreak() siempre retorna un objeto
        Streak streak = plugin.getStreakManager().getStreak(player);
        
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String formattedTime = String.format("&a%02d:%02d", minutes, seconds);

        List<String> lines = Arrays.asList(
            "&6&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            "&e&l⭐ &fJugador: &b" + player.getName(),
            "",
            "&e&l⚔ &fArena: &a" + currentArena,
            "&e&l👑 &fRango: " + RankManager.getRankPrefix(stats.getElo()),
            "",
            "&e&l☠ &fKills: &a" + stats.getKills(),
            "&e&l💀 &fMuertes: &c" + stats.getDeaths(),
            "&e&l🏆 &fElo: &6" + stats.getElo(),
            "&e&l📊 &fKDR: &b" + String.format("%.2f", stats.getKDR()),
            "&e&l⚡ &fRacha: &d" + streak.getKills() + (streak.getMaxKillstreak() > 0 ? " &7(" + streak.getMaxKillstreak() + ")" : ""),
            "&e&l⏳ &fTiempo: " + formattedTime,
            "&6&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        );

        Scoreboard scoreboard = scoreboardManager.createScoreboard(
            "main_scoreboard",
            "&e&l🏆 &6&lKnockbackFFA",
            lines
        );

        scoreboardManager.showScoreboard(tabPlayer, scoreboard);
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
}
