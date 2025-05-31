package mk.kvlzx.commands;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
            if(args[0].equalsIgnoreCase("nashe")){
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +"&f ≽^•⩊•^≼ &b" + player.getName()));

            } else if(args[0].equalsIgnoreCase("fecha")){
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String date = dateFormat.format(new Date());
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "Fecha y hora actual: &e"+ date));

            } else if(args[0].equalsIgnoreCase("get")){
                // /kvknockback get <Author/Version>
                subCommandGet(sender, args);

            } else if(args[0].equalsIgnoreCase("VicioKB")){
            sender.sendMessage(MessageUtils.getColor(
                    MysthicKnockBack.prefix + "&fCuando llamas a un ex top... esperas una batalla digna de dioses. &6[gaboh] &festá listo. ¿Lo estás tú?" + player.getName() + 
                    "&4Recuerda: &fhasta los más grandes caen alguna vez. ¿Será hoy el día en que &6[gaboh] &fpruebe la derrota?" + "&6 DC: gaboh_"));
            } else {
            help(sender);
            }
        
        } else {
        help(sender);
        }
        return true;
    }

    public void help(CommandSender sender){
        sender.sendMessage(MessageUtils.getColor("--------&r &b&lComandos MysthicKnockBack &8&m--------"));
        sender.sendMessage(MessageUtils.getColor("/kb nashe"));
        sender.sendMessage(MessageUtils.getColor("/kb fecha"));
        sender.sendMessage(MessageUtils.getColor("/kb get <author/version>"));
        sender.sendMessage(MessageUtils.getColor("/kb VicioKB"));
        sender.sendMessage(MessageUtils.getColor("--------&r &b&lComandos MysthicKnockBack &8&m--------"));
    }

    public void subCommandGet(CommandSender sender, String[] args){
        if(!(sender.hasPermission("mysthicknockback.command.get"))){
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +"&c No tienes permisos para usar este comando."));
            return;
        }
        if(args.length == 1){
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +"&c Debes usar &7 /kb get author/version>"));
            return;
        }
        if(args[1].equalsIgnoreCase("author")){
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +"&f los autores del plugin son: &b"+ plugin.getDescription().getAuthors()));

        }else if(args[1].equalsIgnoreCase("version")){
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +"&f La version del plugin es: &b" + plugin.getDescription().getVersion()));

        }else{
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +"&c Debes usar &7 /kb get author/version>"));

        }
    }
}