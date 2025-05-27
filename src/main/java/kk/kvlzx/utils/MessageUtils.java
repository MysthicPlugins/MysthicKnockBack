package kk.kvlzx.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtils {
    
    public static String getColor(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void sendMsg(CommandSender sender, String msg) {
        sender.sendMessage(getColor(msg));
    }

    public static String stripColor(String msg) {
        return ChatColor.stripColor(msg);
    }
}