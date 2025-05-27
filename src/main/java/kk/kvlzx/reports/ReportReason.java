package kk.kvlzx.reports;

import org.bukkit.Material;

public enum ReportReason {
    HACKS(Material.DIAMOND_SWORD, "&c&lHacks", "Uso de hacks o ventajas injustas"),
    TOXIC(Material.POISONOUS_POTATO, "&e&lToxicidad", "Comportamiento tóxico o irrespetuoso"),
    TEAMING(Material.GOLD_INGOT, "&6&lTeaming", "Aliarse con otros jugadores"),
    BUG_ABUSE(Material.REDSTONE, "&4&lBug Abuse", "Abuso de bugs del juego"),
    INAPPROPRIATE_SKIN(Material.LEATHER_CHESTPLATE, "&d&lSkin Inapropiada", "Skin no apropiada"),
    OTHER(Material.PAPER, "&7&lOtro", "Otra razón no listada");

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
        for (ReportReason reason : values()) {
            if (reason.displayName.equals(name)) {
                return reason;
            }
        }
        return null;
    }
}
