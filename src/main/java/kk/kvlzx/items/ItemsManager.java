package kk.kvlzx.items;

import org.bukkit.entity.Player;

import kk.kvlzx.items.CustomItem.ItemType;

public class ItemsManager {

    public static void giveSpawnItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(0, CustomItem.create(ItemType.TOP_KILLS));
        player.getInventory().setItem(1, CustomItem.create(ItemType.TOP_KDR));
        player.getInventory().setItem(2, CustomItem.create(ItemType.TOP_STREAKS));
        player.getInventory().setItem(4, CustomItem.createSkull(player));
        player.getInventory().setItem(6, CustomItem.create(ItemType.TOP_ELO));
        player.updateInventory();
    }
    
    public static void givePvPItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(0, CustomItem.create(ItemType.KNOCKER));
        player.getInventory().setItem(1, CustomItem.create(ItemType.BLOCKS));
        player.getInventory().setItem(2, CustomItem.create(ItemType.BOW));
        player.getInventory().setItem(9, CustomItem.create(ItemType.ARROW));
        player.getInventory().setItem(6, CustomItem.create(ItemType.PLATE));
        player.getInventory().setItem(7, CustomItem.create(ItemType.FEATHER));
        player.getInventory().setItem(8, CustomItem.create(ItemType.PEARL));
        player.updateInventory();
    }
}
