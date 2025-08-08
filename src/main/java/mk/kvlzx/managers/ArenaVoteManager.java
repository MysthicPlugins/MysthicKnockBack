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
import mk.kvlzx.config.MainConfig;
import mk.kvlzx.utils.MessageUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ArenaVoteManager {
    private final MysthicKnockBack plugin;
    private final MainConfig mainConfig;

    // Vote status
    private boolean voteActive = false;
    private String votedArena = null;
    private int voteTimeLeft = 0;
    private BukkitTask voteTask;

    // Votes
    private final Set<UUID> yesVotes = ConcurrentHashMap.newKeySet();
    private final Set<UUID> noVotes = ConcurrentHashMap.newKeySet();

    public ArenaVoteManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getMainConfig();
    }

    /**
     * Starts a vote to switch to a specific arena
     */
    public boolean startVote(Player initiator, String arenaName) {
        if (voteActive) {
            initiator.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() +
                    mainConfig.getArenaVoteAlreadyActive()));
            return false;
        }

        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            initiator.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() +
                    mainConfig.getArenaVoteArenaNotFound()));
            return false;
        }

        if (arenaName.equals(plugin.getArenaManager().getCurrentArena())) {
            initiator.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() +
                    mainConfig.getArenaVoteAlreadyInArena()));
            return false;
        }

        if (plugin.getArenaChangeManager().isArenaChanging()) {
            initiator.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() +
                    mainConfig.getArenaVoteAlreadyInArenaChange()));
            return false;
        }

        int arenaTimeLeft = plugin.getScoreboardManager().getArenaTimeLeft();
        if (arenaTimeLeft <= mainConfig.getArenaVoteMinTime()) {
            int minutes = mainConfig.getArenaVoteMinTime() / 60;
            int seconds = mainConfig.getArenaVoteMinTime() % 60;
            String timeFormat = String.format("%02d:%02d", minutes, seconds);

            String message = mainConfig.getArenaVoteCannotVote()
                    .replace("%time%", timeFormat);

            initiator.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            return false;
        }

        voteActive = true;
        votedArena = arenaName;
        voteTimeLeft = mainConfig.getArenaVoteDuration();
        yesVotes.clear();
        noVotes.clear();

        sendClickableVoteMessage();
        startCountdown();

        return true;
    }

    private void changeArena() {
        plugin.getArenaChangeManager().changeArenaImmediately(votedArena, false);
        plugin.getScoreboardManager().resetArenaTimer();
    }

    private void sendClickableVoteMessage() {
        String currentArena = plugin.getArenaManager().getCurrentArena();

        String announcement = mainConfig.getArenaVoteClickeableAnnouncement()
                .replace("%current_arena%", currentArena)
                .replace("%voted_arena%", votedArena)
                .replace("%vote_duration%", String.valueOf(mainConfig.getArenaVoteDuration()));

        Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + announcement));

        TextComponent yesButton = new TextComponent(MessageUtils.getColor(mainConfig.getArenaVoteClickeableYes()));
        yesButton.setBold(true);
        yesButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenavote yes"));
        yesButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(MessageUtils.getColor(mainConfig.getArenaVoteHoverYes())).create()));

        TextComponent noButton = new TextComponent(MessageUtils.getColor(mainConfig.getArenaVoteClickeableNo()));
        noButton.setBold(true);
        noButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenavote no"));
        noButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(MessageUtils.getColor(mainConfig.getArenaVoteHoverNo())).create()));

        TextComponent separator = new TextComponent(MessageUtils.getColor(mainConfig.getArenaVoteSeparator()));

        TextComponent mainMessage = new TextComponent("");
        mainMessage.addExtra(yesButton);
        mainMessage.addExtra(separator);
        mainMessage.addExtra(noButton);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(mainMessage);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
        }
    }

    public boolean vote(Player player, boolean isYes) {
        if (!voteActive) {
            return false;
        }

        UUID playerId = player.getUniqueId();

        yesVotes.remove(playerId);
        noVotes.remove(playerId);

        if (isYes) {
            yesVotes.add(playerId);
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() +
                    mainConfig.getArenaVoteVoteYes().replace("%voted_arena%", votedArena)));
        } else {
            noVotes.add(playerId);
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() +
                    mainConfig.getArenaVoteVoteNo().replace("%voted_arena%", votedArena)));
        }

        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);

        broadcastCurrentVotes();

        return true;
    }

    private void broadcastCurrentVotes() {
        String message = mainConfig.getArenaVoteBroadcastCurrentVotes()
                .replace("%yes_votes%", String.valueOf(yesVotes.size()))
                .replace("%no_votes%", String.valueOf(noVotes.size()));
        Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
    }

    private void startCountdown() {
        voteTask = new BukkitRunnable() {
            @Override
            public void run() {
                voteTimeLeft--;

                if (voteTimeLeft == 10 || voteTimeLeft == 5 || voteTimeLeft <= 4) {
                    String alert = mainConfig.getArenaVoteVotesEnd().replace("%time_left%", String.valueOf(voteTimeLeft));
                    Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + alert));

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

        String result = mainConfig.getArenaVoteVoteResults()
                .replace("%yes_votes%", String.valueOf(totalYes))
                .replace("%no_votes%", String.valueOf(totalNo));
        Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + result));

        if (totalYes + totalNo == 0) {
            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + mainConfig.getArenaVoteNoVotesReceived()));
            return;
        }

        if (totalYes > totalNo) {
            String passedMessage = mainConfig.getArenaVoteVotePassed().replace("%voted_arena%", votedArena);
            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + passedMessage));
            changeArena();
        } else {
            Bukkit.broadcastMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + mainConfig.getArenaVoteVoteFailed()));
        }

        yesVotes.clear();
        noVotes.clear();
        votedArena = null;
    }

    // Getters
    public boolean isVoteActive() { return voteActive; }
    public String getVotedArena() { return votedArena; }
    public int getYesVotes() { return yesVotes.size(); }
    public int getNoVotes() { return noVotes.size(); }
    public int getMinArenaTimeForVote() { return mainConfig.getArenaVoteMinTime(); }

    public void shutdown() {
        if (voteTask != null) {
            voteTask.cancel();
        }
        voteActive = false;
        yesVotes.clear();
        noVotes.clear();
    }
}