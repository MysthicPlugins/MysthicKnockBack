package mk.kvlzx.reports;

import org.bukkit.Material;

import mk.kvlzx.utils.MessageUtils;

public enum ReportReason {
    HACKS(Material.DIAMOND_SWORD, "&c&lHacks", "Use of hacks or unfair advantages"),
    TOXIC(Material.POISONOUS_POTATO, "&e&lToxicity", "Toxic or disrespectful behavior"),
    TEAMING(Material.GOLD_INGOT, "&6&lTeaming", "Teaming up with other players"),
    BUG_ABUSE(Material.REDSTONE, "&4&lBug Abuse", "Exploiting game bugs"),
    INAPPROPRIATE_SKIN(Material.LEATHER_CHESTPLATE, "&d&lInappropriate Skin", "Inappropriate skin"),
    OTHER(Material.PAPER, "&7&lOther", "Other reason not listed");

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
