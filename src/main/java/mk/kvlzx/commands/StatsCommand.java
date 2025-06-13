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

public class StatsCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;

    public StatsCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
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
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cPlayer not found."));
                return true;
            }
            if (sender instanceof Player) {
                Player player = (Player) sender;
                showPlayerStats(player, target);
            } else {
                showPlayerStatsConsole(sender, target);
            }
            return true;
        }

        // A partir de aquí, todos los comandos requieren permiso admin
        if (!sender.hasPermission("kvknockback.admin")) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cYou don't have permission to use this command."));
            return true;
        }

        if (args[0].equalsIgnoreCase("resetall")) {
            resetAllStats();
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&aAll players' statistics have been reset."));
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length < 2) {
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cUsage: /stats reset <player>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cPlayer not found."));
                return true;
            }
            resetPlayerStats(target);
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&a" + target.getName() + "'s statistics have been reset."));
            return true;
        }

        // Comandos de modificación de stats
        if (args.length < 4) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cUsage: /stats <set/add/remove> <elo/kills/deaths/coins> <player> <amount>"));
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

    private void showPlayerStats(Player viewer, Player target) {
        PlayerStats stats = PlayerStats.getStats(target.getUniqueId());
        viewer.sendMessage(MessageUtils.getColor("&8⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"));
        viewer.sendMessage(MessageUtils.getColor("&b" + target.getName() + "'s Statistics"));
        viewer.sendMessage(MessageUtils.getColor("&7Rank: " + RankManager.getRankPrefix(stats.getElo())));
        viewer.sendMessage(MessageUtils.getColor("&7ELO: &6" + stats.getElo()));
        viewer.sendMessage(MessageUtils.getColor("&7Kills: &a" + stats.getKills()));
        viewer.sendMessage(MessageUtils.getColor("&7Deaths: &c" + stats.getDeaths()));
        viewer.sendMessage(MessageUtils.getColor("&7KDR: &b" + String.format("%.2f", stats.getKDR())));
        viewer.sendMessage(MessageUtils.getColor("&7Current Streak: &d" + stats.getCurrentStreak()));
        viewer.sendMessage(MessageUtils.getColor("&7Best Streak: &d" + stats.getMaxStreak()));
        viewer.sendMessage(MessageUtils.getColor("&7Play Time: &e" + stats.getFormattedPlayTime()));
        viewer.sendMessage(MessageUtils.getColor("&7KGCoins: &e" + stats.getKGCoins()));
        viewer.sendMessage(MessageUtils.getColor("&8⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"));
    }

    private void showPlayerStatsConsole(CommandSender sender, Player target) {
        PlayerStats stats = PlayerStats.getStats(target.getUniqueId());
        sender.sendMessage(MessageUtils.getColor("&8⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"));
        sender.sendMessage(MessageUtils.getColor("&b" + target.getName() + "'s Statistics"));
        sender.sendMessage(MessageUtils.getColor("&7ELO: &6" + stats.getElo()));
        sender.sendMessage(MessageUtils.getColor("&7Kills: &a" + stats.getKills()));
        sender.sendMessage(MessageUtils.getColor("&7Deaths: &c" + stats.getDeaths()));
        sender.sendMessage(MessageUtils.getColor("&7KDR: &b" + String.format("%.2f", stats.getKDR())));
        sender.sendMessage(MessageUtils.getColor("&7Current Streak: &d" + stats.getCurrentStreak()));
        sender.sendMessage(MessageUtils.getColor("&7KGCoins: &e" + stats.getKGCoins()));
        sender.sendMessage(MessageUtils.getColor("&8⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"));
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
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cAmount must be a valid number."));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cPlayer not found."));
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
                sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cInvalid statistic. Use: elo, kills, deaths or coins"));
                return;
        }

        String operationName = operation.equals("set") ? "set" : 
                                operation.equals("add") ? "added" : "removed";
        
        sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix +
            String.format("&aYou have %s %d %s to %s", operationName, amount, statName, target.getName())
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
