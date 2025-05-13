package ik.kvlzx.org.commands;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ik.kvlzx.org.IntKnock;
import ik.kvlzx.org.utils.MessageUtils;

public class MainComand implements CommandExecutor{

    private IntKnock plugin;
    public MainComand(IntKnock plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args){

        if(!(sender instanceof Player)){
            // Consola :3
             Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&c Solo puedes usar este comando siendo un jugador."));

            return true;
            
        }

        Player player = (Player) sender;

        if(args.length >= 1){
            if(args[0].equalsIgnoreCase("Nashe")){
                sender.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&f ≽^•⩊•^≼ &b" + player.getName()));


        }else if(args[0].equalsIgnoreCase("Fecha")){
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String date = dateFormat.format(new Date());
            sender.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix + "Fecha y hora actual: &e"+ date));


        }else if(args[0].equalsIgnoreCase("Get")){
            // /IntKnock get <Author/Version>
            subCommandGet(sender, args);
    

        }else if(args[0].equalsIgnoreCase("VicioKB")){
            sender.sendMessage(MessageUtils.getColoredMessage(
                IntKnock.prefix + "&fCuando llamas a un ex top... esperas una batalla digna de dioses. &6[gaboh] &festá listo. ¿Lo estás tú?" + player.getName() + 
                "&4Recuerda: &fhasta los más grandes caen alguna vez. ¿Será hoy el día en que &6[gaboh] &fpruebe la derrota?" + "&6 DC: gaboh_"));


        }else{
            help(sender);
        }
        
        }else{
        help(sender);
        }



        return true;
        }

        public void help(CommandSender sender){

            sender.sendMessage(MessageUtils.getColoredMessage("--------&r &b&lComandos IntKnock &8&m--------"));

            sender.sendMessage(MessageUtils.getColoredMessage("/IntKnock Nashe"));

            sender.sendMessage(MessageUtils.getColoredMessage("/IntKnock Fecha"));

            sender.sendMessage(MessageUtils.getColoredMessage("/IntKnock Get <Author/Version>"));

            sender.sendMessage(MessageUtils.getColoredMessage("/IntKnock VicioKB"));

            sender.sendMessage(MessageUtils.getColoredMessage("--------&r &b&lComandos IntKnock &8&m--------"));

        }


        

        public void subCommandGet(CommandSender sender, String[] args){
            if(!(sender.hasPermission("IntKnock.command.get"))){
                sender.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&c No tienes permisos para usar este comando."));
                return;
            }

            if(args.length ==1){
                sender.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&c Debes usar &7 /IntKnock Get author/Version>"));
                return;
            }

            if(args[1].equalsIgnoreCase("Author")){
                sender.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&f los autores del plugin son: &b"+ plugin.getDescription().getAuthors()));

            }else if(args[1].equalsIgnoreCase("Version")){
                sender.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&f La version del plugin es: &b" + plugin.getDescription().getVersion()));

        }else{
            sender.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&c Debes usar &7 /IntKnock Get author/Version>"));
        }

      }
     
   }
