package kk.kvlzx.utils;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TitleUtils {
    
    /**
     * Envía un título y subtítulo personalizado a un jugador usando NMS (Net Minecraft Server).
     * Este método permite más control sobre la animación y el formato que los métodos nativos de Bukkit.
     *
     * @param player    El jugador que recibirá el título
     * @param title     El texto principal del título (soporta códigos de color con &)
     * @param subtitle  El texto del subtítulo (soporta códigos de color con &)
     * @param fadeIn    Duración en ticks (20 ticks = 1 segundo) de la animación de entrada
     * @param stay      Duración en ticks que el título permanecerá en pantalla
     * @param fadeOut   Duración en ticks de la animación de salida
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        // Obtener la conexión del jugador para enviar paquetes NMS
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        
        // Convertir los mensajes a componentes de chat NMS, procesando los códigos de color
        // y creando objetos JSON que el servidor puede interpretar
        IChatBaseComponent titleComponent = IChatBaseComponent.ChatSerializer.a(
            "{\"text\":\"" + MessageUtils.getColor(title) + "\"}"
        );
        IChatBaseComponent subtitleComponent = IChatBaseComponent.ChatSerializer.a(
            "{\"text\":\"" + MessageUtils.getColor(subtitle) + "\"}"
        );
        
        // Configurar los tiempos de animación para el título y subtítulo
        // Este paquete define cuánto tiempo tomará cada fase de la animación
        PacketPlayOutTitle timesPacket = new PacketPlayOutTitle(
            PacketPlayOutTitle.EnumTitleAction.TIMES, 
            null, 
            fadeIn, 
            stay, 
            fadeOut
        );
        connection.sendPacket(timesPacket);
        
        // Enviar el paquete del título principal
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(
            PacketPlayOutTitle.EnumTitleAction.TITLE, 
            titleComponent
        );
        connection.sendPacket(titlePacket);
        
        // Enviar el paquete del subtítulo
        // Este aparecerá debajo del título principal
        PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(
            PacketPlayOutTitle.EnumTitleAction.SUBTITLE, 
            subtitleComponent
        );
        connection.sendPacket(subtitlePacket);
    }
}
