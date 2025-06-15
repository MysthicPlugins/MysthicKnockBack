package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.TitleUtils;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;

public class StreakManager {
    private static final Map<UUID, Integer> currentStreaks = new HashMap<>();
    private static final Map<UUID, Integer> maxStreaks = new HashMap<>();
    private static final Map<UUID, ArmorStand> playerMvpTags = new HashMap<>();

    public static void addStreak(UUID uuid) {
        int streak = getStreak(uuid) + 1;
        currentStreaks.put(uuid, streak);
        if (streak > getMaxStreak(uuid)) {
            maxStreaks.put(uuid, streak);
        }
        notifyStreak(uuid, streak);
    }

    public static void resetStreak(UUID uuid) {
        if (getStreak(uuid) >= 5) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&c☠ &f" + player.getName() + 
                    " &7lost their streak of &c" + getStreak(uuid) + " &7kills! &c☠"));
                player.playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 1.0f, 1.0f);
            }
        if (player != null) {
            player.setLevel(getStreak(uuid));
            player.setExp(1.0f);
        }
        }
        currentStreaks.put(uuid, 0);
        removeTag(uuid);
    }

    public static int getStreak(UUID uuid) {
        return currentStreaks.getOrDefault(uuid, 0);
    }

    public static int getMaxStreak(UUID uuid) {
        return maxStreaks.getOrDefault(uuid, 0);
    }

    public static void setStreak(UUID uuid, int streak) {
        currentStreaks.put(uuid, streak);
    }

    public static void setMaxStreak(UUID uuid, int maxStreak) {
        maxStreaks.put(uuid, maxStreak);
    }

    private static void notifyStreak(UUID uuid, int streak) {
        if (streak > 0 && streak % 5 == 0) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            
            String playerName = player.getName();
            int elo = PlayerStats.getStats(uuid).getElo();
            
            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&e" + playerName + 
                " &fhas reached a streak of &a" + streak + " &akills!"));
            
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
            }
            
            String rankPrefix = RankManager.getRankPrefix(elo);
            for (Player online : Bukkit.getOnlinePlayers()) {
                TitleUtils.sendTitle(
                    online,
                    MessageUtils.getColor(rankPrefix + " &f" + playerName),
                    "&7Streak of &f" + streak + " &7kills!",
                    10, 40, 10
                );
            }
        }

        updateMvpTag(Bukkit.getPlayer(uuid));
    }

    private static String getMvpTag(int streak) {
        if (streak < 5) return null;
        if (streak >= 500) return "&5MVP+";
        else if (streak >= 300) return "&cMVP";
        else if (streak >= 250) return "&6MVP";
        else if (streak >= 200) return "&eMVP";
        else if (streak >= 150) return "&bMVP";
        else if (streak >= 100) return "&aMVP";
        else if (streak >= 80) return "&9MVP";
        else if (streak >= 60) return "&dMVP";
        else if (streak >= 40) return "&7MVP";
        return "&8MVP";
    }

    private static void updateMvpTag(Player player) {
        if (player == null || !player.isOnline()) return;
        
        UUID uuid = player.getUniqueId();
        int streak = getStreak(uuid);
        String mvpTag = getMvpTag(streak);

        if (streak >= 5) {
            removeTag(uuid);
            
            Location loc = player.getLocation().add(0, 2.2, 0);
            ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setSmall(true);
            armorStand.setMarker(true);
            armorStand.setCustomName(MessageUtils.getColor(mvpTag + " &7- Kills: " + streak));

            playerMvpTags.put(uuid, armorStand);

            // Ocultar el armor stand para el jugador propietario usando NMS
            hideArmorStandFromOwner(player, armorStand);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null || !p.isOnline() || p.isDead() || getStreak(uuid) < 5) {
                        removeTag(uuid);
                        cancel();
                        return;
                    }
                    armorStand.teleport(p.getLocation().add(0, 2.2, 0));
                    int currentStreak = getStreak(uuid);
                    String currentMvpTag = getMvpTag(currentStreak);
                    if (currentMvpTag != null && !armorStand.getCustomName().equals(MessageUtils.getColor(currentMvpTag + " &7- Kills: " + currentStreak))) {
                        armorStand.setCustomName(MessageUtils.getColor(currentMvpTag + " &7- Kills: " + currentStreak));
                    }
                }
            }.runTaskTimer(MysthicKnockBack.getInstance(), 0L, 1L);
        } else {
            removeTag(uuid);
        }
    }

    private static void hideArmorStandFromOwner(Player owner, ArmorStand armorStand) {
        try {
            // Enviar el paquete de destrucción de entidad solo al propietario
            CraftArmorStand craftArmorStand = (CraftArmorStand) armorStand;
            
            int entityId = craftArmorStand.getHandle().getId();
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityId);
            
            // Enviar el paquete solo al jugador propietario
            ((CraftPlayer) owner).getHandle().playerConnection.sendPacket(destroyPacket);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeTag(UUID uuid) {
        ArmorStand oldTag = playerMvpTags.remove(uuid);
        if (oldTag != null && !oldTag.isDead()) {
            oldTag.remove(); // Elimina la entidad del mundo
        }
    }

    public static void cleanup() {
        playerMvpTags.values().forEach(tag -> {
            if (tag != null && !tag.isDead()) {
                tag.remove();
            }
        });
        playerMvpTags.clear();
    }
}
