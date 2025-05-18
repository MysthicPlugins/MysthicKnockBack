package kk.kvlzx.items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.items.CustomItem.ItemType;

public class ItemsManager {
    private static final Map<UUID, ItemStack[]> playerLayouts = new HashMap<>();
    private static final ItemStack[] DEFAULT_LAYOUT = new ItemStack[] {
        CustomItem.create(ItemType.KNOCKER),
        CustomItem.create(ItemType.BLOCKS),
        CustomItem.create(ItemType.BOW),
        null,
        null,
        null,
        CustomItem.create(ItemType.PLATE),
        CustomItem.create(ItemType.FEATHER),
        CustomItem.create(ItemType.PEARL)
    };

    public static void giveSpawnItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(4, CustomItem.createSkull(player, "&a✦ Menú Principal", "&7Click derecho para abrir el menú"));
        player.updateInventory();
    }
    
    public static void givePvPItems(Player player) {
        player.getInventory().clear();
        ItemStack[] layout = getPlayerLayout(player);
        
        // Aplicar layout personalizado o default
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, layout[i]);
        }
        
        // La flecha siempre va en el slot 9
        player.getInventory().setItem(9, CustomItem.create(ItemType.ARROW));
        player.updateInventory();
    }

    public static ItemStack[] getPlayerLayout(Player player) {
        return playerLayouts.getOrDefault(player.getUniqueId(), DEFAULT_LAYOUT.clone());
    }

    public static void savePlayerLayout(Player player, ItemStack[] layout) {
        playerLayouts.put(player.getUniqueId(), layout);
    }
}
