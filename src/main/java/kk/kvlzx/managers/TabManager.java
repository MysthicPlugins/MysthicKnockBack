package kk.kvlzx.managers;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

import java.lang.reflect.Field;

public class TabManager {
    private final KvKnockback plugin;
    private int animationFrame = 0;
    private final String[] headerAnimations = {
        "&b&l≽^•⩊•^≼ &6&lKnockbackFFA &b&l≽^•⩊•^≼",
        "&3&l≽^•⩊•^≼ &e&lKnockbackFFA &3&l≽^•⩊•^≼",
        "&9&l≽^•⩊•^≼ &f&lKnockbackFFA &9&l≽^•⩊•^≼"
    };

    public TabManager(KvKnockback plugin) {
        this.plugin = plugin;
        startAnimation();
    }

    private void startAnimation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateHeaderFooter();
                updatePlayerList();
                animationFrame = (animationFrame + 1) % headerAnimations.length;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void updateHeaderFooter() {
        String header = MessageUtils.getColor(
            "\n" +
            "               " + headerAnimations[animationFrame] + "\n" +
            "                &7¡Demuestra tu habilidad en KnockBack!\n"
        );

        String footer = MessageUtils.getColor(
            "\n" +
            "                    &eTienda: &ftienda.servidor.com\n" +
            "                   &bDiscord: &fdiscord.gg/servidor\n" +
            "              &aJugadores Online: &f" + Bukkit.getOnlinePlayers().size() + "\n"
        );

        IChatBaseComponent headerComponent = ChatSerializer.a("{\"text\": \"" + header + "\"}");
        IChatBaseComponent footerComponent = ChatSerializer.a("{\"text\": \"" + footer + "\"}");

        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
            try {
                Field a = packet.getClass().getDeclaredField("a");
                a.setAccessible(true);
                a.set(packet, headerComponent);
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                b.set(packet, footerComponent);
                craftPlayer.getHandle().playerConnection.sendPacket(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePlayerList() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            String rankPrefix = RankManager.getRankPrefix(PlayerStats.getStats(player.getUniqueId()).getElo());
            int ping = craftPlayer.getHandle().ping;
            
            String displayName = MessageUtils.getColor(
                rankPrefix + "&f " + player.getName() + " &8[&f" + ping + "ms&8]"
            );

            // Actualizar el nombre en la lista de jugadores
            craftPlayer.setPlayerListName(displayName);
            
            // Actualizar el tab para todos los jugadores
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, 
                craftPlayer.getHandle()
            );

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }
}
