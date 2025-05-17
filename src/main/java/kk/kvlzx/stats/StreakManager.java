package kk.kvlzx.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.utils.MessageUtils;

public class StreakManager {

    private final Map<UUID, Streak> playerStreaks = new HashMap<>(); private final Map<UUID, Team> playerTeams = new HashMap<>(); 
    private final Scoreboard scoreboard;
    private final KvKnockback plugin;

    public StreakManager() {
        this.plugin = null; // or pass plugin as parameter if needed
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        // Limpiar equipo de MVP
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith("MVP")) {
                team.unregister();
            }
        }
    }

    public void addKill(Player player){
        UUID uuid = player.getUniqueId();
        Streak streak = playerStreaks.compute(uuid, (k, v) -> v == null ? new Streak() : v);
        streak.addKill();

        // Verificar si es una racha multiplo de 5
        if (streak.isStreak()){
            // Anunciar si el random tiene racha
            Bukkit.broadcastMessage(MessageUtils.getColor("&e" + player.getName() + " &f Ha alcanzado una racha de &a" + streak.getKills() + " &a kills!"));
            // Reproducir sonido al alcanzar racha
            player.getWorld().playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);
        }

        // Gestionar la etiqueta "MVP"
        updateMvpTag(player, streak);
    }

    public void resetStreak(Player player) {
        UUID uuid = player.getUniqueId();
        Streak streak = playerStreaks.get(uuid);
        if (streak != null) {
            streak.reset();
            // Reproducir sonido al perder racha
            player.getWorld().playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 1.0f, 1.0f);
        }
        removeMVPTag(player);
    }

    private void updateMvpTag(Player player, Streak streak) {
        UUID uuid = player.getUniqueId();
        String mvpTag = streak.getMvpTag();

        // Si hay un MvpTag, actuzalizarlo
        if (mvpTag != null){
            Team team = playerTeams.get(uuid);
            if (team == null) {

                // Crear un nuevo equipo para el jugador
                team = scoreboard.registerNewTeam("MVP" + uuid.toString().substring(0, 8));
                team.addEntry(player.getName());
                playerTeams.put(uuid, team);
            }

            team.setPrefix(mvpTag + " ");
        } else {
            // Si ya no califica para Mvp, eliminar tag
            removeMVPTag(player);
        }
    }

    private void removeMVPTag(Player player) {
        UUID uuid = player.getUniqueId();
        Team team = playerTeams.remove(uuid);
        if (team != null) {
            team.removeEntry(player.getName());
            team.unregister();
        }
    }

    public void onDisable() {
        // Limpiar todos los equipos al deshabilitar el plugin
        for (Team team : playerTeams.values()){
            team.unregister();
        }
        
        playerTeams.clear();
        playerStreaks.clear();
            
    }
}
