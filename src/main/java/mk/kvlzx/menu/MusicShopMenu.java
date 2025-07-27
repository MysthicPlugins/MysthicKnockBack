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

    public MusicShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &d&lMusic Store &8•", 54);
        this.musicItems = initializeMusicItems();
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

        // Balance
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current balance: &e" + stats.getKGCoins() + " KGCoins"));

        // Slots disponibles para música (evitando el balance y botón de volver)
        int[] availableSlots = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44,
            45, 46, 47, 48, 50, 51, 52, 53
        };

        // Configurar botones de música
        int slotIndex = 0;
        for (BackgroundMusicItem item : musicItems) {
            if (slotIndex >= availableSlots.length) break;
            
            int slot = availableSlots[slotIndex];
            setupMusicButton(inv, slot, item, player);
            slotIndex++;
        }

        // Botón de volver
        inv.setItem(49, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Filler
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
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
            button.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            ItemMeta meta = button.getItemMeta();
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

        if (event.getSlot() == 49) {
            // Detener música previa si está sonando
            stopPreviewMusic(player);
            plugin.getMenuManager().openMenu(player, "shop");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        String itemName = MessageUtils.stripColor(clicked.getItemMeta().getDisplayName());
        BackgroundMusicItem musicItem = findMusicItem(itemName);
        if (musicItem == null) return;

        // Si es click derecho, reproducir música de muestra
        if (event.isRightClick()) {
            playPreviewMusic(player, musicItem);
            return;
        }

        // Si es click izquierdo, manejar selección o compra
        handleMusicSelection(player, musicItem);
    }

    private void playPreviewMusic(Player player, BackgroundMusicItem musicItem) {
        // Detener música previa si está sonando
        stopPreviewMusic(player);

        // Reproducir música de muestra usando el MusicManager
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
