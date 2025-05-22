package kk.kvlzx.managers;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.tablist.HeaderFooterManager;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

public class TabManager {
    private final KvKnockback plugin;
    private final TabAPI tabAPI;
    private int animationFrame = 0;
    private final String[] headerAnimations = {
        "&b&l≽^•⩊•^≼ &6&lKnockbackFFA &b&l≽^•⩊•^≼",
        "&3&l≽^•⩊•^≼ &e&lKnockbackFFA &3&l≽^•⩊•^≼",
        "&9&l≽^•⩊•^≼ &f&lKnockbackFFA &9&l≽^•⩊•^≼"
    };

    public TabManager(KvKnockback plugin) {
        this.plugin = plugin;
        this.tabAPI = TabAPI.getInstance();
        startAnimation();
        registerPlaceholders();
    }

    private void startAnimation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateHeaderFooter();
                animationFrame = (animationFrame + 1) % headerAnimations.length;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateHeaderFooter() {
        String header = MessageUtils.getColor(
            "\n" + headerAnimations[animationFrame] + "\n" +
            "&7¡Demuestra tu habilidad en KnockBack!\n"
        );

        String footer = MessageUtils.getColor(
            "\n&eTienda: &ftienda.servidor.com" +
            "\n&bDiscord: &fdiscord.gg/servidor" +
            "\n&aJugadores Online: &f%online%\n"
        );

        for (TabPlayer player : tabAPI.getOnlinePlayers()) {
            tabAPI.getHeaderFooterManager().setHeader(player, header);
            tabAPI.getHeaderFooterManager().setFooter(player, footer);
            
            // Setear el formato del tab para cada jugador
            String rankPrefix = RankManager.getRankPrefix(PlayerStats.getStats(player.getUniqueId()).getElo());
            tabAPI.getTabListFormatManager().setPrefix(player, rankPrefix + " ");
            tabAPI.getTabListFormatManager().setSuffix(player, " &8[&f%ping%ms&8]");
        }
    }

    private void registerPlaceholders() {
        PlaceholderManager pm = tabAPI.getPlaceholderManager();
        
        pm.registerPlayerPlaceholder("%rank%", 1000, player -> {
            Player p = plugin.getServer().getPlayer(player.getName());
            if (p != null) {
                return RankManager.getRankPrefix(PlayerStats.getStats(p.getUniqueId()).getElo()) + " ";
            }
            return "";
        });

        pm.registerServerPlaceholder("%online%", 1000, () -> 
            String.valueOf(plugin.getServer().getOnlinePlayers().size())
        );

        // Configurar el formato del tab
        for (TabPlayer player : tabAPI.getOnlinePlayers()) {
            tabAPI.getNameTagManager().setPrefix(player, "%rank%");
        }
    }

    public void onPlayerJoin(PlayerLoadEvent e) {
        updateHeaderFooter();
    }

}
