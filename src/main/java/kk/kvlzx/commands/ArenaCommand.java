package kk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.arena.Arena;
import kk.kvlzx.utils.MessageUtils;

public class ArenaCommand implements CommandExecutor {
    private final KvKnockback plugin;

    public ArenaCommand(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor("&cEste comando solo puede ser usado por jugadores."));
            return true;
        }

        if (!sender.hasPermission("kvknockback.arena")) {
            sender.sendMessage(MessageUtils.getColor("&cNo tienes permiso para usar este comando."));
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
                    sender.sendMessage(MessageUtils.getColor("&aArena " + arenaName + " creada correctamente."));
                } else {
                    sender.sendMessage(MessageUtils.getColor("&cLa arena ya existe."));
                }
                break;
            case "setzone":
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.getColor("&cUso: /arena setzone <arena> <spawn/pvp/void>"));
                    return true;
                }
                String zoneType = args[2].toLowerCase();
                if (!isValidZoneType(zoneType)) {
                    sender.sendMessage(MessageUtils.getColor("&cTipo de zona inválido. Usa: spawn, pvp o void"));
                    return true;
                }
                
                if (plugin.getArenaManager().setZone(arenaName, zoneType, player)) {
                    String action = hasExistingZone(arenaName, zoneType) ? "actualizada" : "establecida";
                    sender.sendMessage(MessageUtils.getColor("&aZona " + zoneType + " " + action + " para la arena " + arenaName));
                } else {
                    sender.sendMessage(MessageUtils.getColor("&cError al establecer la zona. Asegúrate de tener una selección válida."));
                }
                break;
            case "setspawn":
                if (plugin.getArenaManager().setSpawn(arenaName, player.getLocation())) {
                    sender.sendMessage(MessageUtils.getColor("&aSpawn point establecido para la arena " + arenaName));
                } else {
                    sender.sendMessage(MessageUtils.getColor("&cLa arena no existe."));
                }
                break;
            case "delete":
                if (plugin.getArenaManager().deleteArena(arenaName)) {
                    sender.sendMessage(MessageUtils.getColor("&aArena " + arenaName + " eliminada correctamente."));
                } else {
                    sender.sendMessage(MessageUtils.getColor("&cLa arena no existe."));
                }
                break;
            case "setborder":
                if (args.length < 3) {
                    sender.sendMessage(MessageUtils.getColor("&cUso: /arena setborder <arena> <tamaño>"));
                    return true;
                }
                try {
                    int size = Integer.parseInt(args[2]);
                    if (size <= 0) {
                        sender.sendMessage(MessageUtils.getColor("&cEl tamaño debe ser mayor a 0."));
                        return true;
                    }
                    if (plugin.getArenaManager().setBorder(arenaName, size)) {
                        sender.sendMessage(MessageUtils.getColor("&aBorde establecido para la arena " + arenaName));
                        // Si es la arena actual, actualizar el borde inmediatamente
                        if (arenaName.equals(plugin.getArenaManager().getCurrentArena())) {
                            plugin.getArenaManager().setCurrentArena(arenaName);
                        }
                    } else {
                        sender.sendMessage(MessageUtils.getColor("&cLa arena no existe."));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageUtils.getColor("&cEl tamaño debe ser un número válido."));
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
        sender.sendMessage(MessageUtils.getColor("&b=== Comandos de Arena ==="));
        sender.sendMessage(MessageUtils.getColor("&f/arena create <nombre> &7- Crea una nueva arena"));
        sender.sendMessage(MessageUtils.getColor("&f/arena setzone <arena> <spawn/pvp/void> &7- Establece/actualiza una zona"));
        sender.sendMessage(MessageUtils.getColor("&f/arena setspawn <arena> &7- Establece el punto de spawn"));
        sender.sendMessage(MessageUtils.getColor("&f/arena delete <arena> &7- Elimina una arena"));
        sender.sendMessage(MessageUtils.getColor("&f/arena setborder <arena> <tamaño> &7- Establece el borde de la arena"));
    }
}
