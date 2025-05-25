package kk.kvlzx.menus;

public enum MenuType {
    MAIN("&8• &a&lMenú Principal &8•", 45),
    TOP_KILLS("&8• &b⚔ Top Kills &8•", 27),
    TOP_KDR("&8• &6☠ Top KDR &8•", 27),
    TOP_STREAK("&8• &c⚡ Top Rachas &8•", 27),
    TOP_ELO("&8• &e✦ Top ELO &8•", 27),
    TOP_PLAYTIME("&8• &a⌚ Top Horas Jugadas &8•", 27),
    STATS("&8• &b&lEstadísticas &8•", 27),
    INVENTORY_EDITOR("&8• &c&lEditor de Inventario &8•", 9);

    private final String title;
    private final int size;

    MenuType(String title, int size) {
        this.title = title;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public boolean isTopMenu() {
        return name().startsWith("TOP_");
    }
}
