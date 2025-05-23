package kk.kvlzx.menus;

import org.bukkit.Material;

public enum TopType {
    KILLS("&b⚔ Top Kills", Material.DIAMOND_SWORD, "&7Click para ver el ranking"),
    KDR("&6☠ Top KDR", Material.GOLDEN_APPLE, "&7Click para ver el ranking"),
    STREAK("&c⚡ Top Rachas", Material.BLAZE_POWDER, "&7Click para ver el ranking"),
    ELO("&e✦ Top ELO", Material.NETHER_STAR, "&7Click para ver el ranking"),
    PLAYTIME("&a⌚ Top Horas Jugadas", Material.WATCH, "&7Click para ver el ranking");

    private final String title;
    private final Material icon;
    private final String description;
    private final String menuTitle;

    TopType(String title, Material icon, String description) {
        this.title = title;
        this.icon = icon;
        this.description = description;
        this.menuTitle = "&8• " + title + " &8•";
    }

    public String getTitle() { return title; }
    public Material getIcon() { return icon; }
    public String getDescription() { return description; }
    public String getMenuTitle() { return menuTitle; }
}
