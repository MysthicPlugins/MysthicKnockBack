package kk.kvlzx.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.data.StatsData;
import kk.kvlzx.managers.RankManager;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.utils.TitleUtils;

public class PlayerStats {
    private static Map<UUID, PlayerStats> stats = new HashMap<>();
    private final UUID uuid;
    private int kills;
    private int deaths;
    private int elo;
    private int currentStreak;
    private int maxStreak;
    private long playTime; // Tiempo en milisegundos
    private long lastJoin;
    private long lastDeathTime = 0;
    private static final long DEATH_COOLDOWN = 500; // 500ms cooldown
    private static StatsData statsData;
    private static final Map<UUID, ArmorStand> playerMvpTags = new HashMap<>(); // Mapa para rastrear los ArmorStands

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
        this.kills = 0;
        this.deaths = 0;
        this.elo = 500; // ELO inicial modificado a 500
        this.playTime = 0;
        this.lastJoin = System.currentTimeMillis();
    }

    public static PlayerStats getStats(UUID uuid) {
        return stats.computeIfAbsent(uuid, k -> new PlayerStats(k));
    }

    public static Set<UUID> getAllStats() {
        return stats.keySet();
    }

    public static void initializeStatsData(KvKnockback plugin) {
        statsData = new StatsData(plugin);
    }

    public static void loadAllStats() {
        if (statsData == null) return;
        
        // Cargar datos de todos los jugadores almacenados
        ConfigurationSection statsSection = statsData.getConfig().getConfigurationSection("stats");
        if (statsSection != null) {
            for (String uuidStr : statsSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    PlayerStats playerStats = new PlayerStats(uuid);
                    statsData.loadStats(uuid, playerStats);
                    stats.put(uuid, playerStats);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveAllStats() {
        if (statsData == null) return;
        
        // Solo guardar stats de jugadores que han estado online
        stats.forEach((uuid, playerStats) -> {
            try {
                statsData.saveStats(uuid, playerStats);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Limpiar todos los ArmorStands al guardar/desactivar
        for (ArmorStand armorStand : playerMvpTags.values()) {
            if (armorStand != null && !armorStand.isDead()) {
                armorStand.remove();
            }
        }
        playerMvpTags.clear();
    }

    public void saveStats() {
        if (statsData != null) {
            statsData.saveStats(uuid, this);
        }
    }

    public String getMvpTag() {
        if (currentStreak < 5) return null; // No mostrar tag si tiene menos de 5 kills
        if (currentStreak >= 500) return "&5MVP+";
        else if (currentStreak >= 300) return "&cMVP";
        else if (currentStreak >= 250) return "&6MVP";
        else if (currentStreak >= 200) return "&eMVP";
        else if (currentStreak >= 150) return "&bMVP";
        else if (currentStreak >= 100) return "&aMVP";
        else if (currentStreak >= 80) return "&9MVP";
        else if (currentStreak >= 60) return "&dMVP";
        else if (currentStreak >= 40) return "&7MVP";
        return "&8MVP"; // Tag básico para rachas entre 5 y 39
    }

    public void addKill() {
        this.kills++;
        this.currentStreak++;
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
        }
        
        int eloGained = (int)(Math.random() * 10) + 6;
        this.elo += eloGained;

        // Notificar racha si es múltiplo de 5
        if (currentStreak > 0 && currentStreak % 5 == 0) {
            String mvpTag = getMvpTag();
            Player player = Bukkit.getPlayer(uuid);
            String playerName = player != null ? player.getName() : "Desconocido";
            
            Bukkit.broadcastMessage(MessageUtils.getColor("&e" + playerName + 
                " &fha alcanzado una racha de &a" + currentStreak + " &akills!"));
            
            // Reproducir sonido para todos los jugadores
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
            }
            
            // Mostrar título con la racha a todos los jugadores
            if (mvpTag != null) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    TitleUtils.sendTitle(
                        online,
                        MessageUtils.getColor(mvpTag + "&f " + playerName),
                        "&7Racha de &f" + currentStreak + " &7kills!",
                        10, 40, 10
                    );
                }
            }
        }

        // Actualizar el tag de MVP y el ArmorStand
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            updateMvpTag(player);
        }

        // Actualizar solo el rango
        if (player != null && player.isOnline()) {
            RankManager.updatePlayerRank(player, this.elo);
        }
    }

    private void updateMvpTag(Player player) {
        if (player == null || !player.isOnline()) return;
        
        UUID uuid = player.getUniqueId();
        String mvpTag = getMvpTag();

        // Gestionar el ArmorStand
        int kills = currentStreak;
        if (kills >= 5) { // Umbral mínimo para mostrar MVP
            // Remover el ArmorStand anterior si existe
            if (playerMvpTags.containsKey(uuid)) {
                ArmorStand oldTag = playerMvpTags.get(uuid);
                if (oldTag != null && !oldTag.isDead()) {
                    oldTag.remove();
                }
                playerMvpTags.remove(uuid);
            }

            // Crear el ArmorStand
            Location loc = player.getLocation().add(0, 2.2, 0); // Ajustar altura sobre el jugador
            ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            armorStand.setVisible(false); // Hacer el ArmorStand invisible
            armorStand.setGravity(false); // Desactivar gravedad
            armorStand.setCustomNameVisible(true); // Mostrar el nombre personalizado
            armorStand.setSmall(true); // Hacer el ArmorStand más pequeño para mejor visibilidad
            armorStand.setMarker(true); // Hacerlo un marcador
            armorStand.setCustomName(MessageUtils.getColor(mvpTag + " &7- Kills: " + kills + "\n" + ChatColor.WHITE + player.getName()));

            // Guardar el ArmorStand en el mapa
            playerMvpTags.put(uuid, armorStand);

            // Hacer que el ArmorStand siga al jugador
            new BukkitRunnable() {
                @Override
                public void run() {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null || !p.isOnline() || p.isDead() || currentStreak < 5) {
                        armorStand.remove();
                        playerMvpTags.remove(uuid);
                        cancel();
                        return;
                    }
                    armorStand.teleport(p.getLocation().add(0, 2.2, 0));
                }
            }.runTaskTimer(KvKnockback.getInstance(), 0L, 1L);
        } else {
            // Remover el ArmorStand si no hay MVP
            if (playerMvpTags.containsKey(uuid)) {
                ArmorStand oldTag = playerMvpTags.get(uuid);
                if (oldTag != null && !oldTag.isDead()) {
                    oldTag.remove();
                }
                playerMvpTags.remove(uuid);
            }
        }
    }

    public void resetStreak() {
        if (currentStreak >= 5) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Bukkit.broadcastMessage(MessageUtils.getColor("&c☠ &f" + player.getName() + 
                    " &7perdió su racha de &c" + currentStreak + " &7kills! &c☠"));
                player.playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 1.0f, 1.0f);
            }
        }
        currentStreak = 0;

        // Remover el ArmorStand al resetear la racha
        if (playerMvpTags.containsKey(uuid)) {
            ArmorStand oldTag = playerMvpTags.get(uuid);
            if (oldTag != null && !oldTag.isDead()) {
                oldTag.remove();
            }
            playerMvpTags.remove(uuid);
        }
    }

    public boolean canDie() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastDeathTime >= DEATH_COOLDOWN;
    }
    
    public void addDeath() {
        long currentTime = System.currentTimeMillis();
        if (!canDie()) {
            return; // Evitar muertes duplicadas
        }
        lastDeathTime = currentTime;
        
        this.deaths++;
        int eloLost = (int)(Math.random() * 10) + 6; // Random entre 6-15
        this.elo = Math.max(0, this.elo - eloLost); // Evita que el ELO sea negativo
        
        // Actualizar rango si el jugador está online
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            RankManager.updatePlayerRank(player, this.elo);
        }
    }

    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getElo() { return elo; }
    public double getKDR() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public void setElo(int elo) {
        this.elo = Math.max(0, elo);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            RankManager.updatePlayerRank(player, this.elo);
        }
    }

    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
    }

    public void setDeaths(int deaths) {
        this.deaths = Math.max(0, deaths);
    }

    public void updatePlayTime() {
        long now = System.currentTimeMillis();
        long timePassed = now - lastJoin;
        playTime += timePassed;
        lastJoin = now;
    }

    public String getFormattedPlayTime() {
        long totalMinutes = playTime / (1000 * 60);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dh %02dm", hours, minutes);
    }

    public double getPlayTimeHours() {
        return playTime / (1000.0 * 60 * 60);
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void loadStats(StatsData data) {
        String path = "stats." + uuid.toString();
        ConfigurationSection section = data.getConfig().getConfigurationSection(path);
        if (section != null) {
            this.kills = section.getInt("kills", 0);
            this.deaths = section.getInt("deaths", 0);
            this.elo = section.getInt("elo", 500);
            this.maxStreak = section.getInt("maxStreak", 0);
            this.currentStreak = section.getInt("currentStreak", 0);
            this.playTime = section.getLong("playTime", 0);
            // Asegurarse de no perder datos antiguos
            if (this.elo < 500) this.elo = 500; // ELO mínimo
        }
    }
}
