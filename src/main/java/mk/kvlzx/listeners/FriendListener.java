package mk.kvlzx.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mk.kvlzx.commands.FriendCommand;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import java.util.Set;
import java.util.UUID;

public class FriendListener implements Listener {
    private final FriendCommand friendCommand;

    public FriendListener(FriendCommand friendCommand) {
        this.friendCommand = friendCommand;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Set<UUID> friends = friendCommand.getFriends(playerUUID);

        for (UUID friendUUID : friends) {
            Player friend = friendCommand.getPlugin().getServer().getPlayer(friendUUID);
            if (friend != null && friend.isOnline() && friendCommand.areNotificationsEnabled(friendUUID)) {
                MessageUtils.sendMsg(friend, "&eYour friend &b" + player.getName() + "&e has connected.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Set<UUID> friends = friendCommand.getFriends(playerUUID);

        for (UUID friendUUID : friends) {
            Player friend = friendCommand.getPlugin().getServer().getPlayer(friendUUID);
            if (friend != null && friend.isOnline() && friendCommand.areNotificationsEnabled(friendUUID)) {
                MessageUtils.sendMsg(friend, "&eYour friend &b" + player.getName() + "&e has disconnected.");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null && killer instanceof Player) {
            PlayerStats.getStats(killer.getUniqueId()).addKill();
        }
    }
}
