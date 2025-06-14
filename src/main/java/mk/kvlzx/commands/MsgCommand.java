package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import mk.kvlzx.utils.MessageUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MsgCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final FriendCommand friendCommand;
    private final HashMap<UUID, UUID> lastMessage = new HashMap<>();
    private final Set<UUID> socialSpyEnabled = new HashSet<>();

    public MsgCommand(JavaPlugin plugin, FriendCommand friendCommand) {
        this.plugin = plugin;
        this.friendCommand = friendCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMsg(sender, "&cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("socialspy")) {
            handleSocialSpy(player);
            return true;
        }

        if (label.equalsIgnoreCase("r") || label.equalsIgnoreCase("reply")) {
            if (args.length == 0) {
                MessageUtils.sendMsg(player, "&cUsage: /" + label + " <message>");
                return true;
            }
            handleReply(player, args);
            return true;
        }

        if (args.length < 2) {
            MessageUtils.sendMsg(player, "&cUsage: /msg <player> <message>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            MessageUtils.sendMsg(player, "&cPlayer " + args[0] + " is not online.");
            return true;
        }

        if (target.equals(player)) {
            MessageUtils.sendMsg(player, "&cYou cannot send messages to yourself.");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        String filteredMessage = filterMessage(message.toString());
        boolean isFriend = friendCommand.getFriends(player.getUniqueId()).contains(target.getUniqueId());
        String prefix = isFriend ? "&a[FRIEND] " : "&e[MSG] ";
        String formattedMessage = prefix + "&7-> &b" + player.getName() + " &7-> &b" + target.getName() + ": &f" + filteredMessage;

        MessageUtils.sendMsg(player, formattedMessage);
        MessageUtils.sendMsg(target, formattedMessage);
        lastMessage.put(target.getUniqueId(), player.getUniqueId());

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (socialSpyEnabled.contains(onlinePlayer.getUniqueId()) && !onlinePlayer.equals(player) && !onlinePlayer.equals(target)) {
                MessageUtils.sendMsg(onlinePlayer, "&8[SPY] " + formattedMessage);
            }
        }

        logMessage(player.getName(), target.getName(), filteredMessage);
        return true;
    }

    private void handleReply(Player player, String[] args) {
        UUID lastSenderUUID = lastMessage.get(player.getUniqueId());
        if (lastSenderUUID == null) {
            MessageUtils.sendMsg(player, "&cYou have no recent messages to reply to.");
            return;
        }

        Player target = plugin.getServer().getPlayer(lastSenderUUID);
        if (target == null || !target.isOnline()) {
            MessageUtils.sendMsg(player, "&cThe player is not online.");
            return;
        }

        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            message.append(arg).append(" ");
        }

        String filteredMessage = filterMessage(message.toString());
        boolean isFriend = friendCommand.getFriends(player.getUniqueId()).contains(target.getUniqueId());
        String prefix = isFriend ? "&a[FRIEND] " : "&e[MSG] ";
        String formattedMessage = prefix + "&7-> &b" + player.getName() + " &7-> &b" + target.getName() + ": &f" + filteredMessage;

        MessageUtils.sendMsg(player, formattedMessage);
        MessageUtils.sendMsg(target, formattedMessage);
        lastMessage.put(target.getUniqueId(), player.getUniqueId());

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (socialSpyEnabled.contains(onlinePlayer.getUniqueId()) && !onlinePlayer.equals(player) && !onlinePlayer.equals(target)) {
                MessageUtils.sendMsg(onlinePlayer, "&8[SPY] " + formattedMessage);
            }
        }

        logMessage(player.getName(), target.getName(), filteredMessage);
    }

    private void handleSocialSpy(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (player.hasPermission("staff.spy")) {
            if (socialSpyEnabled.contains(playerUUID)) {
                socialSpyEnabled.remove(playerUUID);
                MessageUtils.sendMsg(player, "&8SocialSpy disabled.");
            } else {
                socialSpyEnabled.add(playerUUID);
                MessageUtils.sendMsg(player, "&8SocialSpy enabled.");
            }
        } else {
            MessageUtils.sendMsg(player, "&cYou do not have permission to use SocialSpy.");
        }
    }

    private String filterMessage(String message) {
        List<String> badWords = plugin.getConfig().getStringList("bad-words");
        String filtered = message;
        for (String word : badWords) {
            filtered = filtered.replaceAll("(?i)" + word, "****");
        }
        return filtered;
    }

    private void logMessage(String sender, String target, String message) {
        File logFile = new File(plugin.getDataFolder(), "logs/messages.log");
        if (!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }

        try (FileWriter fw = new FileWriter(logFile, true)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fw.write("[" + timestamp + "] " + sender + " -> " + target + ": " + message + "\n");
        } catch (IOException e) {
            plugin.getLogger().severe("Error writing to log file: " + e.getMessage());
        }
    }
}