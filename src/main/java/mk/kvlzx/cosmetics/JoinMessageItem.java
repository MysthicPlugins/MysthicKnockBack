package mk.kvlzx.cosmetics;

import java.util.ArrayList;
import java.util.List;

public class JoinMessageItem {
    private final String message;
    private final String name;
    private final int price;
    private final String rarity;
    private final String rarityColor;
    private final String description;

    private static final List<JoinMessageItem> ALL_MESSAGES = new ArrayList<>();

    public JoinMessageItem(String message, String name, int price, String rarity, String rarityColor, String description) {
        this.message = message;
        this.name = name;
        this.price = price;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        this.description = description;
        ALL_MESSAGES.add(this);
    }

    public String getMessage() { return message; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public String getDescription() { return description; }

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