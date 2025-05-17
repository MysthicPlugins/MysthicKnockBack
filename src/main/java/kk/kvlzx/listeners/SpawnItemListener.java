package kk.kvlzx.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

public class SpawnItemListener implements Listener {
    private final KvKnockback plugin;

    public SpawnItemListener(KvKnockback plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawnItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // Verificar que el jugador esté en spawn
        String zone = plugin.getArenaManager().getPlayerZone(player);
        if (zone == null || !zone.equals("spawn")) return;

        // Cancelar cualquier interacción con bloques en el spawn
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
        }

        // Solo procesar clicks al aire o bloques con click derecho
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        switch (item.getType()) {
            case DIAMOND_SWORD:
                showTopKills(player);
                break;
            case GOLDEN_APPLE:
                showTopKDR(player);
                break;
            case BLAZE_POWDER:
                showTopStreaks(player);
                break;
            case NETHER_STAR:
                showTopElo(player);
                break;
            case SKULL_ITEM:
                showPlayerStats(player);
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String zone = plugin.getArenaManager().getPlayerZone(player);
        
        // Cancelar la colocación de bloques en el spawn
        if (zone != null && zone.equals("spawn")) {
            event.setCancelled(true);
        }
    }

    private void showTopKills(Player player) {
        // Implementar lógica para mostrar top kills
        player.sendMessage(MessageUtils.getColor("&b&l=== Top Kills ==="));
    }

    private void showTopKDR(Player player) {
        // Implementar lógica para mostrar top KDR
        player.sendMessage(MessageUtils.getColor("&6&l=== Top KDR ==="));
    }

    private void showTopStreaks(Player player) {
        // Implementar lógica para mostrar top rachas
        player.sendMessage(MessageUtils.getColor("&c&l=== Top Rachas ==="));
    }

    private void showTopElo(Player player) {
        // Implementar lógica para mostrar top ELO
        player.sendMessage(MessageUtils.getColor("&e&l=== Top ELO ==="));
    }

    private void showPlayerStats(Player player) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        player.sendMessage(MessageUtils.getColor("&a&l=== Tus Estadísticas ==="));
        player.sendMessage(MessageUtils.getColor("&fKills: &a" + stats.getKills()));
        player.sendMessage(MessageUtils.getColor("&fMuertes: &c" + stats.getDeaths()));
        player.sendMessage(MessageUtils.getColor("&fELO: &6" + stats.getElo()));
        player.sendMessage(MessageUtils.getColor("&fKDR: &b" + String.format("%.2f", stats.getKDR())));
    }
}
