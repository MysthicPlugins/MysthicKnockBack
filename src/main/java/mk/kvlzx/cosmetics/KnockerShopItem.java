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

    static {
        // Knocker por defecto
        addKnocker(Material.STICK, "Palo de Empuje", 0, "COMÚN", "&7", "Un simple palo de madera, pero efectivo");

        // Comunes - 5000-6000 KGCoins
        addKnocker(Material.BONE, "Eclipse Óseo", 5000, "COMÚN", "&7", "Restos de antiguos guerreros, vibran con ecos de batallas olvidadas.");
        addKnocker(Material.RED_ROSE, "Pétalo Solar", 5000, "COMÚN", "&7", "Baila con el viento, pero no te dejes engañar por su fragilidad.");
        addKnocker(Material.LEVER, "Mando Rúnico", 5000, "COMÚN", "&7", "Un simple giro puede desatar el caos o la salvación.");
        addKnocker(Material.TORCH, "Fuego Eterno", 5000, "COMÚN", "&7", "Ilumina el camino, pero nunca revela los secretos de la sombra.");
        addKnocker(Material.PAPER, "Pergamino Lunar", 6000, "COMÚN", "&7", "Sus fibras guardan susurros de hechizos aún no escritos.");
        addKnocker(Material.BREAD, "Miga Sagrada", 6000, "COMÚN", "&7", "Alimento de dioses... o de aldeanos con hambre.");
        addKnocker(Material.ROTTEN_FLESH, "Podredumbre Vil", 5000, "COMÚN", "&7", "¡Ew! Huele a derrota, pero los lobos no se quejan.");

        // Poco comunes - 15000 KGCoins
        addKnocker(Material.RAW_FISH, (byte)3, "Veneno Marino", 15000, "POCO COMÚN", "&a", "¡Cuidado! Su pinchazo esconda un veneno que desafía a los valientes.");
        addKnocker(Material.BOOK, "Tomo Arcano", 15000, "POCO COMÚN", "&a", "Cada página contiene el conocimiento de un mago perdido.");
        addKnocker(Material.APPLE, "Fruto Áureo", 15000, "POCO COMÚN", "&a", "Dicen que su sabor despierta recuerdos de un paraíso perdido.");

        // Raros - 40000-100000 KGCoins
        addKnocker(Material.BLAZE_ROD, "Llama Infernal", 50000, "RARO", "&9", "Forjada en el corazón del Nether, arde con la furia de mil soles.");
        addKnocker(Material.GOLDEN_APPLE, "Corazón Dorado", 100000, "RARO", "&9", "Un mordisco otorga fuerza divina, pero a un costo elevado.");
        addKnocker(Material.SULPHUR, "Polvo Explosivo", 40000, "RARO", "&9", "Un solo grano puede desencadenar una explosión que sacude mundos.");

        // Épicos - 500000 KGCoins
        addKnocker(Material.DIAMOND, "Gema Estelar", 500000, "ÉPICO", "&5", "Nacida en las profundidades, su brillo desafía al mismísimo cielo.");

        // Legendarios - 1000000 KGCoins
        addKnocker(Material.GOLDEN_APPLE, (byte)1, "Alma Encantada", 1000000, "LEGENDARIO", "&6", "Forjada por los dioses del End, su poder trasciende la mortalidad.");
    }

    private static void addKnocker(Material material, String name, int price, String rarity, String rarityColor, String description) {
        addKnocker(material, (byte)0, name, price, rarity, rarityColor, description);
    }

    private static void addKnocker(Material material, byte data, String name, int price, String rarity, String rarityColor, String description) {
        ALL_KNOCKERS.add(new KnockerShopItem(material, data, name, price, rarity, rarityColor, description));
    }

    public KnockerShopItem(Material material, byte data, String name, int price, String rarity, String rarityColor, String description) {
        this.material = material;
        this.data = data;
        this.name = name;
        this.price = price;
        this.rarity = rarity;
        this.rarityColor = rarityColor;
        this.description = description;
    }

    public static List<KnockerShopItem> getAllKnockers() {
        return ALL_KNOCKERS;
    }

    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getRarity() { return rarity; }
    public String getRarityColor() { return rarityColor; }
    public String getDescription() { return description; }
    public byte getData() { return data; }

    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(rarityColor + name));
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtils.getColor(rarityColor + "✦ " + rarity));
        lore.add(MessageUtils.getColor(description));
        item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static KnockerShopItem getByMaterial(Material material, byte data) {
        return ALL_KNOCKERS.stream()
            .filter(item -> item.getMaterial() == material && item.getData() == data)
            .findFirst()
            .orElse(null);
    }
}
