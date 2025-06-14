package mk.kvlzx.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class FriendTabCompleter implements TabCompleter {
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Completar subcomandos
            List<String> subCommands = Arrays.asList(
                "add", "remove", "list", "accept", "deny", "requests", "help"
            );
            
            String input = args[0].toLowerCase();
            completions = subCommands.stream()
                .filter(sub -> sub.startsWith(input))
                .collect(Collectors.toList());
                
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String input = args[1].toLowerCase();
            
            switch (subCommand) {
                case "add":
                    // Sugerir jugadores en línea que no sean amigos y no tengan solicitudes pendientes
                    completions = getAvailablePlayersForFriendRequest(player, input);
                    break;
                    
                case "remove":
                    // Sugerir amigos actuales
                    completions = getCurrentFriends(player, input);
                    break;
                    
                case "accept":
                case "deny":
                    // Sugerir jugadores con solicitudes pendientes
                    completions = getPendingRequests(player, input);
                    break;
                    
                default:
                    completions = new ArrayList<>();
                    break;
            }
        }
        
        return completions;
    }
    
    /**
     * Obtiene jugadores disponibles para enviar solicitud de amistad
     */
    private List<String> getAvailablePlayersForFriendRequest(Player player, String input) {
        List<String> available = new ArrayList<>();
        UUID playerId = player.getUniqueId();
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // No incluir al jugador mismo
            if (onlinePlayer.equals(player)) {
                continue;
            }
            
            UUID targetId = onlinePlayer.getUniqueId();
            String targetName = onlinePlayer.getName();
            
            // Filtrar por el input del usuario
            if (!targetName.toLowerCase().startsWith(input)) {
                continue;
            }
            
            // No incluir si ya son amigos
            if (FriendCommand.areFriends(playerId, targetId)) {
                continue;
            }
            
            // No incluir si ya hay una solicitud pendiente (en cualquier dirección)
            if (hasPendingRequestBetween(playerId, targetId)) {
                continue;
            }
            
            available.add(targetName);
        }
        
        return available.stream()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene la lista de amigos actuales del jugador
     */
    private List<String> getCurrentFriends(Player player, String input) {
        List<String> friendNames = new ArrayList<>();
        UUID playerId = player.getUniqueId();
        
        Set<UUID> friends = FriendCommand.getFriends(playerId);
        if (friends != null) {
            for (UUID friendId : friends) {
                String friendName = getPlayerNameFromUUID(friendId);
                if (friendName != null && friendName.toLowerCase().startsWith(input)) {
                    friendNames.add(friendName);
                }
            }
        }
        
        return friendNames.stream()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene jugadores que han enviado solicitudes pendientes al jugador actual
     */
    private List<String> getPendingRequests(Player player, String input) {
        List<String> requesters = new ArrayList<>();
        UUID playerId = player.getUniqueId();
        
        // Obtener todas las solicitudes pendientes para este jugador
        Set<UUID> pendingRequesterIds = FriendCommand.getPendingRequestsFor(playerId);
        
        for (UUID requesterId : pendingRequesterIds) {
            String requesterName = FriendCommand.getPlayerNamePublic(requesterId);
            
            if (requesterName != null && requesterName.toLowerCase().startsWith(input)) {
                requesters.add(requesterName);
            }
        }
        
        return requesters.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el nombre de un jugador por su UUID
     */
    private String getPlayerNameFromUUID(UUID uuid) {
        return FriendCommand.getPlayerNamePublic(uuid);
    }
    
    /**
     * Verifica si hay una solicitud pendiente entre dos jugadores (en cualquier dirección)
     */
    private boolean hasPendingRequestBetween(UUID player1, UUID player2) {
        return hasPendingRequestFrom(player1, player2) || hasPendingRequestFrom(player2, player1);
    }
    
    /**
     * Verifica si el jugador 'from' ha enviado una solicitud al jugador 'to'
     */
    private boolean hasPendingRequestFrom(UUID from, UUID to) {
        return FriendCommand.hasPendingRequestPublic(from, to);
    }
}
