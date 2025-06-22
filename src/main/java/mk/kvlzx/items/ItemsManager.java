package mk.kvlzx.items;

import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.hotbar.PlayerHotbar;

public class ItemsManager {
    public static void giveSpawnItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(MysthicKnockBack.getInstance().getMainConfig().getSkullSlot(), CustomItem.createSkull(player, MysthicKnockBack.getInstance().getMainConfig().getSkullName(), MysthicKnockBack.getInstance().getMainConfig().getSkullLore()));
    }
    
    public static void givePvPItems(Player player) {
        player.getInventory().clear();
        PlayerHotbar.applyLayout(player);
    }
}
