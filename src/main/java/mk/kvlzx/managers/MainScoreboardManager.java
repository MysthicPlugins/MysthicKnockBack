package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import me.clip.placeholderapi.PlaceholderAPI;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.arena.ArenaManager;
import mk.kvlzx.arena.Zone;
import mk.kvlzx.arena.ZoneType;
import mk.kvlzx.config.TabConfig;
import mk.kvlzx.items.ItemsManager;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.TitleUtils;

public class MainScoreboardManager {
    private final MysthicKnockBack plugin;
    private final TabConfig config;
    private final ScoreboardManager scoreboardManager;
    private boolean placeholderAPIEnabled;
    
    private int timeLeft;
    private boolean arenaChanging = false;
    private int animationFrame = 0;
    private int uniqueSpaceCounter = 0;

    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final Map<UUID, Objective> playerObjectives = new HashMap<>();

    public MainScoreboardManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.config = plugin.getTabConfig();
        this.scoreboardManager = Bukkit.getScoreboardManager();
        
        // Verificar si PlaceholderAPI está disponible
        this.placeholderAPIEnabled = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        
        // Inicializar tiempo desde configuración
        this.timeLeft = config.getScoreArenaChange();
        
        // Solo iniciar si el scoreboard está habilitado
        if (config.isScoreEnabled()) {
            setupScoreboard();
            startArenaRotation();
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
        // Resetear contador al inicio de cada actualización
        uniqueSpaceCounter = 0;
        
        Scoreboard board = playerScoreboards.get(player.getUniqueId());
        Objective obj = playerObjectives.get(player.getUniqueId());

        if (board == null || obj == null) {
            board = scoreboardManager.getNewScoreboard();
            obj = board.registerNewObjective("main", "dummy");
            
            String title = processPlaceholders(config.getScoreTitle(), player);
            obj.setDisplayName(MessageUtils.getColor(title));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            
            playerScoreboards.put(player.getUniqueId(), board);
            playerObjectives.put(player.getUniqueId(), obj);
            player.setScoreboard(board);
        }

        // Limpiar todas las entradas existentes para evitar duplicados
        for (String entry : new HashSet<>(obj.getScoreboard().getEntries())) {
            obj.getScoreboard().resetScores(entry);
        }

        // Resto del método permanece igual...
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
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

        Map<Integer, String> newScores = new HashMap<>();
        int scoreValue = configLines.size() - 1;
        
        for (String line : configLines) {
            String processedLine = processScoreboardPlaceholders(line, player, stats, 
                currentArena, nextArena, formattedTime);
            
            newScores.put(scoreValue, processedLine);
            scoreValue--;
        }

        // Actualizar scores
        for (Map.Entry<Integer, String> entry : newScores.entrySet()) {
            updateScore(obj, entry.getValue(), entry.getKey());
        }
    }

    private String processScoreboardPlaceholders(String text, Player player, PlayerStats stats, 
                                                String currentArena, String nextArena, String formattedTime) {
        // Procesar placeholders básicos del scoreboard
        text = text.replace("%player_name%", player.getName())
                    .replace("%arena_name%", currentArena)
                    .replace("%next_arena%", nextArena)
                    .replace("%time_formatted%", "&e" + formattedTime)
                    .replace("%kills%", String.valueOf(stats.getKills()))
                    .replace("%deaths%", String.valueOf(stats.getDeaths()))
                    .replace("%kdr%", String.format("%.2f", stats.getKDR()));
        
        // Procesar líneas en blanco con espacios únicos
        if (text.equals("%blank_line%")) {
            text = generateUniqueSpace();
        }
        
        // Usar PlaceholderAPI si está disponible
        text = processPlaceholders(text, player);
        
        return text;
    }

    private String generateUniqueSpace() {
        // Usar caracteres invisibles diferentes para cada línea en blanco
        String[] invisibleChars = {
            "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", 
            "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"
        };
        
        // Crear una combinación única usando códigos de color invisibles + espacios
        StringBuilder uniqueSpace = new StringBuilder();
        
        // Usar el contador para generar diferentes tipos de espacios únicos
        int index = uniqueSpaceCounter % invisibleChars.length;
        uniqueSpace.append(invisibleChars[index]);
        
        // Agregar espacios adicionales basados en el contador
        for (int i = 0; i <= (uniqueSpaceCounter / invisibleChars.length); i++) {
            uniqueSpace.append(" ");
        }
        
        // Agregar caracteres de reset para asegurar que sea invisible
        uniqueSpace.append("§r");
        
        uniqueSpaceCounter++;
        return uniqueSpace.toString();
    }

    private void updateScore(Objective obj, String text, int score) {
        String coloredText = MessageUtils.getColor(text);
        
        // Verificar si el score actual ya tiene ese texto
        for (String entry : obj.getScoreboard().getEntries()) {
            Score existingScore = obj.getScore(entry);
            if (existingScore.getScore() == score) {
                if (entry.equals(coloredText)) {
                    return;
                }
                obj.getScoreboard().resetScores(entry);
                break;
            }
        }
        
        Score scoreObj = obj.getScore(coloredText);
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
                    animationFrame++; // Incrementar frame de animación
                    lastSecond = currentSecond;
                    
                    // Usar alertas de configuración
                    List<Integer> alertSeconds = config.getScoreArenaChangeAlert();
                    if (alertSeconds != null && alertSeconds.contains(timeLeft)) {
                        String message = config.getScoreMessageArenaChange();
                        if (message != null) {
                            message = message.replace("%seconds%", String.valueOf(timeLeft));
                            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.prefix + message));
                        }
                    }

                    if (timeLeft <= 0) {
                        rotateArena();
                        timeLeft = config.getScoreArenaChange(); // Resetear desde configuración
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }

    public boolean isArenaChanging() {
        return arenaChanging;
    }

    private void rotateArena() {
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
            arenaChanging = false;
            return;
        }

        // Congelar y preparar a todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setWalkSpeed(0.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 128, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1, false, false));
            player.playSound(player.getLocation(), Sound.PORTAL_TRIGGER, 1.0f, 1.0f);
            player.setNoDamageTicks(100);
        }

        // Secuencia de animación usando configuración
        new BukkitRunnable() {
            int step = 0;
            List<String> loadingFrames = config.getScoreArenaChangeFrames();
            List<String> loadingColors = config.getScoreArenaChangeColors();
            
            @Override
            public void run() {
                if (loadingFrames == null || loadingColors == null || 
                    step >= loadingFrames.size() + 2) {
                    this.cancel();
                    teleportPlayers(currentArena, nextArena, nextSpawn);
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (step < loadingFrames.size()) {
                        // Usar configuración para títulos
                        String title = config.getScoreTitleBeforeChangeTitle();
                        String subtitle = config.getScoreTitleBeforeChangeSubtitle();
                        
                        if (title != null) title = processPlaceholders(title, player);
                        if (subtitle != null) {
                            subtitle = subtitle.replace("%title-animation%", 
                                loadingColors.get(step) + loadingFrames.get(step) + " &7" + (step * 20 + 20) + "%");
                            subtitle = processPlaceholders(subtitle, player);
                        }
                        
                        TitleUtils.sendTitle(
                            player,
                            MessageUtils.getColor(title),
                            MessageUtils.getColor(subtitle),
                            config.getScoreTitleBeforeChangeFadeIn(),
                            config.getScoreTitleBeforeChangeStay(),
                            config.getScoreTitleBeforeChangeFadeOut()
                        );
                        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                    }
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void teleportPlayers(String currentArena, String nextArena, Location nextSpawn) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (currentArena != null) {
                plugin.getArenaManager().removePlayerFromArena(player, currentArena);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemsManager.giveSpawnItems(player);
                    player.teleport(nextSpawn);
                    plugin.getArenaManager().addPlayerToArena(player, nextArena);
                    player.setNoDamageTicks(60);
                    
                    updatePlayerZone(player, nextArena);

                    player.getWorld().getEntities().stream()
                        .filter(entity -> entity.getType() == EntityType.ENDER_PEARL)
                        .filter(entity -> ((EnderPearl) entity).getShooter() == player)
                        .forEach(entity -> entity.remove());
                    
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.setWalkSpeed(0.2f);
                        arenaChanging = false;
                        
                        // Usar configuración para título después del cambio
                        String title = config.getScoreTitleAfterChangeTitle();
                        String subtitle = config.getScoreTitleAfterChangeSubtitle();
                        
                        if (title != null) {
                            title = title.replace("%next_arena%", nextArena);
                            title = processPlaceholders(title, player);
                        }
                        if (subtitle != null) {
                            subtitle = processPlaceholders(subtitle, player);
                        }
                        
                        TitleUtils.sendTitle(player, 
                            MessageUtils.getColor(title), 
                            MessageUtils.getColor(subtitle),
                            config.getScoreTitleAfterChangeFadeIn(),
                            config.getScoreTitleAfterChangeStay(),
                            config.getScoreTitleAfterChangeFadeOut()
                        );
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    }, 30L);
                }
            }.runTaskLater(plugin, 2L);
        }

        plugin.getArenaManager().setCurrentArena(nextArena);
    }

    public void updatePlayerZone(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) return;
        
        Location playerLoc = player.getLocation();
        String currentZone = null;
        
        for (ZoneType zoneType : ZoneType.values()) {
            Zone zone = arena.getZone(zoneType.getId());
            if (zone != null && zone.isInside(playerLoc)) {
                currentZone = zoneType.getId();
                break;
            }
        }
        
        plugin.getArenaManager().setPlayerZone(player, arenaName, currentZone);
    }

    public void removePlayer(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        playerObjectives.remove(player.getUniqueId());
    }

    public void reload() {
        // Recargar configuración
        config.reload();
        
        // Reiniciar tiempo desde configuración
        timeLeft = config.getScoreArenaChange();
        
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