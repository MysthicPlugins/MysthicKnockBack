package mk.kvlzx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.MessagesConfig;
import mk.kvlzx.utils.MessageUtils;

public class MainCommand implements CommandExecutor{

    private MysthicKnockBack plugin;
    private MessagesConfig messages;

    public MainCommand(MysthicKnockBack plugin){
        this.plugin = plugin;
        this.messages = plugin.getMessagesConfig();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args){

        if(!(sender instanceof Player)){
            // Consola :3
            Bukkit.getConsoleSender().sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cYou can only use this command as a player."));
            return true;
        }

        Player player = (Player) sender;

        if(args.length >= 1){
            if(args[0].equalsIgnoreCase("pet")){
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +"&f ≽^•⩊•^≼ &b " + player.getName()));
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("mysthicknockback.reload")) {
                    try {
                        plugin.getMessagesConfig().reload();
                    } catch (Exception e) {
                        sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cAn error occurred while reloading the configuration"));
                        e.printStackTrace();
                        return true;
                    }
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getReloadConfig()));
                } else {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getNoPermission()));
                }
            }
            help(sender);
        } else {
        help(sender);
        }
        return true;
    }

    public void help(CommandSender sender){
        for (String line : messages.getHelp()) {
            sender.sendMessage(MessageUtils.getColor(line));
        } 
    }
}