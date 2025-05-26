package kk.kvlzx.items;

import org.bukkit.entity.Player;

import kk.kvlzx.hotbar.PlayerHotbar;

public class ItemsManager {
    public static void giveSpawnItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(4, CustomItem.createSkull(player, "&a✦ Menú Principal", "&7Click derecho para abrir el menú"));
        player.updateInventory();
    }
    
    public static void givePvPItems(Player player) {
        player.getInventory().clear();
        PlayerHotbar.applyLayout(player);
    }
}
