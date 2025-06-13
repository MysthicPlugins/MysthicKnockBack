package mk.kvlzx.menu;

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

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.KnockerShopItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class KnockerShopMenu extends Menu {
    private final List<KnockerShopItem> shopItems;
    private static String currentCategory = "COMMON";

    public KnockerShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lKnocker Shop &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<KnockerShopItem> initializeShopItems() {
        List<KnockerShopItem> items = new ArrayList<>();
        
        addCommonKnockers(items);
        addUncommonKnockers(items);
        addRareKnockers(items);
        addEpicKnockers(items);
        addLegendaryKnockers(items);
        
        return items;
    }

    private void addCommonKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.STICK, "Default Stick", 0, "COMMON", "&7", "&8The classic stick of all time"));
        items.add(new KnockerShopItem(Material.BONE, "Knockout Bone", 5000, "COMMON", "&7", "&eDogs love it!"));
        items.add(new KnockerShopItem(Material.WOOD_SPADE, "Wooden Shovel", 5000, "COMMON", "&7", "&6Digging your victory!"));
        items.add(new KnockerShopItem(Material.BLAZE_ROD, "Blaze Rod", 5000, "COMMON", "&7", "&cHot to the touch"));
        items.add(new KnockerShopItem(Material.CARROT_STICK, "Carrot on a Stick", 5000, "COMMON", "&7", "&6Rabbits' favorite!"));
    }

    private void addUncommonKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.GOLD_HOE, "Golden Hoe", 15000, "UNCOMMON", "&a", "&eShines like the sun!"));
        items.add(new KnockerShopItem(Material.IRON_SPADE, "Iron Shovel", 15000, "UNCOMMON", "&a", "&7Forged in the mountains"));
        items.add(new KnockerShopItem(Material.STONE_HOE, "Stone Hoe", 15000, "UNCOMMON", "&a", "&8As tough as rock"));
        items.add(new KnockerShopItem(Material.WOOD_HOE, "Wooden Hoe", 15000, "UNCOMMON", "&a", "&6Hand-carved"));
        items.add(new KnockerShopItem(Material.WOOD_SWORD, "Wooden Sword", 15000, "UNCOMMON", "&a", "&6Perfect for training!"));
    }

    private void addRareKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.DIAMOND_HOE, "Diamond Hoe", 50000, "RARE", "&9", "&bThe brightest of all!"));
        items.add(new KnockerShopItem(Material.GOLD_SWORD, "Golden Sword", 50000, "RARE", "&9", "&eThe weapon of kings!"));
        items.add(new KnockerShopItem(Material.IRON_SWORD, "Iron Sword", 50000, "RARE", "&9", "&7Sharp as none"));
        items.add(new KnockerShopItem(Material.DIAMOND_SPADE, "Diamond Shovel", 50000, "RARE", "&9", "&bDigging in style"));
        items.add(new KnockerShopItem(Material.GOLD_SPADE, "Golden Shovel", 50000, "RARE", "&9", "&eGleaming like gold!"));
    }

    private void addEpicKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.DIAMOND_SWORD, "Diamond Sword", 500000, "EPIC", "&5", "&bThe most powerful of all!"));
        items.add(new KnockerShopItem(Material.IRON_AXE, "Iron Axe", 500000, "EPIC", "&5", "&7Cuts like butter!"));
        items.add(new KnockerShopItem(Material.DIAMOND_AXE, "Diamond Axe", 500000, "EPIC", "&5", "&bThe ultimate destroyer!"));
        items.add(new KnockerShopItem(Material.GOLD_AXE, "Golden Axe", 500000, "EPIC", "&5", "&eWorthy of a king!"));
    }

    private void addLegendaryKnockers(List<KnockerShopItem> items) {
        items.add(new KnockerShopItem(Material.NETHER_STAR, "Nether Star", 1000000, "LEGENDARY", "&6", "&c&lThe power of the Wither!"));
        items.add(new KnockerShopItem(Material.GHAST_TEAR, "Ghast Tear", 1000000, "LEGENDARY", "&6", "&f&lTears of power!"));
        items.add(new KnockerShopItem(Material.PRISMARINE_SHARD, "Prismarine Shard", 1000000, "LEGENDARY", "&6", "&3&lThe strength of the ocean!"));
        items.add(new KnockerShopItem(Material.EMERALD, "Emerald of Power", 1000000, "LEGENDARY", "&6", "&a&lWealth is power!"));
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current Balance: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Current Category: " + currentCategory));

        // Mostrar knockers
        int slot = 10;
        for (KnockerShopItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupKnockerButton(inv, slot, item, player, plugin.getCosmeticManager().getPlayerKnocker(player.getUniqueId()));
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to categories"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
    }

    private void setupKnockerButton(Inventory inv, int slot, KnockerShopItem item, Player player, Material currentKnocker) {
        boolean hasKnocker = item.getMaterial() == Material.STICK || plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), item.getMaterial());
        boolean isSelected = currentKnocker == item.getMaterial();
        
        List<String> lore = new ArrayList<>();
        
        lore.add(item.getRarityColor() + "✦ Rarity: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getDescription()));
        lore.add("");
        
        if (item.getMaterial() == Material.STICK && item.getData() == 0) {
            lore.add("&aDefault Knocker");
            lore.add("&8➥ Always available");
            if (isSelected) {
                lore.add("");
                lore.add("&aCurrently selected");
            } else {
                lore.add("");
                lore.add("&eClick to select");
            }
        } else if (hasKnocker) {
            if (isSelected) {
                lore.add("&aCurrently selected");
                lore.add("&8➥ Using this knocker");
            } else {
                lore.add("&eClick to select");
                lore.add("&8➥ You already own this knocker");
            }
        } else {
            lore.add("&7Click to purchase");
            lore.add("");
            lore.add("&8➥ Price: &e" + item.getPrice() + " KGCoins");
        }

        String displayName = (isSelected ? "&b" : item.getRarityColor()) + item.getName();
        ItemStack buttonItem = createItem(item.getMaterial(), displayName, item.getData(), lore.toArray(new String[0]));
        
        if (isSelected) {
            buttonItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
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
            plugin.getMenuManager().openMenu(player, "knocker_categories");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        KnockerShopItem shopItem = findShopItem(clicked);
        if (shopItem == null) return;

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Si ya tiene el knocker o es el palo por defecto
        if (plugin.getCosmeticManager().hasPlayerKnocker(player.getUniqueId(), shopItem.getMaterial())) {
            plugin.getCosmeticManager().setPlayerKnocker(player.getUniqueId(), shopItem.getMaterial());
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&aYou have selected the " + shopItem.getName() + " knocker"));
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= shopItem.getPrice()) {
                stats.removeKGCoins(shopItem.getPrice());
                plugin.getCosmeticManager().addPlayerKnocker(player.getUniqueId(), shopItem.getMaterial());
                plugin.getCosmeticManager().setPlayerKnocker(player.getUniqueId(), shopItem.getMaterial());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&aYou have purchased and selected the " + 
                    shopItem.getName() + " knocker &afor &e" + shopItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cYou don't have enough KGCoins to purchase this knocker."));
            }
        }
    }

    private KnockerShopItem findShopItem(ItemStack clicked) {
        return shopItems.stream()
            .filter(item -> item.getMaterial() == clicked.getType() && item.getData() == clicked.getDurability())
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