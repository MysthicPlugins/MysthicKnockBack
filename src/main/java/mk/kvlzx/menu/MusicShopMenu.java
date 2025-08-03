package mk.kvlzx.menu;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.MusicShopConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MusicShopMenu extends Menu {
    private final MusicShopConfig config;

    public MusicShopMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getMusicShopConfig().getMenuTitle(), plugin.getMusicShopConfig().getMenuSize());
        this.config = plugin.getMusicShopConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance item
        setupBalanceItem(inv, stats);

        // Setup music items
        setupMusicItems(inv, player);

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

    private void setupMusicItems(Inventory inv, Player player) {
        String currentMusic = plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId());
        List<Integer> availableSlots = config.getMusicSlots();

        // Get sorted music by rarity
        List<MusicShopConfig.BackgroundMusicItem> sortedMusic = config.getSortedMusicByRarity();

        int slotIndex = 0;
        for (MusicShopConfig.BackgroundMusicItem musicItem : sortedMusic) {
            if (slotIndex >= availableSlots.size()) break;

            int slot = availableSlots.get(slotIndex);
            setupMusicButton(inv, slot, musicItem, player, currentMusic);
            slotIndex++;
        }
    }

    private void setupMusicButton(Inventory inv, int slot, MusicShopConfig.BackgroundMusicItem musicItem,
                                    Player player, String currentMusic) {

        boolean hasMusic = plugin.getCosmeticManager().hasPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());
        boolean isSelected = currentMusic.equals(musicItem.getName());

        // Determine the status of the music
        String statusKey = determineMusicStatus(musicItem, hasMusic, isSelected, player);
        List<String> statusLore = config.getStatusMessage(statusKey);

        // Build the final lore for the music item
        List<String> finalLore = buildMusicLore(musicItem, statusLore, player);

        // Create the title for the music item
        String title = config.getMusicTitle()
                .replace("%rarity_color%", musicItem.getRarityColor())
                .replace("%music_name%", musicItem.getName());

        // Determine material based on rarity and selection status
        String materialId = config.getMaterialForRarity(musicItem.getRarity(), isSelected);

        // Create the item for the music
        ItemStack musicItemStack = config.createMenuItem(materialId, title, finalLore);

        // Add enchantments if the music is selected
        if (isSelected && config.isEnchantedIfSelected()) {
            musicItemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            if (config.isHideEnchants()) {
                ItemMeta meta = musicItemStack.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                musicItemStack.setItemMeta(meta);
            }
        }

        inv.setItem(slot, musicItemStack);
    }

    private String determineMusicStatus(MusicShopConfig.BackgroundMusicItem musicItem, boolean hasMusic,
                                        boolean isSelected, Player player) {

        if (hasMusic) {
            return isSelected ? "owned_selected" : "owned_click_to_select";
        } else {
            PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
            return stats.getKGCoins() < musicItem.getPrice() ? "insufficient_funds" : "purchasable";
        }
    }

    private List<String> buildMusicLore(MusicShopConfig.BackgroundMusicItem musicItem,
                                        List<String> statusLore, Player player) {
        List<String> finalLore = new ArrayList<>();

        for (String line : config.getMusicLore()) {
            String processedLine = line
                    .replace("%rarity_color%", musicItem.getRarityColor())
                    .replace("%rarity%", musicItem.getRarity())
                    .replace("%music_name%", musicItem.getName());

            if (processedLine.contains("%description%")) {
                finalLore.add(MessageUtils.getColor(musicItem.getDescription()));
            } else if (processedLine.contains("%status_lore%")) {
                for (String statusLine : statusLore) {
                    String processedStatusLine = statusLine
                            .replace("%price%", String.valueOf(musicItem.getPrice()))
                            .replace("%balance%", String.valueOf(PlayerStats.getStats(player.getUniqueId()).getKGCoins()))
                            .replace("%music_name%", musicItem.getName());
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
            plugin.getMusicManager().stopMusicForPlayer(player);
            plugin.getMenuManager().openMenu(player, "shop");
            return;
        }

        // Ignore clicks on special items
        if (clicked == null ||
                clicked.getType().name().equals(config.getFillerMaterial()) ||
                clicked.getType().name().equals(config.getBalanceMaterial())) {
            return;
        }

        // Find the clicked music
        String clickedMusicName = findMusicFromItem(clicked);
        if (clickedMusicName == null) return;

        MusicShopConfig.BackgroundMusicItem musicItem = findMusicItemByName(clickedMusicName);
        if (musicItem == null) return;

        // If it's a right click, play the music
        if (event.isRightClick()) {
            plugin.getMusicManager().playPreviewMusic(player, musicItem.getSound());
            return;
        }

        // If it's a left click, handle music selection
        handleMusicSelection(player, musicItem);
    }

    private String findMusicFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }

        String displayName = MessageUtils.stripColor(item.getItemMeta().getDisplayName());

        // Search for the music by name
        for (MusicShopConfig.BackgroundMusicItem musicItem : config.getMusicItems().values()) {
            if (musicItem.getName().equals(displayName)) {
                return musicItem.getName();
            }
        }

        return null;
    }

    private MusicShopConfig.BackgroundMusicItem findMusicItemByName(String name) {
        return config.getMusicItems().values().stream()
                .filter(item -> item.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void handleMusicSelection(Player player, MusicShopConfig.BackgroundMusicItem musicItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMusic = plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId());

        // If the player already owns the music
        if (plugin.getCosmeticManager().hasPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName())) {
            // If the music is currently selected, deselect it
            if (currentMusic.equals(musicItem.getName())) {
                plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), "none");
                String message = config.getMusicDeselectedMessage();
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                plugin.getMusicManager().stopMusicForPlayer(player);
            } else {
                // Select the music
                plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());
                String message = config.getMusicSelectedMessage().replace("%music_name%", musicItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                plugin.getMusicManager().startMusicForPlayer(player, musicItem.getSound());
            }
            player.closeInventory();
        } else {
            // Buy the music
            if (stats.getKGCoins() >= musicItem.getPrice()) {
                stats.removeKGCoins(musicItem.getPrice());
                plugin.getCosmeticManager().addPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());
                plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());

                String message = config.getMusicPurchasedMessage()
                        .replace("%music_name%", musicItem.getName())
                        .replace("%price%", String.valueOf(musicItem.getPrice()));
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + message));
                plugin.getMusicManager().startMusicForPlayer(player, musicItem.getSound());
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + config.getInsufficientFundsMessage()));
            }
        }
    }
}
