package kk.kvlzx.menus;

public enum TopType {
    KILLS("&b⚔ Top Kills"),
    KDR("&6☠ Top KDR"),
    STREAK("&c⚡ Top Rachas"),
    ELO("&e✦ Top ELO"),
    PLAYTIME("&a⌚ Top Horas Jugadas");

    private final String title;

    TopType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getMenuTitle() {
        return "&8• " + title + " &8•";
    }
}
