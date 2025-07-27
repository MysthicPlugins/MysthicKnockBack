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
import mk.kvlzx.cosmetics.DeathSoundItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class DeathSoundShopMenu extends Menu {
    private final List<DeathSoundItem> shopItems;

    public DeathSoundShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lSound Shop &8•", 54);
        this.shopItems = initializeShopItems();
    }

    private List<DeathSoundItem> initializeShopItems() {
        List<DeathSoundItem> items = new ArrayList<>();

        // Sonidos Comunes
        items.add(new DeathSoundItem(
            "Simple Death", 15000, "COMMON", "&7",
            "&7A classic never fails!",
            Sound.HURT_FLESH, 1.0f, 1.0f));
            
        items.add(new DeathSoundItem(
            "Last Breath", 15000, "COMMON", "&7",
            "&7The final gasp!",
            Sound.GHAST_DEATH, 0.5f, 1.2f));

        // Sonidos Épicos
        items.add(new DeathSoundItem(
            "Dragon Roar", 35000, "EPIC", "&5",
            "&5The dragon's roar!",
            Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f));
            
        items.add(new DeathSoundItem(
            "Wither Curse", 35000, "EPIC", "&5",
            "&5The curse of the Wither!",
            Sound.WITHER_SPAWN, 0.8f, 1.0f));

        // Sonidos Legendarios
        items.add(new DeathSoundItem(
            "Thunder Strike", 75000, "LEGENDARY", "&6",
            "&6The power of thunder!",
            Sound.AMBIENCE_THUNDER, 1.0f, 1.0f));
            
        items.add(new DeathSoundItem(
            "Void Echo", 75000, "LEGENDARY", "&6",
            "&6The echo of the void!",
            Sound.ENDERDRAGON_DEATH, 0.7f, 1.2f));

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
        for (DeathSoundItem item : shopItems) {
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

    private void setupSoundButton(Inventory inv, int slot, DeathSoundItem soundItem, Player player) {
        boolean hasSound = plugin.getCosmeticManager().hasPlayerDeathSound(player.getUniqueId(), soundItem.getName());
        boolean isSelected = plugin.getCosmeticManager().getPlayerDeathSound(player.getUniqueId()).equals(soundItem.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(soundItem.getRarityColor() + "✦ Rarity: " + soundItem.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(soundItem.getDescription()));
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
            lore.add("&8➥ Price: &e" + soundItem.getPrice() + " KGCoins");
        }

        // Crear el botón con el material adecuado según la rareza
        Material material;
        if (isSelected) {
            material = Material.JUKEBOX;
        } else {
            switch (soundItem.getRarity()) {
                case "COMMON":
                    material = Material.NOTE_BLOCK;
                    break;
                case "EPIC":
                    material = Material.RECORD_3;
                    break;
                case "LEGENDARY":
                    material = Material.RECORD_12;
                    break;
                default:
                    material = Material.NOTE_BLOCK;
                    break;
            }
        }

        ItemStack button = createItem(material,
            (isSelected ? "&b" : soundItem.getRarityColor()) + soundItem.getName(),
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
            plugin.getMenuManager().openMenu(player, "shop");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE ||
            clicked.getType() == Material.EMERALD) return;

        String itemName = MessageUtils.stripColor(clicked.getItemMeta().getDisplayName());
        DeathSoundItem soundItem = findSoundItem(itemName);
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

    private void handleSoundSelection(Player player, DeathSoundItem soundItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentSound = plugin.getCosmeticManager().getPlayerDeathSound(player.getUniqueId());

        if (plugin.getCosmeticManager().hasPlayerDeathSound(player.getUniqueId(), soundItem.getName())) {
            if (currentSound.equals(soundItem.getName())) {
                plugin.getCosmeticManager().setPlayerDeathSound(player.getUniqueId(), "none");
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have deselected the sound."));
            } else {
                plugin.getCosmeticManager().setPlayerDeathSound(player.getUniqueId(), soundItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have selected the sound: " + soundItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= soundItem.getPrice()) {
                stats.removeKGCoins(soundItem.getPrice());
                plugin.getCosmeticManager().addPlayerDeathSound(player.getUniqueId(), soundItem.getName());
                plugin.getCosmeticManager().setPlayerDeathSound(player.getUniqueId(), soundItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have purchased and selected the sound " +
                    soundItem.getName() + " &afor &e" + soundItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou don't have enough KGCoins to purchase this sound."));
            }
        }
    }

    private DeathSoundItem findSoundItem(String name) {
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