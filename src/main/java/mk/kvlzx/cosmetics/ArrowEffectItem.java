package mk.kvlzx.cosmetics;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;

public class ArrowEffectItem {
    private final String name;
    private final int price;
    private final String rarity;
    private final String rarityColor;
    private final String description;
    private final Effect effect;
    private final float speed;
    private final int count;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    private static final List<ArrowEffectItem> ALL_EFFECTS = new ArrayList<>();

    public ArrowEffectItem(String name, int price, String rarity, String rarityColor, 
            String description, Effect effect, float speed, int count,
            float offsetX, float offsetY, float offsetZ) {
        this.name = name;
        this.price = price;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        this.description = description;
        this.effect = effect;
        this.speed = speed;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        ALL_EFFECTS.add(this);
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public String getDescription() { return description; }
    public Effect getEffect() { return effect; }
    public float getSpeed() { return speed; }
    public int getCount() { return count; }
    public float getOffsetX() { return offsetX; }
    public float getOffsetY() { return offsetY; }
    public float getOffsetZ() { return offsetZ; }

    public void playEffect(Location location) {
        location.getWorld().spigot().playEffect(location, effect, 0, 0, 
            offsetX, offsetY, offsetZ, speed, count, 64);
    }

    public static List<ArrowEffectItem> getAllEffects() {
        return ALL_EFFECTS;
    }

    public static ArrowEffectItem getByName(String name) {
        return ALL_EFFECTS.stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
