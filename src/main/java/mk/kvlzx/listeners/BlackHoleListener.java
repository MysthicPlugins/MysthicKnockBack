package mk.kvlzx.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.powerup.BlackHoleItem;

public class BlackHoleListener implements Listener {
    
    private final MysthicKnockBack plugin;
    private final BlackHoleItem blackHoleItem;
    
    public BlackHoleListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.blackHoleItem = new BlackHoleItem(plugin);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        plugin.getLogger().info("DEBUG: PlayerInteractEvent disparado");
        plugin.getLogger().info("DEBUG: Acción: " + event.getAction().name());
        plugin.getLogger().info("DEBUG: Jugador: " + event.getPlayer().getName());
        
        // Verificar si es click derecho
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            plugin.getLogger().info("DEBUG: No es click derecho, saliendo");
            return;
        }
        
        plugin.getLogger().info("DEBUG: Es click derecho, verificando item");
        
        ItemStack item = event.getItem();
        if (item == null) {
            plugin.getLogger().info("DEBUG: Item en evento es null");
            return;
        }
        
        plugin.getLogger().info("DEBUG: Item en evento: " + item.getType().name());
        
        // Verificar si es el item de agujero negro
        if (!blackHoleItem.isBlackHoleItem(item)) {
            plugin.getLogger().info("DEBUG: No es item de agujero negro según verificación");
            return;
        }
        
        plugin.getLogger().info("DEBUG: Es item de agujero negro, llamando a handleRightClick");
        
        // Manejar el uso del item
        blackHoleItem.handleRightClick(event);
        
        plugin.getLogger().info("DEBUG: handleRightClick completado");
    }
}
