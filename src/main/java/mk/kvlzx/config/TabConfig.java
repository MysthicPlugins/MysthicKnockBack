package mk.kvlzx.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class TabConfig {
    private final CustomConfig configFile;
    private final MysthicKnockBack plugin;

    private Boolean tabEnabled;
    private Boolean tabAnimationEnabled;
    private Integer tabAnimationInterval;
    private List<TabLine> tabHeaderLines; // Estructura para líneas del header con configuración individual
    private List<TabLine> tabFooterLines; // Nueva estructura para líneas del footer con configuración individual

    private Boolean scoreEnabled;
    private Integer scoreUpdateInterval;
    private String scoreNullArena;
    private String scoreNullNextArena;
    private String scoreTitle;
    private List<String> scoreLines;
    private Integer scoreArenaChange;
    private List<Integer> scoreArenaChangeAlert;
    private Integer scoreTitleBeforeChangeFadeIn;
    private Integer scoreTitleBeforeChangeStay;
    private Integer scoreTitleBeforeChangeFadeOut;
    private String scoreTitleBeforeChangeTitle;
    private String scoreTitleBeforeChangeSubtitle;
    private Integer scoreTitleAfterChangeFadeIn;
    private Integer scoreTitleAfterChangeStay;
    private Integer scoreTitleAfterChangeFadeOut;
    private String scoreTitleAfterChangeTitle;
    private String scoreTitleAfterChangeSubtitle;
    private String scoreMessageArenaChange;
    private List<String> scoreArenaChangeColors;
    private List<String> scoreArenaChangeFrames;

    public TabConfig(MysthicKnockBack plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("tab.yml", "config/global", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        tabEnabled = config.getBoolean("tab.display.enabled");
        tabAnimationEnabled = config.getBoolean("tab.display.header.animation");
        tabAnimationInterval = config.getInt("tab.display.header.animation-delay");
        
        // Cargar líneas del header con configuración individual
        loadHeaderLines(config);
        
        // Cargar líneas del footer con configuración individual
        loadFooterLines(config);

        scoreEnabled = config.getBoolean("scoreboard.enabled");
        scoreUpdateInterval = config.getInt("scoreboard.update-interval");
        scoreNullArena = config.getString("scoreboard.utils.null-arena");
        scoreNullNextArena = config.getString("scoreboard.utils.null-next-arena");
        scoreTitle = config.getString("scoreboard.title");
        scoreLines = config.getStringList("scoreboard.lines");
        scoreArenaChange = config.getInt("scoreboard.arena-change.time");
        scoreArenaChangeAlert = config.getIntegerList("scoreboard.arena-change.seconds-to-alert");
        scoreTitleBeforeChangeFadeIn = config.getInt("scoreboard.arena-change.title-before-change.fade-in");
        scoreTitleBeforeChangeStay = config.getInt("scoreboard.arena-change.title-before-change.stay");
        scoreTitleBeforeChangeFadeOut = config.getInt("scoreboard.arena-change.title-before-change.fade-out");
        scoreTitleBeforeChangeTitle = config.getString("scoreboard.arena-change.title-before-change.title");
        scoreTitleBeforeChangeSubtitle = config.getString("scoreboard.arena-change.title-before-change.subtitle");
        scoreTitleAfterChangeFadeIn = config.getInt("scoreboard.arena-change.title-after-change.fade-in");
        scoreTitleAfterChangeStay = config.getInt("scoreboard.arena-change.title-after-change.stay");
        scoreTitleAfterChangeFadeOut = config.getInt("scoreboard.arena-change.title-after-change.fade-out");
        scoreTitleAfterChangeTitle = config.getString("scoreboard.arena-change.title-after-change.title");
        scoreTitleAfterChangeSubtitle = config.getString("scoreboard.arena-change.title-after-change.subtitle");
        scoreMessageArenaChange = config.getString("scoreboard.message.arena-change");
        scoreArenaChangeColors = config.getStringList("scoreboard.title-animation.colors");
        scoreArenaChangeFrames = config.getStringList("scoreboard.title-animation.frames");
    }

    private void loadHeaderLines(FileConfiguration config) {
        tabHeaderLines = new ArrayList<>();
        
        ConfigurationSection linesSection = config.getConfigurationSection("tab.display.header.lines");
        if (linesSection != null) {
            // Obtener todas las keys y ordenarlas para mantener el orden
            List<String> sortedKeys = new ArrayList<>(linesSection.getKeys(false));
            sortedKeys.sort(String::compareToIgnoreCase);
            
            for (String key : sortedKeys) {
                ConfigurationSection lineSection = linesSection.getConfigurationSection(key);
                if (lineSection != null) {
                    boolean animated = lineSection.getBoolean("animated", false);
                    List<String> content;
                    
                    if (animated) {
                        content = lineSection.getStringList("frames");
                        if (content.isEmpty()) {
                            plugin.getLogger().warning("Animated header line '" + key + "' has no frames defined!");
                            continue;
                        }
                    } else {
                        String singleLine = lineSection.getString("content");
                        if (singleLine == null || singleLine.isEmpty()) {
                            plugin.getLogger().warning("Static header line '" + key + "' has no content defined!");
                            continue;
                        }
                        content = Arrays.asList(singleLine);
                    }
                    
                    tabHeaderLines.add(new TabLine(animated, content));
                }
            }
        } else {
            plugin.getLogger().warning("No header lines configuration found at tab.display.header.lines");
        }
    }

    private void loadFooterLines(FileConfiguration config) {
        tabFooterLines = new ArrayList<>();
        
        // Primero intentar cargar la nueva configuración con líneas individuales
        ConfigurationSection footerLinesSection = config.getConfigurationSection("tab.display.footer.lines");
        if (footerLinesSection != null) {
            // Nueva configuración con líneas individuales
            List<String> sortedKeys = new ArrayList<>(footerLinesSection.getKeys(false));
            sortedKeys.sort(String::compareToIgnoreCase);
            
            for (String key : sortedKeys) {
                ConfigurationSection lineSection = footerLinesSection.getConfigurationSection(key);
                if (lineSection != null) {
                    boolean animated = lineSection.getBoolean("animated", false);
                    List<String> content;
                    
                    if (animated) {
                        content = lineSection.getStringList("frames");
                        if (content.isEmpty()) {
                            plugin.getLogger().warning("Animated footer line '" + key + "' has no frames defined!");
                            continue;
                        }
                    } else {
                        String singleLine = lineSection.getString("content");
                        if (singleLine == null || singleLine.isEmpty()) {
                            plugin.getLogger().warning("Static footer line '" + key + "' has no content defined!");
                            continue;
                        }
                        content = Arrays.asList(singleLine);
                    }
                    
                    tabFooterLines.add(new TabLine(animated, content));
                }
            }
        } else {
            plugin.getLogger().warning("No footer lines configuration found. Please configure either 'tab.display.footer.lines'");
        }
    }

    public void reload() {
        configFile.reloadConfig();
        loadConfig();
    }

    // Getters existentes
    public Boolean isTabEnabled() { return tabEnabled; }
    public Boolean isTabAnimationEnabled() { return tabAnimationEnabled; }
    public Integer getTabAnimationInterval() { return tabAnimationInterval; }

    // Nuevos getters para la funcionalidad mejorada
    public List<TabLine> getTabHeaderLines() { return tabHeaderLines; }
    public List<TabLine> getTabFooterLines() { return tabFooterLines; }

    // Getters del scoreboard (sin cambios)
    public Boolean isScoreEnabled() { return scoreEnabled; }
    public Integer getScoreUpdateInterval() { return scoreUpdateInterval; }
    public String getScoreNullArena() { return scoreNullArena; }
    public String getScoreNullNextArena() { return scoreNullNextArena; }
    public String getScoreTitle() { return scoreTitle; }
    public List<String> getScoreLines() { return scoreLines; }
    public Integer getScoreArenaChange() { return scoreArenaChange; }
    public List<Integer> getScoreArenaChangeAlert() { return scoreArenaChangeAlert; }
    public Integer getScoreTitleBeforeChangeFadeIn() { return scoreTitleBeforeChangeFadeIn; }
    public Integer getScoreTitleBeforeChangeStay() { return scoreTitleBeforeChangeStay; }
    public Integer getScoreTitleBeforeChangeFadeOut() { return scoreTitleBeforeChangeFadeOut; }
    public String getScoreTitleBeforeChangeTitle() { return scoreTitleBeforeChangeTitle; }
    public String getScoreTitleBeforeChangeSubtitle() { return scoreTitleBeforeChangeSubtitle; }
    public Integer getScoreTitleAfterChangeFadeIn() { return scoreTitleAfterChangeFadeIn; }
    public Integer getScoreTitleAfterChangeStay() { return scoreTitleAfterChangeStay; }
    public Integer getScoreTitleAfterChangeFadeOut() { return scoreTitleAfterChangeFadeOut; }
    public String getScoreTitleAfterChangeTitle() { return scoreTitleAfterChangeTitle; }
    public String getScoreTitleAfterChangeSubtitle() { return scoreTitleAfterChangeSubtitle; }
    public String getScoreMessageArenaChange() { return scoreMessageArenaChange; }
    public List<String> getScoreArenaChangeColors() { return scoreArenaChangeColors; }
    public List<String> getScoreArenaChangeFrames() { return scoreArenaChangeFrames; }

    // Clase interna para representar una línea del tab
    public static class TabLine {
        private final boolean animated;
        private final List<String> content;

        public TabLine(boolean animated, List<String> content) {
            this.animated = animated;
            this.content = content != null ? content : new ArrayList<>();
        }

        public boolean isAnimated() {
            return animated;
        }

        public List<String> getContent() {
            return content;
        }

        public String getContentAt(int frame) {
            if (content.isEmpty()) {
                return "";
            }
            if (animated && content.size() > 1) {
                return content.get(frame % content.size());
            } else {
                return content.get(0);
            }
        }
    }
}