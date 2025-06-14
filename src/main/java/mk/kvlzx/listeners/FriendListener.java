package mk.kvlzx.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.commands.FriendCommand;
import mk.kvlzx.utils.MessageUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FriendListener implements Listener {
    
    private final MysthicKnockBack plugin;

    public FriendListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();
        UUID joinedPlayerId = joinedPlayer.getUniqueId();
        
        // Obtener la lista de amigos del jugador que se conecta
        Set<UUID> friendsOfJoinedPlayer = FriendCommand.getFriends(joinedPlayerId);
        
        // Notificar a todos los amigos en línea que este jugador se conectó
        if (friendsOfJoinedPlayer != null && !friendsOfJoinedPlayer.isEmpty()) {
            for (UUID friendId : friendsOfJoinedPlayer) {
                Player onlineFriend = Bukkit.getPlayer(friendId);
                if (onlineFriend != null && onlineFriend.isOnline()) {
                    // Enviar notificación al amigo
                    onlineFriend.sendMessage(MessageUtils.getColor("&a&l✦ &7Friend &a" + joinedPlayer.getName() + " &7has joined the server!"));
                }
            }
        }
        
        // Obtener todos los amigos del jugador que se conecta que estén en línea
        // y notificar al jugador que se conecta sobre sus amigos en línea
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (joinedPlayer.isOnline()) {
                Set<String> onlineFriends = new HashSet<>();
                
                if (friendsOfJoinedPlayer != null && !friendsOfJoinedPlayer.isEmpty()) {
                    for (UUID friendId : friendsOfJoinedPlayer) {
                        Player onlineFriend = Bukkit.getPlayer(friendId);
                        if (onlineFriend != null && onlineFriend.isOnline()) {
                            onlineFriends.add(onlineFriend.getName());
                        }
                    }
                }
                
                // Enviar mensaje de bienvenida con amigos en línea
                if (!onlineFriends.isEmpty()) {
                    joinedPlayer.sendMessage(MessageUtils.getColor("&6&l✦ &eWelcome back! &7Your friends online: &a" + String.join("&7, &a", onlineFriends)));
                }
            }
        }, 20L); // Esperar 1 segundo
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player leavingPlayer = event.getPlayer();
        UUID leavingPlayerId = leavingPlayer.getUniqueId();
        
        // Obtener la lista de amigos del jugador que se desconecta
        Set<UUID> friendsOfLeavingPlayer = FriendCommand.getFriends(leavingPlayerId);
        
        // Notificar a todos los amigos en línea que este jugador se desconectó
        if (friendsOfLeavingPlayer != null && !friendsOfLeavingPlayer.isEmpty()) {
            for (UUID friendId : friendsOfLeavingPlayer) {
                Player onlineFriend = Bukkit.getPlayer(friendId);
                if (onlineFriend != null && onlineFriend.isOnline()) {
                    // Enviar notificación al amigo
                    onlineFriend.sendMessage(MessageUtils.getColor("&c&l✦ &7Friend &c" + leavingPlayer.getName() + " &7has left the server!"));
                }
            }
        }
    }
}
