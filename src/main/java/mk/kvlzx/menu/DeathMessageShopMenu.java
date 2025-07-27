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
import mk.kvlzx.cosmetics.DeathMessageItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class DeathMessageShopMenu extends Menu {
    private final List<DeathMessageItem> shopItems;

    public DeathMessageShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lMessage Shop &8•", 54);
        this.shopItems = initializeShopItems();
    }

    private List<DeathMessageItem> initializeShopItems() {
        List<DeathMessageItem> items = new ArrayList<>();

        // Mensajes Comunes (5)
        items.add(new DeathMessageItem(
            "&b%player% &fsought glory, but found a respawn.",
            "Glory Seeker", 10000, "COMMON", "&7",
            "&7An inevitable fate"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &fran toward eternity, but ran out of battery.",
            "Eternal Runner", 10000, "COMMON", "&7",
            "&7Energy is limited"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &ftried a stellar jump and ended up in a crater.",
            "Stellar Jumper", 10000, "COMMON", "&7",
            "&7The stars were too far"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &fdreamed of flying free, but forgot the parachute.",
            "Reckless Dreamer", 10000, "COMMON", "&7",
            "&7Gravity doesn't forgive"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &ftried a leap to infinity and landed at spawn.",
            "Infinite Jumper", 10000, "COMMON", "&7",
            "&7The spawn always waits"
        ));

        // Mensajes Épicos (8)
        items.add(new DeathMessageItem(
            "&b%player% &5danced with shadows and tripped in the darkness.",
            "Shadow Dancer", 25000, "EPIC", "&5",
            "&5Shadows are treacherous"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &5defied fate, and fate taught them a lesson.",
            "Fate Defier", 25000, "EPIC", "&5",
            "&5Fate always wins"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &5dreamed of being a titan, but the ground was their judge.",
            "Titan Aspirant", 25000, "EPIC", "&5",
            "&5The ground is relentless"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &5wanted to write their saga, but the chapter ended early.",
            "Frustrated Writer", 25000, "EPIC", "&5",
            "&5An unfinished story"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &5wanted to be a comet, but crashed like a meteor.",
            "Failed Comet", 25000, "EPIC", "&5",
            "&5Shone until the end"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &5dreamed of touching the clouds, but the ground woke them up.",
            "Cloud Dreamer", 25000, "EPIC", "&5",
            "&5A harsh awakening"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &5ran with their soul ablaze, but the fire went out.",
            "Blazing Soul", 25000, "EPIC", "&5",
            "&5Flames are fleeting"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &5wanted to be a hero, but the arena wrote another story.",
            "Frustrated Hero", 25000, "EPIC", "&5",
            "&5The arena is cruel"
        ));

        // Mensajes Legendarios (7)
        items.add(new DeathMessageItem(
            "&b%player% &6sought to conquer the sky, but the abyss claimed them.",
            "Fallen Conqueror", 50000, "LEGENDARY", "&6",
            "&6The abyss always claims"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &6leaped into infinity, but infinity didn't answer.",
            "Void Jumper", 50000, "LEGENDARY", "&6",
            "&6Infinity is silent"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &6defied the void, and the void gave a deadly embrace.",
            "Void Defier", 50000, "LEGENDARY", "&6",
            "&6The void always embraces"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &6shone like lightning before fading in the storm.",
            "Fleeting Lightning", 50000, "LEGENDARY", "&6",
            "&6The storm consumes all"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &6danced on the edge of danger, and the edge cut first.",
            "Edge Dancer", 50000, "LEGENDARY", "&6",
            "&6The edge doesn't forgive"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &6defied the abyss, and the abyss whispered 'see you soon'.",
            "Abyss Whisper", 50000, "LEGENDARY", "&6",
            "&6The abyss always returns"
        ));
        items.add(new DeathMessageItem(
            "&b%player% &6wanted to be an epic poem, but rhymed with defeat.",
            "Defeated Poet", 50000, "LEGENDARY", "&6",
            "&6Defeat also rhymes"
        ));

        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMessage = plugin.getCosmeticManager().getPlayerDeathMessage(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current Balance: &e" + stats.getKGCoins() + " KGCoins"));

        // Slots disponibles para mensajes (evitando el balance y botón de volver)
        int[] availableSlots = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44,
            45, 46, 47, 48, 50, 51, 52, 53
        };

        int slotIndex = 0;
        for (DeathMessageItem item : shopItems) {
            if (slotIndex >= availableSlots.length) break;
            
            int slot = availableSlots[slotIndex];
            setupMessageButton(inv, slot, item, player, currentMessage);
            slotIndex++;
        }

        // Botón para volver
        inv.setItem(49, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
    }

    private void setupMessageButton(Inventory inv, int slot, DeathMessageItem item, Player player, String currentMessage) {
        boolean hasMessage = plugin.getCosmeticManager().hasPlayerDeathMessage(player.getUniqueId(), item.getName());
        boolean isSelected = currentMessage.equals(item.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rarity: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor("&7Message: " + item.getMessage().replace("%player%", player.getName())));
        lore.add("");
        
        if (hasMessage) {
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
        switch (item.getRarity()) {
            case "COMMON":
                material = isSelected ? Material.ENCHANTED_BOOK : Material.PAPER;
                break;
            case "EPIC":
                material = isSelected ? Material.ENCHANTED_BOOK : Material.BOOK;
                break;
            case "LEGENDARY":
                material = isSelected ? Material.ENCHANTED_BOOK : Material.BOOK_AND_QUILL;
                break;
            default:
                material = isSelected ? Material.ENCHANTED_BOOK : Material.PAPER;
                break;
        }

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

    private void handleMessageClick(Player player, DeathMessageItem messageItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMessage = plugin.getCosmeticManager().getPlayerDeathMessage(player.getUniqueId());

        // Si ya tiene el mensaje
        if (plugin.getCosmeticManager().hasPlayerDeathMessage(player.getUniqueId(), messageItem.getName())) {
            // Si está seleccionado, deseleccionar
            if (currentMessage.equals(messageItem.getName())) {
                plugin.getCosmeticManager().setPlayerDeathMessage(player.getUniqueId(), "default");
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have deselected the message. Using default messages."));
            } else {
                plugin.getCosmeticManager().setPlayerDeathMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have selected the message: " + messageItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= messageItem.getPrice()) {
                stats.removeKGCoins(messageItem.getPrice());
                plugin.getCosmeticManager().addPlayerDeathMessage(player.getUniqueId(), messageItem.getName());
                plugin.getCosmeticManager().setPlayerDeathMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have purchased and selected the message " +
                    messageItem.getName() + " &afor &e" + messageItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou don't have enough KGCoins to purchase this message."));
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

        if (event.getSlot() == 49) {
            plugin.getMenuManager().openMenu(player, "shop");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        String itemName = clicked.getItemMeta().getDisplayName();
        DeathMessageItem messageItem = findMessageItem(MessageUtils.stripColor(itemName));
        if (messageItem == null) return;

        handleMessageClick(player, messageItem);
    }

    private DeathMessageItem findMessageItem(String name) {
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