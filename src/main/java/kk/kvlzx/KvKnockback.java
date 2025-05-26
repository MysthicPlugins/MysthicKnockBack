package kk.kvlzx;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import kk.kvlzx.arena.ArenaManager;
import kk.kvlzx.commands.ArenaCommand;
import kk.kvlzx.commands.ArenaTabCompleter;
import kk.kvlzx.commands.MainCommand;
import kk.kvlzx.commands.MainTabCompleter;
import kk.kvlzx.commands.ReportCommand;
import kk.kvlzx.commands.ReportTabCompleter;
import kk.kvlzx.commands.StatsCommand;
import kk.kvlzx.commands.StatsTabCompleter;
import kk.kvlzx.listeners.ChatListener;
import kk.kvlzx.listeners.CombatListener;
import kk.kvlzx.listeners.ItemListener;
import kk.kvlzx.listeners.PlayerListener;
import kk.kvlzx.listeners.MenuListener;
import kk.kvlzx.managers.MainScoreboardManager;
import kk.kvlzx.managers.TabManager;
import kk.kvlzx.managers.MenuManager;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class KvKnockback extends JavaPlugin {

    public static String prefix = "&b[&3KvKnockback&b] ";

    public String version = getDescription().getVersion();
    
    private static KvKnockback instance;
    private ArenaManager arenaManager;
    private WorldEditPlugin worldEdit;
    private CombatListener combatListener;
    private MainScoreboardManager scoreboardManager;
    private TabManager tabManager;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;

        worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            getLogger().severe("WorldEdit no encontrado! Deshabilitando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerManagers();
        // Primero inicializar los datos
        PlayerStats.initializeStatsData(this);
        // Luego cargar las estadísticas
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerStats.loadAllStats(); // Mover después de que el servidor esté completamente iniciado
            }
        }.runTaskLater(this, 20L); // Esperar 1 segundo para asegurar que todo esté listo
        
        arenaManager.loadArenas();
        registerCommands();
        registerEvents();
        startPlaytimeUpdater();

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&r");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&b=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&bKvKnockback &fv" + version);
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&fEstado: &aActivado");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&fDesarrollado por: &bKvlzx & Gabo");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&fSoporte Discord: kvlzx, gaboh_");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&b≽^•⩊•^≼");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&b=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&r");
    }

    @Override
    public void onDisable() {
        try {
            // Limpiar bloques antes de guardar
            ItemListener.cleanup();
            
            arenaManager.saveArenas();
            PlayerStats.saveAllStats();
        } catch (Exception e) {
            getLogger().severe("Error al guardar datos: " + e.getMessage());
            e.printStackTrace();
        }

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&r");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&c=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&bKvKnockback &fv" + version);
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&fEstado: &cDesactivado");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&c≽^•⩊•^≼");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), prefix + "&c=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&r");
    }
    
    public void registerManagers() {
        arenaManager = new ArenaManager(this);
        scoreboardManager = new MainScoreboardManager(this);
        tabManager = new TabManager(this);
        menuManager = new MenuManager(this); // Añadir esta línea
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
        getCommand("kvknockback").setExecutor(new MainCommand(this));
        getCommand("kvknockback").setTabCompleter(new MainTabCompleter());
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("report").setTabCompleter(new ReportTabCompleter());
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("arena").setTabCompleter(new ArenaTabCompleter(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("stats").setTabCompleter(new StatsTabCompleter());
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        combatListener = new CombatListener(this);
        getServer().getPluginManager().registerEvents(combatListener, this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
    }

    public static KvKnockback getInstance() {
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
}