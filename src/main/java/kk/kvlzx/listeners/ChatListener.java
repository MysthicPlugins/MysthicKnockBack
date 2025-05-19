package kk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import kk.kvlzx.managers.RankManager;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.KvKnockback;

public class ChatListener implements Listener {
    private final KvKnockback plugin;

    public ChatListener(KvKnockback plugin) {
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
        event.setFormat(MessageUtils.getColor(rankPrefix + " &r" + playerName + "&7: &f " + message));
    }
}

