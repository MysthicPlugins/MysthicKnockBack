package kk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import kk.kvlzx.managers.RankManager;
import kk.kvlzx.KvKnockback;

public class ChatListener implements Listener {
    private final KvKnockback plugin;

    public ChatListener(KvKnockback plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String rankPrefix = RankManager.getRankPrefix(player);
        String playerName = player.getName();
        String message = event.getMessage();

        // Formato del chat: [Rango] Nombre: Mensaje
        event.setFormat(rankPrefix + playerName + " &7: " + message);
    }
}

