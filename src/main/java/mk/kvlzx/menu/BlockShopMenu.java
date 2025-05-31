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
        
        addCommonBlocks(items);
        addUncommonBlocks(items);
        addRareBlocks(items);
        addEpicBlocks(items);
        addLegendaryBlocks(items);
        addTrollBlocks(items);
        
        items.add(new BlockShopItem(Material.BEDROCK, "Bedrock", 50000, "ESPECIAL", "&4", 
            "&4&lDesbloqueado al obtener todos los demás bloques"));
        
        return items;
    }

    private void addCommonBlocks(List<BlockShopItem> items) {
        // Bloques básicos
        items.add(new BlockShopItem(Material.SANDSTONE, "Arenisca", 0, "COMÚN", "&7", "&7Moldeada por los vientos del desierto"));
        items.add(new BlockShopItem(Material.WOOD, "Madera de Roble", 1000, "COMÚN", "&7", "&6Tallada del árbol más antiguo del bosque"));
        items.add(new BlockShopItem(Material.WOOL, "Lana Blanca", 1000, "COMÚN", "&7", "&fTejida con el vellón más puro"));
        items.add(new BlockShopItem(Material.GLASS, "Vidrio", 1000, "COMÚN", "&7", "&7Forjado en las arenas más puras"));
        items.add(new BlockShopItem(Material.MELON_BLOCK, "Bloque de Melón", 1000, "COMÚN", "&7", "&2Dulce y refrescante"));
        items.add(new BlockShopItem(Material.PUMPKIN, "Calabaza", 1000, "COMÚN", "&7", "&6Tallada en la noche de brujas"));
    }

    private void addUncommonBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.SMOOTH_BRICK, "Ladrillos de Piedra", 2500, "POCO COMÚN", "&a", "&7Tallados por maestros canteros"));
        items.add(new BlockShopItem(Material.MOSSY_COBBLESTONE, "Piedra Musgosa", 2500, "POCO COMÚN", "&a", "&2Cubierta por el paso del tiempo"));
        items.add(new BlockShopItem(Material.NETHERRACK, "Piedra del Nether", 2500, "POCO COMÚN", "&a", "&cArdiente al tacto"));
        items.add(new BlockShopItem(Material.SOUL_SAND, "Arena de Almas", 2500, "POCO COMÚN", "&a", "&8Susurra secretos antiguos"));
        items.add(new BlockShopItem(Material.HAY_BLOCK, "Bloque de Heno", 2500, "POCO COMÚN", "&a", "&eCosechado en campos dorados"));
        items.add(new BlockShopItem(Material.IRON_BLOCK, "Bloque de Hierro", 2500, "POCO COMÚN", "&a", "&7Forjado con metal puro"));
    }

    private void addRareBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.QUARTZ_BLOCK, "Bloque de Cuarzo", 5000, "RARO", "&9", "&fExtraído del Nether más profundo"));
        items.add(new BlockShopItem(Material.PACKED_ICE, "Hielo Compacto", 5000, "RARO", "&9", "&bCongelado por dragones de hielo"));
        items.add(new BlockShopItem(Material.COAL_BLOCK, "Bloque de Carbón", 5000, "RARO", "&9", "&8Comprimido durante milenios"));
        items.add(new BlockShopItem(Material.LAPIS_BLOCK, "Bloque de Lapislázuli", 5000, "RARO", "&9", "&9Imbuido con magia antigua"));
        items.add(new BlockShopItem(Material.CLAY, "Bloque de Arcilla", 5000, "RARO", "&9", "&fMoldeado por los dioses"));
        items.add(new BlockShopItem(Material.BOOKSHELF, "Estantería", 5000, "RARO", "&9", "&6Guarda secretos antiguos"));
    }

    // Agregar nueva categoría de bloques troll
    private void addTrollBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.GLASS, "Vidrio Místico", 15000, "TROLL", "&d", "&fTan frágil como engañoso"));
        items.add(new BlockShopItem(Material.STAINED_GLASS, "Vidrio del Void", 15000, "TROLL", "&d", "&8La oscuridad te llama"));
        items.add(new BlockShopItem(Material.NOTE_BLOCK, "Bloque Musical", 15000, "TROLL", "&d", "&eResuena con melodías antiguas"));
        items.add(new BlockShopItem(Material.CHEST, "Cofre del Caos", 15000, "TROLL", "&d", "&6¿Qué habrá dentro?"));
        items.add(new BlockShopItem(Material.TRAPPED_CHEST, "Cofre Trampa", 15000, "TROLL", "&d", "&cCuidado al abrirlo"));
        items.add(new BlockShopItem(Material.ENDER_PORTAL_FRAME, "Marco del Void", 15000, "TROLL", "&d", "&5Portal a la nada"));
        items.add(new BlockShopItem(Material.JUKEBOX, "Caja Musical", 15000, "TROLL", "&d", "&eMelodías del más allá"));
        items.add(new BlockShopItem(Material.ANVIL, "Yunque Ancestral", 15000, "TROLL", "&d", "&7Forjado por titanes"));
        items.add(new BlockShopItem(Material.HOPPER, "Tolva del Vacío", 15000, "TROLL", "&d", "&8Absorbe todo a su paso"));
        items.add(new BlockShopItem(Material.DISPENSER, "Dispensador del Caos", 15000, "TROLL", "&d", "&cDispara sorpresas"));
    }

    private void addEpicBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.PRISMARINE, "Prismarina", 7500, "ÉPICO", "&5", "&3Tesoro de las profundidades marinas"));
        items.add(new BlockShopItem(Material.PRISMARINE, (byte)2, "Prismarina Oscura", 7500, "ÉPICO", "&5", "&3Forjada en las profundidades abisales"));
        items.add(new BlockShopItem(Material.ENDER_STONE, "Piedra del End", 7500, "ÉPICO", "&5", "&fForjada en las tierras del vacío"));
        items.add(new BlockShopItem(Material.SPONGE, "Esponja Ancestral", 7500, "ÉPICO", "&5", "&eAbsorbe la esencia del océano"));
        items.add(new BlockShopItem(Material.SEA_LANTERN, "Linterna Marina", 7500, "ÉPICO", "&5", "&bBrilla con luz de las profundidades"));
        items.add(new BlockShopItem(Material.GLOWSTONE, "Piedra Luminosa", 7500, "ÉPICO", "&5", "&eEmana luz eterna"));
    }

    private void addLegendaryBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.EMERALD_BLOCK, "Bloque de Esmeralda", 10000, "LEGENDARIO", "&6", "&aDestella con riqueza pura"));
        items.add(new BlockShopItem(Material.DIAMOND_BLOCK, "Bloque de Diamante", 10000, "LEGENDARIO", "&6", "&bCristalizado por eones"));
        items.add(new BlockShopItem(Material.GOLD_BLOCK, "Bloque de Oro", 10000, "LEGENDARIO", "&6", "&eForjado por dioses antiguos"));
        items.add(new BlockShopItem(Material.OBSIDIAN, "Obsidiana", 10000, "LEGENDARIO", "&6", "&8Nacida del fuego primordial"));
        items.add(new BlockShopItem(Material.NETHER_BRICK, "Ladrillo del Nether", 10000, "LEGENDARIO", "&6", "&cCreado en el infierno eterno"));
    }

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
        inv.setItem(40, createItem(Material.ARROW, "&c← Volver", 
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

        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "block_categories");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        BlockShopItem shopItem = findShopItem(clicked.getType());
        if (shopItem == null) return;

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Verificación especial para Bedrock
        if (shopItem.getMaterial() == Material.BEDROCK) {
            if (!hasAllBlocks(player.getUniqueId())) {
                player.sendMessage(MessageUtils.getColor("&c¡Necesitas desbloquear todos los demás bloques primero!"));
                return;
            }
        }

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
