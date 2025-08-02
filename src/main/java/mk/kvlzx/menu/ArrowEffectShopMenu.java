package mk.kvlzx.menu;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.ArrowEffectsShopConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import java.util.ArrayList;
import java.util.List;

public class ArrowEffectShopMenu extends Menu {
    private final ArrowEffectsShopConfig config;

    public ArrowEffectShopMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getArrowEffectsShopConfig().getMenuTitle(), plugin.getArrowEffectsShopConfig().getMenuSize());
        this.config = plugin.getArrowEffectsShopConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance item
        setupBalanceItem(inv, stats);

        // Setup arrow effect items
        setupArrowEffectItems(inv, player);

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
            balanceLore
        );
        
        inv.setItem(config.getBalanceSlot(), balanceItem);
    }

    private void setupArrowEffectItems(Inventory inv, Player player) {
        String currentEffect = plugin.getCosmeticManager().getPlayerArrowEffect(player.getUniqueId());
        List<Integer> availableSlots = config.getArrowEffectSlots();
        
        // Get effects sorted by rarity
        List<ArrowEffectsShopConfig.ArrowEffectItem> sortedEffects = config.getSortedArrowEffectsByRarity();
        
        int slotIndex = 0;
        for (ArrowEffectsShopConfig.ArrowEffectItem effectItem : sortedEffects) {
            if (slotIndex >= availableSlots.size()) break;
            
            int slot = availableSlots.get(slotIndex);
            setupArrowEffectButton(inv, slot, effectItem, player, currentEffect);
            slotIndex++;
        }
    }

    private void setupArrowEffectButton(Inventory inv, int slot, ArrowEffectsShopConfig.ArrowEffectItem effectItem, 
                                        Player player, String currentEffect) {
        
        boolean hasEffect = plugin.getCosmeticManager().hasPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
        boolean isSelected = currentEffect.equals(effectItem.getName());
        
        // Determine the status of the effect
        String statusKey = determineEffectStatus(effectItem, hasEffect, isSelected, player);
        List<String> statusLore = config.getStatusMessage(statusKey);
        
        // Build the final lore for the effect item
        List<String> finalLore = buildArrowEffectLore(effectItem, statusLore, player);
        
        // Create the title for the effect item
        String title = config.getArrowEffectTitle()
            .replace("%rarity_color%", effectItem.getRarityColor())
            .replace("%effect_name%", effectItem.getName());
        
        // Determine material based on selection status
        String materialId = isSelected ? config.getMaterialSelected() : config.getMaterialUnselected();
        
        // Create the item for the effect
        ItemStack effectItemStack = config.createMenuItem(materialId, title, finalLore);
        
        // Add enchantments if the effect is selected
        if (isSelected && config.isEnchantedIfSelected()) {
            effectItemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            if (config.isHideEnchants()) {
                ItemMeta meta = effectItemStack.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                effectItemStack.setItemMeta(meta);
            }
        }

        inv.setItem(slot, effectItemStack);
    }

    private String determineEffectStatus(ArrowEffectsShopConfig.ArrowEffectItem effectItem, boolean hasEffect, 
                                            boolean isSelected, Player player) {
        
        if (hasEffect) {
            return isSelected ? "owned_selected" : "owned_click_to_select";
        } else {
            PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
            return stats.getKGCoins() < effectItem.getPrice() ? "insufficient_funds" : "purchasable";
        }
    }

    private List<String> buildArrowEffectLore(ArrowEffectsShopConfig.ArrowEffectItem effectItem, 
                                                List<String> statusLore, Player player) {
        List<String> finalLore = new ArrayList<>();
        
        for (String line : config.getArrowEffectLore()) {
            String processedLine = line
                .replace("%rarity_color%", effectItem.getRarityColor())
                .replace("%rarity%", effectItem.getRarity())
                .replace("%effect_name%", effectItem.getName());
            
            if (processedLine.contains("%effect_description%")) {
                finalLore.add(MessageUtils.getColor(effectItem.getDescription()));
            } else if (processedLine.contains("%status_lore%")) {
                for (String statusLine : statusLore) {
                    String processedStatusLine = statusLine
                        .replace("%price%", String.valueOf(effectItem.getPrice()))
                        .replace("%balance%", String.valueOf(PlayerStats.getStats(player.getUniqueId()).getKGCoins()))
                        .replace("%effect_name%", effectItem.getName());
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
        // Validate that the click is in the menu and not in the player's inventory
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

        // Find the clicked arrow effect
        String clickedEffectName = findArrowEffectFromItem(clicked);
        if (clickedEffectName == null) return;
        
        ArrowEffectsShopConfig.ArrowEffectItem effectItem = findArrowEffectItemByName(clickedEffectName);
        if (effectItem == null) return;

        handleArrowEffectSelection(player, effectItem);
    }

    private String findArrowEffectFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }
        
        String displayName = MessageUtils.stripColor(item.getItemMeta().getDisplayName());
        
        // Search for the effect by name
        for (ArrowEffectsShopConfig.ArrowEffectItem effectItem : config.getArrowEffectItems().values()) {
            if (effectItem.getName().equals(displayName)) {
                return effectItem.getName();
            }
        }
        
        return null;
    }

    private ArrowEffectsShopConfig.ArrowEffectItem findArrowEffectItemByName(String name) {
        return config.getArrowEffectItems().values().stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    private void handleArrowEffectSelection(Player player, ArrowEffectsShopConfig.ArrowEffectItem effectItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentEffect = plugin.getCosmeticManager().getPlayerArrowEffect(player.getUniqueId());

        // If the player already owns the effect
        if (plugin.getCosmeticManager().hasPlayerArrowEffect(player.getUniqueId(), effectItem.getName())) {
            // If the effect is already selected, deselect it
            if (currentEffect.equals(effectItem.getName())) {
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), "none");
                String message = config.getEffectDeselectedMessage();
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            } else {
                // Select the effect
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                String message = config.getEffectSelectedMessage().replace("%effect_name%", effectItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            }
            player.closeInventory();
        } else {
            // Buy the effect
            if (stats.getKGCoins() >= effectItem.getPrice()) {
                stats.removeKGCoins(effectItem.getPrice());
                plugin.getCosmeticManager().addPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                
                String message = config.getEffectPurchasedMessage()
                    .replace("%effect_name%", effectItem.getName())
                    .replace("%price%", String.valueOf(effectItem.getPrice()));
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + config.getInsufficientFundsMessage()));
            }
        }
    }
}