package kk.kvlzx.menus;

public enum MenuType {
    MAIN_MENU("&8• &a&lMenú Principal &8•", 45),
    TOP_MENU("", 27),
    STATS_MENU("&8• &b&lEstadísticas &8•", 27),
    INVENTORY_EDITOR("&8• &c&lEditor de Inventario &8•", 54);

    private String title;
    private final int size;

    MenuType(String title, int size) {
        this.title = title;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setFormattedTitle(String... args) {
        this.title = String.format(title, (Object[]) args);
    }

    public int getSize() {
        return size;
    }
}
