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
        
        if (!mainConfig.getChatEnabled()) {
            return;
        }

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String rankPrefix = RankManager.getRankPrefix(stats.getElo());
        String playerName = player.getName();
        String message = event.getMessage();
        
        // Escapar % en el mensaje del jugador
        String escapedMessage = message.replace("%", "%%");

        // Aplicar formato del chat
        String formattedMessage = MessageUtils.getColor(mainConfig.getChatFormat()
            .replace("%kbffa_rank%", rankPrefix)
            .replace("%player_name%", playerName)
            .replace("%message%", escapedMessage));

        // Aplicar placeholders si está disponible
        if (placeholderAPIEnabled) {
            try {
                formattedMessage = PlaceholderAPI.setPlaceholders(player, formattedMessage);
            } catch (Exception e) {
                plugin.getLogger().warning("Error processing placeholders for player " + playerName + ": " + e.getMessage());
                // Fallback sin placeholders adicionales
                formattedMessage = MessageUtils.getColor(mainConfig.getChatFormat()
                    .replace("%kbffa_rank%", rankPrefix)
                    .replace("%player_name%", playerName)
                    .replace("%message%", escapedMessage));
            }
        }

        // Establecer el formato final
        event.setFormat(formattedMessage);
    }
}

