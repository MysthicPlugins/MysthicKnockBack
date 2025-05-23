package kk.kvlzx.menus;

public enum MenuType {
    MAIN_MENU("&8• &a&lMenú Principal &8•", 27),
    TOP_MENU("&8• &b&lTop %s &8•", 45);

    private final String title;
    private final int size;

    MenuType(String title, int size) {
        this.title = title;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public String getFormattedTitle(Object... args) {
        return String.format(title, args);
    }

    public int getSize() {
        return size;
    }
}
