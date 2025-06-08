package mk.kvlzx.managers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.arena.ArenaManager;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.TitleUtils;

public class MainScoreboardManager {
    private final MysthicKnockBack plugin;
    private int timeLeft = 120;
    private final int ARENA_TIME = 120;
    private final int[] COUNTDOWN_ALERTS = {60, 30, 10, 5, 4, 3, 2, 1};
    private final ScoreboardManager scoreboardManager;
    private boolean arenaChanging = false; // Nueva variable

    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final Map<UUID, Objective> playerObjectives = new HashMap<>();

    public MainScoreboardManager(MysthicKnockBack plugin) {
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
        Scoreboard board = playerScoreboards.get(player.getUniqueId());
        Objective obj = playerObjectives.get(player.getUniqueId());

        if (board == null || obj == null) {
            board = scoreboardManager.getNewScoreboard();
            obj = board.registerNewObjective("main", "dummy");
            obj.setDisplayName(MessageUtils.getColor("&b&lKnockback&3&lFFA"));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            playerScoreboards.put(player.getUniqueId(), board);
            playerObjectives.put(player.getUniqueId(), obj);
            player.setScoreboard(board);
        }

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena == null) currentArena = "Ninguna";
        
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String formattedTime = String.format("&e%02d:%02d", minutes, seconds);
        
        double kdr = stats.getKDR();
        String kdrColor;
        if (kdr < 1.0) {
            kdrColor = "&c"; // Rojo para KDR < 1
        } else if (kdr < 2.0) {
            kdrColor = "&6"; // Naranja para 1 ≤ KDR < 2
        } else if (kdr < 3.0) {
            kdrColor = "&e"; // Amarillo para 2 ≤ KDR < 3
        } else {
            kdrColor = "&a"; // Verde para KDR ≥ 3
        }

        // Crear un buffer con los valores actuales
        Map<Integer, String> newScores = new HashMap<>();
        newScores.put(15, "&7&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        newScores.put(14, " ");
        newScores.put(13, "&b&lINFO:");
        newScores.put(12, " &8➥ &fPlayer: &b" + player.getName());
        newScores.put(11, " &8➥ &fArena: &b" + currentArena);
        newScores.put(10, " ");
        newScores.put(9, "&b&lSTATS:");
        newScores.put(8, " &8➥ &fKills: &a" + stats.getKills());
        newScores.put(7, " &8➥ &fDeaths: &c" + stats.getDeaths());
        newScores.put(6, " &8➥ &fK/D: " + kdrColor + String.format("%.2f", kdr));
        newScores.put(5, " ");
        newScores.put(4, "&b&lTIME:");
        newScores.put(3, " &8➥ &fChange: " + formattedTime);
        newScores.put(2, " &8➥ &fNext: &e" + plugin.getArenaManager().getNextArena());
        newScores.put(1, " ");
        newScores.put(0, "&eplay.mysthicknockback.gg");

        // Actualizar solo los scores que han cambiado
        for (Map.Entry<Integer, String> entry : newScores.entrySet()) {
            updateScore(obj, entry.getValue(), entry.getKey());
        }
    }

    private void updateScore(Objective obj, String text, int score) {
        String coloredText = MessageUtils.getColor(text);
        
        // Verificar si el score actual ya tiene ese texto
        for (String entry : obj.getScoreboard().getEntries()) {
            Score existingScore = obj.getScore(entry);
            if (existingScore.getScore() == score) {
                // Si el texto es el mismo, no hacer nada para evitar parpadeo
                if (entry.equals(coloredText)) {
                    return;
                }
                // Si el texto es diferente, eliminar el score antiguo
                obj.getScoreboard().resetScores(entry);
                break;
            }
        }
        
        // Establecer nuevo score solo si es necesario
        Score scoreObj = obj.getScore(coloredText);
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

    // Añadir métodos para limpiar las referencias cuando el jugador se desconecta
    public void removePlayer(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        playerObjectives.remove(player.getUniqueId());
    }
}
