package kk.kvlzx;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import kk.kvlzx.arena.ArenaManager;
import kk.kvlzx.commands.ArenaCommand;
import kk.kvlzx.commands.MainCommand;
import kk.kvlzx.commands.ReportCommand;
import kk.kvlzx.listeners.CombatListener;
import kk.kvlzx.listeners.ItemListener;
import kk.kvlzx.listeners.PlayerListener;
import net.md_5.bungee.api.ChatColor;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class KvKnockback extends JavaPlugin {

    public static String prefix = "&b[&3KvKnockback&b]";

    public String version = getDescription().getVersion();
    
    private static KvKnockback instance;
    private ArenaManager arenaManager;
    private WorldEditPlugin worldEdit;

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

        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&eIntKnock ha sido activado. &fVersión: " + version));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&eGracias por usar mi plugin :3"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&e≽^•⩊•^≼"));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&eIntKnock ha sido Desactivado. &fVersión: " + version));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&eGracias por usar mi plugin :3"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&e≽^•⩊•^≼"));
    }
    
    public void registerManagers() {
        arenaManager = new ArenaManager(this);
    }

    public void registerCommands() {
        getCommand("kvknockback").setExecutor(new MainCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
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
}