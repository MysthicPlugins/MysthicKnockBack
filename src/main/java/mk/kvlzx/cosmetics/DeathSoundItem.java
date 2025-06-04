package mk.kvlzx.cosmetics;

import org.bukkit.Sound;
import java.util.ArrayList;
import java.util.List;

public class DeathSoundItem {
    private final String name;
    private final int price;
    private final String rarity;
    private final String rarityColor;
    private final String description;
    private final Sound sound;
    private final float volume;
    private final float pitch;

    private static final List<DeathSoundItem> ALL_SOUNDS = new ArrayList<>();

    public DeathSoundItem(String name, int price, String rarity, String rarityColor, 
            String description, Sound sound, float volume, float pitch) {
        this.name = name;
        this.price = price;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        this.description = description;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        ALL_SOUNDS.add(this);
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public String getDescription() { return description; }
    public Sound getSound() { return sound; }
    public float getVolume() { return volume; }
    public float getPitch() { return pitch; }

    public static List<DeathSoundItem> getAllSounds() {
        return ALL_SOUNDS;
    }

    public static DeathSoundItem getByName(String name) {
        return ALL_SOUNDS.stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
