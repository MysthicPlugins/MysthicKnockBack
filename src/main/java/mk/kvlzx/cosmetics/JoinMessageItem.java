package mk.kvlzx.cosmetics;

import java.util.ArrayList;
import java.util.List;

public class JoinMessageItem {
    private final String name;
    private final int price;
    private final String rarity;
    private final String rarityColor;
    private final String message;

    private static final List<JoinMessageItem> ALL_MESSAGES = new ArrayList<>();

    public JoinMessageItem(String name, String message, int price, String rarity, String rarityColor) {
        this.name = name;
        this.message = message;
        this.price = price;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        ALL_MESSAGES.add(this);
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public String getMessage() { return message; }

    public static List<JoinMessageItem> getAllMessages() {
        return ALL_MESSAGES;
    }

    public static JoinMessageItem getByName(String name) {
        return ALL_MESSAGES.stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}