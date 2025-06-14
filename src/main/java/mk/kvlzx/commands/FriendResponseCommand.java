package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import mk.kvlzx.utils.MessageUtils;

public class FriendResponseCommand implements CommandExecutor {
    private final FriendCommand friendCommand;

    public FriendResponseCommand(FriendCommand friendCommand) {
        this.friendCommand = friendCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMsg(sender, "&cThis command can only be used by players.");
            return true;
        }

        if (args.length != 1) {
            MessageUtils.sendMsg(sender, "&cUsage: /" + label + " <player>");
            return true;
        }

        Player player = (Player) sender;
        String senderName = args[0];

        switch (label.toLowerCase()) {
            case "friendaccept":
                friendCommand.handleFriendAccept(player, senderName);
                break;
            case "friendreject":
                friendCommand.handleFriendReject(player, senderName);
                break;
            case "friendignore":
                friendCommand.handleFriendIgnore(player, senderName);
                break;
        }

        return true;
    }
}
