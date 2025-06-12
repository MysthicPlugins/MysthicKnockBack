package mk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.reports.ReportReason;
import mk.kvlzx.utils.MessageUtils;

public class ReportManager {
    private final Map<UUID, String> pendingReports = new HashMap<>();
    private final Map<String, Long> reportCooldowns = new HashMap<>();
    private static final long COOLDOWN = 120000; // 2 minutos en milisegundos
    private final MysthicKnockBack plugin;

    public ReportManager(MysthicKnockBack plugin) {
        this.plugin = plugin;
    }

    public void setReportTarget(UUID reporter, String targetName) {
        pendingReports.put(reporter, targetName);
    }

    public String getReportTarget(UUID reporter) {
        String target = pendingReports.get(reporter);
        return target; 
    }

    public void submitReport(Player reporter, String targetName, ReportReason reason) {
        String reporterId = reporter.getName() + "-" + targetName;
        long currentTime = System.currentTimeMillis();

        if (reportCooldowns.containsKey(reporterId) && 
            currentTime - reportCooldowns.get(reporterId) < COOLDOWN) {
            
            long remainingTime = (reportCooldowns.get(reporterId) + COOLDOWN - currentTime) / 1000;
            
            reporter.sendMessage(MessageUtils.getColor("&cYou must wait " + remainingTime + " seconds to report this player."));
            return;
        }

        // Notificar al staff
        String reportMessage = String.format(
            "&c[Report] &f%s &7has reported &f%s &7for &f%s",
            reporter.getName(),
            targetName,
            reason.getDisplayName()
        );

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("mysthicknockback.reports.view")) {
                staff.sendMessage(MessageUtils.getColor(reportMessage));
            }
        }

        // Notificar al reportador
        reporter.sendMessage(MessageUtils.getColor("&aReport sent successfully."));
        
        // Establecer cooldown
        reportCooldowns.put(reporterId, currentTime);
        pendingReports.remove(reporter.getUniqueId());

    }
}
