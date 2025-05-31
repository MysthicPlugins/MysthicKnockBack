package mk.kvlzx.utils;

import org.bukkit.Material;
import java.util.Arrays;
import java.util.List;

public class BlockUtils {
    public static final List<Material> DECORATIVE_BLOCKS = Arrays.asList(
        Material.SANDSTONE,           // Default
        Material.WOOD,               // Oak Wood solo
        Material.WOOL,              // White Wool solo
        Material.MELON_BLOCK,       // Melon
        Material.PUMPKIN,           // Pumpkin
        Material.SMOOTH_BRICK,      // Stone Brick
        Material.MOSSY_COBBLESTONE, // Mossy Cobble
        Material.NETHERRACK,        // Netherrack
        Material.SOUL_SAND,         // Soul Sand
        Material.HAY_BLOCK,         // Hay
        Material.IRON_BLOCK,        // Iron
        Material.QUARTZ_BLOCK,      // Quartz
        Material.PACKED_ICE,        // Packed Ice
        Material.SNOW_BLOCK,        // Snow
        Material.LAPIS_BLOCK,       // Lapis
        Material.PRISMARINE,        // Prismarine
        Material.ENDER_STONE,         // End Stone
        Material.SPONGE,            // Sponge
        Material.SEA_LANTERN,       // Sea Lantern
        Material.GLOWSTONE,         // Glowstone
        Material.EMERALD_BLOCK,     // Emerald Block
        Material.DIAMOND_BLOCK,     // Diamond Block
        Material.GOLD_BLOCK,        // Gold Block
        Material.OBSIDIAN,          // Obsidian
        Material.NETHER_BRICK,      // Nether Brick
        Material.BEDROCK            // Bedrock (especial)
    );

    public static final List<Material> TROLL_BLOCKS = Arrays.asList(
        Material.GLASS,             // Glass (se moverá a troll)
        Material.STAINED_GLASS,     // Stained Glass (se moverá a troll)
        Material.NOTE_BLOCK,        // Note Block (troll)
        Material.CHEST,             // Chest (troll)
        Material.TRAPPED_CHEST,     // Trapped Chest (troll)
        Material.ENDER_PORTAL_FRAME,// End Portal Frame (troll)
        Material.JUKEBOX,          // Jukebox (troll)
        Material.ANVIL,            // Anvil (troll)
        Material.HOPPER,           // Hopper (troll)
        Material.DISPENSER,        // Dispenser (troll)
        Material.DROPPER           // Dropper (troll)
    );

    public static boolean isDecorativeBlock(Material material) {
        return DECORATIVE_BLOCKS.contains(material) || TROLL_BLOCKS.contains(material);
    }

}
