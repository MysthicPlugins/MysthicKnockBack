package mk.kvlzx.utils;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TitleUtils {

    /**
     * Envía un título y subtítulo al jugador con tiempos personalizados
     * @param player El jugador que recibirá el título
     * @param title El texto principal del título
     * @param subtitle El texto del subtítulo
     * @param fadeIn Tiempo de aparición en ticks
     * @param stay Tiempo de permanencia en ticks
     * @param fadeOut Tiempo de desaparición en ticks
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        
        // Enviar los tiempos
        PacketPlayOutTitle times = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
        craftPlayer.getHandle().playerConnection.sendPacket(times);
        
        // Enviar el título principal si existe
        if (title != null && !title.isEmpty()) {
            IChatBaseComponent titleComponent = IChatBaseComponent.ChatSerializer
                .a("{\"text\": \"" + MessageUtils.getColor(title) + "\"}");
            PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, titleComponent);
            craftPlayer.getHandle().playerConnection.sendPacket(titlePacket);
        }
        
        // Enviar el subtítulo si existe
        if (subtitle != null && !subtitle.isEmpty()) {
            IChatBaseComponent subtitleComponent = IChatBaseComponent.ChatSerializer
                .a("{\"text\": \"" + MessageUtils.getColor(subtitle) + "\"}");
            PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, subtitleComponent);
            craftPlayer.getHandle().playerConnection.sendPacket(subtitlePacket);
        }
    }

    /**
     * Limpia cualquier título que se esté mostrando actualmente
     * @param player El jugador al que se le limpiará el título
     */
    public static void clearTitle(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutTitle clearPacket = new PacketPlayOutTitle(EnumTitleAction.CLEAR, null);
        craftPlayer.getHandle().playerConnection.sendPacket(clearPacket);
    }

    /**
     * Resetea los tiempos de título a los valores por defecto
     * @param player El jugador al que se le resetearán los tiempos
     */
    public static void resetTitle(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutTitle resetPacket = new PacketPlayOutTitle(EnumTitleAction.RESET, null);
        craftPlayer.getHandle().playerConnection.sendPacket(resetPacket);
    }
}
