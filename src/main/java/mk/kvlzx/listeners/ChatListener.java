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

        // Aplicar formato del chat
        String formattedMessage = MessageUtils.getColor(mainConfig.getChatFormat()
            .replace("%kbffa_rank%", rankPrefix)
            .replace("%player_name%", playerName)
            .replace("%message%", message));

        // Aplicar placeholders si PlaceholderAPI está disponible
        if (placeholderAPIEnabled) {
            formattedMessage = PlaceholderAPI.setPlaceholders(player, formattedMessage);
        }

        // Establecer el formato final
        event.setFormat(formattedMessage);
    }
}

