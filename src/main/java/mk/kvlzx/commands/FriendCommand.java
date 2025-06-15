package mk.kvlzx.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.data.FriendData;
import mk.kvlzx.utils.MessageUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class FriendCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;
    private final IgnoreCommand ignoreCommand;
    private final FriendData friendData;
    
    private static Map<UUID, Set<UUID>> friends = new HashMap<>();
    private static Map<UUID, Set<UUID>> pendingRequests = new HashMap<>();
    private static Map<String, UUID> playerUUIDs = new HashMap<>();

    public FriendCommand(MysthicKnockBack plugin, IgnoreCommand ignoreCommand) {
        this.plugin = plugin;
        this.ignoreCommand = ignoreCommand;
        this.friendData = new FriendData(plugin);
        
        // Cargar datos al inicializar
        loadAllData();
    }
    
    public void loadAllData() {
        friends = friendData.loadFriends();
        pendingRequests = friendData.loadPendingRequests();
        playerUUIDs = friendData.loadPlayerUUIDs();
    }
    
    public void saveAllData() {
        friendData.saveAllData(friends, pendingRequests, playerUUIDs);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor("&cOnly players can use this command."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "add":
                handleAddFriend(player, args);
                break;
            case "remove":
                handleRemoveFriend(player, args);
                break;
            case "list":
                handleListFriends(player);
                break;
            case "accept":
                handleAcceptRequest(player, args);
                break;
            case "deny":
                handleDenyRequest(player, args);
                break;
            case "requests":
                handleShowRequests(player);
                break;
            case "help":
                sendHelpMessage(player);
                break;
            default:
                player.sendMessage(MessageUtils.getColor("&cUnknown subcommand. Use /friend help to see help."));
                break;
        }
        
        return true;
    }
    
    private void handleAddFriend(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.getColor("&cUsage: /friend add <player>"));
            return;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(MessageUtils.getColor("&cPlayer " + targetName + " is not online."));
            return;
        }
        
        if (target.equals(player)) {
            player.sendMessage(MessageUtils.getColor("&cYou cannot add yourself as a friend."));
            return;
        }
        
        UUID playerId = player.getUniqueId();
        UUID targetId = target.getUniqueId();
        
        // Verificar si el target ignora al remitente
        if (ignoreCommand.isPlayerIgnored(playerId, targetId)) {
            player.sendMessage(MessageUtils.getColor("&c" + targetName + " is ignoring you."));
            return;
        }
        
        // Ver si los jugadores ya son amigos
        if (areFriends(playerId, targetId)) {
            player.sendMessage(MessageUtils.getColor("&6You are already friends with " + target.getName() + "."));
            return;
        }
        
        // Ver si ya tienen una solicitud pendiente
        if (hasPendingRequest(targetId, playerId)) {
            player.sendMessage(MessageUtils.getColor("&6You already have a pending request from " + target.getName() + "."));
            return;
        }
        
        if (hasPendingRequest(playerId, targetId)) {
            player.sendMessage(MessageUtils.getColor("&6You already sent a request to " + target.getName() + "."));
            return;
        }
        
        // Mandar solicitud
        addPendingRequest(playerId, targetId);
        playerUUIDs.put(player.getName().toLowerCase(), playerId);
        playerUUIDs.put(target.getName().toLowerCase(), targetId);
        
        // Guardar datos después de la modificación
        saveAllData();
        
        player.sendMessage(MessageUtils.getColor("&aFriend request sent to " + target.getName() + "."));
        
        // Mandar los mensajes clickeables al target
        sendClickableRequest(target, player);
    }
    
    private void sendClickableRequest(Player target, Player requester) {
        target.sendMessage(MessageUtils.getColor("&b═-═-═-═-═-═-═-═-═-═-═-═-═-═-═-═-═-═"));
        target.sendMessage(MessageUtils.getColor("&6" + requester.getName() + " has sent you a friend request!"));
        
        // Crear los componentes clickeables
        TextComponent acceptButton = new TextComponent(MessageUtils.getColor("&a[ACCEPT]"));
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + requester.getName()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(MessageUtils.getColor("&aClick to accept the request")).create()));
        
        TextComponent separator = new TextComponent(MessageUtils.getColor("&7 | "));
        
        TextComponent denyButton = new TextComponent(MessageUtils.getColor("&c[DENY]"));
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + requester.getName()));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(MessageUtils.getColor("&cClick to deny the request")).create()));
        
        TextComponent message = new TextComponent("");
        message.addExtra(acceptButton);
        message.addExtra(separator);
        message.addExtra(denyButton);
        
        target.spigot().sendMessage(message);
        target.sendMessage(MessageUtils.getColor("&b═-═-═-═-═-═-═-═-═-═-═-═-═-═-═-═-═-═"));
    }
    
    private void handleRemoveFriend(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.getColor("&cUsage: /friend remove <player>"));
            return;
        }
        
        String targetName = args[1];
        UUID targetId = getPlayerUUID(targetName);
        
        if (targetId == null) {
            player.sendMessage(MessageUtils.getColor("&cPlayer " + targetName + " not found."));
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        if (!areFriends(playerId, targetId)) {
            player.sendMessage(MessageUtils.getColor("&c" + targetName + " is not in your friends list."));
            return;
        }
        
        removeFriend(playerId, targetId);
        
        // Guardar datos después de la modificación
        saveAllData();
        
        player.sendMessage(MessageUtils.getColor("&6You have removed " + targetName + " from your friends list."));
        
        // Notificar al otro jugador si está conectado
        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            target.sendMessage(MessageUtils.getColor("&6" + player.getName() + " has removed you from their friends list."));
        }
    }
    
    private void handleListFriends(Player player) {
        UUID playerId = player.getUniqueId();
        Set<UUID> friendList = friends.getOrDefault(playerId, new HashSet<>());
        
        if (friendList.isEmpty()) {
            player.sendMessage(MessageUtils.getColor("&6You have no friends added."));
            return;
        }
        
        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ FRIENDS LIST ═-═-═-═-═"));
        int count = 1;
        for (UUID friendId : friendList) {
            String friendName = getPlayerName(friendId);
            Player friend = Bukkit.getPlayer(friendId);
            String status = friend != null ? MessageUtils.getColor("&a (Online)") : MessageUtils.getColor("&7 (Offline)");
            player.sendMessage(MessageUtils.getColor("&6" + count + ". &f" + friendName + status));
            count++;
        }
        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ FRIENDS LIST ═-═-═-═-═"));
    }
    
    private void handleAcceptRequest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.getColor("&cUsage: /friend accept <player>"));
            return;
        }
        
        String requesterName = args[1];
        UUID requesterId = getPlayerUUID(requesterName);
        
        if (requesterId == null) {
            player.sendMessage(MessageUtils.getColor("&cPlayer " + requesterName + " not found."));
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        if (!hasPendingRequest(requesterId, playerId)) {
            player.sendMessage(MessageUtils.getColor("&cYou have no pending requests from " + requesterName + "."));
            return;
        }
        
        // Acceptar solicitud
        removePendingRequest(requesterId, playerId);
        addFriend(playerId, requesterId);
        
        // Guardar datos después de la modificación
        saveAllData();
        
        player.sendMessage(MessageUtils.getColor("&aYou have accepted " + requesterName + "'s friend request."));
        
        // Notificar al otro jugador
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null) {
            requester.sendMessage(MessageUtils.getColor("&a" + player.getName() + " has accepted your friend request!"));
        }
    }
    
    private void handleDenyRequest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(MessageUtils.getColor("&cUsage: /friend deny <player>"));
            return;
        }
        
        String requesterName = args[1];
        UUID requesterId = getPlayerUUID(requesterName);
        
        if (requesterId == null) {
            player.sendMessage(MessageUtils.getColor("&cPlayer " + requesterName + " not found."));
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        if (!hasPendingRequest(requesterId, playerId)) {
            player.sendMessage(MessageUtils.getColor("&cYou have no pending requests from " + requesterName + "."));
            return;
        }
        
        // Denegar solicitud
        removePendingRequest(requesterId, playerId);
        
        // Guardar datos después de la modificación
        saveAllData();
        
        player.sendMessage(MessageUtils.getColor("&6You have denied " + requesterName + "'s friend request."));
        
        // Notificar al otro jugador
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null) {
            requester.sendMessage(MessageUtils.getColor("&c" + player.getName() + " has denied your friend request."));
        }
    }
    
    private void handleShowRequests(Player player) {
        UUID playerId = player.getUniqueId();
        Set<UUID> requests = new HashSet<>();
        
        // Encontrar solicitudes pendientes
        for (Map.Entry<UUID, Set<UUID>> entry : pendingRequests.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                requests.add(entry.getKey());
            }
        }
        
        if (requests.isEmpty()) {
            player.sendMessage(MessageUtils.getColor("&eYou have no pending friend requests."));
            return;
        }
        
        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ PENDING REQUESTS ═-═-═-═-═"));
        int count = 1;
        for (UUID requesterId : requests) {
            String requesterName = getPlayerName(requesterId);
            player.sendMessage(MessageUtils.getColor("&6" + count + ". &f" + requesterName));
            count++;
        }
        player.sendMessage(MessageUtils.getColor("&7Use /friend accept <player> or /friend deny <player>"));
        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ PENDING REQUESTS ═-═-═-═-═"));
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ FRIEND SYSTEM ═-═-═-═-═"));
        player.sendMessage(MessageUtils.getColor("&b/friend add <player>&f - Send friend request"));
        player.sendMessage(MessageUtils.getColor("&b/friend remove <player>&f - Remove friend"));
        player.sendMessage(MessageUtils.getColor("&b/friend list&f - View friends list"));
        player.sendMessage(MessageUtils.getColor("&b/friend accept <player>&f - Accept request"));
        player.sendMessage(MessageUtils.getColor("&b/friend deny <player>&f - Deny request"));
        player.sendMessage(MessageUtils.getColor("&b/friend requests&f - View pending requests"));
        player.sendMessage(MessageUtils.getColor("&b/friend help&f - Show this help"));
        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ FRIEND SYSTEM ═-═-═-═-═"));
    }

    // Métodos públicos para el TabCompleter
    public static Set<UUID> getFriends(UUID playerId) {
        return friends.get(playerId);
    }

    public static Set<UUID> getPlayersWhoHaveAsFriend(UUID playerId) {
        Set<UUID> result = new HashSet<>();
        for (Map.Entry<UUID, Set<UUID>> entry : friends.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static boolean areFriends(UUID player1, UUID player2) {
        Set<UUID> friendList = friends.get(player1);
        return friendList != null && friendList.contains(player2);
    }
    
    // Métodos públicos para el TabCompleter
    public static boolean hasPendingRequestPublic(UUID from, UUID to) {
        return hasPendingRequest(from, to);
    }
    
    public static Set<UUID> getPendingRequestsFor(UUID playerId) {
        Set<UUID> requests = new HashSet<>();
        for (Map.Entry<UUID, Set<UUID>> entry : pendingRequests.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                requests.add(entry.getKey());
            }
        }
        return requests;
    }
    
    public static String getPlayerNamePublic(UUID uuid) {
        return getPlayerName(uuid);
    }
    
    public static UUID getPlayerUUIDPublic(String playerName) {
        return getPlayerUUID(playerName);
    }
    
    private static void addFriend(UUID player1, UUID player2) {
        friends.computeIfAbsent(player1, k -> new HashSet<>()).add(player2);
        friends.computeIfAbsent(player2, k -> new HashSet<>()).add(player1);
    }
    
    private static void removeFriend(UUID player1, UUID player2) {
        Set<UUID> list1 = friends.get(player1);
        Set<UUID> list2 = friends.get(player2);
        
        if (list1 != null) list1.remove(player2);
        if (list2 != null) list2.remove(player1);
    }
    
    private static boolean hasPendingRequest(UUID from, UUID to) {
        Set<UUID> requests = pendingRequests.get(from);
        return requests != null && requests.contains(to);
    }
    
    private static void addPendingRequest(UUID from, UUID to) {
        pendingRequests.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }
    
    private static void removePendingRequest(UUID from, UUID to) {
        Set<UUID> requests = pendingRequests.get(from);
        if (requests != null) {
            requests.remove(to);
            if (requests.isEmpty()) {
                pendingRequests.remove(from);
            }
        }
    }
    
    private static UUID getPlayerUUID(String playerName) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }
        
        return playerUUIDs.get(playerName.toLowerCase());
    }
    
    private static String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        
        for (Map.Entry<String, UUID> entry : playerUUIDs.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                return entry.getKey();
            }
        }
        
        return "Unknown Player";
    }
}