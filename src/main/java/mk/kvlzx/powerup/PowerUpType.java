package mk.kvlzx.powerup;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

import mk.kvlzx.utils.MessageUtils;

public enum PowerUpType {
    JUMP_1(MessageUtils.getColor("&aJump I"), Material.RABBIT_FOOT, Arrays.asList(
            MessageUtils.getColor("&7Increases your jump"),
            MessageUtils.getColor("&7Level I for 20 seconds"))),
    
    JUMP_2(MessageUtils.getColor("&aJump II"), Material.RABBIT_FOOT, Arrays.asList(
            MessageUtils.getColor("&7Increases your jump"),
            MessageUtils.getColor("&7Level II for 15 seconds"))),
    
    JUMP_3(MessageUtils.getColor("&aJump III"), Material.RABBIT_FOOT, Arrays.asList(
            MessageUtils.getColor("&7Increases your jump"),
            MessageUtils.getColor("&7Level III for 10 seconds"))),
    
    JUMP_4(MessageUtils.getColor("&aJump IV"), Material.RABBIT_FOOT, Arrays.asList(
            MessageUtils.getColor("&7Increases your jump"),
            MessageUtils.getColor("&7Level IV for 5 seconds"))),
    
    INVISIBILITY(MessageUtils.getColor("&8Invisibility"), Material.FERMENTED_SPIDER_EYE, Arrays.asList(
            MessageUtils.getColor("&7Makes you invisible"),
            MessageUtils.getColor("&7for 5 seconds"))),
    
    KNOCKBACK(MessageUtils.getColor("&6Knockback"), Material.STICK, Arrays.asList(
            MessageUtils.getColor("&7Increases your knockback"),
            MessageUtils.getColor("&7for 10 seconds")));

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
    
    // Método para obtener la duración del efecto jump
    public int getJumpDuration() {
        switch (this) {
            case JUMP_1: return 20; // 20 segundos
            case JUMP_2: return 15; // 15 segundos
            case JUMP_3: return 10; // 10 segundos
            case JUMP_4: return 5;  // 5 segundos
            default: return 0;
        }
    }
    
    // Método para obtener el nivel del efecto jump
    public int getJumpLevel() {
        switch (this) {
            case JUMP_1: return 0; // Nivel 1 (amplifier 0)
            case JUMP_2: return 1; // Nivel 2 (amplifier 1)
            case JUMP_3: return 2; // Nivel 3 (amplifier 2)
            case JUMP_4: return 3; // Nivel 4 (amplifier 3)
            default: return 0;
        }
    }
}
