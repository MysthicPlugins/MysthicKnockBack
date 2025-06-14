package mk.kvlzx.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class ReplyCommand implements CommandExecutor {
    
    private final MysthicKnockBack plugin;

    public ReplyCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(MessageUtils.getColor("&cUsage: /r <message>"));
            return true;
        }
        
        UUID playerId = player.getUniqueId();
        UUID targetId = MsgCommand.getLastMessagedPlayer(playerId);
        
        if (targetId == null) {
            player.sendMessage(MessageUtils.getColor("&cYou have no one to reply to."));
            return true;
        }
        
        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            player.sendMessage(MessageUtils.getColor("&cThe player you want to reply to is no longer online."));
            return true;
        }
        
        // Construir el mensaje
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }
        String message = messageBuilder.toString();
        
        // Verificar si son amigos
        boolean areFriends = FriendCommand.areFriends(playerId, targetId);
        
        // Determinar el tag a usar
        String tag = areFriends ? "&d&lFRIEND" : "&7&lMSG";
        
        // Enviar mensaje al remitente
        String senderMessage = MessageUtils.getColor(tag + " &7to " + target.getName() + "&7: &f" + message);
        player.sendMessage(senderMessage);
        
        // Enviar mensaje al destinatario
        String receiverMessage = MessageUtils.getColor(tag + " &7from " + player.getName() + "&7: &f" + message);
        target.sendMessage(receiverMessage);
        
        return true;
    }
}
