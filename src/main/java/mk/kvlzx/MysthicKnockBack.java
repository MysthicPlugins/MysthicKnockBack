package mk.kvlzx;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import mk.kvlzx.arena.ArenaManager;
import mk.kvlzx.commands.ArenaCommand;
import mk.kvlzx.commands.ArenaTabCompleter;
import mk.kvlzx.commands.MainCommand;
import mk.kvlzx.commands.MainTabCompleter;
import mk.kvlzx.commands.StatsCommand;
import mk.kvlzx.commands.StatsTabCompleter;
import mk.kvlzx.config.BlocksShopConfig;
import mk.kvlzx.config.HotbarMenuConfig;
import mk.kvlzx.config.MainConfig;
import mk.kvlzx.config.MainMenuConfig;
import mk.kvlzx.config.MessagesConfig;
import mk.kvlzx.config.ReportMenuConfig;
import mk.kvlzx.config.ShopMenuConfig;
import mk.kvlzx.config.StatsMenuConfig;
import mk.kvlzx.config.TabConfig;
import mk.kvlzx.config.TopsMenuConfig;
import mk.kvlzx.commands.MusicCommand;
import mk.kvlzx.commands.ReportCommand;
import mk.kvlzx.cosmetics.CosmeticManager;
import mk.kvlzx.data.InventoryData;
import mk.kvlzx.hotbar.PlayerHotbar;
import mk.kvlzx.listeners.ArrowEffectListener;
import mk.kvlzx.listeners.BlackHoleListener;
import mk.kvlzx.listeners.ChatListener;
import mk.kvlzx.listeners.CombatListener;
import mk.kvlzx.listeners.EndermiteListener;
import mk.kvlzx.listeners.ExplosiveArrowListener;
import mk.kvlzx.listeners.ItemListener;
import mk.kvlzx.listeners.JoinMessageListener;
import mk.kvlzx.listeners.PlayerListener;
import mk.kvlzx.listeners.MenuListener;
import mk.kvlzx.managers.CombatManager;
import mk.kvlzx.managers.CooldownManager;
import mk.kvlzx.managers.ItemVerificationManager;
import mk.kvlzx.managers.MainScoreboardManager;
import mk.kvlzx.managers.TabManager;
import mk.kvlzx.managers.WeaponManager;
import mk.kvlzx.managers.MenuManager;
import mk.kvlzx.managers.MusicManager;
import mk.kvlzx.managers.ReportManager;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.utils.PlaceholdersUtils;

public class MysthicKnockBack extends JavaPlugin {
    
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
    private WeaponManager weaponManager;
    private EndermiteListener endermiteListener;
    private MessagesConfig messagesConfig;
    private MainConfig mainConfig;
    private TabConfig tabConfig;
    private MainMenuConfig mainMenuConfig;
    private TopsMenuConfig topsMenuConfig;
    private StatsMenuConfig statsMenuConfig;
    private HotbarMenuConfig hotbarMenuConfig;
    private ReportMenuConfig reportMenuConfig;
    private ShopMenuConfig shopMenuConfig;
    private BlocksShopConfig blocksShopConfig;
    
    private BukkitTask autoSaveTask;
    private BukkitTask weatherTimeTask; // Nueva tarea para controlar clima y tiempo

    @Override
    public void onEnable() {
        instance = this;

        // Cargar configuración primero
        mainConfig = new MainConfig(this);
        messagesConfig = new MessagesConfig(this);
        tabConfig = new TabConfig(this);
        mainMenuConfig = new MainMenuConfig(this);
        topsMenuConfig = new TopsMenuConfig(this);
        statsMenuConfig = new StatsMenuConfig(this);
        hotbarMenuConfig = new HotbarMenuConfig(this);
        reportMenuConfig = new ReportMenuConfig(this);
        shopMenuConfig = new ShopMenuConfig(this);
        blocksShopConfig = new BlocksShopConfig(this);

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

        if (Bukkit.getPluginManager().getPlugin("MysthicFriends") != null) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&d★&8] &dMysthicFriends detected! Both plugins are part of the &bMysthic&d Series.");
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&d★&8] &7Thanks for using both plugins together!");
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&d★&8] &7PlaceholderAPI detected! Placeholder support is enabled.");
            new PlaceholdersUtils().register();
        }

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
        startAutoSave(); // Iniciar auto-guardado con tiempo configurado
        startWeatherTimeControl(); // Nueva función para controlar clima y tiempo
        itemVerificationManager.startVerification();

        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&a✔&8] &aPlugin started successfully");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Auto-save enabled every " + mainConfig.getAutoSaveInterval() + " minutes");
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&bℹ&8] &7Weather and time control enabled (Always day via gamerule, weather checks every 30s)");
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
            saveAllData(); // Usar el método centralizado

            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b2&8] &7Saving arenas...");
            arenaManager.shutdown();
            arenaManager.saveArenas();

            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b3&8] &7Cleaning blocks and items...");
            ItemListener.cleanup();
            cleanupAllDroppedItems();

            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&b4&8] &7Cleaning up endermites...");
            if (endermiteListener != null) {
                endermiteListener.cleanupAllEndermites();
            }
        } catch (Exception e) {
            MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&c!&8] &cError saving data: " + e.getMessage());
            e.printStackTrace();
        }

        // Cancelar todas las tareas
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        if (weatherTimeTask != null) {
            weatherTimeTask.cancel();
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
        weaponManager = new WeaponManager(this);
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

    // Nueva función para controlar el clima y el tiempo
    private void startWeatherTimeControl() {
        // Configurar gamerules iniciales
        setupWorldGamerules();
        
        // Solo verificar clima cada 30 segundos (600 ticks) y tiempo cada 5 minutos por seguridad
        weatherTimeTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (World world : Bukkit.getWorlds()) {
                // Verificar clima (cada ejecución - 30 segundos)
                if (world.hasStorm() || world.isThundering()) {
                    world.setStorm(false);
                    world.setThundering(false);
                    world.setWeatherDuration(0);
                    world.setThunderDuration(0);
                }
                
                // Establecer un tiempo despejado por mucho tiempo
                if (world.getWeatherDuration() < 12000) {
                    world.setWeatherDuration(999999);
                }
                
                // Verificar tiempo solo ocasionalmente por seguridad (por si el gamerule falla)
                // Esto se ejecuta cada 10 veces = cada 5 minutos
                if (System.currentTimeMillis() % 10 == 0) {
                    if (world.getTime() > 12000 || world.getTime() < 0) {
                        world.setTime(6000);
                    }
                }
            }
        }, 600L, 600L); // Cada 30 segundos (600 ticks)
    }
    
    // Método para configurar los gamerules de los mundos
    private void setupWorldGamerules() {
        for (World world : Bukkit.getWorlds()) {
            // Desactivar el ciclo día/noche
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("AnnounceAdvancements", "false");
            
            // Establecer tiempo de día
            world.setTime(6000); // Mediodía
            
            // Configurar clima inicial
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(999999);
            world.setThunderDuration(0);
        }
    }

    // Método de auto-guardado usando el tiempo configurado
    private void startAutoSave() {
        long autoSaveMinutes = mainConfig.getAutoSaveInterval();
        long autoSaveTicks = autoSaveMinutes * 60 * 20; // Convertir minutos a ticks (1 minuto = 1200 ticks)
        
        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&e⚠&8] &7Auto-saving data...");
                saveAllData();
                arenaManager.saveArenas();
                MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&a✔&8] &7Auto-save completed successfully");
            } catch (Exception e) {
                MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&c!&8] &cAuto-save failed: " + e.getMessage());
                e.printStackTrace();
            }
        }, autoSaveTicks, autoSaveTicks); // Usar el tiempo configurado
    }

    // Método para reiniciar el auto-save con nueva configuración
    public void restartAutoSave() {
        // Cancelar el task actual si existe
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        
        // Iniciar nuevo auto-save con la configuración actualizada
        startAutoSave();
        
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&a✔&8] &7Auto-save restarted with new interval: " + mainConfig.getAutoSaveInterval() + " minutes");
    }

    // Método para recargar configuraciones dinámicamente
    public void reloadConfigs() {
        // Recargar configuraciones
        mainConfig.reload();
        messagesConfig.reload();
        tabManager.reload();
        scoreboardManager.reload();
        mainMenuConfig.reload();
        topsMenuConfig.reload();
        statsMenuConfig.reload();
        hotbarMenuConfig.reload();
        reportMenuConfig.reload();
        shopMenuConfig.reload();
        blocksShopConfig.reload();
        
        // Reiniciar auto-save con nueva configuración
        restartAutoSave();
        
        MessageUtils.sendMsg(Bukkit.getConsoleSender(), "&8[&a✔&8] &7Configurations reloaded successfully");
    }

    // Método centralizado para guardar toda la data
    private void saveAllData() {
        PlayerStats.saveAllStats();
        cosmeticManager.saveAll();
        combatManager.cleanup();
        itemVerificationManager.stopVerification();
        weaponManager.saveAllWeapons();
    }

    private void cleanupAllDroppedItems() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    Item item = (Item) entity;

                    // PROTECCIÓN MÚLTIPLE para items de PowerUp
                    boolean isPowerUpItem = false;
                    boolean isBlackHoleItem = false;

                    // Verificar por pickup delay infinito Y nombre personalizado
                    if (item.getPickupDelay() == Integer.MAX_VALUE) {
                        if (item.getCustomName() != null && item.getCustomName().contains("POWERUP_ITEM")) {
                            isPowerUpItem = true;
                        }
                        // BlackHole: nombre personalizado contiene BLACKHOLE_ITEM
                        if (item.getCustomName() != null && item.getCustomName().contains("BLACKHOLE_ITEM")) {
                            isBlackHoleItem = true;
                        }
                    }

                    // Verificar por metadata
                    if (item.hasMetadata("POWERUP_PROTECTED") || item.hasMetadata("POWERUP_ID")) {
                        isPowerUpItem = true;
                    }
                    // BlackHole: metadata especial
                    if (item.hasMetadata("BLACKHOLE_PROTECTED") || item.hasMetadata("BLACKHOLE_ID")) {
                        isBlackHoleItem = true;
                    }

                    // Verificar si está montado en un ArmorStand (indicativo de PowerUp o BlackHole)
                    if (item.getVehicle() instanceof ArmorStand) {
                        ArmorStand vehicle = (ArmorStand) item.getVehicle();
                        if (!vehicle.isVisible() && vehicle.isSmall()) {
                            // Si tiene nombre de BlackHole, marcarlo como tal
                            if (item.getCustomName() != null && item.getCustomName().contains("BLACKHOLE_ITEM")) {
                                isBlackHoleItem = true;
                            } else {
                                isPowerUpItem = true;
                            }
                        }
                    }

                    // Si es un item de PowerUp o BlackHole, NO eliminarlo
                    if (isPowerUpItem || isBlackHoleItem) {
                        continue;
                    }

                    // Eliminar solo items normales que no sean de PowerUp ni BlackHole
                    // Criterios para eliminar:
                    // 1. Items con pickup delay normal (pueden ser recogidos)
                    // 2. Items sin nombre personalizado de PowerUp/BlackHole
                    // 3. Items sin metadata de protección
                    if (item.getPickupDelay() != Integer.MAX_VALUE ||
                        item.getCustomName() == null ||
                        (!item.getCustomName().contains("POWERUP_ITEM") && !item.getCustomName().contains("BLACKHOLE_ITEM"))) {

                        // Verificación adicional: No eliminar si tiene metadata de protección
                        if (!item.hasMetadata("POWERUP_PROTECTED") && !item.hasMetadata("POWERUP_ID")
                                && !item.hasMetadata("BLACKHOLE_PROTECTED") && !item.hasMetadata("BLACKHOLE_ID")) {
                            entity.remove();
                        }
                    }

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
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        combatListener = new CombatListener(this);
        getServer().getPluginManager().registerEvents(combatListener, this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new ArrowEffectListener(this), this);
        endermiteListener = new EndermiteListener(this);
        getServer().getPluginManager().registerEvents(endermiteListener, this);
        getServer().getPluginManager().registerEvents(new JoinMessageListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosiveArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new BlackHoleListener(this), this);
    }

    // Método estático para obtener el prefix dinámicamente
    public static String getPrefix() {
        return getInstance().getMainConfig().getPrefix();
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

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }

    public EndermiteListener getEndermiteListener() {
        return endermiteListener;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public TabConfig getTabConfig() {
        return tabConfig;
    }

    public MainMenuConfig getMainMenuConfig () {
        return mainMenuConfig;
    }

    public TopsMenuConfig getTopsMenuConfig() {
        return topsMenuConfig;
    }

    public StatsMenuConfig getStatsMenuConfig() {
        return statsMenuConfig;
    }

    public HotbarMenuConfig getHotbarMenuConfig() {
        return hotbarMenuConfig;
    }

    public ReportMenuConfig getReportMenuConfig() {
        return reportMenuConfig;
    }

    public ShopMenuConfig getShopMenuConfig() {
        return shopMenuConfig;
    }

    public BlocksShopConfig getBlocksShopConfig() {
        return blocksShopConfig;
    }

}