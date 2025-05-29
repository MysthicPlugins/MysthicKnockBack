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
        items.add(new BlockShopItem(Material.GLASS, "Vidrio", 1000, "COMÚN", "&7"));
        items.add(new BlockShopItem(Material.WOOD, "Madera", 1000, "COMÚN", "&7"));
        items.add(new BlockShopItem(Material.SANDSTONE, "Arenisca", 1000, "COMÚN", "&7"));
        
        // Poco común - Color verde - Precio medio
        items.add(new BlockShopItem(Material.SMOOTH_BRICK, "Ladrillos de Piedra", 2500, "POCO COMÚN", "&a"));
        items.add(new BlockShopItem(Material.STAINED_CLAY, "Arcilla Teñida", 2500, "POCO COMÚN", "&a"));
        items.add(new BlockShopItem(Material.WOOL, "Lana", 2500, "POCO COMÚN", "&a"));
        
        // Raro - Color azul - Precio alto
        items.add(new BlockShopItem(Material.QUARTZ_BLOCK, "Bloque de Cuarzo", 5000, "RARO", "&9"));
        items.add(new BlockShopItem(Material.SNOW_BLOCK, "Bloque de Nieve", 5000, "RARO", "&9"));
        items.add(new BlockShopItem(Material.PACKED_ICE, "Hielo Compacto", 5000, "RARO", "&9"));
        
        // Épico - Color morado - Precio muy alto
        items.add(new BlockShopItem(Material.PRISMARINE, "Prismarina", 7500, "ÉPICO", "&5"));
        items.add(new BlockShopItem(Material.SEA_LANTERN, "Linterna Marina", 7500, "ÉPICO", "&5"));
        items.add(new BlockShopItem(Material.GLOWSTONE, "Piedra Luminosa", 7500, "ÉPICO", "&5"));
        
        // Legendario - Color dorado - Precio extremo
        items.add(new BlockShopItem(Material.ENDER_STONE, "Piedra del End", 10000, "LEGENDARIO", "&6"));
        items.add(new BlockShopItem(Material.NETHER_BRICK, "Ladrillo del Nether", 10000, "LEGENDARIO", "&6"));
        
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
        lore.add("");
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
