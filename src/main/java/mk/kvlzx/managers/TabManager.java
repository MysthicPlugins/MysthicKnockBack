package mk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.ChatConfig;
import mk.kvlzx.config.TabConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TabManager {
    private final MysthicKnockBack plugin;
    private final TabConfig tabConfig;
    private final ChatConfig chatConfig;
    private int animationFrame = 0;
    private BukkitTask animationTask;
    private boolean placeholderAPIEnabled;
    private LuckPerms luckPerms;
    private boolean luckPermsEnabled;

    public TabManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.tabConfig = plugin.getTabConfig();
        this.chatConfig = plugin.getChatConfig();
        
        // Verificar si PlaceholderAPI está disponible
        this.placeholderAPIEnabled = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        
        // Inicializar LuckPerms
        this.luckPermsEnabled = setupLuckPerms();
        
        // Solo iniciar si el tab está habilitado
        if (tabConfig.isTabEnabled()) {
            startAnimation();
        }
    }

    private boolean setupLuckPerms() {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
                this.luckPerms = LuckPermsProvider.get();
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into LuckPerms for tab: " + e.getMessage());
        }
        return false;
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
                updateNameTags(); // Nueva función para actualizar nametags
                
                // Solo avanzar frame si hay animaciones habilitadas
                if (hasAnimations()) {
                    animationFrame++;
                    // Prevenir overflow después de mucho tiempo
                    if (animationFrame > 100000) {
                        animationFrame = 0;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, tabConfig.getTabAnimationInterval());
    }

    private boolean hasAnimations() {
        if (!tabConfig.isTabAnimationEnabled()) {
            return false;
        }
        
        // Verificar si hay líneas animadas en header
        List<TabConfig.TabLine> headerLines = tabConfig.getTabHeaderLines();
        for (TabConfig.TabLine line : headerLines) {
            if (line.isAnimated()) {
                return true;
            }
        }
        
        // Verificar si hay líneas animadas en footer
        List<TabConfig.TabLine> footerLines = tabConfig.getTabFooterLines();
        for (TabConfig.TabLine line : footerLines) {
            if (line.isAnimated()) {
                return true;
            }
        }
        
        return false;
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
        
        List<TabConfig.TabLine> headerLines = tabConfig.getTabHeaderLines();
        if (headerLines != null && !headerLines.isEmpty()) {
            for (int i = 0; i < headerLines.size(); i++) {
                TabConfig.TabLine line = headerLines.get(i);
                String content;
                
                if (line.isAnimated() && tabConfig.isTabAnimationEnabled()) {
                    // Usar frame de animación si está habilitada
                    content = line.getContentAt(animationFrame);
                } else {
                    // Usar contenido estático
                    content = line.getContentAt(0);
                }
                
                // Procesar placeholders para el header (sin jugador específico)
                content = processPlaceholders(content, null);
                headerBuilder.append(content);
                
                if (i < headerLines.size() - 1) {
                    headerBuilder.append("\n");
                }
            }
        }
        
        headerBuilder.append("\n");
        return MessageUtils.getColor(headerBuilder.toString());
    }

    private String buildFooter() {
        StringBuilder footerBuilder = new StringBuilder("\n");
        
        List<TabConfig.TabLine> footerLines = tabConfig.getTabFooterLines();
        if (footerLines != null && !footerLines.isEmpty()) {
            for (int i = 0; i < footerLines.size(); i++) {
                TabConfig.TabLine line = footerLines.get(i);
                String content;
                
                if (line.isAnimated() && tabConfig.isTabAnimationEnabled()) {
                    // Usar frame de animación si está habilitada
                    content = line.getContentAt(animationFrame);
                } else {
                    // Usar contenido estático
                    content = line.getContentAt(0);
                }
                
                // Procesar placeholders para el footer (sin jugador específico)
                content = processPlaceholders(content, null);
                footerBuilder.append(content);
                
                if (i < footerLines.size() - 1) {
                    footerBuilder.append("\n");
                }
            }
        }
        
        footerBuilder.append("\n");
        return MessageUtils.getColor(footerBuilder.toString());
    }

    private void updatePlayerList() {
        // Solo proceder si el formato de tab está habilitado
        if (!chatConfig.isTabEnabled()) {
            return;
        }

        // Obtener y ordenar jugadores por grupo
        List<Player> sortedPlayers = getSortedPlayersByGroup();

        for (Player player : sortedPlayers) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            
            // Obtener formato de tab apropiado
            String displayFormat = getTabFormat(player);
            
            // Procesar placeholders para cada jugador específico
            String displayName = processPlaceholders(displayFormat, player);
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

    /**
     * Nueva función para actualizar los nametags (prefijo y sufijo encima del jugador)
     */
    private void updateNameTags() {
        for (Player target : Bukkit.getOnlinePlayers()) {
            // Obtener el display name personalizado para este jugador
            String displayName = getTabDisplayName(target);
            
            // Separar prefix y suffix del display name
            NameTagData nameTagData = parseNameTagData(displayName, target);
            
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
                
                String teamName = "nt_" + target.getName();
                Team team = board.getTeam(teamName);
                
                if (team == null) {
                    // Crear nuevo team si no existe
                    try {
                        team = board.registerNewTeam(teamName);
                    } catch (IllegalArgumentException e) {
                        // Team ya existe, obtenerlo
                        team = board.getTeam(teamName);
                        if (team == null) continue; // Skip si no se puede obtener
                    }
                }
                
                // Actualizar prefix y suffix
                team.setPrefix(MessageUtils.getColor(nameTagData.getPrefix()));
                team.setSuffix(MessageUtils.getColor(nameTagData.getSuffix()));
                
                // Añadir jugador al team si no está
                if (!team.hasEntry(target.getName())) {
                    team.addEntry(target.getName());
                }
                
                // Aplicar scoreboard al viewer
                viewer.setScoreboard(board);
            }
        }
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
                // Limpiar espacios extra al final del prefix
                prefix = prefix.trim();
            }
            
            // Extraer suffix (todo después del nombre del jugador)
            int afterNameIndex = displayName.indexOf(playerName) + playerName.length();
            if (afterNameIndex < displayName.length()) {
                suffix = displayName.substring(afterNameIndex);
                // Limpiar espacios extra al principio del suffix
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

    private String getTabFormat(Player player) {
        if (luckPermsEnabled) {
            // Intentar obtener formato específico del grupo de LuckPerms
            String primaryGroup = getPlayerPrimaryGroup(player);
            if (primaryGroup != null) {
                String groupFormat = chatConfig.getTabGroupFormat(primaryGroup);
                if (groupFormat != null) {
                    return groupFormat;
                }
            }
        }
        
        // Usar formato por defecto si no hay LuckPerms o no se encontró formato específico
        return chatConfig.getTabDefaultFormat();
    }

    private String getTabDisplayName(Player player) {
        if (luckPermsEnabled) {
            // Intentar obtener display name específico del grupo de LuckPerms
            String primaryGroup = getPlayerPrimaryGroup(player);
            if (primaryGroup != null) {
                String groupDisplayName = chatConfig.getTabGroupDisplayName(primaryGroup);
                if (groupDisplayName != null) {
                    return processPlaceholders(groupDisplayName, player);
                }
            }
        }
        
        // Usar display name por defecto si no hay LuckPerms o no se encontró formato específico
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

    // Nuevo método para ordenar jugadores por grupo según la configuración
    private List<Player> getSortedPlayersByGroup() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        if (!luckPermsEnabled) {
            // Si no está LuckPerms, ordenar alfabéticamente
            Collections.sort(players, Comparator.comparing(Player::getName));
            return players;
        }
        
        // Ordenar por prioridad de grupo y luego por nombre
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                String group1 = getPlayerPrimaryGroup(p1);
                String group2 = getPlayerPrimaryGroup(p2);
                
                int priority1 = chatConfig.getGroupPriority(group1);
                int priority2 = chatConfig.getGroupPriority(group2);
                
                // Comparar por prioridad primero
                int priorityComparison = Integer.compare(priority1, priority2);
                if (priorityComparison != 0) {
                    return priorityComparison;
                }
                
                // Si tienen la misma prioridad, ordenar alfabéticamente
                return p1.getName().compareToIgnoreCase(p2.getName());
            }
        });
        
        return players;
    }

    /**
     * Método para limpiar teams de un jugador cuando se desconecta
     */
    public void cleanupPlayerTeams(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;
            
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            
            String teamName = "nt_" + player.getName();
            Team team = board.getTeam(teamName);
            if (team != null) {
                team.unregister();
            }
        }
    }

    /**
     * Método para actualizar nametags de un jugador específico (útil para cambios de rango)
     */
    public void updatePlayerNameTag(Player player) {
        String displayName = getTabDisplayName(player);
        NameTagData nameTagData = parseNameTagData(displayName, player);
        
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            
            Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
            
            String teamName = "nt_" + player.getName();
            Team team = board.getTeam(teamName);
            
            if (team == null) {
                try {
                    team = board.registerNewTeam(teamName);
                } catch (IllegalArgumentException e) {
                    team = board.getTeam(teamName);
                    if (team == null) continue;
                }
            }
            
            team.setPrefix(MessageUtils.getColor(nameTagData.getPrefix()));
            team.setSuffix(MessageUtils.getColor(nameTagData.getSuffix()));
            
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
            
            viewer.setScoreboard(board);
        }
    }

    public void reload() {
        plugin.getLogger().info("Reloading TabManager...");
        
        // Recargar integración con LuckPerms
        this.luckPermsEnabled = setupLuckPerms();
        
        // Recargar configuración
        tabConfig.reload();
        
        // Debug: mostrar el nuevo orden
        chatConfig.printCurrentOrder();
        
        // Forzar actualización inmediata del orden de jugadores
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updatePlayerList();
            updateNameTags();
            plugin.getLogger().info("TabManager reload completed - Player order updated");
        }, 2L);
        
        // Reiniciar animación si está habilitada
        if (tabConfig.isTabEnabled()) {
            startAnimation();
        } else {
            // Detener animación y limpiar tab si está deshabilitada
            if (animationTask != null) {
                animationTask.cancel();
                animationTask = null;
            }
            clearTabDisplay();
        }
    }

    public void stop() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        clearTabDisplay();
        cleanupAllTeams();
    }

    /**
     * Limpiar todos los teams de nametags
     */
    private void cleanupAllTeams() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            cleanupPlayerTeams(player);
        }
    }

    private void clearTabDisplay() {
        // Limpiar header y footer (enviar componentes vacíos)
        clearHeaderFooter();
        
        // Restaurar nombres originales de los jugadores
        restorePlayerNames();
    }

    private void clearHeaderFooter() {
        // Crear componentes vacíos para header y footer
        IChatBaseComponent emptyComponent = ChatSerializer.a("{\"text\": \"\"}");

        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
            try {
                Field a = packet.getClass().getDeclaredField("a");
                a.setAccessible(true);
                a.set(packet, emptyComponent);
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                b.set(packet, emptyComponent);
                craftPlayer.getHandle().playerConnection.sendPacket(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void restorePlayerNames() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            
            // Restaurar el nombre original del jugador en la lista
            craftPlayer.setPlayerListName(player.getName());
            
            // Enviar paquete de actualización para restaurar el nombre original
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, 
                craftPlayer.getHandle()
            );

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
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