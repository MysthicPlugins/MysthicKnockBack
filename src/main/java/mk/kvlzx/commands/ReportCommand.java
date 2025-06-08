package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

import org.bukkit.command.CommandExecutor;

public class ReportCommand implements CommandExecutor {

        private final MysthicKnockBack plugin;

    public ReportCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command commnad, String string, String[] args){
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor("&cEste comando solo puede ser usado por jugadores."));
            return true;
        }

        Player player = (Player) sender;
        plugin.getMenuManager().openMenu(player, "player_list");
        return true;
    }

}
