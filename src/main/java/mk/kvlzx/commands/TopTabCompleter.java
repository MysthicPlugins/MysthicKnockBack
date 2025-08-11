package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TopTabCompleter implements TabCompleter {
    private final String[] COMMANDS = { "kills", "elo", "kdr", "streak", "time" };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        }

        return completions;
    }
}
