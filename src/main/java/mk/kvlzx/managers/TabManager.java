package mk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.TabConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

import java.lang.reflect.Field;
import java.util.List;

public class TabManager {
    private final MysthicKnockBack plugin;
    private final TabConfig tabConfig;
    private int animationFrame = 0;
    private BukkitTask animationTask;
    private boolean placeholderAPIEnabled;

    public TabManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.tabConfig = plugin.getTabConfig();
        
        // Verificar si PlaceholderAPI está disponible
        this.placeholderAPIEnabled = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        
        // Solo iniciar si el tab está habilitado
        if (tabConfig.isTabEnabled()) {
            startAnimation();
        }
    }

    private void startAnimation() {
        // Cancelar tarea anterior si existe
        if (animationTask != null) {
            animationTask.cancel();
        }
        
        animationTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateHeaderFooter();
                updatePlayerList();
                
                // Solo avanzar frame si la animación está habilitada
                if (tabConfig.isTabAnimationEnabled()) {
                    List<String> animations = tabConfig.getTabHeaderAnimation();
                    if (animations != null && !animations.isEmpty()) {
                        animationFrame = (animationFrame + 1) % animations.size();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, tabConfig.getTabAnimationInterval());
    }

    private void updateHeaderFooter() {
        // Construir header basado en la configuración
        String header = buildHeader();
        
        // Construir footer basado en la configuración
        String footer = buildFooter();

        IChatBaseComponent headerComponent = ChatSerializer.a("{\"text\": \"" + header + "\"}");
        IChatBaseComponent footerComponent = ChatSerializer.a("{\"text\": \"" + footer + "\"}");

        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
            try {
                Field a = packet.getClass().getDeclaredField("a");
                a.setAccessible(true);
                a.set(packet, headerComponent);
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                b.set(packet, footerComponent);
                craftPlayer.getHandle().playerConnection.sendPacket(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String buildHeader() {
        StringBuilder headerBuilder = new StringBuilder("\n");
        
        if (tabConfig.isTabAnimationEnabled()) {
            // Usar líneas con animación
            List<String> animationLines = tabConfig.getTabHeaderAnimation();
            if (animationLines != null && !animationLines.isEmpty()) {
                // Mostrar la línea actual del frame de animación
                String line;
                if (animationFrame < animationLines.size()) {
                    line = animationLines.get(animationFrame);
                } else {
                    line = animationLines.get(0);
                }
                
                // Procesar placeholders para el header (sin jugador específico)
                line = processPlaceholders(line, null);
                headerBuilder.append(line);
            }
        } else {
            // Usar líneas sin animación
            List<String> staticLines = tabConfig.getTabHeaderWithoutAnimation();
            if (staticLines != null && !staticLines.isEmpty()) {
                for (int i = 0; i < staticLines.size(); i++) {
                    String line = processPlaceholders(staticLines.get(i), null);
                    headerBuilder.append(line);
                    if (i < staticLines.size() - 1) {
                        headerBuilder.append("\n");
                    }
                }
            }
        }
        
        headerBuilder.append("\n");
        return MessageUtils.getColor(headerBuilder.toString());
    }

    private String buildFooter() {
        StringBuilder footerBuilder = new StringBuilder("\n");
        
        List<String> footerLines = tabConfig.getTabFooter();
        if (footerLines != null && !footerLines.isEmpty()) {
            for (int i = 0; i < footerLines.size(); i++) {
                String line = footerLines.get(i);
                
                // Procesar placeholders para el footer (sin jugador específico)
                line = processPlaceholders(line, null);
                
                footerBuilder.append(line);
                if (i < footerLines.size() - 1) {
                    footerBuilder.append("\n");
                }
            }
        }
        
        footerBuilder.append("\n");
        return MessageUtils.getColor(footerBuilder.toString());
    }

    private void updatePlayerList() {
        String playerDisplayFormat = tabConfig.getTabPlayerDisplay();
        if (playerDisplayFormat == null || playerDisplayFormat.isEmpty()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            
            // Procesar placeholders para cada jugador específico
            String displayName = processPlaceholders(playerDisplayFormat, player);
            displayName = MessageUtils.getColor(displayName);

            // Actualizar el nombre en la lista de jugadores
            craftPlayer.setPlayerListName(displayName);
            
            // Actualizar el tab para todos los jugadores
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, 
                craftPlayer.getHandle()
            );

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    public void reload() {
        // Recargar configuración
        tabConfig.reload();
        
        // Reiniciar animación si está habilitada
        if (tabConfig.isTabEnabled()) {
            startAnimation();
        } else {
            // Detener animación si está deshabilitada
            if (animationTask != null) {
                animationTask.cancel();
                animationTask = null;
            }
        }
    }

    public void stop() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
    }

    private String processPlaceholders(String text, Player player) {
        if (placeholderAPIEnabled) {
            // Usar PlaceholderAPI
            if (player != null) {
                text = PlaceholderAPI.setPlaceholders(player, text);
            } else {
                text = PlaceholderAPI.setPlaceholders(null, text);
            }
        } else {
            // Fallback a placeholders básicos internos
            text = processBasicPlaceholders(text, player);
        }
        
        return text;
    }

    private String processBasicPlaceholders(String text, Player player) {
        // Placeholders del servidor
        text = text.replace("%server_online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        text = text.replace("%server_max%", String.valueOf(Bukkit.getMaxPlayers()));
        
        // Placeholders del jugador (solo si se proporciona un jugador)
        if (player != null) {
            text = text.replace("%player_name%", player.getName());
            text = text.replace("%player_displayname%", player.getDisplayName());
            text = text.replace("%player_ping%", String.valueOf(((CraftPlayer) player).getHandle().ping));
            
            // Placeholder personalizado para el rank (fallback)
            if (text.contains("%kbffa_rank%")) {
                try {
                    String rankPrefix = RankManager.getRankPrefix(PlayerStats.getStats(player.getUniqueId()).getElo());
                    text = text.replace("%kbffa_rank%", rankPrefix);
                } catch (Exception e) {
                    text = text.replace("%kbffa_rank%", "&7Unranked");
                }
            }
        }
        
        return text;
    }
}
