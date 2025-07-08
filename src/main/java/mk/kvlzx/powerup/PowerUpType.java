package mk.kvlzx.powerup;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

import mk.kvlzx.utils.MessageUtils;

public enum PowerUpType {
    SPEED(MessageUtils.getColor("&bSpeed"), Material.SUGAR, Arrays.asList(
            MessageUtils.getColor("&7Increases your speed"),
            MessageUtils.getColor("&7for 30 seconds"))),
    JUMP(MessageUtils.getColor("&aJump"), Material.RABBIT_FOOT, Arrays.asList(
            MessageUtils.getColor("&7Increases your jump"),
            MessageUtils.getColor("&7for 30 seconds"))),
    STRENGTH(MessageUtils.getColor("&cStrength"), Material.BLAZE_POWDER, Arrays.asList(
            MessageUtils.getColor("&7Increases your damage"),
            MessageUtils.getColor("&7for 20 seconds"))),
    HEALTH(MessageUtils.getColor("&dRegenerate"), Material.GOLDEN_APPLE, Arrays.asList(
            MessageUtils.getColor("&7Regenerates your health"),
            MessageUtils.getColor("&7for 10 seconds"))),
    INVISIBILITY(MessageUtils.getColor("&8Invisibility"), Material.FERMENTED_SPIDER_EYE, Arrays.asList(
            MessageUtils.getColor("&7Makes you invisible"),
            MessageUtils.getColor("&7for 15 seconds"))),
    KNOCKBACK(MessageUtils.getColor("&6Knockback"), Material.STICK, Arrays.asList(
            MessageUtils.getColor("&7Increases your knockback"),
            MessageUtils.getColor("&7for 15 seconds")));

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
