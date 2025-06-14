package mk.kvlzx;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.ChatColor;
import java.util.UUID;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import mk.kvlzx.arena.ArenaManager;
import mk.kvlzx.commands.ArenaCommand;
import mk.kvlzx.commands.ArenaTabCompleter;
import mk.kvlzx.commands.MainCommand;
import mk.kvlzx.commands.MainTabCompleter;
import mk.kvlzx.commands.MsgCommand;
import mk.kvlzx.commands.StatsCommand;
import mk.kvlzx.commands.StatsTabCompleter;
import mk.kvlzx.commands.MusicCommand;
import mk.kvlzx.commands.ReportCommand;
import mk.kvlzx.commands.FriendResponseCommand;
import mk.kvlzx.commands.FriendCommand;
import mk.kvlzx.cosmetics.CosmeticManager;
import mk.kvlzx.data.InventoryData;
import mk.kvlzx.hotbar.PlayerHotbar;
import mk.kvlzx.listeners.ArrowEffectListener;
import mk.kvlzx.listeners.ChatListener;
import mk.kvlzx.listeners.CombatListener;
import mk.kvlzx.listeners.EndermiteListener;
import mk.kvlzx.listeners.ItemListener;
import mk.kvlzx.listeners.PlayerListener;
import mk.kvlzx.listeners.MenuListener;
import mk.kvlzx.managers.CombatManager;
import mk.kvlzx.managers.CooldownManager;
import mk.kvlzx.managers.ItemVerificationManager;
import mk.kvlzx.managers.MainScoreboardManager;
import mk.kvlzx.managers.TabManager;
import mk.kvlzx.managers.MenuManager;
import mk.kvlzx.managers.MusicManager;
import mk.kvlzx.managers.ReportManager;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.listeners.FriendListener;

public class MysthicKnockBack extends JavaPlugin implements Listener {
    public static String prefix = "&b[&3KBFFA&b] ";
    public String version = getDescription().getVersion();
    private static MysthicKnockBack instance;
    private ArenaManager arenaManager;
    private WorldEditPlugin worldEdit;
    private CombatListener combatListener;
    private MainScoreboardManager scoreboardManager;
    private TabManager tabManager;
    private MenuManager menuManager;
    private ReportManager reportManager;
    private CooldownManager cooldownManager;
    private CosmeticManager cosmeticManager;
    private CombatManager combatManager;
    private MusicManager musicManager;
    private ItemVerificationManager itemVerificationManager;
    private FriendCommand friendCommand;
    private Scoreboard scoreboard;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&b██╗  ██╗██████╗ ███████╗███████╗ █████╗ ");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&b██║ ██╔╝██╔══██╗██╔════╝██╔════╝██╔══██╗");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&3█████╔╝ ██████╔╝█████╗  █████╗  ███████║");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&3██╔═██╗ ██╔══██╗██╔══╝  ██╔══╝  ██╔══██║");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&b██║  ██╗██████╔╝██║     ██║     ██║  ██║");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&b╚═╝  ╚═╝╚═════╝ ╚═╝     ╚═╝     ╚═╝  ╚═╝");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "               &b≽^•⩊•^≼");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Status: &aStarting");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Version: &f" + version);
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b1&8] &7Registering managers...");
        registerManagers();

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b2&8] &7Initializing data...");
        PlayerStats.initializeStatsData(this);
        InventoryData inventoryData = new InventoryData(this);
        PlayerHotbar.init(inventoryData);

        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerStats.loadAllStats();
            }
        }.runTaskLater(this, 20L);

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b3&8] &7Loading arenas...");
        arenaManager.loadArenas();

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b4&8] &7Registering commands and events...");
        registerCommands();
        registerEvents();
        startPlaytimeUpdater();
        startItemCleanup();
        itemVerificationManager.startVerification();

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&a✔&8] &aPlugin started successfully");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Developed by: &bKvlzx &8& &bGabo");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
    }

    @Override
    public void onDisable() {
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "                    &b&lKBFFA");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "                  &b≽^•⩊•^≼");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Status: &cShutting down");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Version: &f" + version);
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");

        try {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b1&8] &7Saving data...");
            PlayerStats.saveAllStats();
            cosmeticManager.saveAll();
            combatManager.cleanup();
            itemVerificationManager.stopVerification();
            friendCommand.saveFriends();

            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b2&8] &7Saving arenas...");
            arenaManager.saveArenas();

            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b3&8] &7Cleaning blocks and items...");
            ItemListener.cleanup();
            cleanupAllDroppedItems();
        } catch (Exception e) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&c!&8] &cError saving data: " + e.getMessage());
            e.printStackTrace();
        }

        if (musicManager != null) {
            musicManager.onDisable();
        }

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&c✕&8] &cPlugin disabled successfully");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
    }

    public void registerManagers() {
        arenaManager = new ArenaManager(this);
        scoreboardManager = new MainScoreboardManager(this);
        tabManager = new TabManager(this);
        menuManager = new MenuManager(this);
        reportManager = new ReportManager(this);
        cooldownManager = new CooldownManager(this);
        cosmeticManager = new CosmeticManager(this);
        combatManager = new CombatManager(this);
        musicManager = new MusicManager(this);
        itemVerificationManager = new ItemVerificationManager(this);
    }

    private void startPlaytimeUpdater() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
                stats.updatePlayTime();
            }
        }, 1200L, 1200L);
    }

    private void startItemCleanup() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            cleanupAllDroppedItems();
        }, 600L, 600L);
    }

    private void cleanupAllDroppedItems() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    entity.remove();
                } else if (entity instanceof Arrow) {
                    Arrow arrow = (Arrow) entity;
                    if (arrow.isOnGround() || arrow.getTicksLived() > 600) {
                        entity.remove();
                    }
                } else if (entity instanceof Projectile && !(entity instanceof EnderPearl)) {
                    if (entity.getTicksLived() > 600) {
                        entity.remove();
                    }
                }
            }
        }
    }

    public void registerCommands() {
        getCommand("mysthicknockback").setExecutor(new MainCommand(this));
        getCommand("mysthicknockback").setTabCompleter(new MainTabCompleter());
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("arena").setTabCompleter(new ArenaTabCompleter(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("stats").setTabCompleter(new StatsTabCompleter());
        getCommand("music").setExecutor(new MusicCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
        friendCommand = new FriendCommand(this);
        getCommand("friend").setExecutor(friendCommand);
        getCommand("friendaccept").setExecutor(new FriendResponseCommand(friendCommand));
        getCommand("friendreject").setExecutor(new FriendResponseCommand(friendCommand));
        getCommand("friendignore").setExecutor(new FriendResponseCommand(friendCommand));
        getCommand("msg").setExecutor(new MsgCommand(this, friendCommand));
        getCommand("r").setExecutor(new MsgCommand(this, friendCommand));
        getCommand("socialspy").setExecutor(new MsgCommand(this, friendCommand));
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        combatListener = new CombatListener(this);
        getServer().getPluginManager().registerEvents(combatListener, this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new ArrowEffectListener(this), this);
        getServer().getPluginManager().registerEvents(new EndermiteListener(this), this);
        getServer().getPluginManager().registerEvents(new FriendListener(friendCommand), this);
        getServer().getPluginManager().registerEvents(this, this);
        scoreboard = getServer().getScoreboardManager().getMainScoreboard();
        Team friendTeam = scoreboard.getTeam("friends");
        if (friendTeam == null) {
            friendTeam = scoreboard.registerNewTeam("friends");
            friendTeam.setPrefix(ChatColor.AQUA.toString());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Team friendTeam = scoreboard.getTeam("friends");
        for (UUID friendUUID : friendCommand.getFriends(player.getUniqueId())) {
            Player friend = getServer().getPlayer(friendUUID);
            if (friend != null && friend.isOnline()) {
                friendTeam.addEntry(friend.getName());
            }
        }
        friendTeam.addEntry(player.getName());
    }

    public static MysthicKnockBack getInstance() {
        return instance;
    }

    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public CombatListener getCombatListener() {
        return combatListener;
    }

    public MainScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public ReportManager getReportManager() {
        return reportManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public CosmeticManager getCosmeticManager() {
        return cosmeticManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public ItemVerificationManager getItemVerificationManager() {
        return itemVerificationManager;
    }
}