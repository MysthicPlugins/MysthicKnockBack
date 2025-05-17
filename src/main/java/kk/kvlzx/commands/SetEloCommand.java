package kk.kvlzx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.managers.RankManager;

public class SetEloCommand implements CommandExecutor {

    private final KvKnockback plugin;

    public SetEloCommand(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setelo")) {
            if (args.length != 2) {
                sender.sendMessage("Uso: /setelo <jugador> <elo>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("Jugador no encontrado.");
                return true;
            }
            int elo;
            try {
                elo = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("El Elo debe ser un número.");
                return true;
            }
            plugin.getConfig().set("players." + target.getUniqueId() + ".elo", elo);
            plugin.saveConfig();
            RankManager.updatePlayerRank(target, elo);
            sender.sendMessage("Elo de " + target.getName() + " actualizado a " + elo);
            return true;
        }
        return false;
    }
}