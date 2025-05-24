package kk.kvlzx.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.items.CustomItem.ItemType;

public class ItemsManager {
    public static void giveSpawnItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(4, CustomItem.createSkull(player, "&a✦ Menú Principal", "&7Click derecho para abrir el menú"));
        player.updateInventory();
    }
    
    private static ItemStack[] pvpLayout = new ItemStack[9];

    static {
        // Layout por defecto
        pvpLayout[0] = CustomItem.create(ItemType.KNOCKER);
        pvpLayout[1] = CustomItem.create(ItemType.BLOCKS);
        pvpLayout[2] = CustomItem.create(ItemType.BOW);
        pvpLayout[6] = CustomItem.create(ItemType.PLATE);
        pvpLayout[7] = CustomItem.create(ItemType.FEATHER);
        pvpLayout[8] = CustomItem.create(ItemType.PEARL);
    }

    public static void givePvPItems(Player player) {
        player.getInventory().clear();
        for (int i = 0; i < pvpLayout.length; i++) {
            if (pvpLayout[i] != null) {
                player.getInventory().setItem(i, pvpLayout[i].clone());
            }
        }
        // Flechas siempre en el slot 9
        player.getInventory().setItem(9, CustomItem.create(ItemType.ARROW));
        player.updateInventory();
    }

    public static ItemStack[] getPvPLayout() {
        return pvpLayout.clone();
    }

    public static void savePvPLayout(ItemStack[] newLayout) {
        System.arraycopy(newLayout, 0, pvpLayout, 0, 9);
    }
}
