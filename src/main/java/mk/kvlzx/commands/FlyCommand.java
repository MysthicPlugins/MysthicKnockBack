package mk.kvlzx.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.arena.Zone;
import mk.kvlzx.utils.MessageUtils;

public class FlyCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;

    public FlyCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cThis command can only be executed by players!"));
            return true;
        }

        Player player = (Player) sender;

        // Verificar permisos
        if (!player.hasPermission("mysthicknockback.fly")) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                plugin.getMessagesConfig().getNoPermission()));
            return true;
        }

        // Verificar si está en una zona de spawn
        if (!isPlayerInSpawnZone(player)) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                plugin.getMessagesConfig().getFlyNonInArena()));
            return true;
        }

        // Toggle fly
        boolean newFlyState = !player.getAllowFlight();
        player.setAllowFlight(newFlyState);
        player.setFlying(newFlyState);

        if (newFlyState) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                plugin.getMessagesConfig().getFlyEnabled()));
        } else {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                plugin.getMessagesConfig().getFlyDisabled()));
        }

        return true;
    }

    /**
     * Verifica si el jugador está en una zona de spawn
     */
    private boolean isPlayerInSpawnZone(Player player) {
        String currentArena = plugin.getArenaManager().getCurrentArena();
        if (currentArena == null) {
            return false;
        }

        Arena arena = plugin.getArenaManager().getArena(currentArena);
        if (arena == null) {
            return false;
        }

        Zone spawnZone = arena.getZone("spawn");
        if (spawnZone == null) {
            return false;
        }

        return spawnZone.isInside(player.getLocation());
    }
}
