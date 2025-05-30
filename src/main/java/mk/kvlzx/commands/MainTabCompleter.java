package mk.kvlzx.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

public class MainTabCompleter implements TabCompleter {
    private final String[] COMMANDS = { "nashe", "fecha", "get", "viciokb" };
    private final String[] GET_ARGS = { "author", "version" };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            if (sender.hasPermission("mysthicknockback.command.get")) {
                StringUtil.copyPartialMatches(args[1], Arrays.asList(GET_ARGS), completions);
            }
        }

        return completions;
    }
}
