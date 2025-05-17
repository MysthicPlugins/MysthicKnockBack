package kk.kvlzx;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import kk.kvlzx.arena.ArenaManager;
import kk.kvlzx.commands.ArenaCommand;
import kk.kvlzx.commands.MainCommand;
import kk.kvlzx.commands.ReportCommand;
import kk.kvlzx.listeners.ChatListener;
import kk.kvlzx.listeners.CombatListener;
import kk.kvlzx.listeners.InventoryListener;
import kk.kvlzx.listeners.ItemListener;
import kk.kvlzx.listeners.PlayerListener;
import kk.kvlzx.listeners.SpawnItemListener;
import kk.kvlzx.managers.ScoreboardManager;
import kk.kvlzx.managers.StreakManager;
import kk.kvlzx.utils.MessageUtils;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class KvKnockback extends JavaPlugin {

    public static String prefix = "&b[&3KvKnockback&b] ";

    public String version = getDescription().getVersion();
    
    private static KvKnockback instance;
    private ArenaManager arenaManager;
    private WorldEditPlugin worldEdit;
    private CombatListener combatListener;
    private ScoreboardManager scoreboardManager;
    private StreakManager streakManager;

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
        registerCommands();
        registerEvents();

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
        // Eliminar la scoreboard
        streakManager.onDisable();

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
        scoreboardManager = new ScoreboardManager(this);
        streakManager = new StreakManager();
    }

    public void registerCommands() {
        getCommand("kvknockback").setExecutor(new MainCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("setelo").setExecutor(this); // Registro del comando /setelo
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        combatListener = new CombatListener(this);
        getServer().getPluginManager().registerEvents(combatListener, this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnItemListener(this), this);
        new ChatListener(this); // Registro del ChatListener
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

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public StreakManager getStreakManager() {
        return streakManager;
    }
}