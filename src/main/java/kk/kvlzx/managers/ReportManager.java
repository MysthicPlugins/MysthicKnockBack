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
    private boolean debug = true; // Cambiado a true por defecto

    public ReportManager(KvKnockback plugin) {
        this.plugin = plugin;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private void logDebug(String message) {
        plugin.getLogger().info("[Sistema de Reportes] " + message);
    }

    public void setReportTarget(UUID reporter, String targetName) {
        pendingReports.put(reporter, targetName);
        logDebug("Nuevo objetivo de reporte - Reportador: " + reporter + ", Objetivo: " + targetName);
    }

    public String getReportTarget(UUID reporter) {
        String target = pendingReports.get(reporter);
        logDebug("Getting report target for " + reporter + " - Target: " + target);
        return target; 
    }

    public void submitReport(Player reporter, String targetName, ReportReason reason) {
        String reporterId = reporter.getName() + "-" + targetName;
        long currentTime = System.currentTimeMillis();

        logDebug("Intento de reporte - Reportador: " + reporter.getName() + 
                ", Objetivo: " + targetName + ", Razón: " + reason.getDisplayName());

        if (reportCooldowns.containsKey(reporterId) && 
            currentTime - reportCooldowns.get(reporterId) < COOLDOWN) {
            
            long remainingTime = (reportCooldowns.get(reporterId) + COOLDOWN - currentTime) / 1000;
            logDebug("Reporte bloqueado por cooldown - Tiempo restante: " + remainingTime + " segundos");
            
            reporter.sendMessage(MessageUtils.getColor("&cDebes esperar " + remainingTime + " segundos para reportar a este jugador."));
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

        logDebug("Reporte enviado exitosamente - ID: " + reporterId);
    }

    // Método para ver estado del sistema de reportes (para debug)
    public void printDebugInfo() {
        if (!debug) return;
        
        plugin.getLogger().info("=== Report System Debug Info ===");
        plugin.getLogger().info("Pending reports: " + pendingReports.size());
        plugin.getLogger().info("Active cooldowns: " + reportCooldowns.size());
        
        reportCooldowns.forEach((id, time) -> {
            long remaining = (time + COOLDOWN - System.currentTimeMillis()) / 1000;
            if (remaining > 0) {
                plugin.getLogger().info("Cooldown - ID: " + id + ", Time remaining: " + remaining + "s");
            }
        });
    }
}
