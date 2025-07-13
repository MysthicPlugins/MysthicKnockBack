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
        
        plugin.getLogger().info("DEBUG: handleRightClick llamado para jugador: " + player.getName());
        
        if (item == null) {
            plugin.getLogger().info("DEBUG: Item es null, saliendo");
            return;
        }
        
        plugin.getLogger().info("DEBUG: Item type: " + item.getType().name());
        plugin.getLogger().info("DEBUG: Item amount: " + item.getAmount());
        
        if (!isBlackHoleItem(item)) {
            plugin.getLogger().info("DEBUG: No es un item de agujero negro, saliendo");
            return;
        }
        
        plugin.getLogger().info("DEBUG: Item verificado como agujero negro");
        
        // Verificar si el jugador ya tiene un agujero negro activo
        if (player.hasMetadata("blackhole_active")) {
            plugin.getLogger().info("DEBUG: Jugador ya tiene un agujero negro activo, saliendo");
            return;
        }
        
        plugin.getLogger().info("DEBUG: Jugador no tiene agujero negro activo, continuando");
        
        // Marcar al jugador como que tiene un agujero negro activo
        player.setMetadata("blackhole_active", new FixedMetadataValue(plugin, true));
        plugin.getLogger().info("DEBUG: Metadata 'blackhole_active' agregado al jugador");
        
        // Lanzar el proyectil de agujero negro
        plugin.getLogger().info("DEBUG: Creando proyectil de agujero negro");
        BlackHoleProjectile projectile = new BlackHoleProjectile(plugin, player);
        projectile.launch();
        plugin.getLogger().info("DEBUG: Proyectil lanzado");
        
        // Remover el item del inventario
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            plugin.getLogger().info("DEBUG: Cantidad del item reducida a: " + item.getAmount());
        } else {
            player.getInventory().remove(item);
            plugin.getLogger().info("DEBUG: Item removido del inventario");
        }
        
        event.setCancelled(true);
        plugin.getLogger().info("DEBUG: Evento cancelado");
    }
    
    /**
     * Verifica si un item es el item de agujero negro
     */
    public boolean isBlackHoleItem(ItemStack item) {
        if (item == null) {
            plugin.getLogger().info("DEBUG: isBlackHoleItem - item es null");
            return false;
        }
        
        String expectedMaterial = plugin.getMainConfig().getPowerUpBlackHoleItemId();
        plugin.getLogger().info("DEBUG: isBlackHoleItem - material esperado: " + expectedMaterial);
        plugin.getLogger().info("DEBUG: isBlackHoleItem - material del item: " + item.getType().name());
        
        if (item.getType() != Material.valueOf(expectedMaterial)) {
            plugin.getLogger().info("DEBUG: isBlackHoleItem - material no coincide");
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            plugin.getLogger().info("DEBUG: isBlackHoleItem - meta es null");
            return false;
        }
        
        if (!meta.hasDisplayName()) {
            plugin.getLogger().info("DEBUG: isBlackHoleItem - no tiene display name");
            return false;
        }
        
        String itemDisplayName = meta.getDisplayName();
        // Procesar el nombre esperado con códigos de color
        String expectedDisplayName = MessageUtils.getColor(plugin.getMainConfig().getPowerUpBlackHoleItemName());
        plugin.getLogger().info("DEBUG: isBlackHoleItem - display name del item: '" + itemDisplayName + "'");
        plugin.getLogger().info("DEBUG: isBlackHoleItem - display name esperado procesado: '" + expectedDisplayName + "'");
        
        // Comparar directamente para mayor precisión
        boolean matches = itemDisplayName.equals(expectedDisplayName);
        plugin.getLogger().info("DEBUG: isBlackHoleItem - nombres coinciden: " + matches);
        
        return matches;
    }
}
