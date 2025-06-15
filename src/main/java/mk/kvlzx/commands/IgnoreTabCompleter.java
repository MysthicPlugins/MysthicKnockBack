package mk.kvlzx.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;

public class IgnoreTabCompleter implements TabCompleter {
    private final MysthicKnockBack plugin;
    private final IgnoreCommand ignoreCommand;

    public IgnoreTabCompleter(MysthicKnockBack plugin, IgnoreCommand ignoreCommand) {
        this.plugin = plugin;
        this.ignoreCommand = ignoreCommand;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Primer argumento: subcomandos
            List<String> subCommands = Arrays.asList("add", "remove", "list");
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            switch (subCommand) {
                case "add":
                    // Para 'add': mostrar jugadores online que no están ignorados
                    completions.addAll(getAvailablePlayersToIgnore(player, input));
                    break;

                case "remove":
                    // Para 'remove': mostrar jugadores que están siendo ignorados
                    completions.addAll(getIgnoredPlayerNames(player, input));
                    break;

                case "list":
                    // 'list' no necesita más argumentos
                    break;
            }
        }

        return completions;
    }

    private List<String> getAvailablePlayersToIgnore(Player player, String input) {
        List<String> availablePlayers = new ArrayList<>();
        UUID playerUUID = player.getUniqueId();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Excluir al propio jugador
            if (onlinePlayer.getUniqueId().equals(playerUUID)) {
                continue;
            }

            // Excluir jugadores ya ignorados
            Set<UUID> ignoredByPlayer = ignoreCommand.getIgnoredPlayers(playerUUID);
            if (ignoredByPlayer.contains(onlinePlayer.getUniqueId())) {
                continue;
            }

            String playerName = onlinePlayer.getName();
            if (playerName.toLowerCase().startsWith(input)) {
                availablePlayers.add(playerName);
            }
        }

        return availablePlayers.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private List<String> getIgnoredPlayerNames(Player player, String input) {
        List<String> ignoredNames = new ArrayList<>();
        
        // Obtener la lista de UUIDs ignorados del comando ignore
        Set<UUID> ignoredUUIDs = ignoreCommand.getIgnoredPlayers(player.getUniqueId());
        
        for (UUID ignoredUUID : ignoredUUIDs) {
            String ignoredName = Bukkit.getOfflinePlayer(ignoredUUID).getName();
            if (ignoredName != null && ignoredName.toLowerCase().startsWith(input)) {
                ignoredNames.add(ignoredName);
            }
        }

        return ignoredNames.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}
