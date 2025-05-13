package ik.kvlzx.org.utils;

import net.md_5.bungee.api.ChatColor;

public class MessageUtils {

    public static String getColoredMessage(String message){
        return ChatColor.translateAlternateColorCodes('&', message);

    }

}