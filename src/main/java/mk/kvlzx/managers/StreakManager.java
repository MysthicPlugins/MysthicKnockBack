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
import mk.kvlzx.config.MainConfig;
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
        int currentStreak = getStreak(uuid);
        if (currentStreak >= 5) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                MainConfig config = MysthicKnockBack.getInstance().getMainConfig();
                String streakLostMessage = config.getStreakMessageLost()
                    .replace("%player%", player.getName())
                    .replace("%streak%", String.valueOf(currentStreak));
                
                Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + streakLostMessage));
                player.playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 1.0f, 1.0f);
            }
            if (player != null) {
                player.setLevel(currentStreak);
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
            MainConfig config = MysthicKnockBack.getInstance().getMainConfig();
            
            String streakReachedMessage = config.getStreakMessageReached()
                .replace("%player%", playerName)
                .replace("%streak%", String.valueOf(streak));
            
            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + streakReachedMessage));
            
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
            }
            
            // Usar configuración de título si está habilitada
            if (config.getStreakTitleEnabled()) {
                String rankPrefix = RankManager.getRankPrefix(elo);
                String titleText = config.getStreakTitleTitle()
                    .replace("%rank%", rankPrefix)
                    .replace("%player%", playerName);
                String subtitleText = config.getStreakTitleSubtitle()
                    .replace("%streak%", String.valueOf(streak));
                
                for (Player online : Bukkit.getOnlinePlayers()) {
                    TitleUtils.sendTitle(
                        online,
                        MessageUtils.getColor(titleText),
                        MessageUtils.getColor(subtitleText),
                        config.getStreakTitleFadeIn(),
                        config.getStreakTitleStay(),
                        config.getStreakTitleFadeOut()
                    );
                }
            }
        }

        updateMvpTag(Bukkit.getPlayer(uuid));
    }

    private static String getMvpTag(int streak) {
        
        if (streak < 40) {
            return null;
        }
        
        MainConfig config = MysthicKnockBack.getInstance().getMainConfig();
        if (config == null) {
            return null;
        }
        
        String tag = null;
        if (streak >= 500) tag = config.getStreakTag500();
        else if (streak >= 300) tag = config.getStreakTag300();
        else if (streak >= 250) tag = config.getStreakTag250();
        else if (streak >= 200) tag = config.getStreakTag200();
        else if (streak >= 150) tag = config.getStreakTag150();
        else if (streak >= 100) tag = config.getStreakTag100();
        else if (streak >= 80) tag = config.getStreakTag80();
        else if (streak >= 60) tag = config.getStreakTag60();
        else if (streak >= 40) tag = config.getStreakTag40();
        
        if (tag == null) {
            return null;
        }
        
        String coloredTag = MessageUtils.getColor(tag);
        
        return coloredTag;
    }

    private static void updateMvpTag(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        int streak = getStreak(uuid);
        String mvpTag = getMvpTag(streak);

        if (streak >= 40 && mvpTag != null) {
            removeTag(uuid);
            
            MainConfig config = MysthicKnockBack.getInstance().getMainConfig();
            
            // Verificar configuración
            if (config == null) {
                return;
            }
            
            // Usar offsets configurables para la posición del ArmorStand
            Location loc = player.getLocation().add(
                config.getStreakArmorStandX(),
                config.getStreakArmorStandY(),
                config.getStreakArmorStandZ()
            );
            
            try {
                ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setCustomNameVisible(true);
                armorStand.setSmall(true);
                armorStand.setMarker(true);
                
                // Usar nombre configurable para el ArmorStand
                String armorStandName = config.getStreakArmorStandName()
                    .replace("%tag%", mvpTag)
                    .replace("%kills%", String.valueOf(streak));
                String coloredName = MessageUtils.getColor(armorStandName);
                armorStand.setCustomName(coloredName);

                playerMvpTags.put(uuid, armorStand);

                // Intentar ocultar el armor stand
                try {
                    hideArmorStandFromOwner(player, armorStand);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Crear el runnable para actualizar posición
                createUpdateTask(uuid, armorStand);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } else {
            removeTag(uuid);
        }
    }

    private static void createUpdateTask(UUID uuid, ArmorStand armorStand) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline() || p.isDead() || getStreak(uuid) < 40) {
                    removeTag(uuid);
                    cancel();
                    return;
                }
                
                MainConfig currentConfig = MysthicKnockBack.getInstance().getMainConfig();
                if (currentConfig == null) {
                    return;
                }
                
                armorStand.teleport(p.getLocation().add(
                    currentConfig.getStreakArmorStandX(),
                    currentConfig.getStreakArmorStandY(),
                    currentConfig.getStreakArmorStandZ()
                ));
                
                int currentStreak = getStreak(uuid);
                String currentMvpTag = getMvpTag(currentStreak);
                
                if (currentMvpTag != null) {
                    String updatedName = currentConfig.getStreakArmorStandName()
                        .replace("%tag%", currentMvpTag)
                        .replace("%kills%", String.valueOf(currentStreak));
                    String coloredName = MessageUtils.getColor(updatedName);
                    
                    if (!armorStand.getCustomName().equals(coloredName)) {
                        armorStand.setCustomName(coloredName);
                    }
                }
                
                if (p != null) {
                    p.setLevel(getStreak(uuid));
                    p.setExp(1.0f);
                }
            }
        }.runTaskTimer(MysthicKnockBack.getInstance(), 0L, 1L);
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