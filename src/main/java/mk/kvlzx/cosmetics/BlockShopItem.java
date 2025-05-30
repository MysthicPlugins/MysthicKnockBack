package mk.kvlzx.cosmetics;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class BlockShopItem {
    private final Material material;
    private final String name;
    private final int price;
    private final String rarity;
    private final String rarityColor;
    private final String description;
    private final byte data;

    private static final List<BlockShopItem> ALL_BLOCKS = new ArrayList<>();

    public BlockShopItem(Material material, String name, int price, String rarity, String rarityColor, String description) {
        this(material, (byte)0, name, price, rarity, rarityColor, description);
    }

    public BlockShopItem(Material material, byte data, String name, int price, String rarity, String rarityColor, String description) {
        this.material = material;
        this.data = data;
        this.name = name;
        this.price = price;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        this.description = description;
        ALL_BLOCKS.add(this);
    }

    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public String getLore() { return description; }

    public static BlockShopItem getByMaterial(Material material) {
        return ALL_BLOCKS.stream()
            .filter(item -> item.getMaterial() == material)
            .findFirst()
            .orElse(null);
    }

    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(rarityColor + name));
        List<String> lore = new ArrayList<>();
        lore.add(rarityColor + "✦ " + rarity);
        lore.add(description);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
