package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import me.clip.placeholderapi.PlaceholderAPI;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.ChatConfig;
import mk.kvlzx.config.TabConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class MainScoreboardManager {
    private final MysthicKnockBack plugin;
    private final TabConfig tabConfig;
    private final ChatConfig chatConfig;
    private final ScoreboardManager scoreboardManager;
    private boolean placeholderAPIEnabled;
    private LuckPerms luckPerms;
    private boolean luckPermsEnabled;
    
    private int timeLeft;

    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final Map<UUID, Objective> playerObjectives = new HashMap<>();
    
    // Cache para evitar actualizaciones innecesarias
    private final Map<UUID, Map<Integer, String>> lastScoreCache = new HashMap<>();
    private final Map<UUID, String> lastTitleCache = new HashMap<>();
    private final Map<UUID, String> lastNameTagCache = new HashMap<>(); // Cache para nametags
    
    // Espacios únicos pre-generados para evitar regeneración constante
    private final Map<Integer, String> uniqueSpaces = new HashMap<>();
    
    public MainScoreboardManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.tabConfig = plugin.getTabConfig();
        this.chatConfig = plugin.getChatConfig();
        this.scoreboardManager = Bukkit.getScoreboardManager();
        
        // Verificar si PlaceholderAPI está disponible
        this.placeholderAPIEnabled = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        
        // Inicializar LuckPerms
        this.luckPermsEnabled = setupLuckPerms();
        
        // Inicializar tiempo desde configuración
        this.timeLeft = tabConfig.getScoreArenaChange();
        
        // Pre-generar espacios únicos
        generateUniqueSpaces();
        
        // Solo iniciar si el scoreboard está habilitado
        if (tabConfig.isScoreEnabled()) {
            setupScoreboard();
            startArenaRotation();
        }
        
        // Iniciar actualización de nametags si el tab está habilitado
        if (chatConfig.isTabEnabled()) {
            startNameTagUpdater();
        }
    }

    private boolean setupLuckPerms() {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
                this.luckPerms = LuckPermsProvider.get();
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into LuckPerms for scoreboard: " + e.getMessage());
        }
        return false;
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
                if (!tabConfig.isScoreEnabled()) {
                    this.cancel();
                    return;
                }
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, tabConfig.getScoreUpdateInterval(), tabConfig.getScoreUpdateInterval());
    }

    private void startNameTagUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!chatConfig.isTabEnabled()) {
                    this.cancel();
                    return;
                }
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerNameTag(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Actualizar cada segundo
    }

    private void updatePlayerScoreboard(Player player) {
        UUID playerId = player.getUniqueId();
        Scoreboard board = playerScoreboards.get(playerId);
        Objective obj = playerObjectives.get(playerId);

        if (board == null || obj == null) {
            board = scoreboardManager.getNewScoreboard();
            obj = board.registerNewObjective("main", "dummy");
            
            String title = processPlaceholders(tabConfig.getScoreTitle(), player);
            obj.setDisplayName(MessageUtils.getColor(title));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            
            playerScoreboards.put(playerId, board);
            playerObjectives.put(playerId, obj);
            player.setScoreboard(board);
            
            // Inicializar cache para este jugador
            lastScoreCache.put(playerId, new HashMap<>());
            lastTitleCache.put(playerId, "");
            lastNameTagCache.put(playerId, "");
        }

        // Verificar si el título cambió
        String newTitle = processPlaceholders(tabConfig.getScoreTitle(), player);
        String lastTitle = lastTitleCache.get(playerId);
        if (!newTitle.equals(lastTitle)) {
            obj.setDisplayName(MessageUtils.getColor(newTitle));
            lastTitleCache.put(playerId, newTitle);
        }

        // Obtener datos del jugador
        PlayerStats stats = PlayerStats.getStats(playerId);
        String currentArena = plugin.getArenaManager().getCurrentArena();
        String nextArena = plugin.getArenaManager().getNextArena();
        
        if (currentArena == null) currentArena = tabConfig.getScoreNullArena();
        if (nextArena == null) nextArena = tabConfig.getScoreNullNextArena();
        
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        List<String> configLines = tabConfig.getScoreLines();
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

    /**
     * Actualizar nametags para un jugador específico
     */
    public void updatePlayerNameTag(Player player) {
        if (!chatConfig.isTabEnabled()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        Scoreboard board = playerScoreboards.get(playerId);

        // Si no tiene scoreboard, crear una básica
        if (board == null) {
            board = scoreboardManager.getNewScoreboard();
            playerScoreboards.put(playerId, board);
            player.setScoreboard(board);
        }
        
        // CLAVE: Actualizar teams para TODOS los jugadores en el scoreboard de CADA jugador
        updateAllPlayersInScoreboard(board);
        
        // Actualizar el team específico de este jugador
        updatePlayerTeamInAllScoreboards(player);
    }

    /**
     * Método para actualizar todos los jugadores en un scoreboard específico
     */
    private void updateAllPlayersInScoreboard(Scoreboard targetBoard) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String teamName = getTeamNameForTabOrder(onlinePlayer);
            String displayName = getTabDisplayName(onlinePlayer);
            NameTagData nameTagData = parseNameTagData(displayName, onlinePlayer);
            boolean isInvisible = onlinePlayer.hasPotionEffect(PotionEffectType.INVISIBILITY);
            
            Team team = targetBoard.getTeam(teamName);
            if (team == null) {
                try {
                    team = targetBoard.registerNewTeam(teamName);
                } catch (IllegalArgumentException e) {
                    team = targetBoard.getTeam(teamName);
                    if (team == null) continue;
                }
            }

            if (isInvisible) {
                team.setNameTagVisibility(NameTagVisibility.NEVER);
            } else {
                team.setNameTagVisibility(NameTagVisibility.ALWAYS);
            }

            // Actualizar prefix y suffix
            team.setPrefix(MessageUtils.getColor(nameTagData.getPrefix()));
            team.setSuffix(MessageUtils.getColor(nameTagData.getSuffix()));

            // Añadir jugador al team si no está
            if (!team.hasEntry(onlinePlayer.getName())) {
                team.addEntry(onlinePlayer.getName());
            }
        }
    }

    /**
     * Método para actualizar el team de un jugador específico en todos los scoreboards
     */
    private void updatePlayerTeamInAllScoreboards(Player player) {
        String teamName = getTeamNameForTabOrder(player);
        String displayName = getTabDisplayName(player);
        NameTagData nameTagData = parseNameTagData(displayName, player);
        boolean isInvisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);
        
        // Actualizar en todos los scoreboards de todos los jugadores
        for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
            UUID targetId = targetPlayer.getUniqueId();
            Scoreboard targetBoard = playerScoreboards.get(targetId);

            if (targetBoard == null) {
                targetBoard = scoreboardManager.getNewScoreboard();
                playerScoreboards.put(targetId, targetBoard);
                targetPlayer.setScoreboard(targetBoard);
            }

            Team team = targetBoard.getTeam(teamName);
            if (team == null) {
                try {
                    team = targetBoard.registerNewTeam(teamName);
                } catch (IllegalArgumentException e) {
                    team = targetBoard.getTeam(teamName);
                    if (team == null) continue;
                }
            }

            if (isInvisible) {
                team.setNameTagVisibility(NameTagVisibility.NEVER);
            } else {
                team.setNameTagVisibility(NameTagVisibility.ALWAYS);
            }

            // Actualizar prefix y suffix
            team.setPrefix(MessageUtils.getColor(nameTagData.getPrefix()));
            team.setSuffix(MessageUtils.getColor(nameTagData.getSuffix()));

            // Añadir jugador al team si no está
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        }
        
        // Actualizar cache solo después de actualizar todos los scoreboards
        UUID playerId = player.getUniqueId();
        lastNameTagCache.put(playerId, displayName);
    }

    /**
     * Método especial para cuando un jugador se conecta - inicializa su scoreboard con todos los teams
     */
    public void setupNewPlayerScoreboard(Player player) {
        if (!chatConfig.isTabEnabled()) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        Scoreboard board = scoreboardManager.getNewScoreboard();
        
        // Inicializar scoreboard con todos los jugadores actuales
        updateAllPlayersInScoreboard(board);
        
        playerScoreboards.put(playerId, board);
        player.setScoreboard(board);
        
        // Inicializar cache
        lastNameTagCache.put(playerId, "");
        
        // Actualizar el team de este jugador en todos los demás scoreboards
        updatePlayerTeamInAllScoreboards(player);
    }

    /**
     * Generar nombre de team que controle el orden en el tab
     * El orden alfabético de los nombres de team determina el orden en el tab
     */
    private String getTeamNameForTabOrder(Player player) {
        if (!luckPermsEnabled) {
            String playerName = player.getName();
            // Limitar a 14 caracteres para que "z_" + nombre no exceda 16
            if (playerName.length() > 14) {
                playerName = playerName.substring(0, 14);
            }
            return "z_" + playerName.toLowerCase(); // Sin LuckPerms, ordenar al final
        }

        String primaryGroup = getPlayerPrimaryGroup(player);
        int priority = chatConfig.getGroupPriority(primaryGroup);
        
        // Formatear prioridad con ceros a la izquierda para orden alfabético correcto
        // Ejemplo: 00, 01, 17
        String formattedPriority = String.format("%02d", priority);
        String playerName = player.getName();

        // Los nombres de team en MC 1.8 están limitados a 16 caracteres.
        // Se usa un esquema: "prio_playerName" -> 2 + 1 + 13 = 16
        int maxPlayerNameLength = 13;
        if (playerName.length() > maxPlayerNameLength) {
            playerName = playerName.substring(0, maxPlayerNameLength);
        }
        
        // El nombre del team es único por jugador y controla el orden.
        return formattedPriority + "_" + playerName.toLowerCase();
    }

    /**
     * Actualizar nametags de todos los jugadores
     */
    public void updateAllNameTags() {
        if (!chatConfig.isTabEnabled()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerNameTag(player);
        }
    }

    /**
     * Limpiar teams de un jugador cuando se desconecta
     */
    public void cleanupPlayerTeams(Player player) {
        String teamName = getTeamNameForTabOrder(player);
        
        for (UUID playerId : playerScoreboards.keySet()) {
            Scoreboard board = playerScoreboards.get(playerId);
            if (board != null) {
                Team team = board.getTeam(teamName);
                if (team != null) {
                    team.unregister();
                }
            }
        }
    }

    private String getTabDisplayName(Player player) {
        if (luckPermsEnabled) {
            String primaryGroup = getPlayerPrimaryGroup(player);
            if (primaryGroup != null) {
                String groupDisplayName = chatConfig.getTabGroupDisplayName(primaryGroup);
                if (groupDisplayName != null) {
                    return processPlaceholders(groupDisplayName, player);
                }
            }
        }
        
        return processPlaceholders(chatConfig.getTabDefaultDisplayName(), player);
    }

    private String getPlayerPrimaryGroup(Player player) {
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getPrimaryGroup();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting LuckPerms group for player " + player.getName() + ": " + e.getMessage());
        }
        
        return "default";
    }

    /**
     * Parsear el display name para extraer prefix y suffix para nametags
     */
    private NameTagData parseNameTagData(String displayName, Player player) {
        String prefix = "";
        String suffix = "";
        
        // Remover códigos de color para procesar
        String cleanDisplayName = displayName.replaceAll("§[0-9a-fk-or]", "");
        String playerName = player.getName();
        
        // Encontrar la posición del nombre del jugador en el display name
        int playerNameIndex = cleanDisplayName.indexOf(playerName);
        
        if (playerNameIndex != -1) {
            // Extraer prefix (todo antes del nombre del jugador)
            if (playerNameIndex > 0) {
                prefix = displayName.substring(0, displayName.indexOf(playerName));
                prefix = prefix.trim();
            }
            
            // Extraer suffix (todo después del nombre del jugador)
            int afterNameIndex = displayName.indexOf(playerName) + playerName.length();
            if (afterNameIndex < displayName.length()) {
                suffix = displayName.substring(afterNameIndex);
                suffix = suffix.trim();
                // Si el suffix solo contiene información del ping, no mostrarlo en el nametag
                if (suffix.matches(".*\\[.*ms\\].*")) {
                    suffix = "";
                }
            }
        } else {
            // Si no se encuentra el nombre, usar todo como prefix
            prefix = displayName.replace(playerName, "").trim();
        }
        
        // Limitar longitud para evitar problemas visuales
        if (prefix.length() > 16) {
            prefix = prefix.substring(0, 16);
        }
        if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
        }
        
        return new NameTagData(prefix, suffix);
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
                if (!tabConfig.isScoreEnabled()) {
                    this.cancel();
                    return;
                }
                
                long currentSecond = System.currentTimeMillis() / 1000;
                
                if (currentSecond > lastSecond) {
                    timeLeft--;
                    lastSecond = currentSecond;
                    
                    // Usar alertas de configuración
                    List<Integer> alertSeconds = tabConfig.getScoreArenaChangeAlert();
                    if (alertSeconds != null && alertSeconds.contains(timeLeft)) {
                        String message = tabConfig.getScoreMessageArenaChange();
                        if (message != null) {
                            message = message.replace("%seconds%", String.valueOf(timeLeft));
                            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                        }
                    }

                    if (timeLeft <= 0) {
                        rotateArena();
                        timeLeft = tabConfig.getScoreArenaChange();
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
        timeLeft = tabConfig.getScoreArenaChange();
    }
    
    /**
     * Obtener tiempo restante del timer (para verificar restricciones de votación)
     */
    public int getArenaTimeLeft() {
        return timeLeft;
    }

    public void removePlayer(Player player) {
        UUID playerId = player.getUniqueId();
        String teamName = getTeamNameForTabOrder(player);
        
        // Limpiar el team de este jugador en TODOS los scoreboards
        for (UUID otherPlayerId : playerScoreboards.keySet()) {
            Scoreboard otherBoard = playerScoreboards.get(otherPlayerId);
            if (otherBoard != null) {
                Team team = otherBoard.getTeam(teamName);
                if (team != null) {
                    team.removeEntry(player.getName());
                    // Si el team no tiene más entradas, eliminarlo
                    if (team.getEntries().isEmpty()) {
                        team.unregister();
                    }
                }
            }
        }
        
        // Limpiar datos del jugador
        playerScoreboards.remove(playerId);
        playerObjectives.remove(playerId);
        lastScoreCache.remove(playerId);
        lastTitleCache.remove(playerId);
        lastNameTagCache.remove(playerId);
    }

    public void reload() {
        // Recargar configuración
        tabConfig.reload();
        
        // Reinicializar LuckPerms
        this.luckPermsEnabled = setupLuckPerms();
        
        // Reiniciar tiempo desde configuración
        timeLeft = tabConfig.getScoreArenaChange();
        
        // Limpiar caches
        lastScoreCache.clear();
        lastTitleCache.clear();
        lastNameTagCache.clear();
        
        // Regenerar espacios únicos
        uniqueSpaces.clear();
        generateUniqueSpaces();
        
        // Limpiar scoreboards existentes si el scoreboard está deshabilitado
        if (!tabConfig.isScoreEnabled()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setScoreboard(scoreboardManager.getMainScoreboard());
            }
            playerScoreboards.clear();
            playerObjectives.clear();
        }
        
        // Actualizar nametags con nueva configuración
        if (chatConfig.isTabEnabled()) {
            updateAllNameTags();
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

    /**
     * Clase para almacenar datos del nametag
     */
    private static class NameTagData {
        private final String prefix;
        private final String suffix;

        public NameTagData(String prefix, String suffix) {
            this.prefix = prefix != null ? prefix : "";
            this.suffix = suffix != null ? suffix : "";
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }
    }
}