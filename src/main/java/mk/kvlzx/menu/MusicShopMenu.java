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
import mk.kvlzx.cosmetics.BackgroundMusicItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class MusicShopMenu extends Menu {
    private final List<BackgroundMusicItem> musicItems;
    private static String currentCategory = "COMMON";

    public MusicShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &d&lMusic Store &8•", 45);
        this.musicItems = initializeMusicItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<BackgroundMusicItem> initializeMusicItems() {
        List<BackgroundMusicItem> items = new ArrayList<>();

        // Common music (15000 coins)
        items.add(new BackgroundMusicItem(
            "Far", 15000, "COMMON", "&7",
            "&fA distant melody!", 
            "records.far", 1.0f, 1.0f));

        items.add(new BackgroundMusicItem(
            "Mall", 15000, "COMMON", "&7",
            "&fSounds of the mall!", 
            "records.mall", 1.0f, 1.0f));

        items.add(new BackgroundMusicItem(
            "Strad", 15000, "COMMON", "&7",
            "&fA classic melody!", 
            "records.strad", 1.0f, 1.0f));

        // Epic music (35000 coins)
        items.add(new BackgroundMusicItem(
            "Cat", 35000, "EPIC", "&5",
            "&5The cat's disc!", 
            "records.cat", 1.0f, 1.0f));

        items.add(new BackgroundMusicItem(
            "Chirp", 35000, "EPIC", "&5",
            "&5Bird melodies!", 
            "records.chirp", 1.0f, 1.0f));

        // Legendary music (75000 coins)
        items.add(new BackgroundMusicItem(
            "Mellohi", 75000, "LEGENDARY", "&6",
            "&6The mystical melody!", 
            "records.mellohi", 1.0f, 1.0f));

        items.add(new BackgroundMusicItem(
            "Stal", 75000, "LEGENDARY", "&6",
            "&6The steel music!", 
            "records.stal", 1.0f, 1.0f));

        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Current balance
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current balance: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Current category: " + currentCategory));

        // Display music
        int slot = 10;
        for (BackgroundMusicItem item : musicItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupMusicButton(inv, slot, item, player);
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }

        // Back button
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to categories"));

        // Filler
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
    }

    private void setupMusicButton(Inventory inv, int slot, BackgroundMusicItem item, Player player) {
        boolean hasMusic = plugin.getCosmeticManager().hasPlayerBackgroundMusic(player.getUniqueId(), item.getName());
        boolean isSelected = plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId())
                            .equals(item.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rarity: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getDescription()));
        lore.add("");
        lore.add("&eRight-click to hear a sample");
        lore.add("");
        
        if (hasMusic) {
            if (isSelected) {
                lore.add("&aCurrently selected");
                lore.add("&eClick to deselect");
            } else {
                lore.add("&eClick to select");
            }
        } else {
            lore.add("&7Click to buy");
            lore.add("");
            lore.add("&8➥ Price: &e" + item.getPrice() + " KGCoins");
        }

        Material material = isSelected ? Material.JUKEBOX : Material.RECORD_12;
        ItemStack button = createItem(material, 
            (isSelected ? "&b" : item.getRarityColor()) + item.getName(), 
            lore.toArray(new String[0]));

        if (isSelected) {
            ItemMeta meta = button.getItemMeta();
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            button.setItemMeta(meta);
        }

        inv.setItem(slot, button);
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

        if (event.getSlot() == 40) {
            // Stop any preview music
            stopPreviewMusic(player);
            plugin.getMenuManager().openMenu(player, "music_categories");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        String itemName = MessageUtils.stripColor(clicked.getItemMeta().getDisplayName());
        BackgroundMusicItem musicItem = findMusicItem(itemName);
        if (musicItem == null) return;

        // If right-click, play preview
        if (event.isRightClick()) {
            playPreviewMusic(player, musicItem);
            return;
        }

        // If left-click, handle purchase/selection
        handleMusicSelection(player, musicItem);
    }

    private void playPreviewMusic(Player player, BackgroundMusicItem musicItem) {
        // Stop any previous music
        stopPreviewMusic(player);

        // Use the MusicManager to play the music
        plugin.getMusicManager().playPreviewMusic(player, musicItem.getSound());
    }

    private void stopPreviewMusic(Player player) {
        plugin.getMusicManager().stopMusicForPlayer(player);
    }

    private void handleMusicSelection(Player player, BackgroundMusicItem musicItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMusic = plugin.getCosmeticManager().getPlayerBackgroundMusic(player.getUniqueId());

        if (plugin.getCosmeticManager().hasPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName())) {
            if (currentMusic.equals(musicItem.getName())) {
                plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), "none");
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have deselected the music."));
                stopBackgroundMusic(player);
            } else {
                plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have selected the music: " + musicItem.getName()));
                startBackgroundMusic(player, musicItem);
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= musicItem.getPrice()) {
                stats.removeKGCoins(musicItem.getPrice());
                plugin.getCosmeticManager().addPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());
                plugin.getCosmeticManager().setPlayerBackgroundMusic(player.getUniqueId(), musicItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have purchased and selected the music " + 
                    musicItem.getName() + " &afor &e" + musicItem.getPrice() + " KGCoins&a!"));
                startBackgroundMusic(player, musicItem);
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou don't have enough KGCoins to purchase this music."));
            }
        }
    }

    private BackgroundMusicItem findMusicItem(String name) {
        return musicItems.stream()
            .filter(item -> MessageUtils.stripColor(item.getName()).equals(name))
            .findFirst()
            .orElse(null);
    }

    private void startBackgroundMusic(Player player, BackgroundMusicItem musicItem) {
        plugin.getMusicManager().startMusicForPlayer(player, musicItem.getSound());
    }

    private void stopBackgroundMusic(Player player) {
        plugin.getMusicManager().stopMusicForPlayer(player);
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
