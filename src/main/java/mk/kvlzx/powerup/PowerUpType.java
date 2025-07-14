package mk.kvlzx.powerup;

import java.util.List;

import org.bukkit.Material;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;
import java.util.stream.Collectors;

public enum PowerUpType {
    JUMP_1(MessageUtils.getColor(MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump1Name()), 
            Material.valueOf(MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump1Id()),
            MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump1Lore().stream()
                .map(MessageUtils::getColor)
                .collect(Collectors.toList())
    ),
    
    JUMP_2(MessageUtils.getColor(MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump2Name()), 
            Material.valueOf(MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump2Id()),
            MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump2Lore().stream()
                .map(MessageUtils::getColor)
                .collect(Collectors.toList())
    ),
    
    JUMP_3(MessageUtils.getColor(MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump3Name()), 
            Material.valueOf(MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump3Id()),
            MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump3Lore().stream()
                .map(MessageUtils::getColor)
                .collect(Collectors.toList())
    ),
    
    JUMP_4(MessageUtils.getColor(MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump4Name()), 
            Material.valueOf(MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump4Id()),
            MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump4Lore().stream()
                .map(MessageUtils::getColor)
                .collect(Collectors.toList())
    ),
    
    INVISIBILITY(MessageUtils.getColor(MysthicKnockBack.getInstance().getMainConfig().getPowerUpInvisibilityName()), 
                Material.valueOf(MysthicKnockBack.getInstance().getMainConfig().getPowerUpInvisibilityId()),
                MysthicKnockBack.getInstance().getMainConfig().getPowerUpInvisibilityLore().stream()
                    .map(MessageUtils::getColor)
                    .collect(Collectors.toList())
    ),
    
    KNOCKBACK(MessageUtils.getColor(MysthicKnockBack.getInstance().getMainConfig().getPowerUpKnockbackName()), 
                Material.valueOf(MysthicKnockBack.getInstance().getMainConfig().getPowerUpKnockbackId()),
                MysthicKnockBack.getInstance().getMainConfig().getPowerUpKnockbackLore().stream()
                    .map(MessageUtils::getColor)
                    .collect(Collectors.toList())
    ),

    EXPLOSIVE_ARROW(MessageUtils.getColor(MysthicKnockBack.getInstance().getMainConfig().getPowerUpExplosiveArrowName()),
            Material.valueOf(MysthicKnockBack.getInstance().getMainConfig().getPowerUpExplosiveArrowId()),
            MysthicKnockBack.getInstance().getMainConfig().getPowerUpExplosiveArrowLore().stream()
                    .map(MessageUtils::getColor)
                    .collect(Collectors.toList())
    ),
    
    BLACK_HOLE(MessageUtils.getColor(MysthicKnockBack.getInstance().getMainConfig().getPowerUpBlackHoleName()),
            Material.valueOf(MysthicKnockBack.getInstance().getMainConfig().getPowerUpBlackHoleId()),
            MysthicKnockBack.getInstance().getMainConfig().getPowerUpBlackHoleLore().stream()
                    .map(MessageUtils::getColor)
                    .collect(Collectors.toList())
    ),
    
    DOUBLE_PEARL(MessageUtils.getColor(MysthicKnockBack.getInstance().getMainConfig().getPowerUpDoublePearlName()),
            Material.valueOf(MysthicKnockBack.getInstance().getMainConfig().getPowerUpDoublePearlId()),
            MysthicKnockBack.getInstance().getMainConfig().getPowerUpDoublePearlLore().stream()
                    .map(MessageUtils::getColor)
                    .collect(Collectors.toList())
    );

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
            case JUMP_1: return MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump1EffectDuration() * 20;
            case JUMP_2: return MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump2EffectDuration() * 20;
            case JUMP_3: return MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump3EffectDuration() * 20;
            case JUMP_4: return MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump4EffectDuration() * 20;
            default: return 0;
        }
    }
    
    // Método para obtener el nivel del efecto jump
    public int getJumpLevel() {
        switch (this) {
            case JUMP_1: return MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump1EffectLevel();
            case JUMP_2: return MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump2EffectLevel();
            case JUMP_3: return MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump3EffectLevel();
            case JUMP_4: return MysthicKnockBack.getInstance().getMainConfig().getPowerUpJump4EffectLevel();
            default: return 0;
        }
    }
}