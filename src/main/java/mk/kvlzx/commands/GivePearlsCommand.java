package mk.kvlzx.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.utils.MessageUtils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class GivePearlsCommand implements CommandExecutor {
    private final MysthicKnockBack plugin;

    public GivePearlsCommand(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("mysthicknockback.givepearls")) {
            sender.sendMessage(MessageUtils.getColor("&cYou do not have permission to use this command."));
            return true;
        }

        Player target = null;

        // Si no se especifica jugador y el sender es un jugador, darse a sí mismo
        if (args.length == 0) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(MessageUtils.getColor("&cYou must specify a player from the console."));
                sender.sendMessage(MessageUtils.getColor("&cUsage: /givepearls <player>"));
                return true;
            }
        }
        // Si se especifica jugador
        else if (args.length == 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(MessageUtils.getColor("&cPlayer &e" + args[0] + " &cis not online."));
                return true;
            }
        }
        // Demasiados argumentos
        else {
            sender.sendMessage(MessageUtils.getColor("&cUsage: /givepearls [player]"));
            return true;
        }

        // Crear las perlas personalizadas con stack de 128 usando NMS
        ItemStack pearls = CustomItem.create(CustomItem.ItemType.PEARL);
        pearls = makeStackable(pearls, 128);

        // Dar las perlas al jugador
        target.getInventory().addItem(pearls);

        // Mensajes de confirmación
        if (sender.equals(target)) {
            sender.sendMessage(MessageUtils.getColor("&aYou have given yourself &e128 pearls&a."));
        } else {
            sender.sendMessage(MessageUtils.getColor("&aYou have given &e128 pearls &ato &e" + target.getName() + "&a."));
            target.sendMessage(MessageUtils.getColor("&aYou have received &e128 pearls &afrom &e" + sender.getName() + "&a."));
        }

        return true;
    }

    /**
     * Método para hacer un item stackeable hasta cierta cantidad usando NMS
     */
    private ItemStack makeStackable(ItemStack item, int amount) {
        try {
            // Convertir a NMS
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            
            // Obtener o crear NBT
            NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
            
            // Establecer el tamaño del stack directamente
            nmsItem.count = amount;
            
            // Aplicar NBT
            nmsItem.setTag(compound);
            
            // Convertir de vuelta a Bukkit
            return CraftItemStack.asBukkitCopy(nmsItem);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error modifying item stack: " + e.getMessage());
            e.printStackTrace();
            return item;
        }
    }
}