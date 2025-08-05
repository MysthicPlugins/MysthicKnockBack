package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import me.clip.placeholderapi.PlaceholderAPI;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.TabConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class MainScoreboardManager {
    private final MysthicKnockBack plugin;
    private final TabConfig config;
    private final ScoreboardManager scoreboardManager;
    private boolean placeholderAPIEnabled;
    
    private int timeLeft;

    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final Map<UUID, Objective> playerObjectives = new HashMap<>();
    
    // Cache para evitar actualizaciones innecesarias
    private final Map<UUID, Map<Integer, String>> lastScoreCache = new HashMap<>();
    private final Map<UUID, String> lastTitleCache = new HashMap<>();
    
    // Espacios únicos pre-generados para evitar regeneración constante
    private final Map<Integer, String> uniqueSpaces = new HashMap<>();
    
    public MainScoreboardManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.config = plugin.getTabConfig();
        this.scoreboardManager = Bukkit.getScoreboardManager();
        
        // Verificar si PlaceholderAPI está disponible
        this.placeholderAPIEnabled = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        
        // Inicializar tiempo desde configuración
        this.timeLeft = config.getScoreArenaChange();
        
        // Pre-generar espacios únicos
        generateUniqueSpaces();
        
        // Solo iniciar si el scoreboard está habilitado
        if (config.isScoreEnabled()) {
            setupScoreboard();
            startArenaRotation();
        }
    }

    private void generateUniqueSpaces() {
        // Pre-generar hasta 50 espacios únicos diferentes
        String[] invisibleChars = {
            "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", 
            "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"
        };
        
        for (int i = 0; i < 50; i++) {
            StringBuilder uniqueSpace = new StringBuilder();
            int index = i % invisibleChars.length;
            uniqueSpace.append(invisibleChars[index]);
            
            for (int j = 0; j <= (i / invisibleChars.length); j++) {
                uniqueSpace.append(" ");
            }
            
            uniqueSpace.append("§r");
            uniqueSpaces.put(i, uniqueSpace.toString());
        }
    }

    private void setupScoreboard() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!config.isScoreEnabled()) {
                    this.cancel();
                    return;
                }
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, config.getScoreUpdateInterval(), config.getScoreUpdateInterval());
    }

    private void updatePlayerScoreboard(Player player) {
        UUID playerId = player.getUniqueId();
        Scoreboard board = playerScoreboards.get(playerId);
        Objective obj = playerObjectives.get(playerId);

        if (board == null || obj == null) {
            board = scoreboardManager.getNewScoreboard();
            obj = board.registerNewObjective("main", "dummy");
            
            String title = processPlaceholders(config.getScoreTitle(), player);
            obj.setDisplayName(MessageUtils.getColor(title));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            
            playerScoreboards.put(playerId, board);
            playerObjectives.put(playerId, obj);
            player.setScoreboard(board);
            
            // Inicializar cache para este jugador
            lastScoreCache.put(playerId, new HashMap<>());
            lastTitleCache.put(playerId, "");
        }

        // Verificar si el título cambió
        String newTitle = processPlaceholders(config.getScoreTitle(), player);
        String lastTitle = lastTitleCache.get(playerId);
        if (!newTitle.equals(lastTitle)) {
            obj.setDisplayName(MessageUtils.getColor(newTitle));
            lastTitleCache.put(playerId, newTitle);
        }

        // Obtener datos del jugador
        PlayerStats stats = PlayerStats.getStats(playerId);
        String currentArena = plugin.getArenaManager().getCurrentArena();
        String nextArena = plugin.getArenaManager().getNextArena();
        
        if (currentArena == null) currentArena = config.getScoreNullArena();
        if (nextArena == null) nextArena = config.getScoreNullNextArena();
        
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        List<String> configLines = config.getScoreLines();
        if (configLines == null || configLines.isEmpty()) {
            return;
        }

        // Procesar nuevas líneas
        Map<Integer, String> newScores = new HashMap<>();
        int scoreValue = configLines.size() - 1;
        int blankLineCounter = 0;
        
        for (String line : configLines) {
            String processedLine = processScoreboardPlaceholders(line, player, stats, 
                currentArena, nextArena, formattedTime, blankLineCounter);
            
            if (line.equals("%blank_line%")) {
                blankLineCounter++;
            }
            
            newScores.put(scoreValue, processedLine);
            scoreValue--;
        }

        // Obtener cache anterior del jugador
        Map<Integer, String> lastScores = lastScoreCache.get(playerId);

        // Si el cache no existe, inicializarlo para evitar NullPointerException
        if (lastScores == null) {
            lastScores = new HashMap<>();
            lastScoreCache.put(playerId, lastScores);
        }
        
        // Solo actualizar scores que hayan cambiado
        for (Map.Entry<Integer, String> entry : newScores.entrySet()) {
            int score = entry.getKey();
            String newText = entry.getValue();
            String lastText = lastScores.get(score);
            
            if (!newText.equals(lastText)) {
                updateScoreOptimized(obj, newText, lastText, score);
                lastScores.put(score, newText);
            }
        }
        
        // Remover scores que ya no existen
        Set<Integer> scoresToRemove = new HashSet<>(lastScores.keySet());
        scoresToRemove.removeAll(newScores.keySet());
        
        for (Integer scoreToRemove : scoresToRemove) {
            String textToRemove = lastScores.get(scoreToRemove);
            if (textToRemove != null) {
                obj.getScoreboard().resetScores(MessageUtils.getColor(textToRemove));
            }
            lastScores.remove(scoreToRemove);
        }
    }

    private String processScoreboardPlaceholders(String text, Player player, PlayerStats stats, 
                                                String currentArena, String nextArena, 
                                                String formattedTime, int blankLineIndex) {
        // Procesar placeholders básicos del scoreboard
        text = text.replace("%player_name%", player.getName())
                    .replace("%arena_name%", currentArena)
                    .replace("%next_arena%", nextArena)
                    .replace("%time_formatted%", "&e" + formattedTime)
                    .replace("%kills%", String.valueOf(stats.getKills()))
                    .replace("%deaths%", String.valueOf(stats.getDeaths()))
                    .replace("%kdr%", String.format("%.2f", stats.getKDR()));
        
        // Procesar líneas en blanco con espacios únicos pre-generados
        if (text.equals("%blank_line%")) {
            text = uniqueSpaces.getOrDefault(blankLineIndex, " ");
        }
        
        // Usar PlaceholderAPI si está disponible
        text = processPlaceholders(text, player);
        
        return text;
    }

    private void updateScoreOptimized(Objective obj, String newText, String oldText, int score) {
        String coloredNewText = MessageUtils.getColor(newText);
        
        // Obtener todos los scores actuales en esta posición
        Set<String> currentEntries = new HashSet<>();
        for (String entry : obj.getScoreboard().getEntries()) {
            Score entryScore = obj.getScore(entry);
            if (entryScore.getScore() == score) {
                currentEntries.add(entry);
            }
        }
        
        // Limpiar todas las entradas en esta posición
        for (String entry : currentEntries) {
            obj.getScoreboard().resetScores(entry);
        }
        
        // Agregar el nuevo texto
        Score scoreObj = obj.getScore(coloredNewText);
        scoreObj.setScore(score);
    }

    private void startArenaRotation() {
        new BukkitRunnable() {
            private long lastSecond = System.currentTimeMillis() / 1000;

            @Override
            public void run() {
                if (!config.isScoreEnabled()) {
                    this.cancel();
                    return;
                }
                
                long currentSecond = System.currentTimeMillis() / 1000;
                
                if (currentSecond > lastSecond) {
                    timeLeft--;
                    lastSecond = currentSecond;
                    
                    // Usar alertas de configuración
                    List<Integer> alertSeconds = config.getScoreArenaChangeAlert();
                    if (alertSeconds != null && alertSeconds.contains(timeLeft)) {
                        String message = config.getScoreMessageArenaChange();
                        if (message != null) {
                            message = message.replace("%seconds%", String.valueOf(timeLeft));
                            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                        }
                    }

                    if (timeLeft <= 0) {
                        rotateArena();
                        timeLeft = config.getScoreArenaChange();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }

    private void rotateArena() {
        plugin.getArenaChangeManager().rotateToNextArena();
    }

    /**
     * Reiniciar el timer de arena (usado después de votaciones exitosas)
     */
    public void resetArenaTimer() {
        timeLeft = config.getScoreArenaChange();
    }
    
    /**
     * Obtener tiempo restante del timer (para verificar restricciones de votación)
     */
    public int getArenaTimeLeft() {
        return timeLeft;
    }

    public void removePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        playerScoreboards.remove(playerId);
        playerObjectives.remove(playerId);
        lastScoreCache.remove(playerId);
        lastTitleCache.remove(playerId);
    }

    public void reload() {
        // Recargar configuración
        config.reload();
        
        // Reiniciar tiempo desde configuración
        timeLeft = config.getScoreArenaChange();
        
        // Limpiar caches
        lastScoreCache.clear();
        lastTitleCache.clear();
        
        // Regenerar espacios únicos
        uniqueSpaces.clear();
        generateUniqueSpaces();
        
        // Limpiar scoreboards existentes si el scoreboard está deshabilitado
        if (!config.isScoreEnabled()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setScoreboard(scoreboardManager.getMainScoreboard());
            }
            playerScoreboards.clear();
            playerObjectives.clear();
        }
    }

    private String processPlaceholders(String text, Player player) {
        if (text == null) return "";
        
        if (placeholderAPIEnabled) {
            if (player != null) {
                text = PlaceholderAPI.setPlaceholders(player, text);
            } else {
                text = PlaceholderAPI.setPlaceholders(null, text);
            }
        }
        
        return text;
    }
}