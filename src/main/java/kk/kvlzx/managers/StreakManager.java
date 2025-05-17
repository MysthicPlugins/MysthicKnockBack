package kk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import kk.kvlzx.stats.Streak;
import kk.kvlzx.utils.MessageUtils;

public class StreakManager {

    private final Map<UUID, Streak> playerStreaks = new HashMap<>();
    private final Scoreboard scoreboard;

    public StreakManager() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        // Limpiar objetivos anteriores de below_name
        if (scoreboard.getObjective("mvpDisplay") != null) {
            scoreboard.getObjective("mvpDisplay").unregister();
        }
        // Crear nuevo objetivo para below_name
        Objective belowName = scoreboard.registerNewObjective("mvpDisplay", "dummy");
        belowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
        belowName.setDisplayName(MessageUtils.getColor("&6Kills"));
    }

    public void addKill(Player player) {
        UUID uuid = player.getUniqueId();
        Streak streak = playerStreaks.compute(uuid, (k, v) -> v == null ? new Streak() : v);
        streak.addKill();

        // Verificar si es una racha multiplo de 5
        if (streak.isStreak()){
            // Anunciar si el random tiene racha
            Bukkit.broadcastMessage(MessageUtils.getColor("&e" + player.getName() + " &fha alcanzado una racha de &a " + streak.getKills() + " &a kills!"));
            // Reproducir sonido al alcanzar racha
            player.getWorld().playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
        }

        // Gestionar la etiqueta "MVP"
        updateMvpTag(player, streak);
    }

    public Streak getStreak(Player player) {
        return playerStreaks.computeIfAbsent(player.getUniqueId(), k -> new Streak());
    }

    private void updateMvpTag(Player player, Streak streak) {
        String mvpTag = streak.getMvpTag();
        
        // Actualizar el nombre debajo del jugador
        if (mvpTag != null) {
            Objective belowName = scoreboard.getObjective("mvpDisplay");
            if (belowName != null) {
                Score score = belowName.getScore(player.getName());
                score.setScore(streak.getKills());
                String display = MessageUtils.getColor(mvpTag + " &7[" + streak.getKills() + "⚔]");
                player.setPlayerListName(display);
            }
        } else {
            player.setPlayerListName(player.getName());
        }
    }

    public void resetStreak(Player player) {
        UUID uuid = player.getUniqueId();
        Streak streak = playerStreaks.get(uuid);
        if (streak != null) {
            if (streak.getKills() >= 5) {
                Bukkit.broadcastMessage(MessageUtils.getColor("&c☠ &f" + player.getName() + 
                    " &7perdió su racha de &c" + streak.getKills() + " &7kills! &c☠"));
            }
            streak.reset();
            player.playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 1.0f, 1.0f);
        }
        
        Objective belowName = scoreboard.getObjective("mvpDisplay");
        if (belowName != null) {
            belowName.getScore(player.getName()).setScore(0);
        }
        player.setPlayerListName(player.getName());
    }

    public void onDisable() {
        if (scoreboard.getObjective("mvpDisplay") != null) {
            scoreboard.getObjective("mvpDisplay").unregister();
        }
        playerStreaks.clear();
    }
}
