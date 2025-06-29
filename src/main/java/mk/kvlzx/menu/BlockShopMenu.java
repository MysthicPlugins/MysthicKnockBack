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
    private static String currentCategory = "COMMON";

    public BlockShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lBlock Shop &8•", 45);
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
        
        items.add(new BlockShopItem(Material.BEDROCK, "Bedrock", 50000, "SPECIAL", "&4", 
            "&4&lUnlocked by obtaining all other blocks"));
        
        return items;
    }

    private void addCommonBlocks(List<BlockShopItem> items) {
        // Bloques básicos
        items.add(new BlockShopItem(Material.SANDSTONE, "Sandstone", 0, "COMMON", "&7", "&7Shaped by the desert winds"));
        items.add(new BlockShopItem(Material.WOOD, "Oak Wood", 1000, "COMMON", "&7", "&6Carved from the oldest tree in the forest"));
        items.add(new BlockShopItem(Material.WOOL, "White Wool", 1000, "COMMON", "&7", "&fWoven from the purest fleece"));
        items.add(new BlockShopItem(Material.MELON_BLOCK, "Melon Block", 1000, "COMMON", "&7", "&2Sweet and refreshing"));
        items.add(new BlockShopItem(Material.PUMPKIN, "Pumpkin", 1000, "COMMON", "&7", "&6Carved on Halloween night"));
    }

    private void addUncommonBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.SMOOTH_BRICK, "Stone Bricks", 2500, "UNCOMMON", "&a", "&7Carved by master masons"));
        items.add(new BlockShopItem(Material.MOSSY_COBBLESTONE, "Mossy Cobblestone", 2500, "UNCOMMON", "&a", "&2Covered by the passage of time"));
        items.add(new BlockShopItem(Material.NETHERRACK, "Netherrack", 2500, "UNCOMMON", "&a", "&cBurning to the touch"));
        items.add(new BlockShopItem(Material.COAL_ORE, "Coal Ore", 2500, "UNCOMMON", "&a", "&8Mined from the depths of the earth"));
        items.add(new BlockShopItem(Material.HAY_BLOCK, "Hay Block", 2500, "UNCOMMON", "&a", "&eHarvested from golden fields"));
        items.add(new BlockShopItem(Material.IRON_BLOCK, "Iron Block", 2500, "UNCOMMON", "&a", "&7Forged from pure metal"));
    }

    private void addRareBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.QUARTZ_BLOCK, "Quartz Block", 5000, "RARE", "&9", "&fMined from the deepest Nether"));
        items.add(new BlockShopItem(Material.PACKED_ICE, "Packed Ice", 5000, "RARE", "&9", "&bFrozen by ice dragons"));
        items.add(new BlockShopItem(Material.COAL_BLOCK, "Coal Block", 5000, "RARE", "&9", "&8Compressed over millennia"));
        items.add(new BlockShopItem(Material.LAPIS_BLOCK, "Lapis Lazuli Block", 5000, "RARE", "&9", "&9Imbued with ancient magic"));
        items.add(new BlockShopItem(Material.CLAY, "Clay Block", 5000, "RARE", "&9", "&fMolded by the gods"));
        items.add(new BlockShopItem(Material.BOOKSHELF, "Bookshelf", 5000, "RARE", "&9", "&6Holds ancient secrets"));
    }

    // Agregar nueva categoría de bloques troll
    private void addTrollBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.GLASS, "Mystic Glass", 15000, "TROLL", "&d", "&fAs fragile as it is deceptive"));
        items.add(new BlockShopItem(Material.STAINED_GLASS, "Void Glass", 15000, "TROLL", "&d", "&8The darkness calls you"));
        items.add(new BlockShopItem(Material.NOTE_BLOCK, "Music Block", 15000, "TROLL", "&d", "&eResonates with ancient melodies"));
        items.add(new BlockShopItem(Material.CHEST, "Chaos Chest", 15000, "TROLL", "&d", "&6What's inside?"));
        items.add(new BlockShopItem(Material.TRAPPED_CHEST, "Trapped Chest", 15000, "TROLL", "&d", "&cBeware when opening"));
        items.add(new BlockShopItem(Material.ENDER_PORTAL_FRAME, "Void Frame", 15000, "TROLL", "&d", "&5Portal to nothingness"));
        items.add(new BlockShopItem(Material.JUKEBOX, "Jukebox", 15000, "TROLL", "&d", "&eMelodies from beyond"));
        items.add(new BlockShopItem(Material.ANVIL, "Ancestral Anvil", 15000, "TROLL", "&d", "&7Forged by titans"));
        items.add(new BlockShopItem(Material.HOPPER, "Void Hopper", 15000, "TROLL", "&d", "&8Absorbs everything in its path"));
        items.add(new BlockShopItem(Material.DISPENSER, "Chaos Dispenser", 15000, "TROLL", "&d", "&cFires surprises"));
    }

    private void addEpicBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.PRISMARINE, "Prismarine", 7500, "EPIC", "&5", "&3Treasure from the ocean depths"));
        items.add(new BlockShopItem(Material.COAL_BLOCK, "Coal Block", 7500, "EPIC", "&5", "&3Forged in the heart of a volcano"));
        items.add(new BlockShopItem(Material.ENDER_STONE, "End Stone", 7500, "EPIC", "&5", "&fForged in the lands of the void"));
        items.add(new BlockShopItem(Material.SPONGE, "Ancestral Sponge", 7500, "EPIC", "&5", "&eAbsorbs the essence of the ocean"));
        items.add(new BlockShopItem(Material.SEA_LANTERN, "Sea Lantern", 7500, "EPIC", "&5", "&bGlows with light from the depths"));
        items.add(new BlockShopItem(Material.GLOWSTONE, "Glowstone", 7500, "EPIC", "&5", "&eEmits eternal light"));
    }

    private void addLegendaryBlocks(List<BlockShopItem> items) {
        items.add(new BlockShopItem(Material.EMERALD_BLOCK, "Emerald Block", 10000, "LEGENDARY", "&6", "&aSparkles with pure wealth"));
        items.add(new BlockShopItem(Material.DIAMOND_BLOCK, "Diamond Block", 10000, "LEGENDARY", "&6", "&bCrystallized over eons"));
        items.add(new BlockShopItem(Material.GOLD_BLOCK, "Gold Block", 10000, "LEGENDARY", "&6", "&eForged by ancient gods"));
        items.add(new BlockShopItem(Material.OBSIDIAN, "Obsidian", 10000, "LEGENDARY", "&6", "&8Born from primordial fire"));
        items.add(new BlockShopItem(Material.NETHER_BRICK, "Nether Brick", 10000, "LEGENDARY", "&6", "&cCreated in eternal hell"));
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current Balance: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Current Category: " + currentCategory));

        // Si es la categoría ESPECIAL (Bedrock), mostrar en el centro
        if (currentCategory.equals("SPECIAL")) {
            for (BlockShopItem item : shopItems) {
                if (item.getRarity().equals("SPECIAL")) {
                    setupBlockButton(inv, 22, item, player, plugin.getCosmeticManager().getPlayerBlock(player.getUniqueId()));
                    break;
                }
            }
        } else {
            // Para otras categorías, mantener el layout original
            int slot = 10;
            for (BlockShopItem item : shopItems) {
                if (item.getRarity().equals(currentCategory)) {
                    if (slot > 34) break;
                    setupBlockButton(inv, slot, item, player, plugin.getCosmeticManager().getPlayerBlock(player.getUniqueId()));
                    slot++;
                    if ((slot + 1) % 9 == 0) slot += 2;
                }
            }
        }

        // Botones de navegación
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to categories"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15);
        fillEmptySlots(inv, filler);
    }

    private void setupBlockButton(Inventory inv, int slot, BlockShopItem item, Player player, Material currentBlock) {
        boolean hasBlock = item.getMaterial() == Material.SANDSTONE || plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), item.getMaterial());
        boolean isSelected = currentBlock == item.getMaterial();
        
        List<String> lore = new ArrayList<>();
        
        lore.add(item.getRarityColor() + "✦ Rarity: " + item.getRarity());
        lore.add("");
        
        if (item.getMaterial() == Material.SANDSTONE) {
            lore.add("&aDefault Block");
            lore.add("&8➥ Always available");
            if (isSelected) {
                lore.add("");
                lore.add("&aCurrently selected");
            } else {
                lore.add("");
                lore.add("&eClick to select");
            }
        } else if (hasBlock) {
            if (isSelected) {
                lore.add("&aCurrently selected");
                lore.add("&8➥ Using this block");
            } else {
                lore.add("&eClick to select");
                lore.add("&8➥ You already own this block");
            }
        } else {
            lore.add("&7Click to purchase");
            lore.add("");
            lore.add("&8➥ Price: &e" + item.getPrice() + " KGCoins");
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
        // Validar que el click sea en el menú y no en el inventario del jugador
        if (!isValidClick(event)) {
            event.setCancelled(true);
            return;
        }
        
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
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou need to unlock all other blocks first!"));
                return;
            }
        }

        if (plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), shopItem.getMaterial())) {
            plugin.getCosmeticManager().setPlayerBlock(player.getUniqueId(), shopItem.getMaterial());
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have selected the " + shopItem.getName() + " block"));
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= shopItem.getPrice()) {
                stats.removeKGCoins(shopItem.getPrice());
                plugin.getCosmeticManager().addPlayerBlock(player.getUniqueId(), shopItem.getMaterial());
                plugin.getCosmeticManager().setPlayerBlock(player.getUniqueId(), shopItem.getMaterial());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have purchased and selected the " +
                    shopItem.getName() + " block &afor &e" + shopItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou don't have enough KGCoins to purchase this block."));
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