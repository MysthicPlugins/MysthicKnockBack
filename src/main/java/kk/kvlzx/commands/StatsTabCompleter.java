package kk.kvlzx.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class StatsTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Primer argumento: subcomandos basados en permisos
            List<String> availableCommands = new ArrayList<>();
            availableCommands.add("view");
            
            if (sender.hasPermission("kvknockback.stats.modify")) {
                availableCommands.addAll(Arrays.asList("setelo", "setkills", "setdeaths"));
            }
            
            StringUtil.copyPartialMatches(args[0], availableCommands, completions);
        } else if (args.length == 2) {
            // Segundo argumento: jugadores online
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            StringUtil.copyPartialMatches(args[1], playerNames, completions);
        } else if (args.length == 3 && sender.hasPermission("kvknockback.stats.modify")) {
            // Tercer argumento: valores sugeridos para cada comando
            switch (args[0].toLowerCase()) {
                case "setelo":
                    completions.addAll(Arrays.asList("500", "1000", "1500", "2000"));
                    break;
                case "setkills":
                case "setdeaths":
                    completions.addAll(Arrays.asList("0", "10", "50", "100"));
                    break;
            }
        }

        return completions;
    }
}
