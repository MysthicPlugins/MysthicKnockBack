package mk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.clip.placeholderapi.PlaceholderAPI;
import mk.kvlzx.utils.MessageUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import mk.kvlzx.MysthicKnockBack;

public class ChatListener implements Listener {

    private final MysthicKnockBack plugin;
    private LuckPerms luckPerms;
    private boolean luckPermsEnabled;

    public ChatListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.luckPermsEnabled = setupLuckPerms();
    }

    private boolean setupLuckPerms() {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
                this.luckPerms = LuckPermsProvider.get();
                plugin.getLogger().info("LuckPerms integration enabled for chat formatting!");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into LuckPerms: " + e.getMessage());
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Verificar si el chat está habilitado
        if (!plugin.getChatConfig().isChatEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Obtener el formato de chat apropiado
        String chatFormat = getChatFormat(player);
        
        // Formatear el mensaje de forma segura
        String formattedMessage = formatChatMessage(player, message, chatFormat);
        
        // Establecer el formato del evento
        event.setFormat(formattedMessage);
    }

    private String getChatFormat(Player player) {
        if (luckPermsEnabled) {
            // Intentar obtener formato específico del grupo de LuckPerms
            String primaryGroup = getPlayerPrimaryGroup(player);
            if (primaryGroup != null) {
                String groupFormat = plugin.getChatConfig().getGroupFormat(primaryGroup);
                if (groupFormat != null) {
                    return groupFormat;
                }
            }
        }
        
        // Usar formato por defecto si no hay LuckPerms o no se encontró formato específico
        return plugin.getChatConfig().getDefaultFormat();
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

    private String formatChatMessage(Player player, String message, String format) {
        String processedMessage =  message.replace("%", "%%");
        
        // Crear template temporal para procesar el formato sin el mensaje
        String templateFormat = format.replace("%message%", "{MESSAGE_PLACEHOLDER}");
        
        // Reemplazar placeholders básicos del jugador
        templateFormat = templateFormat
                .replace("%player_name%", player.getName())
                .replace("%player%", player.getName());

        // Aplicar placeholders de PlaceholderAPI SOLO al formato, NO al mensaje del jugador
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            templateFormat = PlaceholderAPI.setPlaceholders(player, templateFormat);
        }

        // Aplicar colores al formato
        templateFormat = MessageUtils.getColor(templateFormat);
        
        // Finalmente, insertar el mensaje procesado del jugador
        String finalMessage = templateFormat.replace("{MESSAGE_PLACEHOLDER}", processedMessage);

        return finalMessage;
    }

    // Método para recargar la integración con LuckPerms
    public void reloadLuckPermsIntegration() {
        this.luckPermsEnabled = setupLuckPerms();
    }
}
