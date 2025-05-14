package kk.kvlzx.listeners;

import org.bukkit.event.Listener;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.items.Arrow;
import kk.kvlzx.items.Blocks;
import kk.kvlzx.items.Bow;
import kk.kvlzx.items.Feather;
import kk.kvlzx.items.Knocker;
import kk.kvlzx.items.Pearl;
import kk.kvlzx.items.Plate;
import kk.kvlzx.utils.MessageUtils;

public class PlayerListener implements Listener {
    private final KvKnockback plugin;

    public PlayerListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(MessageUtils.getColor("&9&l¡Bienvenido! &7Domina la arena y deja tu marca."));

        // Knockers
        Knocker knockers = new Knocker("&5 Knocker", Arrays.asList(MessageUtils.getColor("&8 No es la fuerza, es la técnica.")), Material.STICK);

        // Blocks
        Blocks blocks = new Blocks("&3 Arenisca", Arrays.asList(MessageUtils.getColor("&8 Un clásico del desierto")), Material.SANDSTONE);

        // Bow
        Bow bow = new Bow("&4 Mazakarko", Arrays.asList(MessageUtils.getColor("&8 Un disparo, un impacto, un salto al vacío.")), Material.BOW);

        // Arrow
        Arrow arrow = new Arrow("&5 Flecha", Material.ARROW);

        // Plate
        Plate plate = new Plate("&6 Placa", Arrays.asList(MessageUtils.getColor("&8 ¿Listo para volar? Pisa y verás.")), Material.GOLD_PLATE);

        // Feather
        Feather feather = new Feather("&e Pluma", Arrays.asList(MessageUtils.getColor("&8 No es magia, es pura aerodinámica.")), Material.FEATHER);

        // Pearl
        Pearl pearl = new Pearl("&5 Perla", Arrays.asList(MessageUtils.getColor("&8 Cada lanzamiento reescribe tu destino.")), Material.ENDER_PEARL);

        player.getInventory().clear();
        player.getInventory().setItem(0, knockers.getItem());
        player.getInventory().setItem(1, blocks.getItem());
        player.getInventory().setItem(2, bow.getItem());
        player.getInventory().setItem(9, arrow.getItem());
        player.getInventory().setItem(6, plate.getItem());
        player.getInventory().setItem(7, feather.getItem());
        player.getInventory().setItem(8, pearl.getItem());
    }
}