package kk.kvlzx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.menu.ReportMenu;

public class ReportCommand implements CommandExecutor {

    private final KvKnockback plugin;

    public ReportCommand(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor("&cEste comando solo puede ser usado por jugadores."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(MessageUtils.getColor("&cUso correcto: /report <jugador>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(MessageUtils.getColor("&cJugador no encontrado."));
            return true;
        }

        if (target == player) {
            player.sendMessage(MessageUtils.getColor("&cNo puedes reportarte a ti mismo."));
            return true;
        }

        // Abrir el menú de reporte
        ReportMenu reportMenu = new ReportMenu(plugin, target);
        player.openInventory(reportMenu.getInventory(player));
        return true;
    }
}