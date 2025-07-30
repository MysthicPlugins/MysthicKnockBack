package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import mk.kvlzx.config.BlocksShopConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.BlockUtils;
import mk.kvlzx.utils.MessageUtils;

public class BlockShopMenu extends Menu {
    private final BlocksShopConfig config;

    public BlockShopMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getBlocksShopConfig().getMenuTitle(), plugin.getBlocksShopConfig().getMenuSize());
        this.config = plugin.getBlocksShopConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance item
        setupBalanceItem(inv, stats);

        // Setup block items
        setupBlockItems(inv, player);

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

    private void setupBlockItems(Inventory inv, Player player) {
        Material currentBlock = plugin.getCosmeticManager().getPlayerBlock(player.getUniqueId());
        List<Integer> availableSlots = config.getBlockSlots();
        Map<String, BlocksShopConfig.BlockItem> blockItems = config.getBlockItems();
        
        int slotIndex = 0;
        for (Map.Entry<String, BlocksShopConfig.BlockItem> entry : blockItems.entrySet()) {
            if (slotIndex >= availableSlots.size()) break;
            
            String blockKey = entry.getKey();
            BlocksShopConfig.BlockItem blockItem = entry.getValue();
            Material blockMaterial = BlockUtils.getMaterialFromKey(blockKey);
            
            if (blockMaterial != null) {
                int slot = availableSlots.get(slotIndex);
                setupBlockButton(inv, slot, blockKey, blockItem, blockMaterial, player, currentBlock);
                slotIndex++;
            }
        }
    }

    private void setupBlockButton(Inventory inv, int slot, String blockKey, BlocksShopConfig.BlockItem blockItem, 
                                    Material blockMaterial, Player player, Material currentBlock) {
        
        boolean hasBlock = blockItem.isDefault() || plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), blockMaterial);
        boolean isSelected = currentBlock == blockMaterial;
        
        // Determine the status of the block
        String statusKey = determineBlockStatus(blockItem, hasBlock, isSelected, player);
        List<String> statusLore = config.getStatusMessage(statusKey);
        
        // Build the final lore for the block item
        List<String> finalLore = buildBlockLore(blockItem, statusLore, player);
        
        // Create the title for the block item
        String title = config.getBlockTitle()
            .replace("%rarity_color%", blockItem.getRarityColor())
            .replace("%block_name%", blockItem.getName());
        
        if (isSelected) {
            title = "&b" + blockItem.getName();
        }
        
        // Create the item for the block
        ItemStack blockItemStack = config.createMenuItem(blockMaterial.name(), title, finalLore);
        
        // Add enchantments if the block is selected
        if (isSelected && config.isEnchantedIfSelected()) {
            blockItemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            if (config.isHideEnchants()) {
                ItemMeta meta = blockItemStack.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                blockItemStack.setItemMeta(meta);
            }
        }

        inv.setItem(slot, blockItemStack);
    }

    private String determineBlockStatus(BlocksShopConfig.BlockItem blockItem, boolean hasBlock, 
                                        boolean isSelected, Player player) {
        
        if (blockItem.isDefault()) {
            return isSelected ? "default_selected" : "default_click_to_select";
        } else if (hasBlock) {
            return isSelected ? "owned_selected" : "owned_click_to_select";
        } else {
            // Special verification for bedrock
            if (blockItem.hasSpecialRequirement() && "all_blocks".equals(blockItem.getSpecialRequirement())) {
                if (!hasAllBlocks(player.getUniqueId())) {
                    return "bedrock_locked";
                }
            }
            
            PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
            return stats.getKGCoins() < blockItem.getPrice() ? "insufficient_funds" : "purchasable";
        }
    }

    private List<String> buildBlockLore(BlocksShopConfig.BlockItem blockItem, List<String> statusLore, Player player) {
        List<String> finalLore = new ArrayList<>();
        
        for (String line : config.getBlockLore()) {
            String processedLine = line
                .replace("%rarity_color%", blockItem.getRarityColor())
                .replace("%rarity%", blockItem.getRarity())
                .replace("%block_lore%", blockItem.getLore())
                .replace("%block_name%", blockItem.getName());
            
            if (processedLine.contains("%status_lore%")) {
                for (String statusLine : statusLore) {
                    String processedStatusLine = statusLine
                        .replace("%price%", String.valueOf(blockItem.getPrice()))
                        .replace("%balance%", String.valueOf(PlayerStats.getStats(player.getUniqueId()).getKGCoins()))
                        .replace("%block_name%", blockItem.getName());
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

        // Find the clicked block
        String clickedBlockKey = findBlockKeyFromMaterial(clicked.getType());
        if (clickedBlockKey == null) return;
        
        BlocksShopConfig.BlockItem blockItem = config.getBlockItem(clickedBlockKey);
        if (blockItem == null) return;
        
        Material blockMaterial = BlockUtils.getMaterialFromKey(clickedBlockKey);
        if (blockMaterial == null) return;

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Special verification for bedrock
        if (blockItem.hasSpecialRequirement() && "all_blocks".equals(blockItem.getSpecialRequirement())) {
            if (!hasAllBlocks(player.getUniqueId())) {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + config.getBedrockLockedMessage()));
                return;
            }
        }

        if (blockItem.isDefault() || plugin.getCosmeticManager().hasPlayerBlock(player.getUniqueId(), blockMaterial)) {
            // Select block
            plugin.getCosmeticManager().setPlayerBlock(player.getUniqueId(), blockMaterial);
            String message = config.getBlockSelectedMessage().replace("%block_name%", blockItem.getName());
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            player.closeInventory();
        } else {
            // Purchase block
            if (stats.getKGCoins() >= blockItem.getPrice()) {
                stats.removeKGCoins(blockItem.getPrice());
                plugin.getCosmeticManager().addPlayerBlock(player.getUniqueId(), blockMaterial);
                plugin.getCosmeticManager().setPlayerBlock(player.getUniqueId(), blockMaterial);
                
                String message = config.getBlockPurchasedMessage()
                    .replace("%block_name%", blockItem.getName())
                    .replace("%price%", String.valueOf(blockItem.getPrice()));
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + config.getInsufficientFundsMessage()));
            }
        }
    }

    private String findBlockKeyFromMaterial(Material material) {
        return BlockUtils.getKeyFromMaterial(material);
    }

    private boolean hasAllBlocks(UUID uuid) {
        Map<String, BlocksShopConfig.BlockItem> allBlocks = config.getBlockItems();
        
        int totalBlocks = (int) allBlocks.values().stream()
            .filter(item -> !item.hasSpecialRequirement())
            .count();
            
        int ownedBlocks = 0;
        for (Map.Entry<String, BlocksShopConfig.BlockItem> entry : allBlocks.entrySet()) {
            BlocksShopConfig.BlockItem item = entry.getValue();
            if (!item.hasSpecialRequirement()) {
                Material material = BlockUtils.getMaterialFromKey(entry.getKey());
                if (material != null && (item.isDefault() || plugin.getCosmeticManager().hasPlayerBlock(uuid, material))) {
                    ownedBlocks++;
                }
            }
        }
            
        return totalBlocks == ownedBlocks;
    }
}