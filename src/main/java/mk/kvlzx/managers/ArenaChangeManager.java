package mk.kvlzx.managers;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.clip.placeholderapi.PlaceholderAPI;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.arena.Zone;
import mk.kvlzx.arena.ZoneType;
import mk.kvlzx.config.TabConfig;
import mk.kvlzx.items.ItemsManager;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.TitleUtils;

public class ArenaChangeManager {
    private final MysthicKnockBack plugin;
    private final TabConfig config;
    
    // Estado del cambio de arena
    private boolean isChangingArena = false;
    private long arenaChangeStartTime;
    private static final long MAX_ARENA_CHANGE_TIME = 5000; // 5 segundos máximo
    
    public ArenaChangeManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.config = plugin.getTabConfig();
    }
    
    /**
     * Verifica si hay un cambio de arena en progreso
     */
    public boolean isArenaChanging() {
        // Si ha pasado demasiado tiempo, forzar el fin del cambio
        if (isChangingArena && System.currentTimeMillis() - arenaChangeStartTime > MAX_ARENA_CHANGE_TIME) {
            completeArenaChange();
            return false;
        }
        return isChangingArena;
    }
    
    /**
     * Inicia el proceso de cambio de arena (congelando jugadores)
     */
    public void startArenaChange() {
        isChangingArena = true;
        arenaChangeStartTime = System.currentTimeMillis();
        
        // Congelar a todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            freezePlayer(player);
        }
    }
    
    /**
     * Completa el proceso de cambio de arena (descongelando jugadores)
     */
    public void completeArenaChange() {
        isChangingArena = false;
        
        // Descongelar a todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            unfreezePlayer(player);
        }
    }
    
    /**
     * Cambia inmediatamente a una arena específica (para votaciones)
     */
    public void changeArenaImmediately(String targetArenaName) {
        changeArenaImmediately(targetArenaName, false);
    }
    
    /**
     * Cambia inmediatamente a una arena específica
     * @param targetArenaName Arena destino
     * @param showAnimation Mostrar animación de cambio
     */
    public void changeArenaImmediately(String targetArenaName, boolean showAnimation) {
        Arena targetArena = plugin.getArenaManager().getArena(targetArenaName);
        if (targetArena == null || targetArena.getSpawnLocation() == null) {
            completeArenaChange();
            return;
        }
        
        startArenaChange();
        
        if (showAnimation) {
            // Mostrar animación y luego cambiar
            showChangeAnimation(targetArenaName, () -> performArenaChange(targetArenaName));
        } else {
            // Cambio directo sin animación
            new BukkitRunnable() {
                @Override
                public void run() {
                    performArenaChange(targetArenaName);
                }
            }.runTaskLater(plugin, 10L); // Pequeño delay para el freeze
        }
    }
    
    /**
     * Cambia a la siguiente arena con animación completa (para rotación automática)
     */
    public void rotateToNextArena() {
        String nextArena =plugin.getArenaManager().getNextArena();
        if (nextArena == null) {
            return;
        }
        
        startArenaChange();
        showChangeAnimation(nextArena, () -> performArenaChange(nextArena));
    }
    
    /**
     * Muestra la animación de cambio de arena
     */
    private void showChangeAnimation(String targetArenaName, Runnable onComplete) {
        // Anuncio inicial
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.PORTAL_TRIGGER, 1.0f, 1.0f);
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
                    onComplete.run();
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (step < loadingFrames.size()) {
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
    
    /**
     * Realiza el cambio físico de arena
     */
    private void performArenaChange(String targetArenaName) {
        String currentArena = plugin.getArenaManager().getCurrentArena();
        Arena targetArena = plugin.getArenaManager().getArena(targetArenaName);
        Location targetSpawn = targetArena.getSpawnLocation();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Limpiar efectos de powerups
            plugin.getArenaManager().getPowerUpManager().clearAllPowerUpEffects(player);
            
            // Remover de arena actual
            if (currentArena != null) {
                plugin.getArenaManager().removePlayerFromArena(player, currentArena);
                plugin.getArenaManager().getPowerUpManager().cleanupArenaPowerUpsOnly(currentArena);
            }
            
            // Teleportar y configurar jugador
            player.teleport(targetSpawn);
            ItemsManager.giveSpawnItems(player);
            plugin.getArenaManager().addPlayerToArena(player, targetArenaName);
            
            // Actualizar zona del jugador  
            updatePlayerZone(player, targetArenaName);
            
            // Remover enderpearls del jugador
            player.getWorld().getEntities().stream()
                .filter(entity -> entity.getType() == EntityType.ENDER_PEARL)
                .filter(entity -> ((EnderPearl) entity).getShooter() == player)
                .forEach(entity -> entity.remove());
            
            // Configurar protección
            player.setNoDamageTicks(60);
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
        
        // Actualizar arena actual en el manager
        plugin.getArenaManager().setCurrentArena(targetArenaName);
        
        // NUEVO: Reactivar el sistema de PowerUps de la nueva arena (por si acaso)
        plugin.getArenaManager().getPowerUpManager().reactivateArena(targetArenaName);
        
        // Mostrar título de confirmación y descongelar
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Título de confirmación
                    String title = config.getScoreTitleAfterChangeTitle();
                    String subtitle = config.getScoreTitleAfterChangeSubtitle();
                    
                    if (title != null) {
                        title = title.replace("%next_arena%", targetArenaName);
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
                }
                
                // Completar el cambio (descongelar)
                completeArenaChange();
            }
        }.runTaskLater(plugin, 30L);
    }
    
    /**
     * Congela un jugador durante el cambio de arena
     */
    private void freezePlayer(Player player) {
        player.setWalkSpeed(0.0f);
        player.setFoodLevel(0);
        player.setSaturation(0.0f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 128, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, false, false));
        player.setNoDamageTicks(200);
    }
    
    /**
     * Descongela un jugador después del cambio de arena
     */
    private void unfreezePlayer(Player player) {
        player.setWalkSpeed(0.2f);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }
    
    /**
     * Actualiza la zona de un jugador en una arena
     */
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
    
    /**
     * Procesa placeholders (si PlaceholderAPI está disponible)
     */
    private String processPlaceholders(String text, Player player) {
        if (text == null) return "";
        
        // Verificar si PlaceholderAPI está disponible
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                return PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                // PlaceholderAPI no disponible o error
                return text;
            }
        }
        
        return text;
    }
}
