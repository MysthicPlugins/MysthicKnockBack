package mk.kvlzx.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.BlocksShopConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlockUtils {
    public static final Map<String, Material> BLOCK_MATERIALS = new HashMap<String, Material>() {{
        put("sandstone", Material.SANDSTONE);
        put("oak_wood", Material.WOOD);
        put("white_wool", Material.WOOL);
        put("melon_block", Material.MELON_BLOCK);
        put("pumpkin", Material.PUMPKIN);
        put("stone_bricks", Material.SMOOTH_BRICK);
        put("mossy_cobblestone", Material.MOSSY_COBBLESTONE);
        put("netherrack", Material.NETHERRACK);
        put("coal_ore", Material.COAL_ORE);
        put("hay_block", Material.HAY_BLOCK);
        put("iron_block", Material.IRON_BLOCK);
        put("quartz_block", Material.QUARTZ_BLOCK);
        put("packed_ice", Material.PACKED_ICE);
        put("coal_block", Material.COAL_BLOCK);
        put("lapis_block", Material.LAPIS_BLOCK);
        put("clay", Material.CLAY);
        put("bookshelf", Material.BOOKSHELF);
        put("prismarine", Material.PRISMARINE);
        put("end_stone", Material.ENDER_STONE);
        put("ancestral_sponge", Material.SPONGE);
        put("sea_lantern", Material.SEA_LANTERN);
        put("glowstone", Material.GLOWSTONE);
        put("emerald_block", Material.EMERALD_BLOCK);
        put("diamond_block", Material.DIAMOND_BLOCK);
        put("gold_block", Material.GOLD_BLOCK);
        put("obsidian", Material.OBSIDIAN);
        put("nether_brick", Material.NETHER_BRICK);
        put("mystic_glass", Material.GLASS);
        put("music_block", Material.NOTE_BLOCK);
        put("void_frame", Material.ENDER_PORTAL_FRAME);
        put("jukebox", Material.JUKEBOX);
        put("void_hopper", Material.HOPPER);
        put("chaos_dispenser", Material.DISPENSER);
        put("bedrock", Material.BEDROCK);
    }};

    public static final List<Material> DECORATIVE_BLOCKS = Arrays.asList(
        Material.SANDSTONE,         // Default
        Material.WOOD,              // Oak Wood solo
        Material.WOOL,              // White Wool solo
        Material.MELON_BLOCK,       // Melon
        Material.PUMPKIN,           // Pumpkin
        Material.SMOOTH_BRICK,      // Stone Brick
        Material.MOSSY_COBBLESTONE, // Mossy Cobble
        Material.NETHERRACK,        // Netherrack
        Material.COAL_ORE,         // Coal Ore
        Material.HAY_BLOCK,         // Hay
        Material.IRON_BLOCK,        // Iron
        Material.QUARTZ_BLOCK,      // Quartz
        Material.PACKED_ICE,        // Packed Ice
        Material.SNOW_BLOCK,        // Snow
        Material.LAPIS_BLOCK,       // Lapis
        Material.PRISMARINE,        // Prismarine
        Material.COAL_BLOCK,        // Coal Block
        Material.ENDER_STONE,       // End Stone
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
        Material.NOTE_BLOCK,        // Note Block (troll)
        Material.ENDER_PORTAL_FRAME,// End Portal Frame (troll)
        Material.JUKEBOX,           // Jukebox (troll)
        Material.HOPPER,            // Hopper (troll)
        Material.DISPENSER,         // Dispenser (troll)
        Material.DROPPER            // Dropper (troll)
    );

    public static boolean isDecorativeBlock(Material material) {
        return DECORATIVE_BLOCKS.contains(material) || TROLL_BLOCKS.contains(material);
    }

    /**
     * Crea un ItemStack de bloque usando la configuración
     */
    public static ItemStack createBlockItem(Material blockType, UUID uuid) {
        BlocksShopConfig config = MysthicKnockBack.getInstance().getBlocksShopConfig();
        
        // Buscar el bloque en la configuración
        for (Map.Entry<String, BlocksShopConfig.BlockItem> entry : config.getBlockItems().entrySet()) {
            String blockKey = entry.getKey();
            BlocksShopConfig.BlockItem blockItem = entry.getValue();
            
            Material configMaterial = getMaterialFromKey(blockKey);
            
            if (configMaterial == blockType) {
                // Crear el item usando la configuración
                ItemStack item = new ItemStack(blockType, 64);
                ItemMeta meta = item.getItemMeta();
                
                String displayName = blockItem.getRarityColor() + blockItem.getName();
                meta.setDisplayName(MessageUtils.getColor(displayName));
                
                List<String> lore = new ArrayList<>();
                lore.add(MessageUtils.getColor(blockItem.getRarityColor() + blockItem.getRarity()));
                lore.add(MessageUtils.getColor(blockItem.getLore()));
                meta.setLore(lore);
                
                item.setItemMeta(meta);
                return item;
            }
        }
        
        // Fallback: crear item básico si no se encuentra en la configuración
        return new ItemStack(blockType, 64);
    }

    /**
     * Obtiene el Material de Minecraft a partir de la clave del config
     */
    public static Material getMaterialFromKey(String blockKey) {
        return BLOCK_MATERIALS.get(blockKey.toLowerCase());
    }
    
    /**
     * Obtiene la clave del config a partir del Material de Minecraft
     */
    public static String getKeyFromMaterial(Material material) {
        for (Map.Entry<String, Material> entry : BLOCK_MATERIALS.entrySet()) {
            if (entry.getValue() == material) {
                return entry.getKey();
            }
        }
        return null;
    }

}
