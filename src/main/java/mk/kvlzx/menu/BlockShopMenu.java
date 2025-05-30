package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.BlockShopItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class BlockShopMenu extends Menu {
    private final List<BlockShopItem> shopItems;
    private static String currentCategory = "COMÚN";

    public BlockShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lTienda de Bloques &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<BlockShopItem> initializeShopItems() {
        List<BlockShopItem> items = new ArrayList<>();
        
        // Común - Color gris - 1000
        addCommonBlocks(items);
        
        // Poco común - Color verde - 2500
        addUncommonBlocks(items);
        
        // Raro - Color azul - 5000
        addRareBlocks(items);
        
        // Épico - Color morado - 7500
        addEpicBlocks(items);
        
        // Legendario - Color dorado - 10000
        addLegendaryBlocks(items);
        
        // Bedrock - Especial
        items.add(new BlockShopItem(Material.BEDROCK, "Bedrock", 50000, "ESPECIAL", "&4", 
            "&4&lDesbloqueado al obtener todos los demás bloques"));
        
        return items;
    }

    private void addCommonBlocks(List<BlockShopItem> items) {
        // Bloques de Madera
        items.add(new BlockShopItem(Material.WOOD, (byte)0, "Madera de Roble", 1000, "COMÚN", "&7", "&6Tallada del árbol más antiguo del bosque"));
        items.add(new BlockShopItem(Material.WOOD, (byte)1, "Madera de Abeto", 1000, "COMÚN", "&7", "&6Extraída de los bosques nevados"));
        items.add(new BlockShopItem(Material.WOOD, (byte)2, "Madera de Abedul", 1000, "COMÚN", "&7", "&6Cortada de los árboles más altos"));
        items.add(new BlockShopItem(Material.WOOD, (byte)3, "Madera de Jungla", 1000, "COMÚN", "&7", "&6Encontrada en las profundidades de la selva"));
        items.add(new BlockShopItem(Material.WOOD, (byte)4, "Madera de Acacia", 1000, "COMÚN", "&7", "&6Proveniente de las sabanas ardientes"));
        items.add(new BlockShopItem(Material.WOOD, (byte)5, "Madera Oscura", 1000, "COMÚN", "&7", "&6Extraída del bosque tenebroso"));

        // Bloques Básicos
        items.add(new BlockShopItem(Material.STONE, "Piedra Lisa", 1000, "COMÚN", "&7", "&7Pulida por los antiguos constructores"));
        items.add(new BlockShopItem(Material.COBBLESTONE, "Piedra", 1000, "COMÚN", "&7", "&7Extraída de las montañas más resistentes"));
        items.add(new BlockShopItem(Material.GLASS, "Vidrio", 1000, "COMÚN", "&7", "&7Forjado en las arenas más puras"));
        items.add(new BlockShopItem(Material.SANDSTONE, "Arenisca", 0, "COMÚN", "&7", "&7Moldeada por los vientos del desierto"));
        items.add(new BlockShopItem(Material.BRICK, "Ladrillos", 1000, "COMÚN", "&7", "&7Horneados en los hornos ancestrales"));
    }

    private void addUncommonBlocks(List<BlockShopItem> items) {
        // Lanas de Colores
        items.add(new BlockShopItem(Material.WOOL, (byte)0, "Lana Blanca", 2500, "POCO_COMÚN", "&a", "&fTejida con el vellón más puro"));
        items.add(new BlockShopItem(Material.WOOL, (byte)1, "Lana Naranja", 2500, "POCO_COMÚN", "&a", "&6Teñida con el ocaso"));
        items.add(new BlockShopItem(Material.WOOL, (byte)4, "Lana Amarilla", 2500, "POCO_COMÚN", "&a", "&eTocada por el sol"));
        items.add(new BlockShopItem(Material.WOOL, (byte)11, "Lana Azul", 2500, "POCO_COMÚN", "&a", "&9Tejida con el cielo"));
        items.add(new BlockShopItem(Material.WOOL, (byte)14, "Lana Roja", 2500, "POCO_COMÚN", "&a", "&cTeñida con pasión"));

        // Arcillas Teñidas
        items.add(new BlockShopItem(Material.STAINED_CLAY, (byte)0, "Arcilla Blanca", 2500, "POCO_COMÚN", "&a", "&fMoldeada en las nubes"));
        items.add(new BlockShopItem(Material.STAINED_CLAY, (byte)4, "Arcilla Amarilla", 2500, "POCO_COMÚN", "&a", "&eEndurecida por el sol"));
        items.add(new BlockShopItem(Material.STAINED_CLAY, (byte)14, "Arcilla Roja", 2500, "POCO_COMÚN", "&a", "&cCocida en lava"));

        // Otros
        items.add(new BlockShopItem(Material.SMOOTH_BRICK, "Ladrillos de Piedra", 2500, "POCO_COMÚN", "&a", "&7Tallados por maestros canteros"));
        items.add(new BlockShopItem(Material.MOSSY_COBBLESTONE, "Piedra Musgosa", 2500, "POCO_COMÚN", "&a", "&2Cubierta por el paso del tiempo"));
    }

    private void addRareBlocks(List<BlockShopItem> items) {
        // Bloques Premium
        items.add(new BlockShopItem(Material.QUARTZ_BLOCK, "Bloque de Cuarzo", 5000, "RARO", "&9", "&fExtraído del Nether más profundo"));
        items.add(new BlockShopItem(Material.PACKED_ICE, "Hielo Compacto", 5000, "RARO", "&9", "&bCongelado por dragones de hielo"));
        items.add(new BlockShopItem(Material.SNOW_BLOCK, "Nieve Eterna", 5000, "RARO", "&9", "&fNunca se derrite"));
        
        // Vidrios de Colores
        items.add(new BlockShopItem(Material.STAINED_GLASS, (byte)0, "Cristal Místico", 5000, "RARO", "&9", "&fTransparente como el alma"));
        items.add(new BlockShopItem(Material.STAINED_GLASS, (byte)11, "Cristal Oceánico", 5000, "RARO", "&9", "&bReflejos del mar profundo"));
        items.add(new BlockShopItem(Material.STAINED_GLASS, (byte)14, "Cristal Carmesí", 5000, "RARO", "&9", "&cTintado con sangre de dragón"));
    }

    private void addEpicBlocks(List<BlockShopItem> items) {
        // Bloques Especiales
        items.add(new BlockShopItem(Material.PRISMARINE, "Prismarina", 7500, "ÉPICO", "&5", "&3Cristalizada en las profundidades"));
        items.add(new BlockShopItem(Material.SEA_LANTERN, "Linterna Marina", 7500, "ÉPICO", "&5", "&bIluminada por almas marinas"));
        items.add(new BlockShopItem(Material.GLOWSTONE, "Piedra Luminosa", 7500, "ÉPICO", "&5", "&eEmana luz eterna"));
        items.add(new BlockShopItem(Material.OBSIDIAN, "Obsidiana", 7500, "ÉPICO", "&5", "&8Forjada en el núcleo del mundo"));
        items.add(new BlockShopItem(Material.EMERALD_BLOCK, "Bloque de Esmeralda", 7500, "ÉPICO", "&5", "&aComprimido con riqueza pura"));
    }

    private void addLegendaryBlocks(List<BlockShopItem> items) {
        // Bloques Legendarios
        items.add(new BlockShopItem(Material.DIAMOND_BLOCK, "Bloque de Diamante", 10000, "LEGENDARIO", "&6", "&bForjado con los cristales más puros"));
        items.add(new BlockShopItem(Material.GOLD_BLOCK, "Bloque de Oro", 10000, "LEGENDARIO", "&6", "&eComprimido con oro divino"));
        items.add(new BlockShopItem(Material.ENDER_STONE, "Piedra del End", 10000, "LEGENDARIO", "&6", "&fExtraída del vacío mismo"));
        items.add(new BlockShopItem(Material.NETHER_BRICK, "Ladrillo del Nether", 10000, "LEGENDARIO", "&6", "&cForjado en el infierno"));
        items.add(new BlockShopItem(Material.COAL_BLOCK, "Bloque de Carbón", 10000, "LEGENDARIO", "&6", "&8Comprimido durante eones"));
        items.add(new BlockShopItem(Material.REDSTONE_BLOCK, "Bloque de Redstone", 10000, "LEGENDARIO", "&6", "&cPalpita con energía antigua"));
    }

    // Implementar los otros métodos add*Blocks de manera similar

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lTu Balance",
            "&7Balance actual: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Categoría actual: " + currentCategory));

        // Filtrar bloques por categoría
        Material currentBlock = plugin.getCosmeticManager().getPlayerBlock(player.getUniqueId());
        int slot = 10;
        for (BlockShopItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupBlockButton(inv, slot, item, player, currentBlock);
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }

        // Botones de navegación
        inv.setItem(39, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver a las categorías"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15);
        fillEmptySlots(inv, filler);
    }

    private void setupBlockButton(Inventory inv, int slot, BlockShopItem item, Player player, Material currentBlock) {
        boolean hasBlock = item.getMaterial() == Material.SANDSTONE || plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), item.getMaterial());
        boolean isSelected = currentBlock == item.getMaterial();
        
        List<String> lore = new ArrayList<>();
        
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
        
        if (item.getMaterial() == Material.SANDSTONE) {
            lore.add("&aBloque por defecto");
            lore.add("&8➥ Siempre disponible");
            if (isSelected) {
                lore.add("");
                lore.add("&aSeleccionado actualmente");
            } else {
                lore.add("");
                lore.add("&eClick para seleccionar");
            }
        } else if (hasBlock) {
            if (isSelected) {
                lore.add("&aSeleccionado actualmente");
                lore.add("&8➥ Usando este bloque");
            } else {
                lore.add("&eClick para seleccionar");
                lore.add("&8➥ Ya posees este bloque");
            }
        } else {
            lore.add("&7Click para comprar");
            lore.add("");
            lore.add("&8➥ Precio: &e" + item.getPrice() + " KGCoins");
        }

        String displayName = (isSelected ? "&b" : item.getRarityColor()) + item.getName();
        ItemStack buttonItem = createItem(item.getMaterial(), displayName, lore.toArray(new String[0]));
        
        // Añadir encantamiento visual si es el bloque seleccionado
        if (isSelected) {
            buttonItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            // Ocultar el texto del encantamiento
            ItemMeta meta = buttonItem.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            buttonItem.setItemMeta(meta);
        }

        inv.setItem(slot, buttonItem);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (event.getSlot() == 39) {
            plugin.getMenuManager().openMenu(player, "block_categories");
            return;
        }

        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "shop");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        BlockShopItem shopItem = findShopItem(clicked.getType());
        if (shopItem == null) return;

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        if (plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), shopItem.getMaterial())) {
            plugin.getCosmeticManager().setPlayerBlock(player.getUniqueId(), shopItem.getMaterial());
            player.sendMessage(MessageUtils.getColor("&aHas seleccionado el bloque de " + shopItem.getName()));
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= shopItem.getPrice()) {
                stats.removeKGCoins(shopItem.getPrice());
                plugin.getCosmeticManager().addPlayerBlock(player.getUniqueId(), shopItem.getMaterial());
                plugin.getCosmeticManager().setPlayerBlock(player.getUniqueId(), shopItem.getMaterial());
                player.sendMessage(MessageUtils.getColor("&a¡Has comprado y seleccionado el bloque de " + 
                    shopItem.getName() + " &apor &e" + shopItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor("&cNo tienes suficientes KGCoins para comprar este bloque."));
            }
        }
        
        // Verificación especial para Bedrock
        if (shopItem.getMaterial() == Material.BEDROCK) {
            if (!hasAllBlocks(player.getUniqueId())) {
                player.sendMessage(MessageUtils.getColor("&c¡Necesitas desbloquear todos los demás bloques primero!"));
                return;
            }
        }
    }

    private BlockShopItem findShopItem(Material material) {
        return shopItems.stream()
            .filter(item -> item.getMaterial() == material)
            .findFirst()
            .orElse(null);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, name, (byte) 0, lore);
    }

    private ItemStack createItem(Material material, String name, byte data, String... lore) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        
        if (lore.length > 0) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(MessageUtils.getColor(line));
            }
            meta.setLore(coloredLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private boolean hasAllBlocks(UUID uuid) {
        int totalBlocks = (int) shopItems.stream()
            .filter(item -> item.getMaterial() != Material.BEDROCK)
            .count();
            
        int ownedBlocks = (int) shopItems.stream()
            .filter(item -> item.getMaterial() != Material.BEDROCK)
            .filter(item -> plugin.getCosmeticManager().hasPlayerBlock(uuid, item.getMaterial()))
            .count();
            
        return totalBlocks == ownedBlocks;
    }
}
