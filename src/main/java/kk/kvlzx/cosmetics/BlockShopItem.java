package kk.kvlzx.cosmetics;

import org.bukkit.Material;

public class BlockShopItem {
    private final Material material;
    private final String name;
    private final int price;
    private final String rarity;
    private final String rarityColor;
    private final String lore;

    public BlockShopItem(Material material, String name, int price, String rarity, String rarityColor, String lore) {
        this.material = material;
        this.name = name;
        this.price = price;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        this.lore = lore;
    }

    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public String getLore() { return lore; }
}
