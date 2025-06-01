package mk.kvlzx.cosmetics;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import mk.kvlzx.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class KnockerShopItem {
    private final Material material;
    private final String name;
    private final int price;
    private final String rarity;
    private final String rarityColor;
    private final String description;
    private final byte data;

    private static final List<KnockerShopItem> ALL_KNOCKERS = new ArrayList<>();

    public KnockerShopItem(Material material, String name, int price, String rarity, String rarityColor, String description) {
        this(material, (byte)0, name, price, rarity, rarityColor, description);
    }

    public KnockerShopItem(Material material, byte data, String name, int price, String rarity, String rarityColor, String description) {
        this.material = material;
        this.data = data;
        this.name = name;
        this.price = price;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        this.description = description;
        ALL_KNOCKERS.add(this);
    }

    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public String getDescription() { return description; }
    public byte getData() { return data; }

    public static KnockerShopItem getByMaterial(Material material) {
        return ALL_KNOCKERS.stream()
            .filter(item -> item.getMaterial() == material)
            .findFirst()
            .orElse(null);
    }

    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(rarityColor + name));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.getColor(rarityColor + "✦ " + rarity));
        lore.add(MessageUtils.getColor(description));
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        return item;
    }
}
