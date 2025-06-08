package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.KillSoundItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class KillSoundShopMenu extends Menu {
    private final List<KillSoundItem> shopItems;
    private static String currentCategory = "COMMON";

    public KillSoundShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lKill Sound Shop &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<KillSoundItem> initializeShopItems() {
        List<KillSoundItem> items = new ArrayList<>();

        // Sonidos Comunes
        items.add(new KillSoundItem(
            "Victory Strike", 15000, "COMMON", "&7",
            "&eThe sound of victory!", 
            Sound.LEVEL_UP, 1.0f, 1.0f));
            
        items.add(new KillSoundItem(
            "Critical Hit", 15000, "COMMON", "&7",
            "&eCritical strike!", 
            Sound.SUCCESSFUL_HIT, 1.0f, 0.8f));

        // Sonidos Épicos
        items.add(new KillSoundItem(
            "Blaze Fury", 35000, "EPIC", "&5",
            "&dThe fury of the blaze!", 
            Sound.BLAZE_DEATH, 1.0f, 0.7f));
            
        items.add(new KillSoundItem(
            "Wither Strike", 35000, "EPIC", "&5",
            "&dThe power of the wither!", 
            Sound.WITHER_HURT, 0.8f, 1.0f));

        // Sonidos Legendarios
        items.add(new KillSoundItem(
            "Dragon Wrath", 75000, "LEGENDARY", "&6",
            "&6The wrath of the dragon!", 
            Sound.ENDERDRAGON_GROWL, 1.0f, 0.5f));
            
        items.add(new KillSoundItem(
            "Thunder Smite", 75000, "LEGENDARY", "&6",
            "&6The fury of thunder!", 
            Sound.AMBIENCE_THUNDER, 1.0f, 0.8f));

        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current Balance: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Current Category: " + currentCategory));

        // Mostrar sonidos
        int slot = 10;
        for (KillSoundItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupSoundButton(inv, slot, item, player);
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

    private void setupSoundButton(Inventory inv, int slot, KillSoundItem item, Player player) {
        boolean hasSound = plugin.getCosmeticManager().hasPlayerKillSound(player.getUniqueId(), item.getName());
        boolean isSelected = plugin.getCosmeticManager().getPlayerKillSound(player.getUniqueId()).equals(item.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rarity: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getDescription()));
        lore.add("");
        lore.add("&eRight-click to preview");
        lore.add("");
        
        if (hasSound) {
            if (isSelected) {
                lore.add("&aCurrently selected");
                lore.add("&eClick to deselect");
            } else {
                lore.add("&eClick to select");
            }
        } else {
            lore.add("&7Click to purchase");
            lore.add("");
            lore.add("&8➥ Price: &e" + item.getPrice() + " KGCoins");
        }

        Material material = isSelected ? Material.JUKEBOX : Material.NOTE_BLOCK;
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
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "kill_sound_categories");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        String itemName = MessageUtils.stripColor(clicked.getItemMeta().getDisplayName());
        KillSoundItem soundItem = findSoundItem(itemName);
        if (soundItem == null) return;

        // Si es click derecho, reproducir muestra
        if (event.isRightClick()) {
            player.playSound(player.getLocation(), 
                soundItem.getSound(), 
                soundItem.getVolume(), 
                soundItem.getPitch());
            return;
        }

        // Si es click izquierdo, manejar compra/selección
        handleSoundSelection(player, soundItem);
    }

    private void handleSoundSelection(Player player, KillSoundItem soundItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentSound = plugin.getCosmeticManager().getPlayerKillSound(player.getUniqueId());

        if (plugin.getCosmeticManager().hasPlayerKillSound(player.getUniqueId(), soundItem.getName())) {
            if (currentSound.equals(soundItem.getName())) {
                plugin.getCosmeticManager().setPlayerKillSound(player.getUniqueId(), "none");
                player.sendMessage(MessageUtils.getColor("&aYou have deselected the sound."));
            } else {
                plugin.getCosmeticManager().setPlayerKillSound(player.getUniqueId(), soundItem.getName());
                player.sendMessage(MessageUtils.getColor("&aYou have selected the sound: " + soundItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= soundItem.getPrice()) {
                stats.removeKGCoins(soundItem.getPrice());
                plugin.getCosmeticManager().addPlayerKillSound(player.getUniqueId(), soundItem.getName());
                plugin.getCosmeticManager().setPlayerKillSound(player.getUniqueId(), soundItem.getName());
                player.sendMessage(MessageUtils.getColor("&aYou have purchased and selected the sound " + 
                    soundItem.getName() + " &afor &e" + soundItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor("&cYou don't have enough KGCoins to purchase this sound."));
            }
        }
    }

    private KillSoundItem findSoundItem(String name) {
        return shopItems.stream()
            .filter(item -> MessageUtils.stripColor(item.getName()).equals(name))
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