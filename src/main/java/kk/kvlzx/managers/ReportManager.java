package kk.kvlzx.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.reports.ReportReason;
import kk.kvlzx.utils.MessageUtils;

public class ReportManager {
    private final Map<UUID, String> pendingReports = new HashMap<>();
    private final Map<String, Long> reportCooldowns = new HashMap<>();
    private static final long COOLDOWN = 300000; // 5 minutos en milisegundos
    private final KvKnockback plugin;

    public ReportManager(KvKnockback plugin) {
        this.plugin = plugin;
    }

    public void setReportTarget(UUID reporter, String targetName) {
        pendingReports.put(reporter, targetName);
    }

    public String getReportTarget(UUID reporter) {
        return pendingReports.get(reporter);
    }

    public void submitReport(Player reporter, String targetName, ReportReason reason) {
        String reporterId = reporter.getName() + "-" + targetName;
        long currentTime = System.currentTimeMillis();

        if (reportCooldowns.containsKey(reporterId) && 
            currentTime - reportCooldowns.get(reporterId) < COOLDOWN) {
            reporter.sendMessage(MessageUtils.getColor("&cDebes esperar 5 minutos entre reportes al mismo jugador."));
            return;
        }

        // Notificar al staff
        String reportMessage = String.format(
            "&c[Reporte] &f%s &7ha reportado a &f%s &7por &f%s",
            reporter.getName(),
            targetName,
            reason.getDisplayName()
        );

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("kvknockback.reports.view")) {
                staff.sendMessage(MessageUtils.getColor(reportMessage));
            }
        }

        // Notificar al reportador
        reporter.sendMessage(MessageUtils.getColor("&aReporte enviado correctamente."));
        
        // Establecer cooldown
        reportCooldowns.put(reporterId, currentTime);
        pendingReports.remove(reporter.getUniqueId());
    }
}
