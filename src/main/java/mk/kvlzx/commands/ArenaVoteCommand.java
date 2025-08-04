package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class ArenaVoteCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;

    public ArenaVoteCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cThis command can only be executed by players!"));
            return true;
        }

        Player player = (Player) sender;
        
        // Comando para abrir menú de selección (/arenavote)
        if (command.getName().equals("arenavote")) {
            if (!player.hasPermission("mysthicknockback.vote.start")) {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                    plugin.getMessagesConfig().getNoPermission()));
                return true;
            }
            
            plugin.getMenuManager().openMenu(player, "select_arena");
            return true;
        }
        
        // Comando para votar (/arenuvote yes/no) - comando oculto para clicks
        if (command.getName().equals("arenuvote")) {
            if (args.length == 0) {
                return true;
            }
            
            String vote = args[0].toLowerCase();
            if (vote.equals("yes")) {
                plugin.getArenaVoteManager().vote(player, true);
            } else if (vote.equals("no")) {
                plugin.getArenaVoteManager().vote(player, false);
            }
            return true;
        }
        
        return true;
    }
}
