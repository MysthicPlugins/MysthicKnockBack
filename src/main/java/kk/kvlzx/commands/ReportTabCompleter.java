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

public class ReportTabCompleter implements TabCompleter {
    private final String[] REASONS = {
        "hacks", "killaura", "reach", "fly", "speed", "antikb",
        "toxic", "spam", "insultos", "amenazas", "otros"
    };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Primer argumento: jugadores online excepto uno mismo
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getName().equals(sender.getName())) {
                    playerNames.add(player.getName());
                }
            }
            StringUtil.copyPartialMatches(args[0], playerNames, completions);
        } else if (args.length == 2) {
            // Segundo argumento: razones predefinidas
            StringUtil.copyPartialMatches(args[1], Arrays.asList(REASONS), completions);
        }

        return completions;
    }
}
