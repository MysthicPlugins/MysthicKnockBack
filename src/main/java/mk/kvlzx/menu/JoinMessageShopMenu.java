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
import mk.kvlzx.config.JoinMessagesShopConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class JoinMessageShopMenu extends Menu {
    private final JoinMessagesShopConfig config;

    public JoinMessageShopMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getJoinMessagesShopConfig().getMenuTitle(), plugin.getJoinMessagesShopConfig().getMenuSize());
        this.config = plugin.getJoinMessagesShopConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance item
        setupBalanceItem(inv, stats);

        // Setup join message items
        setupJoinMessageItems(inv, player);

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

    private void setupJoinMessageItems(Inventory inv, Player player) {
        String currentMessage = plugin.getCosmeticManager().getPlayerJoinMessage(player.getUniqueId());
        List<Integer> availableSlots = config.getJoinMessageSlots();
        
        // Get sorted join messages by rarity
        List<JoinMessagesShopConfig.JoinMessageItem> sortedMessages = config.getSortedJoinMessagesByRarity();
        
        int slotIndex = 0;
        for (JoinMessagesShopConfig.JoinMessageItem messageItem : sortedMessages) {
            if (slotIndex >= availableSlots.size()) break;
            
            int slot = availableSlots.get(slotIndex);
            setupMessageButton(inv, slot, messageItem, player, currentMessage);
            slotIndex++;
        }
    }

    private void setupMessageButton(Inventory inv, int slot, JoinMessagesShopConfig.JoinMessageItem messageItem, 
                                    Player player, String currentMessage) {
        
        boolean hasMessage = plugin.getCosmeticManager().hasPlayerJoinMessage(player.getUniqueId(), messageItem.getName());
        boolean isSelected = currentMessage.equals(messageItem.getName());
        
        // Determine the status of the message
        String statusKey = determineMessageStatus(messageItem, hasMessage, isSelected, player);
        List<String> statusLore = config.getStatusMessage(statusKey);
        
        // Build the final lore for the message item
        List<String> finalLore = buildJoinMessageLore(messageItem, statusLore, player);
        
        // Create the title for the message item
        String title = config.getJoinMessageTitle()
            .replace("%rarity_color%", messageItem.getRarityColor())
            .replace("%message_name%", messageItem.getName());
        
        // Determine material based on rarity and selection status
        String materialId = config.getMaterialForRarity(messageItem.getRarity(), isSelected);
        
        // Create the item for the message
        ItemStack messageItemStack = config.createMenuItem(materialId, title, finalLore);
        
        // Add enchantments if the message is selected
        if (isSelected && config.isEnchantedIfSelected()) {
            messageItemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            if (config.isHideEnchants()) {
                ItemMeta meta = messageItemStack.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                messageItemStack.setItemMeta(meta);
            }
        }

        inv.setItem(slot, messageItemStack);
    }

    private String determineMessageStatus(JoinMessagesShopConfig.JoinMessageItem messageItem, boolean hasMessage, 
                                            boolean isSelected, Player player) {
        
        if (hasMessage) {
            return isSelected ? "owned_selected" : "owned_click_to_select";
        } else {
            PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
            return stats.getKGCoins() < messageItem.getPrice() ? "insufficient_funds" : "purchasable";
        }
    }

    private List<String> buildJoinMessageLore(JoinMessagesShopConfig.JoinMessageItem messageItem, 
                                                List<String> statusLore, Player player) {
        List<String> finalLore = new ArrayList<>();
        
        for (String line : config.getJoinMessageLore()) {
            String processedLine = line
                .replace("%rarity_color%", messageItem.getRarityColor())
                .replace("%rarity%", messageItem.getRarity())
                .replace("%message_name%", messageItem.getName());
            
            if (processedLine.contains("%preview_message%")) {
                String previewMessage = messageItem.getMessage().replace("%player%", player.getName());
                finalLore.add(MessageUtils.getColor(previewMessage));
            } else if (processedLine.contains("%description%")) {
                finalLore.add(MessageUtils.getColor(messageItem.getDescription()));
            } else if (processedLine.contains("%status_lore%")) {
                for (String statusLine : statusLore) {
                    String processedStatusLine = statusLine
                        .replace("%price%", String.valueOf(messageItem.getPrice()))
                        .replace("%balance%", String.valueOf(PlayerStats.getStats(player.getUniqueId()).getKGCoins()))
                        .replace("%message_name%", messageItem.getName());
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

        // Find the clicked join message
        String clickedMessageName = findJoinMessageFromItem(clicked);
        if (clickedMessageName == null) return;
        
        JoinMessagesShopConfig.JoinMessageItem messageItem = findJoinMessageItemByName(clickedMessageName);
        if (messageItem == null) return;

        // Handle message selection
        handleMessageSelection(player, messageItem);
    }

    private String findJoinMessageFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }
        
        String displayName = MessageUtils.stripColor(item.getItemMeta().getDisplayName());
        
        // Search for the message by name
        for (JoinMessagesShopConfig.JoinMessageItem messageItem : config.getJoinMessageItems().values()) {
            if (messageItem.getName().equals(displayName)) {
                return messageItem.getName();
            }
        }
        
        return null;
    }

    private JoinMessagesShopConfig.JoinMessageItem findJoinMessageItemByName(String name) {
        return config.getJoinMessageItems().values().stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    private void handleMessageSelection(Player player, JoinMessagesShopConfig.JoinMessageItem messageItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMessage = plugin.getCosmeticManager().getPlayerJoinMessage(player.getUniqueId());

        // If the player already owns the message
        if (plugin.getCosmeticManager().hasPlayerJoinMessage(player.getUniqueId(), messageItem.getName())) {
            // If the message is currently selected, deselect it
            if (currentMessage.equals(messageItem.getName())) {
                plugin.getCosmeticManager().setPlayerJoinMessage(player.getUniqueId(), "default");
                String message = config.getMessageDeselectedMessage();
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            } else {
                // Select the message
                plugin.getCosmeticManager().setPlayerJoinMessage(player.getUniqueId(), messageItem.getName());
                String message = config.getMessageSelectedMessage().replace("%message_name%", messageItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            }
            player.closeInventory();
        } else {
            // Buy the message
            if (stats.getKGCoins() >= messageItem.getPrice()) {
                stats.removeKGCoins(messageItem.getPrice());
                plugin.getCosmeticManager().addPlayerJoinMessage(player.getUniqueId(), messageItem.getName());
                plugin.getCosmeticManager().setPlayerJoinMessage(player.getUniqueId(), messageItem.getName());
                
                String message = config.getMessagePurchasedMessage()
                    .replace("%message_name%", messageItem.getName())
                    .replace("%price%", String.valueOf(messageItem.getPrice()));
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + config.getInsufficientFundsMessage()));
            }
        }
    }
}
