package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class IgnoreCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;
    private final Map<UUID, Set<UUID>> ignoredPlayers = new ConcurrentHashMap<>();
    private File ignoreFile;
    private FileConfiguration ignoreConfig;

    public IgnoreCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
        loadIgnoredPlayers();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showIgnoreCommands(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                if (args.length != 2) {
                    player.sendMessage(MessageUtils.getColor("&cUsage: /ignore add <player>"));
                    return true;
                }
                handleIgnoreAdd(player, args[1]);
                break;

            case "remove":
                if (args.length != 2) {
                    player.sendMessage(MessageUtils.getColor("&cUsage: /ignore remove <player>"));
                    return true;
                }
                handleIgnoreRemove(player, args[1]);
                break;

            case "list":
                handleIgnoreList(player);
                break;

            default:
                player.sendMessage(MessageUtils.getColor("&cUnknown subcommand. Use /ignore to see available commands."));
                break;
        }

        return true;
    }

    private void showIgnoreCommands(Player player) {
        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ IGNORE COMMANDS ═-═-═-═-═"));
        player.sendMessage(MessageUtils.getColor("&b/ignore add <player>&f - Ignore a player"));
        player.sendMessage(MessageUtils.getColor("&b/ignore remove <player>&f - Stop ignoring a player"));
        player.sendMessage(MessageUtils.getColor("&b/ignore list&f - Show ignored players"));
        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ IGNORE COMMANDS ═-═-═-═-═"));
    }

    private void handleIgnoreAdd(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        UUID targetUUID = target != null ? target.getUniqueId() : plugin.getServer().getOfflinePlayer(targetName).getUniqueId();
        UUID playerUUID = player.getUniqueId();

        if (targetUUID.equals(playerUUID)) {
            player.sendMessage(MessageUtils.getColor("&cYou cannot ignore yourself."));
            return;
        }

        Set<UUID> ignored = ignoredPlayers.computeIfAbsent(playerUUID, k -> new HashSet<>());
        if (ignored.contains(targetUUID)) {
            player.sendMessage(MessageUtils.getColor("&cYou are already ignoring " + targetName + "."));
            return;
        }

        ignored.add(targetUUID);
        saveIgnoredPlayers();
        player.sendMessage(MessageUtils.getColor("&aYou have ignored " + targetName + "."));
    }

    private void handleIgnoreRemove(Player player, String targetName) {
        Player target = plugin.getServer().getPlayer(targetName);
        UUID targetUUID = target != null ? target.getUniqueId() : plugin.getServer().getOfflinePlayer(targetName).getUniqueId();
        UUID playerUUID = player.getUniqueId();

        Set<UUID> ignored = ignoredPlayers.getOrDefault(playerUUID, new HashSet<>());
        if (!ignored.contains(targetUUID)) {
            player.sendMessage(MessageUtils.getColor("&cYou are not ignoring " + targetName + "."));
            return;
        }

        ignored.remove(targetUUID);
        saveIgnoredPlayers();
        player.sendMessage(MessageUtils.getColor("&aYou have stopped ignoring " + targetName + "."));
    }

    private void handleIgnoreList(Player player) {
        Set<UUID> ignored = ignoredPlayers.getOrDefault(player.getUniqueId(), new HashSet<>());
        if (ignored.isEmpty()) {
            player.sendMessage(MessageUtils.getColor("&6You are not ignoring any players."));
            return;
        }

        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ IGNORED PLAYERS ═-═-═-═-═"));
        int count = 1;
        for (UUID ignoredUUID : ignored) {
            String ignoredName = plugin.getServer().getOfflinePlayer(ignoredUUID).getName();
            player.sendMessage(MessageUtils.getColor("&6" + count + ". &f" + ignoredName));
            count++;
        }
        player.sendMessage(MessageUtils.getColor("&b═-═-═-═-═ IGNORED PLAYERS ═-═-═-═-═"));
    }

    public boolean isPlayerIgnored(UUID senderUUID, UUID targetUUID) {
        return ignoredPlayers.getOrDefault(targetUUID, new HashSet<>()).contains(senderUUID);
    }

    public void saveIgnoredPlayers() {
        ignoreFile = new File(plugin.getDataFolder(), "ignore.yml");
        ignoreConfig = new YamlConfiguration();

        for (Map.Entry<UUID, Set<UUID>> entry : ignoredPlayers.entrySet()) {
            UUID playerUUID = entry.getKey();
            Set<UUID> ignoredSet = entry.getValue();
            List<String> ignoredUUIDs = new ArrayList<>();
            for (UUID ignoredUUID : ignoredSet) {
                ignoredUUIDs.add(ignoredUUID.toString());
            }
            ignoreConfig.set(playerUUID.toString() + ".ignored", ignoredUUIDs);
        }

        try {
            ignoreConfig.save(ignoreFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving ignore.yml: " + e.getMessage());
        }
    }

    private void loadIgnoredPlayers() {
        ignoreFile = new File(plugin.getDataFolder(), "ignore.yml");
        ignoreConfig = YamlConfiguration.loadConfiguration(ignoreFile);

        for (String playerUUIDStr : ignoreConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(playerUUIDStr);
            List<String> ignoredUUIDs = ignoreConfig.getStringList(playerUUIDStr + ".ignored");
            Set<UUID> ignoredSet = new HashSet<>();
            for (String ignoredUUID : ignoredUUIDs) {
                ignoredSet.add(UUID.fromString(ignoredUUID));
            }
            ignoredPlayers.put(playerUUID, ignoredSet);
        }
    }
}
