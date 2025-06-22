package mk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.JoinMessageItem;
import mk.kvlzx.utils.MessageUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class JoinMessageListener implements Listener {

    private final MysthicKnockBack plugin;
    private final Random random = new Random();
    public static List<String> JOIN_MESSAGES = MysthicKnockBack.getInstance().getMessagesConfig().getJoinMessages();

    public JoinMessageListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String messageName = plugin.getCosmeticManager().getPlayerJoinMessage(player.getUniqueId());
        String joinMessage;
        if (messageName.equals("default")) {
            joinMessage = JOIN_MESSAGES.get(random.nextInt(JOIN_MESSAGES.size()));
        } else {
            JoinMessageItem messageItem = JoinMessageItem.getByName(messageName);
            joinMessage = messageItem != null ? messageItem.getMessage() : JOIN_MESSAGES.get(0);
        }
        event.setJoinMessage(MessageUtils.getColor(joinMessage.replace("%player%", player.getName())));
    }
}
