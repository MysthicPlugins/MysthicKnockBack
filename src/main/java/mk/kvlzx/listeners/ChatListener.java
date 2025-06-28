package mk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.clip.placeholderapi.PlaceholderAPI;
import mk.kvlzx.managers.RankManager;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.MainConfig;

public class ChatListener implements Listener {
    private final MysthicKnockBack plugin;
    private final MainConfig mainConfig;
    private final boolean placeholderAPIEnabled;

    public ChatListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getMainConfig();
        
        this.placeholderAPIEnabled = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Verificar si el chat está habilitado
        if (!mainConfig.getChatEnabled()) {
            return;
        }

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String rankPrefix = RankManager.getRankPrefix(stats.getElo());

        String playerName = player.getName();
        String message = event.getMessage();
        
        // IMPORTANTE: Escapar los % en el mensaje del jugador antes de procesar placeholders
        String escapedMessage = escapePlayerMessage(message);

        // Aplicar formato del chat - usar el mensaje escapado
        String formattedMessage = MessageUtils.getColor(mainConfig.getChatFormat()
            .replace("%kbffa_rank%", rankPrefix)
            .replace("%player_name%", playerName)
            .replace("%message%", escapedMessage));

        // Aplicar placeholders si PlaceholderAPI está disponible
        if (placeholderAPIEnabled) {
            try {
                formattedMessage = PlaceholderAPI.setPlaceholders(player, formattedMessage);
            } catch (Exception e) {
                // Si hay error con placeholders, usar el mensaje sin procesar placeholders adicionales
                plugin.getLogger().warning("Error processing placeholders for player " + playerName + ": " + e.getMessage());
                formattedMessage = MessageUtils.getColor(mainConfig.getChatFormat()
                    .replace("%kbffa_rank%", rankPrefix)
                    .replace("%player_name%", playerName)
                    .replace("%message%", escapedMessage));
            }
        }

        // Restaurar los % originales en el mensaje final
        formattedMessage = unescapePlayerMessage(formattedMessage);

        // Establecer el formato final
        event.setFormat(formattedMessage);
    }

    private String escapePlayerMessage(String message) {
        // Reemplazar % con un marcador temporal único que no cause conflictos
        return message.replace("%", "§PERCENT§");
    }

    private String unescapePlayerMessage(String message) {
        // Restaurar el % original
        return message.replace("§PERCENT§", "%");
    }
}

