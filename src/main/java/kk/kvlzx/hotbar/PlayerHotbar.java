package kk.kvlzx.hotbar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.items.CustomItem;
import kk.kvlzx.items.CustomItem.ItemType;

public class PlayerHotbar {
    private static final Map<UUID, ItemStack[]> playerLayouts = new HashMap<>();
    private static final ItemStack[] DEFAULT_LAYOUT = new ItemStack[9];

    static {
        // Layout por defecto
        DEFAULT_LAYOUT[0] = CustomItem.create(ItemType.KNOCKER);
        DEFAULT_LAYOUT[1] = CustomItem.create(ItemType.BLOCKS);
        DEFAULT_LAYOUT[2] = CustomItem.create(ItemType.BOW);
        DEFAULT_LAYOUT[6] = CustomItem.create(ItemType.PLATE);
        DEFAULT_LAYOUT[7] = CustomItem.create(ItemType.FEATHER);
        DEFAULT_LAYOUT[8] = CustomItem.create(ItemType.PEARL);
    }

    public static void setPlayerLayout(UUID uuid, ItemStack[] layout) {
        ItemStack[] copy = new ItemStack[9];
        System.arraycopy(layout, 0, copy, 0, 9);
        playerLayouts.put(uuid, copy);
    }

    public static ItemStack[] getPlayerLayout(UUID uuid) {
        return playerLayouts.getOrDefault(uuid, DEFAULT_LAYOUT).clone();
    }

    public static void applyLayout(Player player) {
        ItemStack[] layout = getPlayerLayout(player.getUniqueId());
        for (int i = 0; i < 9; i++) {
            if (layout[i] != null) {
                player.getInventory().setItem(i, layout[i].clone());
            }
        }
        // Flechas siempre en el slot 9
        player.getInventory().setItem(9, CustomItem.create(ItemType.ARROW));
        player.updateInventory();
    }

    public static void resetLayout(UUID uuid) {
        playerLayouts.remove(uuid);
    }
}
