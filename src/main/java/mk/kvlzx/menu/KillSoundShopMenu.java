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

    public KillSoundShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lKill Sound Shop &8•", 54);
        this.shopItems = initializeShopItems();
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
            "&7Current Balance: &e" + stats.getKGCoins() + " KGCoins"));

        // Slots disponibles para sonidos (evitando el balance y botón de volver)
        int[] availableSlots = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44,
            45, 46, 47, 48, 50, 51, 52, 53
        };

        int slotIndex = 0;
        for (KillSoundItem item : shopItems) {
            if (slotIndex >= availableSlots.length) break;
            
            int slot = availableSlots[slotIndex];
            setupSoundButton(inv, slot, item, player);
            slotIndex++;
        }

        // Botón para volver
        inv.setItem(49, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
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

        // Crear el botón con el material adecuado según la rareza
        Material material;
        if (isSelected) {
            material = Material.JUKEBOX;
        } else {
            switch (item.getRarity()) {
                case "COMMON":
                    material = Material.NOTE_BLOCK;
                    break;
                case "EPIC":
                    material = Material.RECORD_11;
                    break;
                case "LEGENDARY":
                    material = Material.RECORD_12;
                    break;
                default:
                    material = Material.NOTE_BLOCK;
            }
        }

        ItemStack button = createItem(material, 
            (isSelected ? "&b" : item.getRarityColor()) + item.getName(), 
            lore.toArray(new String[0]));

        if (isSelected) {
            button.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            // Ocultar el texto del encantamiento
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
            plugin.getMenuManager().openMenu(player, "shop");
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
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have deselected the sound."));
            } else {
                plugin.getCosmeticManager().setPlayerKillSound(player.getUniqueId(), soundItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have selected the sound: " + soundItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= soundItem.getPrice()) {
                stats.removeKGCoins(soundItem.getPrice());
                plugin.getCosmeticManager().addPlayerKillSound(player.getUniqueId(), soundItem.getName());
                plugin.getCosmeticManager().setPlayerKillSound(player.getUniqueId(), soundItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have purchased and selected the sound " +
                    soundItem.getName() + " &afor &e" + soundItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou don't have enough KGCoins to purchase this sound."));
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