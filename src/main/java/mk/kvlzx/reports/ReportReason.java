package mk.kvlzx.reports;

import org.bukkit.Material;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public enum ReportReason {
    HACKS(
        Material.valueOf(MysthicKnockBack.getInstance().getReportMenuConfig().getReasonHacksId()), 
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonHacksName(), 
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonHacksLore()
    ),
    TOXIC(
        Material.valueOf(MysthicKnockBack.getInstance().getReportMenuConfig().getReasonToxicId()),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonToxicName(),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonToxicLore()
    ),
    TEAMING(
        Material.valueOf(MysthicKnockBack.getInstance().getReportMenuConfig().getReasonTeamingId()),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonTeamingName(),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonTeamingLore()
    ),
    BUG_ABUSE(
        Material.valueOf(MysthicKnockBack.getInstance().getReportMenuConfig().getReasonBugAbuseId()),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonBugAbuseName(),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonBugAbuseLore()
    ),
    INAPPROPRIATE_SKIN(
        Material.valueOf(MysthicKnockBack.getInstance().getReportMenuConfig().getReasonInappropriateSkinId()),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonInappropriateSkinName(),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonInappropriateSkinLore()
    ),
    OTHER(
        Material.valueOf(MysthicKnockBack.getInstance().getReportMenuConfig().getReasonOtherId()),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonOtherName(),
        MysthicKnockBack.getInstance().getReportMenuConfig().getReasonOtherLore()
    );

    private final Material icon;
    private final String displayName;
    private final String description;

    ReportReason(Material icon, String displayName, String description) {
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static ReportReason getByDisplayName(String name) {
        if (name == null) return null;
        
        // Remover el formateo de color que pueda quedar
        name = MessageUtils.stripColor(name);
        
        for (ReportReason reason : values()) {
            String reasonName = MessageUtils.stripColor(reason.displayName);
            if (reasonName.equals(name)) {
                return reason;
            }
        }
        return null;
    }
}
