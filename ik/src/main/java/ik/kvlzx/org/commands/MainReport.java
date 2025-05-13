package ik.kvlzx.org.commands;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ik.kvlzx.org.IntKnock;
import ik.kvlzx.org.utils.MessageUtils;

public class MainReport implements CommandExecutor{

    @SuppressWarnings("unused")
    private IntKnock plugin;
    public MainReport(IntKnock plugin){
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

        if(args.length >= 2){
            if(args[0].equalsIgnoreCase("Report")){
                sender.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&b Uso correcto es /report <jugador> <razón>"));
                return true;
      }

         Player target = Bukkit.getPlayer(args[0]);
            if(target == null){
            sender.sendMessage(MessageUtils.getColoredMessage("El jugador "+ args[0]+ " no se encuentra en linea."));

            return true;
        }


        String reason = "";
        for(Player Op: Bukkit.getOnlinePlayers()){
            if(Op.hasPermission("IntKnock.Report")){
                for(int i = 1; i < args.length; i++){
                    reason += args[i] + " ";
                }
                Op.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&c"+ player.getName() + " ha reportado a " + target.getName() + " por " + reason));
                
            }
        }

        player.sendMessage(MessageUtils.getColoredMessage(IntKnock.prefix +"&a Reporte enviado correctamente."));
       return true;


        }
        return true;
     }
       
}