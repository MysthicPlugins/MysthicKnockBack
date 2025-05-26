package kk.kvlzx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.managers.RankManager;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

public class StatsCommand implements CommandExecutor {
    private final KvKnockback plugin;

    public StatsCommand(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("kvknockback.stats")) {
            sender.sendMessage(MessageUtils.getColor("&cNo tienes permiso para usar este comando."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Ver stats de un jugador
        if (args.length == 2 && subCommand.equals("view")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(MessageUtils.getColor("&cJugador no encontrado."));
                return true;
            }
            showStats(sender, target);
            return true;
        }

        // Modificar stats
        if (args.length == 3) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(MessageUtils.getColor("&cJugador no encontrado."));
                return true;
            }

            if (!sender.hasPermission("kvknockback.stats.modify")) {
                sender.sendMessage(MessageUtils.getColor("&cNo tienes permiso para modificar estadísticas."));
                return true;
            }

            try {
                int value = Integer.parseInt(args[2]);
                PlayerStats stats = PlayerStats.getStats(target.getUniqueId());

                switch (subCommand) {
                    case "setelo":
                        stats.setElo(value);
                        sender.sendMessage(MessageUtils.getColor("&aELO de " + target.getName() + " establecido a " + value));
                        break;
                    case "setkills":
                        stats.setKills(value);
                        sender.sendMessage(MessageUtils.getColor("&aKills de " + target.getName() + " establecidas a " + value));
                        break;
                    case "setdeaths":
                        stats.setDeaths(value);
                        sender.sendMessage(MessageUtils.getColor("&aMuertes de " + target.getName() + " establecidas a " + value));
                        break;
                    default:
                        sendHelp(sender);
                        break;
                }
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(MessageUtils.getColor("&cDebes ingresar un número válido."));
                return true;
            }
        }

        sendHelp(sender);
        return true;
    }

    private void showStats(CommandSender sender, Player target) {
        // Mostrar siempre en formato texto
        PlayerStats stats = PlayerStats.getStats(target.getUniqueId());
        String rankPrefix = RankManager.getRankPrefix(stats.getElo());
        
        sender.sendMessage(MessageUtils.getColor("&b&l=== Estadísticas de " + target.getName() + " &b&l==="));
        sender.sendMessage(MessageUtils.getColor("&fRango: " + rankPrefix));
        sender.sendMessage(MessageUtils.getColor("&fKills: &a" + stats.getKills()));
        sender.sendMessage(MessageUtils.getColor("&fMuertes: &c" + stats.getDeaths()));
        sender.sendMessage(MessageUtils.getColor("&fKDR: &b" + String.format("%.2f", stats.getKDR())));
        sender.sendMessage(MessageUtils.getColor("&fELO: &6" + stats.getElo()));
        sender.sendMessage(MessageUtils.getColor("&fRacha actual: &d" + stats.getCurrentStreak()));
        sender.sendMessage(MessageUtils.getColor("&fMejor racha: &d" + stats.getMaxStreak()));
        sender.sendMessage(MessageUtils.getColor("&fTiempo jugado: &e" + stats.getFormattedPlayTime()));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtils.getColor("&b=== Comandos de Stats ==="));
        sender.sendMessage(MessageUtils.getColor("&f/stats view <jugador> &7- Ver estadísticas"));
        if (sender.hasPermission("kvknockback.stats.modify")) {
            sender.sendMessage(MessageUtils.getColor("&f/stats setelo <jugador> <cantidad> &7- Establecer ELO"));
            sender.sendMessage(MessageUtils.getColor("&f/stats setkills <jugador> <cantidad> &7- Establecer kills"));
            sender.sendMessage(MessageUtils.getColor("&f/stats setdeaths <jugador> <cantidad> &7- Establecer muertes"));
        }
    }
}
