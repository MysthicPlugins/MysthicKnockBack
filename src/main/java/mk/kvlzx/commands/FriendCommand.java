package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class FriendCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Map<UUID, Set<UUID>> friends = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> requestCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> notificationsEnabled = new ConcurrentHashMap<>();
    private File friendFile;
    private FileConfiguration friendConfig;
    private final int maxFriends;
    private final long requestCooldown = 60 * 1000; // 1 minute

    public FriendCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.maxFriends = plugin.getConfig().getInt("max-friends", 50);
        loadFriends();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMsg(sender, "&cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showFriendCommands(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                if (args.length != 2) {
                    MessageUtils.sendMsg(player, "&cUsage: /friend add <player>");
                    return true;
                }
                handleFriendAdd(player, args[1]);
                break;

            case "list":
                handleFriendList(player, false);
                break;

            case "online":
                handleFriendList(player, true);
                break;

            case "remove":
                if (args.length != 2) {
                    MessageUtils.sendMsg(player, "&cUsage: /friend remove <player>");
                    return true;
                }
                handleFriendRemove(player, args[1]);
                break;

            case "requests":
                handleFriendRequests(player);
                break;

            case "toggle":
                handleToggleNotifications(player);
                break;

            default:
                MessageUtils.sendMsg(player, "&cUnknown subcommand. Use /friend to see available commands.");
                break;
        }

        return true;
    }

    private void showFriendCommands(Player player) {
        MessageUtils.sendMsg(player, "&e=== Friend Commands ===");
        MessageUtils.sendMsg(player, "&b/friend add <player> &f- Sends a friend request to a player.");
        MessageUtils.sendMsg(player, "&b/friend list &f- Shows your friend list.");
        MessageUtils.sendMsg(player, "&b/friend online &f- Shows your online friends.");
        MessageUtils.sendMsg(player, "&b/friend remove <player> &f- Removes a player from your friend list.");
        MessageUtils.sendMsg(player, "&b/friend requests &f- Shows pending friend requests.");
        MessageUtils.sendMsg(player, "&b/friend toggle &f- Toggles friend notifications on/off.");
    }

    private void handleFriendAdd(Player sender, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            MessageUtils.sendMsg(sender, "&cPlayer " + targetName + " is not online.");
            return;
        }

        if (target.equals(sender)) {
            MessageUtils.sendMsg(sender, "&cYou cannot send a friend request to yourself.");
            return;
        }

        UUID senderUUID = sender.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        if (friends.getOrDefault(senderUUID, new HashSet<>()).contains(targetUUID)) {
            MessageUtils.sendMsg(sender, "&cYou are already friends with " + targetName + ".");
            return;
        }

        if (friends.getOrDefault(senderUUID, new HashSet<>()).size() >= maxFriends) {
            MessageUtils.sendMsg(sender, "&cYou have reached the friend limit (" + maxFriends + ").");
            return;
        }

        Map<UUID, Long> senderCooldowns = requestCooldowns.computeIfAbsent(senderUUID, k -> new HashMap<>());
        if (senderCooldowns.containsKey(targetUUID) && (System.currentTimeMillis() - senderCooldowns.get(targetUUID)) < requestCooldown) {
            MessageUtils.sendMsg(sender, "&cYou must wait before sending another request to " + targetName + ".");
            return;
        }

        if (pendingRequests.getOrDefault(targetUUID, new HashSet<>()).contains(senderUUID)) {
            MessageUtils.sendMsg(sender, "&cYou have already sent a friend request to " + targetName + ".");
            return;
        }

        pendingRequests.computeIfAbsent(targetUUID, k -> new HashSet<>()).add(senderUUID);
        senderCooldowns.put(targetUUID, System.currentTimeMillis());
        saveFriends();

        MessageUtils.sendMsg(target, "&e" + sender.getName() + " has sent you a friend request.");

        TextComponent acceptButton = new TextComponent("[ACCEPT]");
        acceptButton.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendaccept " + sender.getName()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Accept the friend request").color(net.md_5.bungee.api.ChatColor.GREEN).create()));

        TextComponent rejectButton = new TextComponent("[REJECT]");
        rejectButton.setColor(net.md_5.bungee.api.ChatColor.RED);
        rejectButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendreject " + sender.getName()));
        rejectButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Reject the friend request").color(net.md_5.bungee.api.ChatColor.RED).create()));

        TextComponent ignoreButton = new TextComponent("[IGNORE]");
        ignoreButton.setColor(net.md_5.bungee.api.ChatColor.GRAY);
        ignoreButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendignore " + sender.getName()));
        ignoreButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Ignore the friend request").color(net.md_5.bungee.api.ChatColor.GRAY).create()));

        target.spigot().sendMessage(acceptButton, rejectButton, ignoreButton);
        MessageUtils.sendMsg(sender, "&aFriend request sent to " + targetName + ".");
    }

    private void handleFriendList(Player player, boolean onlineOnly) {
        Set<UUID> friendUUIDs = friends.getOrDefault(player.getUniqueId(), new HashSet<>());
        if (friendUUIDs.isEmpty()) {
            MessageUtils.sendMsg(player, "&eYou have no friends added.");
            return;
        }

        MessageUtils.sendMsg(player, "&e=== Friend List" + (onlineOnly ? " (Online)" : "") + " ===");
        for (UUID friendUUID : friendUUIDs) {
            Player friend = plugin.getServer().getPlayer(friendUUID);
            if (onlineOnly && (friend == null || !friend.isOnline())) {
                continue;
            }
            String friendName = friend != null ? friend.getName() : plugin.getServer().getOfflinePlayer(friendUUID).getName();
            int kills = PlayerStats.getStats(friendUUID).getKills();

            TextComponent friendLabel = new TextComponent(ChatColor.AQUA + friendName + ChatColor.WHITE + " (Kills: " + kills + ") ");
            TextComponent messageButton = new TextComponent("[Message]");
            messageButton.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            messageButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msg " + friendName));
            messageButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Send a message").color(net.md_5.bungee.api.ChatColor.YELLOW).create()));

            TextComponent removeButton = new TextComponent("[Remove]");
            removeButton.setColor(net.md_5.bungee.api.ChatColor.RED);
            removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend remove " + friendName));
            removeButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Remove friend").color(net.md_5.bungee.api.ChatColor.RED).create()));

            player.spigot().sendMessage(friendLabel, messageButton, removeButton);
        }
    }

    private void handleFriendRemove(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        UUID targetUUID = target != null ? target.getUniqueId() : plugin.getServer().getOfflinePlayer(targetName).getUniqueId();

        Set<UUID> playerFriends = friends.getOrDefault(player.getUniqueId(), new HashSet<>());
        if (!playerFriends.contains(targetUUID)) {
            MessageUtils.sendMsg(player, "&c" + targetName + " is not in your friend list.");
            return;
        }

        playerFriends.remove(targetUUID);
        friends.put(player.getUniqueId(), playerFriends);

        Set<UUID> targetFriends = friends.getOrDefault(targetUUID, new HashSet<>());
        targetFriends.remove(player.getUniqueId());
        friends.put(targetUUID, targetFriends);

        saveFriends();

        MessageUtils.sendMsg(player, "&aYou have removed " + targetName + " from your friend list.");
        if (target != null && target.isOnline()) {
            MessageUtils.sendMsg(target, "&e" + player.getName() + " has removed you from their friend list.");
        }
    }

    private void handleFriendRequests(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<UUID> pending = pendingRequests.getOrDefault(playerUUID, new HashSet<>());
        if (pending.isEmpty()) {
            MessageUtils.sendMsg(player, "&eYou have no pending friend requests.");
            return;
        }

        MessageUtils.sendMsg(player, "&e=== Pending Friend Requests ===");
        for (UUID senderUUID : pending) {
            String senderName = plugin.getServer().getOfflinePlayer(senderUUID).getName();

            TextComponent requestLabel = new TextComponent(ChatColor.AQUA + senderName + ": ");
            TextComponent acceptButton = new TextComponent("[ACCEPT]");
            acceptButton.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendaccept " + senderName));
            acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Accept").color(net.md_5.bungee.api.ChatColor.GREEN).create()));

            TextComponent rejectButton = new TextComponent("[REJECT]");
            rejectButton.setColor(net.md_5.bungee.api.ChatColor.RED);
            rejectButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friendreject " + senderName));
            rejectButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Reject").color(net.md_5.bungee.api.ChatColor.RED).create()));

            player.spigot().sendMessage(requestLabel, acceptButton, rejectButton);
        }
    }

    private void handleToggleNotifications(Player player) {
        UUID playerUUID = player.getUniqueId();
        boolean currentState = notificationsEnabled.getOrDefault(playerUUID, true);
        notificationsEnabled.put(playerUUID, !currentState);
        MessageUtils.sendMsg(player, "&eFriend notifications " + (!currentState ? "enabled" : "disabled") + ".");
    }

    public void handleFriendAccept(Player player, String senderName) {
        Player sender = plugin.getServer().getPlayer(senderName);
        if (sender == null || !sender.isOnline()) {
            MessageUtils.sendMsg(player, "&cPlayer " + senderName + " is not online.");
            return;
        }

        UUID senderUUID = sender.getUniqueId();
        UUID playerUUID = player.getUniqueId();

        if (!pendingRequests.getOrDefault(playerUUID, new HashSet<>()).contains(senderUUID)) {
            MessageUtils.sendMsg(player, "&cYou do not have a pending friend request from " + senderName + ".");
            return;
        }

        if (friends.getOrDefault(playerUUID, new HashSet<>()).size() >= maxFriends) {
            MessageUtils.sendMsg(player, "&cYou have reached the friend limit (" + maxFriends + ").");
            pendingRequests.get(playerUUID).remove(senderUUID);
            saveFriends();
            return;
        }

        friends.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(senderUUID);
        friends.computeIfAbsent(senderUUID, k -> new HashSet<>()).add(playerUUID);
        pendingRequests.get(playerUUID).remove(senderUUID);

        saveFriends();

        MessageUtils.sendMsg(player, "&aYou have accepted " + senderName + "'s friend request.");
        MessageUtils.sendMsg(sender, "&a" + player.getName() + " has accepted your friend request.");
    }

    public void handleFriendReject(Player player, String senderName) {
        Player sender = plugin.getServer().getPlayer(senderName);
        UUID senderUUID = sender != null ? sender.getUniqueId() : plugin.getServer().getOfflinePlayer(senderName).getUniqueId();
        UUID playerUUID = player.getUniqueId();

        if (!pendingRequests.getOrDefault(playerUUID, new HashSet<>()).contains(senderUUID)) {
            MessageUtils.sendMsg(player, "&cYou do not have a pending friend request from " + senderName + ".");
            return;
        }

        pendingRequests.get(playerUUID).remove(senderUUID);
        saveFriends();

        MessageUtils.sendMsg(player, "&cYou have rejected " + senderName + "'s friend request.");
        if (sender != null && sender.isOnline()) {
            MessageUtils.sendMsg(sender, "&c" + player.getName() + " has rejected your friend request.");
        }
    }

    public void handleFriendIgnore(Player player, String senderName) {
        Player sender = plugin.getServer().getPlayer(senderName);
        UUID senderUUID = sender != null ? sender.getUniqueId() : plugin.getServer().getOfflinePlayer(senderName).getUniqueId();
        UUID playerUUID = player.getUniqueId();

        if (!pendingRequests.getOrDefault(playerUUID, new HashSet<>()).contains(senderUUID)) {
            MessageUtils.sendMsg(player, "&cYou do not have a pending friend request from " + senderName + ".");
            return;
        }

        pendingRequests.get(playerUUID).remove(senderUUID);
        saveFriends();

        MessageUtils.sendMsg(player, "&8You have ignored " + senderName + "'s friend request.");
    }

    public Set<UUID> getFriends(UUID playerUUID) {
        return friends.getOrDefault(playerUUID, new HashSet<>());
    }

    public boolean areNotificationsEnabled(UUID playerUUID) {
        return notificationsEnabled.getOrDefault(playerUUID, true);
    }

    public void saveFriends() {
        friendFile = new File(plugin.getDataFolder(), "friends.yml");
        friendConfig = new YamlConfiguration();

        for (Map.Entry<UUID, Set<UUID>> entry : friends.entrySet()) {
            UUID playerUUID = entry.getKey();
            Set<UUID> friendSet = entry.getValue();
            List<String> friendUUIDs = new java.util.ArrayList<>();
            for (UUID friendUUID : friendSet) {
                friendUUIDs.add(friendUUID.toString());
            }
            friendConfig.set(playerUUID.toString() + ".friends", friendUUIDs);
        }

        for (Map.Entry<UUID, Set<UUID>> entry : pendingRequests.entrySet()) {
            UUID playerUUID = entry.getKey();
            Set<UUID> pendingSet = entry.getValue();
            List<String> pendingUUIDs = new java.util.ArrayList<>();
            for (UUID pendingUUID : pendingSet) {
                pendingUUIDs.add(pendingUUID.toString());
            }
            friendConfig.set(playerUUID.toString() + ".pending", pendingUUIDs);
        }

        try {
            friendConfig.save(friendFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving friends.yml: " + e.getMessage());
        }
    }

    private void loadFriends() {
        friendFile = new File(plugin.getDataFolder(), "friends.yml");
        friendConfig = YamlConfiguration.loadConfiguration(friendFile);

        for (String playerUUIDStr : friendConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(playerUUIDStr);
            List<String> friendUUIDs = friendConfig.getStringList(playerUUIDStr + ".friends");
            Set<UUID> friendSet = new HashSet<>();
            for (String friendUUID : friendUUIDs) {
                friendSet.add(UUID.fromString(friendUUID));
            }
            friends.put(playerUUID, friendSet);

            List<String> pendingUUIDs = friendConfig.getStringList(playerUUIDStr + ".pending");
            Set<UUID> pendingSet = new HashSet<>();
            for (String pendingUUID : pendingUUIDs) {
                pendingSet.add(UUID.fromString(pendingUUID));
            }
            pendingRequests.put(playerUUID, pendingSet);
        }
    }
}