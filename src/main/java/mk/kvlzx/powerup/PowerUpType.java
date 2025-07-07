package mk.kvlzx.powerup;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

public enum PowerUpType {
    SPEED("§bSpeed", Material.SUGAR, Arrays.asList("§7Increases your speed", "§7for 30 seconds")),
    JUMP("§aJump", Material.RABBIT_FOOT, Arrays.asList("§7Increases your jump", "§7for 30 seconds")),
    STRENGTH("§cStrength", Material.BLAZE_POWDER, Arrays.asList("§7Increases your damage", "§7for 20 seconds")),
    HEALTH("§dRegenerate", Material.GOLDEN_APPLE, Arrays.asList("§7Regenerates your health", "§7for 10 seconds")),
    INVISIBILITY("§8Invisibility", Material.FERMENTED_SPIDER_EYE, Arrays.asList("§7Makes you invisible", "§7for 15 seconds")),
    KNOCKBACK("§6Knockback", Material.STICK, Arrays.asList("§7Increases your knockback", "§7for 15 seconds"));

    private final String displayName;
    private final Material material;
    private final List<String> lore;

    PowerUpType(String displayName, Material material, List<String> lore) {
        this.displayName = displayName;
        this.material = material;
        this.lore = lore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public List<String> getLore() {
        return lore;
    }

    public static PowerUpType getRandom() {
        PowerUpType[] values = values();
        return values[(int) (Math.random() * values.length)];
    }
}
