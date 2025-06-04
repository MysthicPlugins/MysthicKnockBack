package mk.kvlzx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StatsTabCompleter implements TabCompleter {
    private final List<String> operations = Arrays.asList("set", "add", "remove", "reset", "resetall");
    private final List<String> stats = Arrays.asList("elo", "kills", "deaths", "coins");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("mysthicknockback.admin")) {
            // Si no tiene permisos de admin, solo sugerir nombres de jugadores
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            }
            return completions;
        }

        switch (args.length) {
            case 1:
                List<String> allSuggestions = new ArrayList<>(operations);
                // Añadir nombres de jugadores a las sugerencias
                Bukkit.getOnlinePlayers().forEach(player -> allSuggestions.add(player.getName()));
                return allSuggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            case 2:
                if (args[0].equalsIgnoreCase("reset")) {
                    return getOnlinePlayerNames(args[1]);
                } else if (isOperation(args[0])) {
                    return stats.stream()
                        .filter(stat -> stat.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
            case 3:
                if (isOperation(args[0])) {
                    return getOnlinePlayerNames(args[2]);
                }
                break;
            case 4:
                if (isOperation(args[0])) {
                    List<String> suggestions = new ArrayList<>();
                    switch (args[1].toLowerCase()) {
                        case "elo":
                            suggestions.addAll(Arrays.asList("1000", "1200", "1500", "2000"));
                            break;
                        case "kills":
                        case "deaths":
                            suggestions.addAll(Arrays.asList("1", "5", "10", "50", "100"));
                            break;
                        case "coins":
                            suggestions.addAll(Arrays.asList("1000", "5000", "10000", "50000"));
                            break;
                    }
                    return suggestions.stream()
                        .filter(s -> s.startsWith(args[3]))
                        .collect(Collectors.toList());
                }
                break;
        }

        return completions;
    }

    private boolean isOperation(String arg) {
        return arg.equalsIgnoreCase("set") || 
                arg.equalsIgnoreCase("add") || 
                arg.equalsIgnoreCase("remove");
    }

    private List<String> getOnlinePlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }
}
