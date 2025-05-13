package ik.kvlzx.org;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import ik.kvlzx.org.arenas.ArenaBuilder;
import ik.kvlzx.org.commands.MainComand;
import ik.kvlzx.org.commands.MainReport;
import ik.kvlzx.org.listeners.PlayerListener;
import net.md_5.bungee.api.ChatColor;

public class IntKnock extends JavaPlugin {

    public static String prefix = "&b[§3IntKnock&b]";

    public String version = getDescription().getVersion();
    
    private static IntKnock instance;

    private ArenaBuilder arenaBuilder;

    public static IntKnock getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        instance = this;

        arenaBuilder = new ArenaBuilder();

        registerEvents();
        registerCommands();

        Bukkit.getScheduler().runTask(this, () -> {
            World world = Bukkit.getWorlds().get(0);
            if (world == null) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cNo se pudo cargar un mundo válido."));
                return;
            }
            Location spawnPoint = new Location(world, 0, 64, 0);
            arenaBuilder.buildArenaOnStart(world, spawnPoint);
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&eArena construida en el mundo: " + world.getName()));
        });

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

    public void registerCommands() {
        this.getCommand("IntKnock").setExecutor(new MainComand(this));
        this.getCommand("Report").setExecutor(new MainReport(this));
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public ArenaBuilder getArenaBuilder() {
        return arenaBuilder;
    }
}