package mk.kvlzx.managers;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.utils.MessageUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ArenaVoteManager {
    private final MysthicKnockBack plugin;
    
    // Estado de votación
    private boolean voteActive = false;
    private String votedArena = null;
    private int voteTimeLeft = 0;
    private BukkitTask voteTask;
    
    // Votos
    private final Set<UUID> yesVotes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> noVotes = ConcurrentHashMap.newKeySet();
    
    // Configuración
    private final int VOTE_DURATION = 30; // 30 segundos
    
    public ArenaVoteManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Inicia una votación para cambiar a una arena específica
     */
    public boolean startVote(Player initiator, String arenaName) {
        if (voteActive) {
            initiator.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cAlready voting for an arena change!"));
            return false;
        }
        
        // Verificar que la arena existe
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            initiator.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cArena not found!"));
            return false;
        }
        
        // Verificar que no es la arena actual
        if (arenaName.equals(plugin.getArenaManager().getCurrentArena())) {
            initiator.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cWe're already in that arena!"));
            return false;
        }
        
        // Verificar que no hay cambio de arena en progreso
        if (plugin.getArenaChangeManager().isArenaChanging()) {
            initiator.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cArena is already changing!"));
            return false;
        }
        
        // Iniciar votación
        voteActive = true;
        votedArena = arenaName;
        voteTimeLeft = VOTE_DURATION;
        yesVotes.clear();
        noVotes.clear();
        
        // Enviar mensaje clickeable a todos los jugadores
        sendClickableVoteMessage();
        
        // Iniciar countdown
        startCountdown();
        
        return true;
    }
    
    // ... resto del código igual hasta changeArena() ...
    
    private void changeArena() {
        // USAR EL NUEVO ArenaChangeManager - SIN ANIMACIÓN para votos
        plugin.getArenaChangeManager().changeArenaImmediately(votedArena, false);
        
        // Mensaje de confirmación
        Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
            "&a&lArena changed successfully! &r&7Welcome to &b" + votedArena));
    }
    
    // ... resto del código igual (sendClickableVoteMessage, vote, startCountdown, finishVote, etc.) ...
    
    private void sendClickableVoteMessage() {
        String currentArena = plugin.getArenaManager().getCurrentArena();
        
        // Mensaje de anuncio
        String announcement = MysthicKnockBack.getPrefix() + 
            "&e&lArena Vote Started! &r&7Change from &b" + currentArena + 
            " &7to &b" + votedArena + "&7? (&e" + VOTE_DURATION + "s&7)";
        Bukkit.broadcastMessage(MessageUtils.getColor(announcement));
        
        // Crear componentes clickeables
        TextComponent yesButton = new TextComponent(MessageUtils.getColor("  &a[YES]  "));
        yesButton.setBold(true);
        yesButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenuvote yes"));
        yesButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(MessageUtils.getColor("&aClick to vote YES")).create()));
        
        TextComponent noButton = new TextComponent(MessageUtils.getColor("  &c[NO]  "));
        noButton.setBold(true);
        noButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenuvote no"));
        noButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder(MessageUtils.getColor("&cClick to vote NO")).create()));
        
        TextComponent separator = new TextComponent(MessageUtils.getColor(" &7| "));
        
        // Mensaje principal
        TextComponent mainMessage = new TextComponent(MessageUtils.getColor(" "));
        mainMessage.addExtra(yesButton);
        mainMessage.addExtra(separator);
        mainMessage.addExtra(noButton);
        
        // Enviar a todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(mainMessage);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
        }
    }
    
    public boolean vote(Player player, boolean isYes) {
        if (!voteActive) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cNo active vote!"));
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Remover voto anterior si existe
        yesVotes.remove(playerId);
        noVotes.remove(playerId);
        
        // Registrar nuevo voto
        if (isYes) {
            yesVotes.add(playerId);
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&aYou voted &lYES &r&afor &b" + votedArena));
        } else {
            noVotes.add(playerId);
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cYou voted &lNO &r&cfor &b" + votedArena));
        }
        
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
        
        // Mostrar votos actuales
        broadcastCurrentVotes();
        
        return true;
    }
    
    private void broadcastCurrentVotes() {
        String message = MysthicKnockBack.getPrefix() + 
            "&7Votes: &a" + yesVotes.size() + " YES &7- &c" + noVotes.size() + " NO";
        Bukkit.broadcastMessage(MessageUtils.getColor(message));
    }
    
    private void startCountdown() {
        voteTask = new BukkitRunnable() {
            @Override
            public void run() {
                voteTimeLeft--;
                
                // Alertas
                if (voteTimeLeft == 10 || voteTimeLeft == 5 || voteTimeLeft <= 3) {
                    String alert = MysthicKnockBack.getPrefix() + 
                        "&eVote ends in &b" + voteTimeLeft + " &eseconds!";
                    Bukkit.broadcastMessage(MessageUtils.getColor(alert));
                    
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 
                            voteTimeLeft <= 3 ? 2.0f : 1.0f);
                    }
                }
                
                if (voteTimeLeft <= 0) {
                    finishVote();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
    
    private void finishVote() {
        voteActive = false;
        
        if (voteTask != null) {
            voteTask.cancel();
            voteTask = null;
        }
        
        int totalYes = yesVotes.size();
        int totalNo = noVotes.size();
        int totalVotes = totalYes + totalNo;
        
        // Mostrar resultado
        String result = MysthicKnockBack.getPrefix() + 
            "&e&lVote Results: &a" + totalYes + " YES &7- &c" + totalNo + " NO";
        Bukkit.broadcastMessage(MessageUtils.getColor(result));
        
        if (totalVotes == 0) {
            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cNo votes received!"));
            return;
        }
        
        if (totalYes > totalNo) {
            // ¡Cambiar arena!
            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&a&lVote PASSED! &r&7Changing to &b" + votedArena));
            changeArena();
        } else {
            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&c&lVote FAILED! &r&7Staying in current arena."));
        }
        
        // Limpiar
        yesVotes.clear();
        noVotes.clear();
        votedArena = null;
    }
    
    // Getters
    public boolean isVoteActive() { return voteActive; }
    public String getVotedArena() { return votedArena; }
    public int getYesVotes() { return yesVotes.size(); }
    public int getNoVotes() { return noVotes.size(); }
    
    public void shutdown() {
        if (voteTask != null) {
            voteTask.cancel();
        }
        voteActive = false;
        yesVotes.clear();
        noVotes.clear();
    }
}
