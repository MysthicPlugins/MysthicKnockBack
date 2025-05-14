package kk.kvlzx.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.items.Pearl;
import kk.kvlzx.utils.MessageUtils;

import java.util.Arrays;

public class CombatListener implements Listener {
    private final KvKnockback plugin;

    public CombatListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        DamageCause cause = event.getCause();

        // Solo cancelamos el daño por caída
        if (cause == DamageCause.FALL) {
            event.setCancelled(true);
        } else if (cause == DamageCause.VOID) {
            player.setHealth(0.0D);
        }
        event.setDamage(0.0D);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (cause == EntityDamageEvent.DamageCause.VOID || cause == EntityDamageEvent.DamageCause.CUSTOM) {
            player.setHealth(0.0);
            return;
        }

        event.setDamage(0.0D);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            ((Player) event.getEntity()).setFoodLevel(20);
            ((Player) event.getEntity()).setSaturation(20.0f);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer == victim) {
            return;
        }

        // -------------------------PROVISIONAL!!!-------------------------
        killer.playSound(killer.getLocation(), Sound.SWIM, 1.0f, 1.0f);

        int pearlSlot = 8;
        ItemStack currentItem = killer.getInventory().getItem(pearlSlot);

        Pearl pearl = new Pearl(
            "&5 Perla",
            Arrays.asList(MessageUtils.getColor("&5 Cada lanzamiento reescribe tu destino.")),
            Material.ENDER_PEARL
        );
        ItemStack pearlItem = pearl.getItem();
        pearlItem.setAmount(1);

        if (currentItem == null || currentItem.getType() == Material.AIR) {
            killer.getInventory().setItem(pearlSlot, pearlItem);
        } else if (currentItem.getType() == Material.ENDER_PEARL) {
            int currentAmount = currentItem.getAmount();
            if (currentAmount < 128) {
                currentItem.setAmount(currentAmount + 1);
                killer.getInventory().setItem(pearlSlot, currentItem);
            }
        }

        killer.updateInventory();
    }
}
