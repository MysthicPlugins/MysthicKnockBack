package mk.kvlzx.config;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.config.CustomConfig;

public class TabConfig {
    private final CustomConfig configFile;

    private Boolean tabEnabled;
    private Boolean tabaAnimationEnabled;
    private Integer tabAnimationInterval;
    private List<String> tabHeaderAnimation;
    private List<String> tabHeadherWithoutAnimation;
    private List<String> tabFooter;
    private String tabPlayerDisplay;

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
        configFile = new CustomConfig("config.yml", "config/global", plugin);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        tabEnabled = config.getBoolean("tab.display.enabled");
        tabaAnimationEnabled = config.getBoolean("tab.display.header.animation");
        tabAnimationInterval = config.getInt("tab.display.header.animation-delay");
        tabHeaderAnimation = config.getStringList("tab.display.header.lines-with-animation");
        tabHeadherWithoutAnimation = config.getStringList("tab.display.header.lines-without-animation");
        tabFooter = config.getStringList("tab.display.footer");
        tabPlayerDisplay = config.getString("tab.display.player-display");

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
        scoreMessageArenaChange = config.getString("scoreboard.message");
        scoreArenaChangeColors = config.getStringList("scoreboard.title-animation.colors");
        scoreArenaChangeFrames = config.getStringList("scoreboard.title-animation.frames");
    }

    public void reload() {
        configFile.reloadConfig();
        loadConfig();
    }

    public Boolean isTabEnabled() { return tabEnabled; }
    public Boolean isTabAnimationEnabled() { return tabaAnimationEnabled; }
    public Integer getTabAnimationInterval() { return tabAnimationInterval; }
    public List<String> getTabHeaderAnimation() { return tabHeaderAnimation;  }
    public List<String> getTabHeaderWithoutAnimation() { return tabHeadherWithoutAnimation; }
    public List<String> getTabFooter() { return tabFooter; }
    public String getTabPlayerDisplay() { return tabPlayerDisplay; }

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
}
