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
import mk.kvlzx.config.DeathSoundsShopConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class DeathSoundShopMenu extends Menu {
    private final DeathSoundsShopConfig config;

    public DeathSoundShopMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getDeathSoundsShopConfig().getMenuTitle(), plugin.getDeathSoundsShopConfig().getMenuSize());
        this.config = plugin.getDeathSoundsShopConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance item
        setupBalanceItem(inv, stats);

        // Setup death sound items
        setupDeathSoundItems(inv, player);

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

    private void setupDeathSoundItems(Inventory inv, Player player) {
        String currentSound = plugin.getCosmeticManager().getPlayerDeathSound(player.getUniqueId());
        List<Integer> availableSlots = config.getDeathSoundSlots();
        
        // Get sorted death sounds by rarity
        List<DeathSoundsShopConfig.DeathSoundItem> sortedSounds = config.getSortedDeathSoundsByRarity();
        
        int slotIndex = 0;
        for (DeathSoundsShopConfig.DeathSoundItem soundItem : sortedSounds) {
            if (slotIndex >= availableSlots.size()) break;
            
            int slot = availableSlots.get(slotIndex);
            setupSoundButton(inv, slot, soundItem, player, currentSound);
            slotIndex++;
        }
    }

    private void setupSoundButton(Inventory inv, int slot, DeathSoundsShopConfig.DeathSoundItem soundItem, 
                                    Player player, String currentSound) {
        
        boolean hasSound = plugin.getCosmeticManager().hasPlayerDeathSound(player.getUniqueId(), soundItem.getName());
        boolean isSelected = currentSound.equals(soundItem.getName());
        
        // Determine the status of the sound
        String statusKey = determineSoundStatus(soundItem, hasSound, isSelected, player);
        List<String> statusLore = config.getStatusMessage(statusKey);
        
        // Build the final lore for the sound item
        List<String> finalLore = buildDeathSoundLore(soundItem, statusLore, player);
        
        // Create the title for the sound item
        String title = config.getDeathSoundTitle()
            .replace("%rarity_color%", soundItem.getRarityColor())
            .replace("%sound_name%", soundItem.getName());
        
        // Determine material based on rarity and selection status
        String materialId = config.getMaterialForRarity(soundItem.getRarity(), isSelected);
        
        // Create the item for the sound
        ItemStack soundItemStack = config.createMenuItem(materialId, title, finalLore);
        
        // Add enchantments if the sound is selected
        if (isSelected && config.isEnchantedIfSelected()) {
            soundItemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            if (config.isHideEnchants()) {
                ItemMeta meta = soundItemStack.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                soundItemStack.setItemMeta(meta);
            }
        }

        inv.setItem(slot, soundItemStack);
    }

    private String determineSoundStatus(DeathSoundsShopConfig.DeathSoundItem soundItem, boolean hasSound, 
                                        boolean isSelected, Player player) {
        
        if (hasSound) {
            return isSelected ? "owned_selected" : "owned_click_to_select";
        } else {
            PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
            return stats.getKGCoins() < soundItem.getPrice() ? "insufficient_funds" : "purchasable";
        }
    }

    private List<String> buildDeathSoundLore(DeathSoundsShopConfig.DeathSoundItem soundItem, 
                                            List<String> statusLore, Player player) {
        List<String> finalLore = new ArrayList<>();
        
        for (String line : config.getDeathSoundLore()) {
            String processedLine = line
                .replace("%rarity_color%", soundItem.getRarityColor())
                .replace("%rarity%", soundItem.getRarity())
                .replace("%sound_name%", soundItem.getName());
            
            if (processedLine.contains("%description%")) {
                finalLore.add(MessageUtils.getColor(soundItem.getDescription()));
            } else if (processedLine.contains("%status_lore%")) {
                for (String statusLine : statusLore) {
                    String processedStatusLine = statusLine
                        .replace("%price%", String.valueOf(soundItem.getPrice()))
                        .replace("%balance%", String.valueOf(PlayerStats.getStats(player.getUniqueId()).getKGCoins()))
                        .replace("%sound_name%", soundItem.getName());
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

        // Find the clicked death sound
        String clickedSoundName = findDeathSoundFromItem(clicked);
        if (clickedSoundName == null) return;
        
        DeathSoundsShopConfig.DeathSoundItem soundItem = findDeathSoundItemByName(clickedSoundName);
        if (soundItem == null) return;

        // If it's a right click, play the sound
        if (event.isRightClick()) {
            player.playSound(player.getLocation(),
                soundItem.getSound(),
                soundItem.getVolume(),
                soundItem.getPitch());
            return;
        }

        // If it's a left click, handle sound selection
        handleSoundSelection(player, soundItem);
    }

    private String findDeathSoundFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }
        
        String displayName = MessageUtils.stripColor(item.getItemMeta().getDisplayName());
        
        // Buscar el sonido por nombre
        for (DeathSoundsShopConfig.DeathSoundItem soundItem : config.getDeathSoundItems().values()) {
            if (soundItem.getName().equals(displayName)) {
                return soundItem.getName();
            }
        }
        
        return null;
    }

    private DeathSoundsShopConfig.DeathSoundItem findDeathSoundItemByName(String name) {
        return config.getDeathSoundItems().values().stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    private void handleSoundSelection(Player player, DeathSoundsShopConfig.DeathSoundItem soundItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentSound = plugin.getCosmeticManager().getPlayerDeathSound(player.getUniqueId());

        // If the player already owns the sound
        if (plugin.getCosmeticManager().hasPlayerDeathSound(player.getUniqueId(), soundItem.getName())) {
            // If the sound is currently selected, deselect it
            if (currentSound.equals(soundItem.getName())) {
                plugin.getCosmeticManager().setPlayerDeathSound(player.getUniqueId(), "none");
                String message = config.getSoundDeselectedMessage();
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            } else {
                // Select the sound
                plugin.getCosmeticManager().setPlayerDeathSound(player.getUniqueId(), soundItem.getName());
                String message = config.getSoundSelectedMessage().replace("%sound_name%", soundItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
            }
            player.closeInventory();
        } else {
            // Buy the sound
            if (stats.getKGCoins() >= soundItem.getPrice()) {
                stats.removeKGCoins(soundItem.getPrice());
                plugin.getCosmeticManager().addPlayerDeathSound(player.getUniqueId(), soundItem.getName());
                plugin.getCosmeticManager().setPlayerDeathSound(player.getUniqueId(), soundItem.getName());
                
                String message = config.getSoundPurchasedMessage()
                    .replace("%sound_name%", soundItem.getName())
                    .replace("%price%", String.valueOf(soundItem.getPrice()));
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + config.getInsufficientFundsMessage()));
            }
        }
    }
}