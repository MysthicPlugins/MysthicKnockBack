package kk.kvlzx.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import kk.kvlzx.utils.MessageUtils;

public class ItemsManager {

    public static void giveSpawnItems(Player player) {
        player.getInventory().clear();

        // Ejemplos de items
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemStack book = new ItemStack(Material.BOOK);
        
        player.getInventory().setItem(0, book);
        player.getInventory().setItem(1, compass);

        player.updateInventory();
    }
    
    public static void givePvPItems(Player player) {
        player.getInventory().clear();

        Knocker knockers = new Knocker("&5Knocker", Arrays.asList(MessageUtils.getColor("&8 No es la fuerza, es la técnica.")), Material.STICK);
        Blocks blocks = new Blocks("&3Arenisca", Arrays.asList(MessageUtils.getColor("&8 Un clásico del desierto")), Material.SANDSTONE);
        Bow bow = new Bow("&4Mazakarko", Arrays.asList(MessageUtils.getColor("&8 Un disparo, un impacto, un salto al vacío.")), Material.BOW);
        Arrow arrow = new Arrow("&5Flecha", Material.ARROW);
        Plate plate = new Plate("&6Placa", Arrays.asList(MessageUtils.getColor("&8 ¿Listo para volar? Pisa y verás.")), Material.GOLD_PLATE);
        Feather feather = new Feather("&ePluma", Arrays.asList(MessageUtils.getColor("&8 No es magia, es pura aerodinámica.")), Material.FEATHER);
        Pearl pearl = new Pearl("&5Perla", Arrays.asList(MessageUtils.getColor("&8 Cada lanzamiento reescribe tu destino.")), Material.ENDER_PEARL);

        player.getInventory().setItem(0, knockers.getItem());
        player.getInventory().setItem(1, blocks.getItem());
        player.getInventory().setItem(2, bow.getItem());
        player.getInventory().setItem(9, arrow.getItem());
        player.getInventory().setItem(6, plate.getItem());
        player.getInventory().setItem(7, feather.getItem());
        player.getInventory().setItem(8, pearl.getItem());
        player.updateInventory();
    }
}
