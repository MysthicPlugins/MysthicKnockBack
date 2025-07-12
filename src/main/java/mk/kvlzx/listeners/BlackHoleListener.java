package mk.kvlzx.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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
        // Verificar si es click derecho
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Verificar si es el item de agujero negro
        if (!blackHoleItem.isBlackHoleItem(event.getItem())) {
            return;
        }
        
        // Manejar el uso del item
        blackHoleItem.handleRightClick(event);
    }
}
