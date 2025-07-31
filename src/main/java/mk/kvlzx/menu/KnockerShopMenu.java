package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.KnockersShopConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class KnockerShopMenu extends Menu {
    private final KnockersShopConfig config;

    public KnockerShopMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getKnockersShopConfig().getMenuTitle(), plugin.getKnockersShopConfig().getMenuSize());
        this.config = plugin.getKnockersShopConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance item
        setupBalanceItem(inv, stats);

        // Setup knocker items (ordenados por rareza)
        setupKnockerItems(inv, player);

        // Back button
        setupBackButton(inv);

        // Fill empty slots with filler items
        if (config.isFillEmptySlots()) {
            fillEmptySlots(inv);
        }
    }

    private void setupBalanceItem(Inventory inv, PlayerStats stats) {
        List<String> balanceLore = new ArrayList<>();
        for (String line : config.getBalanceLore()) {
            balanceLore.add(line.replace("%balance%", String.valueOf(stats.getKGCoins())));
        }
        
        ItemStack balanceItem = config.createMenuItem(
            config.getBalanceMaterial(), 
            config.getBalanceTitle(), 
            0,
            balanceLore
        );
        
        inv.setItem(config.getBalanceSlot(), balanceItem);
    }

    private void setupKnockerItems(Inventory inv, Player player) {
        Material currentKnocker = plugin.getCosmeticManager().getPlayerKnocker(player.getUniqueId());
        List<Integer> availableSlots = config.getKnockerSlots();
        
        // Ordenar knockers por rareza
        List<Map.Entry<String, KnockersShopConfig.KnockerItem>> sortedKnockers = getSortedKnockersByRarity();
        
        int slotIndex = 0;
        for (Map.Entry<String, KnockersShopConfig.KnockerItem> entry : sortedKnockers) {
            if (slotIndex >= availableSlots.size()) break;
            
            String knockerKey = entry.getKey();
            KnockersShopConfig.KnockerItem knockerItem = entry.getValue();
            Material knockerMaterial = knockerItem.getMaterial();
            
            if (knockerMaterial != null) {
                int slot = availableSlots.get(slotIndex);
                setupKnockerButton(inv, slot, knockerKey, knockerItem, knockerMaterial, player, currentKnocker);
                slotIndex++;
            }
        }
    }

    /**
     * Ordena los knockers por rareza siguiendo el orden: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
     */
    private List<Map.Entry<String, KnockersShopConfig.KnockerItem>> getSortedKnockersByRarity() {
        Map<String, KnockersShopConfig.KnockerItem> knockerItems = config.getKnockerItems();
        
        // Define the rarity order
        Map<String, Integer> rarityOrder = new HashMap<>();
        rarityOrder.put("COMMON", 1);
        rarityOrder.put("UNCOMMON", 2);
        rarityOrder.put("RARE", 3);
        rarityOrder.put("EPIC", 4);
        rarityOrder.put("LEGENDARY", 5);
        
        // Convert the map to a list and sort it
        List<Map.Entry<String, KnockersShopConfig.KnockerItem>> sortedList = new ArrayList<>(knockerItems.entrySet());
        
        sortedList.sort((entry1, entry2) -> {
            KnockersShopConfig.KnockerItem item1 = entry1.getValue();
            KnockersShopConfig.KnockerItem item2 = entry2.getValue();
            
            // First compare by rarity
            int rarity1 = rarityOrder.getOrDefault(item1.getRarity(), 999);
            int rarity2 = rarityOrder.getOrDefault(item2.getRarity(), 999);
            
            if (rarity1 != rarity2) {
                return Integer.compare(rarity1, rarity2);
            }
            
            // If rarity is the same, compare by price
            if (item1.getPrice() != item2.getPrice()) {
                return Integer.compare(item1.getPrice(), item2.getPrice());
            }
            
            // If rarity and price are the same, compare by name
            return item1.getName().compareToIgnoreCase(item2.getName());
        });
        
        return sortedList;
    }

    private void setupKnockerButton(Inventory inv, int slot, String knockerKey, KnockersShopConfig.KnockerItem knockerItem, 
                                    Material knockerMaterial, Player player, Material currentKnocker) {
        
        boolean hasKnocker = knockerItem.isDefault() || plugin.getCosmeticManager().hasPlayerKnocker(player.getUniqueId(), knockerMaterial);
        boolean isSelected = currentKnocker == knockerMaterial;
        
        // Determine the status of the knocker
        String statusKey = determineKnockerStatus(knockerItem, hasKnocker, isSelected, player);
        List<String> statusLore = config.getStatusMessage(statusKey);
        
        // Build the final lore for the knocker item
        List<String> finalLore = buildKnockerLore(knockerItem, statusLore, player);
        
        // Create the title for the knocker item
        String title = config.getKnockerTitle()
            .replace("%rarity_color%", knockerItem.getRarityColor())
            .replace("%knocker_name%", knockerItem.getName());
        
        // Create the item for the knocker
        ItemStack knockerItemStack = config.createMenuItem(knockerItem.getMaterial().name(), title, knockerItem.getData(), finalLore);
        
        // Add enchantments if the knocker is selected
        if (isSelected && config.isEnchantedIfSelected()) {
            knockerItemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            if (config.isHideEnchants()) {
                ItemMeta meta = knockerItemStack.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                knockerItemStack.setItemMeta(meta);
            }
        }

        inv.setItem(slot, knockerItemStack);
    }

    private String determineKnockerStatus(KnockersShopConfig.KnockerItem knockerItem, boolean hasKnocker, 
                                        boolean isSelected, Player player) {
        
        if (knockerItem.isDefault()) {
            return isSelected ? "default_selected" : "default_click_to_select";
        } else if (hasKnocker) {
            return isSelected ? "owned_selected" : "owned_click_to_select";
        } else {
            PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
            return stats.getKGCoins() < knockerItem.getPrice() ? "insufficient_funds" : "purchasable";
        }
    }

    private List<String> buildKnockerLore(KnockersShopConfig.KnockerItem knockerItem, List<String> statusLore, Player player) {
        List<String> finalLore = new ArrayList<>();
        
        for (String line : config.getKnockerLore()) {
            String processedLine = line
                .replace("%rarity_color%", knockerItem.getRarityColor())
                .replace("%rarity%", knockerItem.getRarity())
                .replace("%knocker_lore%", knockerItem.getLore())
                .replace("%knocker_name%", knockerItem.getName());
            
            if (processedLine.contains("%status_lore%")) {
                for (String statusLine : statusLore) {
                    String processedStatusLine = statusLine
                        .replace("%price%", String.valueOf(knockerItem.getPrice()))
                        .replace("%balance%", String.valueOf(PlayerStats.getStats(player.getUniqueId()).getKGCoins()))
                        .replace("%knocker_name%", knockerItem.getName());
                    finalLore.add(processedStatusLine);
                }
            } else {
                finalLore.add(processedLine);
            }
        }
        
        return finalLore;
    }

    private void setupBackButton(Inventory inv) {
        ItemStack backButton = config.createMenuItem(
            config.getBackButtonMaterial(),
            config.getBackButtonTitle(),
            0,
            config.getBackButtonLore()
        );
        
        inv.setItem(config.getBackButtonSlot(), backButton);
    }

    private void fillEmptySlots(Inventory inv) {
        Material fillerMaterial;
        try {
            fillerMaterial = Material.valueOf(config.getFillerMaterial().toUpperCase());
        } catch (IllegalArgumentException e) {
            fillerMaterial = Material.STAINED_GLASS_PANE;
        }
        
        ItemStack filler = new ItemStack(fillerMaterial, 1, (short) config.getFillerData());
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(config.getFillerTitle()));
        filler.setItemMeta(meta);
        
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!isValidClick(event)) {
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        // Back button
        if (event.getSlot() == config.getBackButtonSlot()) {
            plugin.getMenuManager().openMenu(player, "shop");
            return;
        }

        // Ignore clicks on special items
        if (clicked == null || 
            clicked.getType().name().equals(config.getFillerMaterial()) || 
            clicked.getType().name().equals(config.getBalanceMaterial())) {
            return;
        }

        // Find the clicked knocker
        String clickedKnockerKey = findKnockerKeyFromMaterial(clicked.getType(), clicked.getDurability());
        if (clickedKnockerKey == null) return;
        
        KnockersShopConfig.KnockerItem knockerItem = config.getKnockerItem(clickedKnockerKey);
        if (knockerItem == null) return;
        
        Material knockerMaterial = knockerItem.getMaterial();
        if (knockerMaterial == null) return;

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        if (knockerItem.isDefault() || plugin.getCosmeticManager().hasPlayerKnocker(player.getUniqueId(), knockerMaterial)) {
            // Select knocker
            plugin.getCosmeticManager().setPlayerKnocker(player.getUniqueId(), knockerMaterial);
            String message = config.getKnockerSelectedMessage().replace("%knocker_name%", knockerItem.getName());
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            player.closeInventory();
        } else {
            // Purchase knocker
            if (stats.getKGCoins() >= knockerItem.getPrice()) {
                stats.removeKGCoins(knockerItem.getPrice());
                plugin.getCosmeticManager().addPlayerKnocker(player.getUniqueId(), knockerMaterial);
                plugin.getCosmeticManager().setPlayerKnocker(player.getUniqueId(), knockerMaterial);
                
                String message = config.getKnockerPurchasedMessage()
                    .replace("%knocker_name%", knockerItem.getName())
                    .replace("%price%", String.valueOf(knockerItem.getPrice()));
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + config.getInsufficientFundsMessage()));
            }
        }
    }

    private String findKnockerKeyFromMaterial(Material material, short data) {
        for (Map.Entry<String, KnockersShopConfig.KnockerItem> entry : config.getKnockerItems().entrySet()) {
            KnockersShopConfig.KnockerItem item = entry.getValue();
            if (item.getMaterial() == material && item.getData() == data) {
                return entry.getKey();
            }
        }
        return null;
    }
}