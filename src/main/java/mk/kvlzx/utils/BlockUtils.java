package mk.kvlzx.utils;

import org.bukkit.Material;
import java.util.Arrays;
import java.util.List;

public class BlockUtils {
    public static final List<Material> DECORATIVE_BLOCKS = Arrays.asList(
        Material.SANDSTONE,
        Material.GLASS,
        Material.STAINED_CLAY,
        Material.QUARTZ_BLOCK,
        Material.SMOOTH_BRICK,
        Material.WOOD,
        Material.WOOL,
        Material.SNOW_BLOCK,
        Material.GLOWSTONE,
        Material.NETHER_BRICK,
        Material.PRISMARINE,
        Material.SEA_LANTERN,
        Material.ENDER_STONE,
        Material.PACKED_ICE,
        Material.COBBLESTONE,
        Material.BRICK,
        Material.MOSSY_COBBLESTONE,
        Material.CLAY,
        Material.EMERALD_BLOCK,
        Material.DIAMOND_BLOCK,
        Material.OBSIDIAN,
        Material.GOLD_BLOCK,
        Material.COAL_BLOCK,
        Material.REDSTONE_BLOCK,
        Material.BEDROCK
    );

    public static boolean isDecorativeBlock(Material material) {
        return DECORATIVE_BLOCKS.contains(material);
    }
}
