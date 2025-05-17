package kk.kvlzx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.utils.MessageUtils;

public class ReportCommand implements CommandExecutor{

    private KvKnockback plugin;
    public ReportCommand(KvKnockback plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args){

        if(!(sender instanceof Player)){
            // Consola :3
            Bukkit.getConsoleSender().sendMessage(MessageUtils.getColor(KvKnockback.prefix +"&c Solo puedes usar este comando siendo un jugador."));
            return true;
            
        }

        Player player = (Player) sender;

        if(args.length >= 2){
            if(args[0].equalsIgnoreCase("Report")){
                sender.sendMessage(MessageUtils.getColor(KvKnockback.prefix +"&b Uso correcto es /report <jugador> <razón>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if(target == null){
                sender.sendMessage(MessageUtils.getColor("El jugador "+ args[0]+ " no se encuentra en linea."));

                return true;
            }

            String reason = "";
            for(Player op: Bukkit.getOnlinePlayers()){
                if(op.hasPermission("kvknockback.report")){
                    for(int i = 1; i < args.length; i++){
                        reason += args[i] + " ";
                    }
                    op.sendMessage(MessageUtils.getColor(KvKnockback.prefix +"&c"+ player.getName() + " ha reportado a " + target.getName() + " por " + reason));
                    
                }
            }

            player.sendMessage(MessageUtils.getColor(KvKnockback.prefix +"&a Reporte enviado correctamente."));
            return true;


        }
        return true;
    }
}