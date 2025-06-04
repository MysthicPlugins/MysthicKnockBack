package mk.kvlzx.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.ArrowEffectItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArrowEffectListener implements Listener {
    private final MysthicKnockBack plugin;
    private final Map<UUID, ArrowEffectItem> activeArrows = new HashMap<>();

    public ArrowEffectListener(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;
        
        Player shooter = (Player) arrow.getShooter();
        String effectName = plugin.getCosmeticManager().getPlayerArrowEffect(shooter.getUniqueId());
        
        if (effectName.equals("none")) return;
        
        ArrowEffectItem effect = ArrowEffectItem.getByName(effectName);
        if (effect == null) return;

        activeArrows.put(arrow.getUniqueId(), effect);
        
        // Iniciar la animación
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isDead() || arrow.isOnGround() || !activeArrows.containsKey(arrow.getUniqueId())) {
                    activeArrows.remove(arrow.getUniqueId());
                    this.cancel();
                    return;
                }
                effect.playEffect(arrow.getLocation());
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();
        
        ArrowEffectItem effect = activeArrows.remove(arrow.getUniqueId());
        if (effect != null) {
            // Efecto final al impactar
            effect.playEffect(arrow.getLocation());
        }
    }
}
