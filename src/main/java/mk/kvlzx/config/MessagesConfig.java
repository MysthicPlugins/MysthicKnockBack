package mk.kvlzx.config;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import mk.kvlzx.utils.config.CustomConfig;
import mk.kvlzx.MysthicKnockBack;

public class MessagesConfig {
    private final CustomConfig configFile;


    // Main
    private String reloadConfig;
    private String noPermission;
    private List<String> help;

    // Arena
    private String arenaCreate;
    private String arenaAlreadyExist;
    private String arenaZoneUsage;
    private String arenaZoneInvalid;
    private String arenaZoneSuccess;
    private String arenaZoneError;
    private String arenaSetSpawnSuccess;
    private String arenaSetSpawnError;
    private String arenaDeleteSuccess;
    private String arenaDeleteError;
    private String arenaSetBorderUsage;
    private String arenaSetBorderErrorSize;
    private String arenaSetBorderSuccess;
    private String arenaSetBorderError;
    private String arenaSetBorderNumberError;
    private List<String> arenaHelpMessage;

    // Stats
    private String statsUsage;
    private String statsNotFound;
    private String statsReset;
    private String statsResetAll;
    private String statsSet;
    private String statsAdd;
    private String statsRemove;
    private String statsInvalidStat;
    private String statsInvalidValue;
    private String statsNoPermission;
    private String statsAmountError;
    private String statsAdminUsage;
    private String statsResetSuccess;
    private String statsResetAllSuccess;
    private String statsStatUpdated;
    private List<String> statsFormat;

    public MessagesConfig(MysthicKnockBack plugin) {
        configFile = new CustomConfig("messages.yml", "config/global", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        // Main
        reloadConfig = config.getString("messages.commands.main.reload");
        noPermission = config.getString("messages.commands.main.no-permission");
        help = config.getStringList("messages.commands.main.help");
        
        // Arena
        arenaCreate = config.getString("messages.commands.arena.create.create");
        arenaAlreadyExist = config.getString("messages.commands.arena.create.already-exist");
        arenaZoneUsage = config.getString("messages.commands.arena.zone.usage");
        arenaZoneInvalid = config.getString("messages.commands.arena.zone.invalid-zone");
        arenaZoneSuccess = config.getString("messages.commands.arena.zone.successfully");
        arenaZoneError = config.getString("messages.commands.arena.zone.error");
        arenaSetSpawnSuccess = config.getString("messages.commands.arena.setspawn.successfully");
        arenaSetSpawnError = config.getString("messages.commands.arena.setspawn.error");
        arenaDeleteSuccess = config.getString("messages.commands.arena.delete.successfully");
        arenaDeleteError = config.getString("messages.commands.arena.delete.error");
        arenaSetBorderUsage = config.getString("messages.commands.arena.setborder.usage");
        arenaSetBorderErrorSize = config.getString("messages.commands.arena.setborder.error-size");
        arenaSetBorderSuccess = config.getString("messages.commands.arena.setborder.successfully");
        arenaSetBorderError = config.getString("messages.commands.arena.setborder.error");
        arenaSetBorderNumberError = config.getString("messages.commands.arena.setborder.number-error");
        arenaHelpMessage = config.getStringList("messages.commands.arena.help-message");
        
        // Stats
        statsUsage = config.getString("messages.commands.stats.usage");
        statsNotFound = config.getString("messages.commands.stats.not-found");
        statsReset = config.getString("messages.commands.stats.reset");
        statsResetAll = config.getString("messages.commands.stats.resetall");
        statsSet = config.getString("messages.commands.stats.set");
        statsAdd = config.getString("messages.commands.stats.add");
        statsRemove = config.getString("messages.commands.stats.remove");
        statsInvalidStat = config.getString("messages.commands.stats.invalid-stat");
        statsInvalidValue = config.getString("messages.commands.stats.invalid-value");
        statsNoPermission = config.getString("messages.commands.stats.no-permission");
        statsAmountError = config.getString("messages.commands.stats.amount-error");
        statsAdminUsage = config.getString("messages.commands.stats.admin-usage");
        statsResetSuccess = config.getString("messages.commands.stats.reset-success");
        statsResetAllSuccess = config.getString("messages.commands.stats.resetall-success");
        statsStatUpdated = config.getString("messages.commands.stats.stat-updated");
        statsFormat = config.getStringList("messages.commands.stats.format");
    }

    public void reload() {
        configFile.reloadConfig();
        loadConfig();
    }

    // Main Getters
    public String getReloadConfig() { return reloadConfig; }
    public String getNoPermission() { return noPermission; }
    public List<String> getHelp() { return help; }

    // Arena Getters
    public String getArenaCreate() { return arenaCreate; }
    public String getArenaAlreadyExist() { return arenaAlreadyExist; }
    public String getArenaZoneUsage() { return arenaZoneUsage; }
    public String getArenaZoneInvalid() { return arenaZoneInvalid; }
    public String getArenaZoneSuccess() { return arenaZoneSuccess; }
    public String getArenaZoneError() { return arenaZoneError; }
    public String getArenaSetSpawnSuccess() { return arenaSetSpawnSuccess; }
    public String getArenaSetSpawnError() { return arenaSetSpawnError; }
    public String getArenaDeleteSuccess() { return arenaDeleteSuccess; }
    public String getArenaDeleteError() { return arenaDeleteError; }
    public String getArenaSetBorderUsage() { return arenaSetBorderUsage; }
    public String getArenaSetBorderErrorSize() { return arenaSetBorderErrorSize; }
    public String getArenaSetBorderSuccess() { return arenaSetBorderSuccess; }
    public String getArenaSetBorderError() { return arenaSetBorderError; }
    public String getArenaSetBorderNumberError() { return arenaSetBorderNumberError; }
    public List<String> getArenaHelpMessage() { return arenaHelpMessage; }
    
    // Stats Getters
    public String getStatsUsage() { return statsUsage; }
    public String getStatsNotFound() { return statsNotFound; }
    public String getStatsReset() { return statsReset; }
    public String getStatsResetAll() { return statsResetAll; }
    public String getStatsSet() { return statsSet; }
    public String getStatsAdd() { return statsAdd; }
    public String getStatsRemove() { return statsRemove; }
    public String getStatsInvalidStat() { return statsInvalidStat; }
    public String getStatsInvalidValue() { return statsInvalidValue; }
    public String getStatsNoPermission() { return statsNoPermission; }
    public String getStatsAmountError() { return statsAmountError; }
    public String getStatsAdminUsage() { return statsAdminUsage; }
    public String getStatsResetSuccess() { return statsResetSuccess; }
    public String getStatsResetAllSuccess() { return statsResetAllSuccess; }
    public String getStatsStatUpdated() { return statsStatUpdated; }
    public List<String> getStatsFormat() { return statsFormat; }
}