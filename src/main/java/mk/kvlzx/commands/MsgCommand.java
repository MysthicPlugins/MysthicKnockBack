package mk.kvlzx.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class MsgCommand implements CommandExecutor {
    
    private final MysthicKnockBack plugin;
    private final IgnoreCommand ignoreCommand;
    // Almacenar la última persona con la que cada jugador habló
    private static Map<UUID, UUID> lastMessagedPlayer = new HashMap<>();

    public MsgCommand(MysthicKnockBack plugin, IgnoreCommand ignoreCommand) {
        this.plugin = plugin;
        this.ignoreCommand = ignoreCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            player.sendMessage(MessageUtils.getColor("&cUsage: /msg <player> <message>"));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(MessageUtils.getColor("&cPlayer " + targetName + " is not online."));
            return true;
        }
        
        if (target.equals(player)) {
            player.sendMessage(MessageUtils.getColor("&cYou cannot send a message to yourself."));
            return true;
        }
        
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();
        
        // Verificar si el target ignora al remitente
        if (ignoreCommand.isPlayerIgnored(playerId, targetId)) {
            player.sendMessage(MessageUtils.getColor("&c" + targetName + " is ignoring you."));
            return true;
        }
        
        // Construir el mensaje
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }
        String message = messageBuilder.toString();
        
        // Verificar si son amigos
        boolean areFriends = FriendCommand.areFriends(playerId, targetId);
        
        // Determinar el tag a usar
        String tag = areFriends ? "&7[&d&lFRIEND&7]" : "&7[&bMSG&7]";
        
        // Enviar mensaje al remitente
        String senderMessage = MessageUtils.getColor(tag + " &7from &8" + player.getName() + "&8: &f" + message);
        player.sendMessage(senderMessage);
        
        // Enviar mensaje al destinatario
        String receiverMessage = MessageUtils.getColor(tag + " &7from &8" + player.getName() + "&8: &f" + message);
        target.sendMessage(receiverMessage);
        
        // Actualizar el registro de último mensaje para ambos jugadores
        lastMessagedPlayer.put(playerId, targetId);
        lastMessagedPlayer.put(targetId, playerId);
        
        return true;
    }
    
    // Método estático para obtener el último jugador con el que se habló
    public static UUID getLastMessagedPlayer(UUID playerId) {
        return lastMessagedPlayer.get(playerId);
    }
    
    // Método estático para verificar si un jugador tiene conversación reciente
    public static boolean hasRecentConversation(UUID playerId) {
        return lastMessagedPlayer.containsKey(playerId);
    }
}