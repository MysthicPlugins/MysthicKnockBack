package mk.kvlzx.powerup;

import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class BlackHoleItem {
    private final MysthicKnockBack plugin;
    
    public BlackHoleItem(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Crea el item de agujero negro
     */
    public ItemStack createBlackHoleItem() {
        ItemStack item = new ItemStack(Material.valueOf(plugin.getMainConfig().getPowerUpBlackHoleItemId()));
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getColor(plugin.getMainConfig().getPowerUpBlackHoleItemName()));
            meta.setLore(plugin.getMainConfig().getPowerUpBlackHoleItemLore().stream().
                    map(MessageUtils::getColor)
                    .collect(Collectors.toList()
            ));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Maneja el uso del item de agujero negro
     */
    public void handleRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !isBlackHoleItem(item)) {
            return;
        }
        
        // Verificar si el jugador ya tiene un agujero negro activo
        if (player.hasMetadata("blackhole_active")) {
            return;
        }
        
        // Marcar al jugador como que tiene un agujero negro activo
        player.setMetadata("blackhole_active", new FixedMetadataValue(plugin, true));
        
        // Lanzar el proyectil de agujero negro
        BlackHoleProjectile projectile = new BlackHoleProjectile(plugin, player);
        projectile.launch();
        
        // Remover el item del inventario
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }
        
        event.setCancelled(true);
    }
    
    /**
     * Verifica si un item es el item de agujero negro
     */
    public boolean isBlackHoleItem(ItemStack item) {
        if (item == null || item.getType() != Material.valueOf(plugin.getMainConfig().getPowerUpBlackHoleItemId())) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        return meta.getDisplayName().contains(plugin.getMainConfig().getPowerUpBlackHoleItemName());
    }
}
