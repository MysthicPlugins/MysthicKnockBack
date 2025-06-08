package mk.kvlzx;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import mk.kvlzx.arena.ArenaManager;
import mk.kvlzx.commands.ArenaCommand;
import mk.kvlzx.commands.ArenaTabCompleter;
import mk.kvlzx.commands.MainCommand;
import mk.kvlzx.commands.MainTabCompleter;
import mk.kvlzx.commands.StatsCommand;
import mk.kvlzx.commands.StatsTabCompleter;
import mk.kvlzx.commands.MusicCommand;
import mk.kvlzx.cosmetics.CosmeticManager;
import mk.kvlzx.data.InventoryData;
import mk.kvlzx.hotbar.PlayerHotbar;
import mk.kvlzx.listeners.ArrowEffectListener;
import mk.kvlzx.listeners.ChatListener;
import mk.kvlzx.listeners.CombatListener;
import mk.kvlzx.listeners.ItemListener;
import mk.kvlzx.listeners.PlayerListener;
import mk.kvlzx.listeners.MenuListener;
import mk.kvlzx.managers.CombatManager;
import mk.kvlzx.managers.CooldownManager;
import mk.kvlzx.managers.MainScoreboardManager;
import mk.kvlzx.managers.TabManager;
import mk.kvlzx.managers.MenuManager;
import mk.kvlzx.managers.MusicManager;
import mk.kvlzx.managers.ReportManager;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class MysthicKnockBack extends JavaPlugin {

    public static String prefix = "&b[&3KvKnockback&b] ";

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

    @Override
    public void onEnable() {
        instance = this;

        // Nuevo banner ASCII grande
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
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Estado: &aIniciando");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Versión: &f" + version);
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b1&8] &7Registrando managers...");
        registerManagers();

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b2&8] &7Inicializando datos...");
        PlayerStats.initializeStatsData(this);
        InventoryData inventoryData = new InventoryData(this);
        PlayerHotbar.init(inventoryData);

        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerStats.loadAllStats(); // Mover después de que el servidor esté completamente iniciado
            }
        }.runTaskLater(this, 20L); // Esperar 1 segundo para asegurar que todo esté listo

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b3&8] &7Cargando arenas...");
        arenaManager.loadArenas();

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b4&8] &7Registrando comandos y eventos...");
        registerCommands();
        registerEvents();
        startPlaytimeUpdater();

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&a✔&8] &aPlugin iniciado correctamente");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Desarrollado por: &bKvlzx &8& &bGabo");
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
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Estado: &cDesactivando");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Versión: &f" + version);
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");

        try {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b1&8] &7Guardando datos...");
            PlayerStats.saveAllStats();
            cosmeticManager.saveAll();
            combatManager.cleanup();

            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b2&8] &7Guardando arenas...");
            arenaManager.saveArenas();

            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b3&8] &7Limpiando bloques e items...");
            ItemListener.cleanup();
        } catch (Exception e) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&c!&8] &cError al guardar datos: " + e.getMessage());
            e.printStackTrace();
        }

        if (musicManager != null) {
            musicManager.onDisable();
        }

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&c✕&8] &cPlugin desactivado correctamente");
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
    }

    private void startPlaytimeUpdater() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
                stats.updatePlayTime();
            }
        }, 1200L, 1200L); // 1200 ticks = 1 minuto
    }

    public void registerCommands() {
        getCommand("mysthicknockback").setExecutor(new MainCommand(this));
        getCommand("mysthicknockback").setTabCompleter(new MainTabCompleter());
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("arena").setTabCompleter(new ArenaTabCompleter(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("stats").setTabCompleter(new StatsTabCompleter());
        getCommand("music").setExecutor(new MusicCommand(this));
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        combatListener = new CombatListener(this);
        getServer().getPluginManager().registerEvents(combatListener, this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new ArrowEffectListener(this), this);
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
}