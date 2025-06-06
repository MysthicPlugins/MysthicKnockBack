package mk.kvlzx.cosmetics;

import java.util.ArrayList;
import java.util.List;

public class BackgroundMusicItem {
    private final String name;
    private final int price;
    private final String rarity;
    private final String rarityColor;
    private final String description;
    private final String sound;
    private final float volume;
    private final float pitch;

    private static final List<BackgroundMusicItem> ALL_MUSIC = new ArrayList<>();

    public BackgroundMusicItem(String name, int price, String rarity, String rarityColor, 
            String description, String sound, float volume, float pitch) {
        this.name = name;
        this.price = price;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        this.description = description;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        ALL_MUSIC.add(this);
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public String getDescription() { return description; }
    public String getSound() { return sound; }
    public float getVolume() { return volume; }
    public float getPitch() { return pitch; }

    public static List<BackgroundMusicItem> getAllMusic() {
        return ALL_MUSIC;
    }

    public static BackgroundMusicItem getByName(String name) {
        return ALL_MUSIC.stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
