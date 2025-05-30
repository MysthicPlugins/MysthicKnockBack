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
import mk.kvlzx.cosmetics.CosmeticManager;
import mk.kvlzx.data.InventoryData;
import mk.kvlzx.hotbar.PlayerHotbar;
import mk.kvlzx.listeners.ChatListener;
import mk.kvlzx.listeners.CombatListener;
import mk.kvlzx.listeners.ItemListener;
import mk.kvlzx.listeners.PlayerListener;
import mk.kvlzx.listeners.MenuListener;
import mk.kvlzx.managers.CooldownManager;
import mk.kvlzx.managers.MainScoreboardManager;
import mk.kvlzx.managers.TabManager;
import mk.kvlzx.managers.MenuManager;
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
        
        // Inicializar InventoryData antes de cargar estadísticas
        InventoryData inventoryData = new InventoryData(this);
        PlayerHotbar.init(inventoryData);
        
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
            cosmeticManager.saveAll();
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
        menuManager = new MenuManager(this);
        reportManager = new ReportManager(this);
        cooldownManager = new CooldownManager(this);
        cosmeticManager = new CosmeticManager(this);
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
}