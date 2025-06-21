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
    public static List<String> JOIN_MESSAGES = Arrays.asList(
        "&e¡Watch out! &b{player} &ehas entered the server.",
        "&6{player} &ahas arrived to conquer the world.",
        "&c¡Alert! &f{player} &chas joined the chaos.",
        "&a{player} &bhas appeared with style on the server.",
        "&d¡Welcome, {player}! &5Let the adventure begin.",
        "&f{player} &7has connected stealthily...",
        "&b{player} &3has landed from another dimension.",
        "&e¡{player} &6has entered with a legendary aura!",
        "&c{player} &4is here to break blocks and hearts.",
        "&a¡Everyone greet {player}! &2New hero on the server.",
        "&b{player} &9has joined the epic battle.",
        "&d{player} &5brings magic and chaos to the server.",
        "&f¡{player} &ehas arrived to build their destiny!",
        "&6¡Attention! &c{player} &6is ready to dominate.",
        "&a{player} &2has joined the green side of the server.",
        "&b¡{player} &3has entered with an enchanted pickaxe!",
        "&e{player} &7whispers: 'Time to mine...'.",
        "&c¡Danger! &f{player} &cis here to cause trouble.",
        "&d{player} &bhas arrived with an elytra and big dreams.",
        "&a¡{player} &eis ready to explore the infinite!"
    );

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
        event.setJoinMessage(MessageUtils.getColor(joinMessage.replace("{player}", player.getName())));
    }
}
