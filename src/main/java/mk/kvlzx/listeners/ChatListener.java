package mk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import mk.kvlzx.managers.RankManager;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.MysthicKnockBack;

public class ChatListener implements Listener {
    private final MysthicKnockBack plugin;

    public ChatListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String rankPrefix = RankManager.getRankPrefix(stats.getElo());
        String playerName = player.getName();
        String message = event.getMessage();

        // Formato del chat: [Rango] Nombre: Mensaje
        event.setFormat(MessageUtils.getColor(rankPrefix + " &r" + playerName + "&7: &f" + message));
    }
}

