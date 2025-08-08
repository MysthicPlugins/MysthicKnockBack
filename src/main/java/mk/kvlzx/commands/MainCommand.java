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

    public MainCommand(MysthicKnockBack plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args){

        if(!(sender instanceof Player)){
            // Permitir que la consola use el reload
            if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
                boolean success = plugin.reloadConfigs();
                if (success) {
                    Bukkit.getConsoleSender().sendMessage(MessageUtils.getColor(
                        MysthicKnockBack.getPrefix() + 
                        plugin.getMessagesConfig().getReloadConfig()
                    ));
                } else {
                    Bukkit.getConsoleSender().sendMessage(MessageUtils.getColor(
                        MysthicKnockBack.getPrefix() + 
                        "&cAn error occurred while reloading the configuration. Check console for details.")
                    );
                }
                return true;
            } else {
                Bukkit.getConsoleSender().sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou can only use this command as a player."));
                return true;
            }
        }

        Player player = (Player) sender;

        if(args.length >= 1){
            if(args[0].equalsIgnoreCase("pet")){
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() +"&f ≽^•⩊•^≼ &b " + player.getName()));            
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("mysthicknockback.reload")) {
                    boolean success = plugin.reloadConfigs();
                    if (success) {
                        sender.sendMessage(MessageUtils.getColor(
                            MysthicKnockBack.getPrefix() + 
                            plugin.getMessagesConfig().getReloadConfig()
                        ));
                    } else {
                        sender.sendMessage(MessageUtils.getColor(
                            MysthicKnockBack.getPrefix() + 
                            "&cAn error occurred while reloading the configuration. Check console for details.")
                        );
                    }
                    return true;
                } else {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + plugin.getMessagesConfig().getNoPermission()));
                }
                return true;
            }
            help(sender);
        } else {
            help(sender);
        }
        return true;
    }

    public void help(CommandSender sender){
        MessagesConfig messages = plugin.getMessagesConfig();
        for (String line : messages.getHelp()) {
            sender.sendMessage(MessageUtils.getColor(line));
        } 
    }
}