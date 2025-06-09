package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.utils.MessageUtils;

public class ArenaCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;

    public ArenaCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor("&cThis command can only be executed by a player."));
            return true;
        }

        if (!sender.hasPermission("mysthicknockback.arena")) {
            sender.sendMessage(MessageUtils.getColor("&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        Player player = (Player) sender;
        String subCommand = args[0].toLowerCase();
        String arenaName = args[1];

        switch (subCommand) {
            case "create":
                if (plugin.getArenaManager().createArena(arenaName)) {
                    sender.sendMessage(MessageUtils.getColor("&aArena " + arenaName + " created successfully."));
                } else {
                    sender.sendMessage(MessageUtils.getColor("&cThe arena already exists."));
                }
                break;
            case "setzone":
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.getColor("&cUsage: /arena setzone <arena> <spawn/pvp/void>"));
                    return true;
                }
                String zoneType = args[2].toLowerCase();
                if (!isValidZoneType(zoneType)) {
                    sender.sendMessage(MessageUtils.getColor("&cInvalid zone type. Use: spawn, pvp or void"));
                    return true;
                }
                
                if (plugin.getArenaManager().setZone(arenaName, zoneType, player)) {
                    String action = hasExistingZone(arenaName, zoneType) ? "updated" : "set";
                    sender.sendMessage(MessageUtils.getColor("&aZone " + zoneType + " " + action + " for arena " + arenaName));
                } else {
                    sender.sendMessage(MessageUtils.getColor("&cError setting zone. Make sure you have a valid selection."));
                }
                break;
            case "setspawn":
                if (plugin.getArenaManager().setSpawn(arenaName, player.getLocation())) {
                    sender.sendMessage(MessageUtils.getColor("&aSpawn point set for arena " + arenaName));
                } else {
                    sender.sendMessage(MessageUtils.getColor("&cThe arena does not exist."));
                }
                break;
            case "delete":
                if (plugin.getArenaManager().deleteArena(arenaName)) {
                    sender.sendMessage(MessageUtils.getColor("&aArena " + arenaName + " deleted successfully."));
                } else {
                    sender.sendMessage(MessageUtils.getColor("&cThe arena does not exist."));
                }
                break;
            case "setborder":
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.getColor("&cUsage: /arena setborder <arena> <size>"));
                    return true;
                }
                try {
                    int size = Integer.parseInt(args[2]);
                    if (size <= 0) {
                        sender.sendMessage(MessageUtils.getColor("&cSize must be greater than 0."));
                        return true;
                    }
                    if (plugin.getArenaManager().setBorder(arenaName, size)) {
                        sender.sendMessage(MessageUtils.getColor("&aBorder set for arena " + arenaName));
                        // Si la arena es la actual, actualizamos el borde inmediatamente
                        if (arenaName.equals(plugin.getArenaManager().getCurrentArena())) {
                            plugin.getArenaManager().setCurrentArena(arenaName);
                        }
                    } else {
                        sender.sendMessage(MessageUtils.getColor("&cThe arena does not exist."));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.getColor("&cSize must be a valid number."));
                }
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private boolean isValidZoneType(String zoneType) {
        return zoneType.equals("spawn") || zoneType.equals("pvp") || zoneType.equals("void");
    }

    private boolean hasExistingZone(String arenaName, String zoneType) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        return arena != null && arena.getZone(zoneType) != null;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtils.getColor("&b=== Arena Commands ==="));
        sender.sendMessage(MessageUtils.getColor("&f/arena create <name> &7- Creates a new arena"));
        sender.sendMessage(MessageUtils.getColor("&f/arena setzone <arena> <spawn/pvp/void> &7- Sets/updates a zone"));
        sender.sendMessage(MessageUtils.getColor("&f/arena setspawn <arena> &7- Sets the spawn point"));
        sender.sendMessage(MessageUtils.getColor("&f/arena delete <arena> &7- Deletes an arena"));
        sender.sendMessage(MessageUtils.getColor("&f/arena setborder <arena> <size> &7- Sets the arena border"));
    }
}
