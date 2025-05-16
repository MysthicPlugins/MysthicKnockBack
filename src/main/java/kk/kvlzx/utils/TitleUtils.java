package kk.kvlzx.utils;

import org.bukkit.entity.Player;

public class TitleUtils {
    
    /**
     * @param player    El jugador que recibirá el título
     * @param title     El texto principal del título (soporta códigos de color con &)
     * @param subtitle  El texto del subtítulo (soporta códigos de color con &)
     */
    public static void sendTitle(Player player, String title, String subtitle) {
        // Enviar titulos utilizando el metodo de Bukkit
        player.sendTitle(MessageUtils.getColor(title), MessageUtils.getColor(subtitle));
    }
}
