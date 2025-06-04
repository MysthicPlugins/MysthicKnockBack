package mk.kvlzx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class MainCommand implements CommandExecutor{

    private MysthicKnockBack plugin;

    public MainCommand(MysthicKnockBack plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args){

        if(!(sender instanceof Player)){
            // Consola :3
            Bukkit.getConsoleSender().sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +"&c Solo puedes usar este comando siendo un jugador."));
            return true;
        }

        Player player = (Player) sender;

        if(args.length >= 1){
            if(args[0].equalsIgnoreCase("pet")){
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +"&f ≽^•⩊•^≼ &b " + player.getName()));
            }
            help(sender);
        } else {
        help(sender);
        }
        return true;
    }

    public void help(CommandSender sender){
        sender.sendMessage(MessageUtils.getColor("--------&r &b&lCommands MysthicKnockBack &8&m--------"));
        sender.sendMessage(MessageUtils.getColor("/kb pet"));
        sender.sendMessage(MessageUtils.getColor("/arena create <name>"));
        sender.sendMessage(MessageUtils.getColor("/arena setzone <arena> <zoneType>"));
        sender.sendMessage(MessageUtils.getColor("/arena setborder <arena> <size>"));
        sender.sendMessage(MessageUtils.getColor("/arena setspawn <arena>"));
        sender.sendMessage(MessageUtils.getColor("/arena delete <arena>"));
        sender.sendMessage(MessageUtils.getColor("/stats"));
        sender.sendMessage(MessageUtils.getColor("/stats <player>"));
        sender.sendMessage(MessageUtils.getColor("/stats set <player> <stat> <value>"));
        sender.sendMessage(MessageUtils.getColor("/stats add <player> <stat> <value>"));
        sender.sendMessage(MessageUtils.getColor("/stats remove <player> <stat> <value>"));
        sender.sendMessage(MessageUtils.getColor("/stats reset <player>"));
        sender.sendMessage(MessageUtils.getColor("/stats resetall"));
        sender.sendMessage(MessageUtils.getColor("--------&r &b&lCommands MysthicKnockBack &8&m--------"));
    }
}