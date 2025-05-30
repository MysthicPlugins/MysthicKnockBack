package kk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.cosmetics.BlockShopItem;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;

public class BlockShopMenu extends Menu {
    private final List<BlockShopItem> shopItems;

    public BlockShopMenu(KvKnockback plugin) {
        super(plugin, "&8• &e&lTienda de Bloques &8•", 45);
        this.shopItems = initializeShopItems();
    }

    private List<BlockShopItem> initializeShopItems() {
        List<BlockShopItem> items = new ArrayList<>();
        
        // Común - Color gris - Precio bajo
        items.add(new BlockShopItem(Material.GLASS, "Vidrio", 1000, "COMÚN", "&7", "&bForjado en el aliento de un dragón celestial."));
        items.add(new BlockShopItem(Material.WOOD, "Madera", 1000, "COMÚN", "&7", "&6Tallada del árbol del mundo, Yggdrasil."));
        items.add(new BlockShopItem(Material.SANDSTONE, "Arenisca", 1000, "COMÚN", "&7", "&eMoldeada por los vientos del desierto infinito."));
        items.add(new BlockShopItem(Material.COBBLESTONE, "Piedra", 1000, "COMÚN", "&7", "&7Extraída del corazón de una montaña viviente."));
        items.add(new BlockShopItem(Material.BRICK, "Ladrillo", 1000, "COMÚN", "&7", "&cCocido en el fuego de un volcán sagrado."));
        
        // Poco común - Color verde - Precio medio
        items.add(new BlockShopItem(Material.SMOOTH_BRICK, "Ladrillos de Piedra", 2500, "POCO COMÚN", "&a", "&aPulido por las manos de un gigante de piedra."));
        items.add(new BlockShopItem(Material.STAINED_CLAY, "Arcilla Teñida", 2500, "POCO COMÚN", "&a", "&aTeñida con los colores del amanecer eterno."));
        items.add(new BlockShopItem(Material.WOOL, "Lana", 2500, "POCO COMÚN", "&a", "&aTejida con el vellón de un carnero celestial."));
        items.add(new BlockShopItem(Material.MOSSY_COBBLESTONE, "Piedra Musgosa", 2500, "POCO COMÚN", "&a", "&aCubierto por el musgo de un bosque sagrado."));
        items.add(new BlockShopItem(Material.CLAY, "Arcilla", 2500, "POCO COMÚN", "&a", "&aMoldeada en el lecho de un río divino."));
        
        // Raro - Color azul - Precio alto
        items.add(new BlockShopItem(Material.QUARTZ_BLOCK, "Bloque de Cuarzo", 5000, "RARO", "&9", "&9Forjado en las profundidades del Nether eterno."));
        items.add(new BlockShopItem(Material.SNOW_BLOCK, "Bloque de Nieve", 5000, "RARO", "&9", "&9Congelado en el aliento de un dragón de hielo."));
        items.add(new BlockShopItem(Material.PACKED_ICE, "Hielo Compacto", 5000, "RARO", "&9", "&9Hielo que nunca se derrite, sellado por magia."));
        items.add(new BlockShopItem(Material.EMERALD_BLOCK, "Bloque de Esmeralda", 5000, "RARO", "&9", "&9Forjado con gemas de un tesoro perdido."));
        items.add(new BlockShopItem(Material.DIAMOND_BLOCK, "Bloque de Diamante", 5000, "RARO", "&9", "&9Cristalizado bajo la presión de mil eras."));
        
        // Épico - Color morado - Precio muy alto
        items.add(new BlockShopItem(Material.PRISMARINE, "Prismarina", 7500, "ÉPICO", "&5", "&5Extraída del templo de un guardián abisal."));
        items.add(new BlockShopItem(Material.SEA_LANTERN, "Linterna Marina", 7500, "ÉPICO", "&5", "&5Ilumina con el brillo de un océano perdido."));
        items.add(new BlockShopItem(Material.GLOWSTONE, "Piedra Luminosa", 7500, "ÉPICO", "&5", "&5Brilla con el polvo de estrellas caídas."));
        items.add(new BlockShopItem(Material.OBSIDIAN, "Obsidiana", 7500, "ÉPICO", "&5", "&5Forjada en el cruce de lava y agua eterna."));
        items.add(new BlockShopItem(Material.GOLD_BLOCK, "Bloque de Oro", 7500, "ÉPICO", "&5", "&5Forjado con el oro de un rey inmortal."));
        
        // Legendario - Color dorado - Precio extremo
        items.add(new BlockShopItem(Material.ENDER_STONE, "Piedra del End", 10000, "LEGENDARIO", "&6", "&6Extraída del vacío del fin del mundo."));
        items.add(new BlockShopItem(Material.NETHER_BRICK, "Ladrillo del Nether", 10000, "LEGENDARIO", "&6", "&6Cocido en los fuegos del Nether eterno."));
        items.add(new BlockShopItem(Material.COAL_BLOCK, "Bloque de Carbón", 10000, "LEGENDARIO", "&6", "&6Forjado con el carbón de un bosque quemado."));
        items.add(new BlockShopItem(Material.REDSTONE_BLOCK, "Bloque de Redstone", 10000, "LEGENDARIO", "&6", "&6Pulsa con la energía de la tierra viva."));
        
        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance
        inv.setItem(4, createItem(Material.EMERALD, "&a&lTu Balance",
            "&7Balance actual: &e" + stats.getKGCoins() + " KGCoins"));

        // Bloques disponibles
        Material currentBlock = plugin.getCosmeticManager().getPlayerBlock(player.getUniqueId());
        int slot = 10;
        for (BlockShopItem item : shopItems) {
            if (slot > 34) break; // Prevenir overflow
            setupBlockButton(inv, slot, item, player, currentBlock);
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        // Comprobar si tiene todos los bloques para mostrar la bedrock
        boolean hasAllBlocks = true;
        for (BlockShopItem item : shopItems) {
            if (!plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), item.getMaterial())) {
                hasAllBlocks = false;
                break;
            }
        }

        // Añadir bedrock si tiene todos los bloques
        if (hasAllBlocks) {
            inv.setItem(31, createItem(Material.BEDROCK, "&4&l⚝ &8Bedrock &4&l⚝",
                "&7Bloque exclusivo por tener",
                "&7toda la colección completada",
                "",
                "&8➥ Click para seleccionar"));
        }

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Volver", "&7Click para volver a la tienda"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15);
        fillEmptySlots(inv, filler);
    }

    private void setupBlockButton(Inventory inv, int slot, BlockShopItem item, Player player, Material currentBlock) {
        boolean hasBlock = plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), item.getMaterial());
        boolean isSelected = currentBlock == item.getMaterial();
        
        List<String> lore = new ArrayList<>();
        
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
        
        if (hasBlock) {
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
            plugin.getMenuManager().openMenu(player, "shop");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        // Manejar click en bedrock
        if (clicked.getType() == Material.BEDROCK) {
            plugin.getCosmeticManager().setPlayerBlock(player.getUniqueId(), Material.BEDROCK);
            player.sendMessage(MessageUtils.getColor("&4&l⚝ &aHas seleccionado el bloque de &8Bedrock"));
            player.closeInventory();
            return;
        }

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
}
