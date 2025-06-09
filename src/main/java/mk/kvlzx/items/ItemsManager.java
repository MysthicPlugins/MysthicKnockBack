package mk.kvlzx.items;

import org.bukkit.entity.Player;

import mk.kvlzx.hotbar.PlayerHotbar;

public class ItemsManager {
    public static void giveSpawnItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(4, CustomItem.createSkull(player, "&a✦ Main Menu", "&7Right click to open the menu"));
    }
    
    public static void givePvPItems(Player player) {
        player.getInventory().clear();
        PlayerHotbar.applyLayout(player);
    }
}
