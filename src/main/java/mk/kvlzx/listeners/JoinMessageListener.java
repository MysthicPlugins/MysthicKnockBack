package mk.kvlzx.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.KillMessageItem;
import mk.kvlzx.utils.MessageUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class JoinMessageListener implements Listener {

    private final MysthicKnockBack plugin;
    private final Random random = new Random();
    public static List<String> JOIN_MESSAGES = Arrays.asList(
        "&e¡Cuidado! &b{player} &eha entrado al servidor.",
        "&6{player} &aha llegado para conquistar el mundo.",
        "&c¡Alerta! &f{player} &cse ha unido al caos.",
        "&a{player} &bha aparecido con estilo en el servidor.",
        "&d¡Bienvenido, {player}! &5Que comience la aventura.",
        "&f{player} &7se ha conectado sigilosamente...",
        "&b{player} &3ha aterrizado desde otra dimensión.",
        "&e¡{player} &6ha entrado con un aura legendaria!",
        "&c{player} &4está aquí para romper bloques y corazones.",
        "&a¡Todos saluden a {player}! &2Nuevo héroe en el servidor.",
        "&b{player} &9se ha unido a la batalla épica.",
        "&d{player} &5trae magia y caos al servidor.",
        "&f¡{player} &eha llegado para construir su destino!",
        "&6¡Atención! &c{player} &6está listo para dominar.",
        "&a{player} &2se ha unido al lado verde del servidor.",
        "&b¡{player} &3ha entrado con un pico encantado!",
        "&e{player} &7susurra: 'Hora de minar...'.",
        "&c¡Peligro! &f{player} &cestá aquí para causar problemas.",
        "&d{player} &bha llegado con un elytra y sueños grandes.",
        "&a¡{player} &eestá listo para explorar el infinito!"
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
            KillMessageItem messageItem = KillMessageItem.getByName(messageName);
            joinMessage = messageItem != null ? messageItem.getMessage() : JOIN_MESSAGES.get(0);
        }
        Bukkit.broadcastMessage(MessageUtils.getColor(
            joinMessage.replace("{player}", player.getName())
        ));
    }
}
