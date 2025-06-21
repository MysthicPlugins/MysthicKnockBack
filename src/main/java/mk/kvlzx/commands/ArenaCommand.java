package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.config.MessagesConfig;
import mk.kvlzx.utils.MessageUtils;

public class ArenaCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;
    private final MessagesConfig messages;

    public ArenaCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessagesConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor(messages.getNoPermission()));
            return true;
        }

        if (!sender.hasPermission("mysthicknockback.arena")) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getNoPermission()));
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
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaCreate().replace("%arena%", arenaName)));
                } else {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaAlreadyExist()));
                }
                break;
            case "setzone":
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaZoneUsage()));
                    return true;
                }
                String zoneType = args[2].toLowerCase();
                if (!isValidZoneType(zoneType)) {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaZoneInvalid()));
                    return true;
                }
                boolean existed = hasExistingZone(arenaName, zoneType);
                if (plugin.getArenaManager().setZone(arenaName, zoneType, player)) {
                    String action = existed ? "updated" : "set";
                    String msg = messages.getArenaZoneSuccess()
                        .replace("%zone_type%", zoneType)
                        .replace("%action%", action)
                        .replace("%arena%", arenaName);
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + msg));
                } else {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaZoneError()));
                }
                break;
            case "setspawn":
                if (plugin.getArenaManager().setSpawn(arenaName, player.getLocation())) {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaSetSpawnSuccess().replace("%arena%", arenaName)));
                } else {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaSetSpawnError()));
                }
                break;
            case "delete":
                if (plugin.getArenaManager().deleteArena(arenaName)) {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaDeleteSuccess().replace("%arena%", arenaName)));
                } else {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaDeleteError()));
                }
                break;
            case "setborder":
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaSetBorderUsage()));
                    return true;
                }
                try {
                    int size = Integer.parseInt(args[2]);
                    if (size <= 0) {
                        sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaSetBorderErrorSize()));
                        return true;
                    }
                    if (plugin.getArenaManager().setBorder(arenaName, size)) {
                        sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaSetBorderSuccess().replace("%arena%", arenaName)));
                        if (arenaName.equals(plugin.getArenaManager().getCurrentArena())) {
                            plugin.getArenaManager().setCurrentArena(arenaName);
                        }
                    } else {
                        sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaSetBorderError()));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + messages.getArenaSetBorderNumberError()));
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
        for (String line : messages.getArenaHelpMessage()) {
            sender.sendMessage(MessageUtils.getColor(line));
        }
    }
}
