package kk.kvlzx.commands;

import java.util.stream.Collectors;

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

        // Ver stats no requiere permiso especial
        if (subCommand.equals("view") && args.length == 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(MessageUtils.getColor("&cJugador no encontrado."));
                return true;
            }
            showStats(sender, target);
            return true;
        }

        // Todos los demás comandos requieren permiso de modificación
        if (!sender.hasPermission("kvknockback.stats.modify")) {
            sender.sendMessage(MessageUtils.getColor("&cNo tienes permiso para modificar estadísticas."));
            return true;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(MessageUtils.getColor("&cJugador no encontrado."));
            return true;
        }

        PlayerStats stats = PlayerStats.getStats(target.getUniqueId());

        switch (subCommand) {
            case "setelo":
            case "setkills":
            case "setdeaths":
            case "setcoins":
                if (args.length != 3) {
                    sender.sendMessage(MessageUtils.getColor("&cUso: /stats " + subCommand + " <jugador> <cantidad>"));
                    return true;
                }
                try {
                    int value = Integer.parseInt(args[2]);
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
                        case "setcoins":
                            stats.setKGCoins(value);
                            sender.sendMessage(MessageUtils.getColor("&aKGCoins de " + target.getName() + " establecidos a " + value));
                            break;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.getColor("&cDebes ingresar un número válido."));
                }
                break;
            case "reset":
                resetPlayerStats(target);
                sender.sendMessage(MessageUtils.getColor("&aEstadísticas de " + target.getName() + " reseteadas."));
                break;
            case "resetall":
                resetAllStats();
                sender.sendMessage(MessageUtils.getColor("&aEstadísticas de todos los jugadores reseteadas."));
                break;
            default:
                sendHelp(sender);
                break;
        }

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
        sender.sendMessage(MessageUtils.getColor("&fKGCoins: &e" + stats.getKGCoins()));
    }

    private void resetPlayerStats(Player player) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        stats.setKills(0);
        stats.setDeaths(0);
        stats.setElo(500);
        stats.resetStreak();
        stats.setKGCoins(0);
        RankManager.updatePlayerRank(player, 500); // Actualizar el rango al default
    }

    private void resetAllStats() {
        for (PlayerStats stats : PlayerStats.getAllStats().stream()
                .map(PlayerStats::getStats)
                .collect(Collectors.toList())) {
            Player player = Bukkit.getPlayer(stats.getUUID());
            if (player != null && player.isOnline()) {
                resetPlayerStats(player);
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtils.getColor("&b=== Comandos de Stats ==="));
        sender.sendMessage(MessageUtils.getColor("&f/stats view <jugador> &7- Ver estadísticas"));
        if (sender.hasPermission("kvknockback.stats.modify")) {
            sender.sendMessage(MessageUtils.getColor("&f/stats setelo <jugador> <cantidad> &7- Establecer ELO"));
            sender.sendMessage(MessageUtils.getColor("&f/stats setkills <jugador> <cantidad> &7- Establecer kills"));
            sender.sendMessage(MessageUtils.getColor("&f/stats setdeaths <jugador> <cantidad> &7- Establecer muertes"));
            sender.sendMessage(MessageUtils.getColor("&f/stats setcoins <jugador> <cantidad> &7- Establecer KGCoins"));
        }
        if (sender.hasPermission("kvknockback.stats.reset")) {
            sender.sendMessage(MessageUtils.getColor("&f/stats reset <jugador> &7- Resetear estadísticas"));
        }
        if (sender.hasPermission("kvknockback.stats.resetall")) {
            sender.sendMessage(MessageUtils.getColor("&f/stats resetall &7- Resetear todas las estadísticas"));
        }
    }
}
