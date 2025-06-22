package mk.kvlzx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.managers.RankManager;
import mk.kvlzx.config.MessagesConfig;

public class StatsCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;
    private final MessagesConfig messages;

    public StatsCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.getColor("&cThis command can only be used by players."));
                return true;
            }
            Player player = (Player) sender;
            plugin.getMenuManager().openMenu(player, "stats");
            return true;
        }

        // Ver stats de otro jugador: /stats <jugador>
        if (args.length == 1 && !isOperationCommand(args[0])) {
            String targetName = args[0];
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsNotFound()));
                return true;
            }
            showPlayerStats(sender, target);
            return true;
        }

        // A partir de aquí, todos los comandos requieren permiso admin
        if (!sender.hasPermission("kvknockback.stats.admin")) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsNoPermission()));
            return true;
        }

        if (args[0].equalsIgnoreCase("resetall")) {
            resetAllStats();
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsResetAllSuccess()));
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length < 2) {
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsAdminUsage()));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsNotFound()));
                return true;
            }
            resetPlayerStats(target);
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsResetSuccess().replace("%player%", target.getName())));
            return true;
        }

        // Comandos de modificación de stats
        if (args.length < 4) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsAdminUsage()));
            return true;
        }

        handleStatModification(sender, args);
        return true;
    }

    private boolean isOperationCommand(String arg) {
        return arg.equalsIgnoreCase("set") || 
                arg.equalsIgnoreCase("add") || 
                arg.equalsIgnoreCase("remove") || 
                arg.equalsIgnoreCase("reset") || 
                arg.equalsIgnoreCase("resetall");
    }

    private void showPlayerStats(CommandSender sender, Player target) {
        PlayerStats stats = PlayerStats.getStats(target.getUniqueId());
        String rank = RankManager.getRankPrefix(stats.getElo());
        for (String line : messages.getStatsFormat()) {
            sender.sendMessage(MessageUtils.getColor(line
                .replace("%player%", target.getName())
                .replace("%rank%", rank)
                .replace("%elo%", String.valueOf(stats.getElo()))
                .replace("%kills%", String.valueOf(stats.getKills()))
                .replace("%deaths%", String.valueOf(stats.getDeaths()))
                .replace("%kdr%", String.format("%.2f", stats.getKDR()))
                .replace("%current_streak%", String.valueOf(stats.getCurrentStreak()))
                .replace("%max_streak%", String.valueOf(stats.getMaxStreak()))
                .replace("%playtime%", stats.getFormattedPlayTime())
                .replace("%kgcoins%", String.valueOf(stats.getKGCoins()))
            ));
        }
    }

    private void resetAllStats() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            resetPlayerStats(player);
        }
    }

    private void resetPlayerStats(Player player) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        stats.setElo(500);
        stats.setKills(0);
        stats.setDeaths(0);
        stats.setKGCoins(0);
    }

    private void handleStatModification(CommandSender sender, String[] args) {
        String operation = args[0].toLowerCase();
        String stat = args[1].toLowerCase();
        String targetName = args[2];
        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsAmountError()));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsNotFound()));
            return;
        }

        PlayerStats stats = PlayerStats.getStats(target.getUniqueId());
        String statName;
        int currentValue;

        switch (stat) {
            case "elo":
                statName = "ELO";
                currentValue = stats.getElo();
                updateStat(operation, () -> stats.setElo(amount),
                            () -> stats.setElo(currentValue + amount),
                            () -> stats.setElo(currentValue - amount));
                break;
            case "kills":
                statName = "kills";
                currentValue = stats.getKills();
                updateStat(operation, () -> stats.setKills(amount),
                            () -> stats.setKills(currentValue + amount),
                            () -> stats.setKills(currentValue - amount));
                break;
            case "deaths":
                statName = "deaths";
                currentValue = stats.getDeaths();
                updateStat(operation, () -> stats.setDeaths(amount),
                            () -> stats.setDeaths(currentValue + amount),
                            () -> stats.setDeaths(currentValue - amount));
                break;
            case "coins":
                statName = "coins";
                currentValue = stats.getKGCoins();
                updateStat(operation, () -> stats.setKGCoins(amount),
                            () -> stats.addKGCoins(amount),
                            () -> stats.removeKGCoins(amount));
                break;
            default:
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsInvalidStat()));
                return;
        }

        String operationName = operation.equals("set") ? "set" : 
                                operation.equals("add") ? "added" : "removed";
        sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + messages.getStatsStatUpdated()
            .replace("%operation%", operationName)
            .replace("%amount%", String.valueOf(amount))
            .replace("%stat%", statName)
            .replace("%player%", target.getName())
        ));
    }

    private void updateStat(String operation, Runnable set, Runnable add, Runnable remove) {
        switch (operation) {
            case "set": set.run(); break;
            case "add": add.run(); break;
            case "remove": remove.run(); break;
        }
    }
}
