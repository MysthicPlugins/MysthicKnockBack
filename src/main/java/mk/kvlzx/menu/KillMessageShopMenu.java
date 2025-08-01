package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.KillMessagesShopConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class KillMessageShopMenu extends Menu {
    private final KillMessagesShopConfig config;

    public KillMessageShopMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getKillMessagesShopConfig().getMenuTitle(), plugin.getKillMessagesShopConfig().getMenuSize());
        this.config = plugin.getKillMessagesShopConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance item
        setupBalanceItem(inv, stats);

        // Setup kill message items
        setupKillMessageItems(inv, player);

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

    private void setupKillMessageItems(Inventory inv, Player player) {
        String currentMessage = plugin.getCosmeticManager().getPlayerKillMessage(player.getUniqueId());
        List<Integer> availableSlots = config.getKillMessageSlots();
        
        // Obtener mensajes ordenados por rareza
        List<KillMessagesShopConfig.KillMessageItem> sortedMessages = config.getSortedKillMessagesByRarity();
        
        int slotIndex = 0;
        for (KillMessagesShopConfig.KillMessageItem messageItem : sortedMessages) {
            if (slotIndex >= availableSlots.size()) break;
            
            int slot = availableSlots.get(slotIndex);
            setupKillMessageButton(inv, slot, messageItem, player, currentMessage);
            slotIndex++;
        }
    }

    private void setupKillMessageButton(Inventory inv, int slot, KillMessagesShopConfig.KillMessageItem messageItem, 
                                        Player player, String currentMessage) {
        
        boolean hasMessage = plugin.getCosmeticManager().hasPlayerKillMessage(player.getUniqueId(), messageItem.getName());
        boolean isSelected = currentMessage.equals(messageItem.getName());
        
        // Determine the status of the message
        String statusKey = determineMessageStatus(messageItem, hasMessage, isSelected, player);
        List<String> statusLore = config.getStatusMessage(statusKey);
        
        // Build the final lore for the message item
        List<String> finalLore = buildKillMessageLore(messageItem, statusLore, player);
        
        // Create the title for the message item
        String title = config.getKillMessageTitle()
            .replace("%rarity_color%", messageItem.getRarityColor())
            .replace("%message_name%", messageItem.getName());
        
        // Determine material based on selection status
        String materialId = isSelected ? config.getMaterialSelected() : config.getMaterialUnselected();
        
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

    private String determineMessageStatus(KillMessagesShopConfig.KillMessageItem messageItem, boolean hasMessage, 
                                            boolean isSelected, Player player) {
        
        if (hasMessage) {
            return isSelected ? "owned_selected" : "owned_click_to_select";
        } else {
            PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
            return stats.getKGCoins() < messageItem.getPrice() ? "insufficient_funds" : "purchasable";
        }
    }

    private List<String> buildKillMessageLore(KillMessagesShopConfig.KillMessageItem messageItem, 
                                                List<String> statusLore, Player player) {
        List<String> finalLore = new ArrayList<>();
        
        // Get a random victim name for preview
        String victimName = getRandomVictimName(player);
        
        for (String line : config.getKillMessageLore()) {
            String processedLine = line
                .replace("%rarity_color%", messageItem.getRarityColor())
                .replace("%rarity%", messageItem.getRarity())
                .replace("%message_name%", messageItem.getName());
            
            if (processedLine.contains("%preview_message%")) {
                String previewMessage = messageItem.getMessage()
                    .replace("%player%", player.getName())
                    .replace("%victim%", victimName);
                finalLore.add(MessageUtils.getColor(previewMessage));
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

    private String getRandomVictimName(Player player) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        // Remove the current player from the list
        players.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
        
        if (players.isEmpty()) {
            return config.getDefaultVictimName();
        } else {
            Player randomPlayer = players.get((int) (Math.random() * players.size()));
            return randomPlayer.getName();
        }
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
        // Validar que el click sea en el menú y no en el inventario del jugador
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

        // Find the clicked kill message
        String clickedMessageName = findKillMessageFromItem(clicked);
        if (clickedMessageName == null) return;
        
        KillMessagesShopConfig.KillMessageItem messageItem = findKillMessageItemByName(clickedMessageName);
        if (messageItem == null) return;

        handleKillMessageSelection(player, messageItem);
    }

    private String findKillMessageFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }
        
        String displayName = MessageUtils.stripColor(item.getItemMeta().getDisplayName());
        
        // Buscar el mensaje por nombre
        for (KillMessagesShopConfig.KillMessageItem messageItem : config.getKillMessageItems().values()) {
            if (messageItem.getName().equals(displayName)) {
                return messageItem.getName();
            }
        }
        
        return null;
    }

    private KillMessagesShopConfig.KillMessageItem findKillMessageItemByName(String name) {
        return config.getKillMessageItems().values().stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    private void handleKillMessageSelection(Player player, KillMessagesShopConfig.KillMessageItem messageItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMessage = plugin.getCosmeticManager().getPlayerKillMessage(player.getUniqueId());

        // If the player already owns the message
        if (plugin.getCosmeticManager().hasPlayerKillMessage(player.getUniqueId(), messageItem.getName())) {
            // If the message is already selected, deselect it
            if (currentMessage.equals(messageItem.getName())) {
                plugin.getCosmeticManager().setPlayerKillMessage(player.getUniqueId(), "default");
                String message = config.getMessageDeselectedMessage();
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            } else {
                // Select the message
                plugin.getCosmeticManager().setPlayerKillMessage(player.getUniqueId(), messageItem.getName());
                String message = config.getMessageSelectedMessage().replace("%message_name%", messageItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            }
            player.closeInventory();
        } else {
            // Buy the message
            if (stats.getKGCoins() >= messageItem.getPrice()) {
                stats.removeKGCoins(messageItem.getPrice());
                plugin.getCosmeticManager().addPlayerKillMessage(player.getUniqueId(), messageItem.getName());
                plugin.getCosmeticManager().setPlayerKillMessage(player.getUniqueId(), messageItem.getName());
                
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