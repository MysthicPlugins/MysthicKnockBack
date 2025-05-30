package mk.kvlzx.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;

public class ArenaTabCompleter implements TabCompleter {
    private final MysthicKnockBack plugin;
    private final String[] COMMANDS = { "create", "setzone", "setspawn", "setborder", "delete" };
    private final String[] ZONE_TYPES = { "spawn", "pvp", "void" };
    private final String[] BORDER_SIZES = { "50", "100", "150", "200", "250", "300" };

    public ArenaTabCompleter(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("mysthicknockback.arena")) {
            return completions;
        }

        if (args.length == 1) {
            // Primer argumento: subcomandos
            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        } else if (args.length == 2) {
            // Segundo argumento: nombre de arena
            List<String> arenas = plugin.getArenaManager().getArenas()
                .stream()
                .map(Arena::getName)
                .collect(Collectors.toList());

            // Si es create, no mostrar arenas existentes
            if (!args[0].equalsIgnoreCase("create")) {
                StringUtil.copyPartialMatches(args[1], arenas, completions);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setzone")) {
                // Tercer argumento para setzone: tipos de zona
                StringUtil.copyPartialMatches(args[2], Arrays.asList(ZONE_TYPES), completions);
            } else if (args[0].equalsIgnoreCase("setborder")) {
                // Tercer argumento para setborder: tamaños predefinidos
                StringUtil.copyPartialMatches(args[2], Arrays.asList(BORDER_SIZES), completions);
            }
        }

        return completions;
    }
}
