package mk.kvlzx.commands;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TopCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;

    public TopCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(MessageUtils.getColor(
                MysthicKnockBack.getPrefix() + 
                plugin.getMessagesConfig().getTopUsage())
            );
            return true;
        }

        String topType = args[0].toLowerCase();

        switch (topType) {
            case "kills":
                plugin.getMenuManager().openMenu(player, "top_kills");
                break;
            case "elo":
                plugin.getMenuManager().openMenu(player, "top_elo");
                break;
            case "kdr":
                plugin.getMenuManager().openMenu(player, "top_kdr");
                break;
            case "streak":
                plugin.getMenuManager().openMenu(player, "top_streak");
                break;
            case "time":
                plugin.getMenuManager().openMenu(player, "top_time");
                break;
            default:
                player.sendMessage(MessageUtils.getColor(
                    MysthicKnockBack.getPrefix() + 
                    plugin.getMessagesConfig().getTopNotFound())
                );
                break;
        }

        return true;
    }
}
