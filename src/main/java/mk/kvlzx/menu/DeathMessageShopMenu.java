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
    private static String currentCategory = "COMMON";

    public DeathMessageShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lMessage Shop &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<DeathMessageItem> initializeShopItems() {
        List<DeathMessageItem> items = new ArrayList<>();

        // Mensajes Comunes (5)
        items.add(new DeathMessageItem(
            "&b%s &fsought glory, but found a respawn.",
            "Glory Seeker", 10000, "COMMON", "&7",
            "&7An inevitable fate"
        ));
        items.add(new DeathMessageItem(
            "&b%s &fran toward eternity, but ran out of battery.",
            "Eternal Runner", 10000, "COMMON", "&7",
            "&7Energy is limited"
        ));
        items.add(new DeathMessageItem(
            "&b%s &ftried a stellar jump and ended up in a crater.",
            "Stellar Jumper", 10000, "COMMON", "&7",
            "&7The stars were too far"
        ));
        items.add(new DeathMessageItem(
            "&b%s &fdreamed of flying free, but forgot the parachute.",
            "Reckless Dreamer", 10000, "COMMON", "&7",
            "&7Gravity doesn't forgive"
        ));
        items.add(new DeathMessageItem(
            "&b%s &ftried a leap to infinity and landed at spawn.",
            "Infinite Jumper", 10000, "COMMON", "&7",
            "&7The spawn always waits"
        ));

        // Mensajes Épicos (8)
        items.add(new DeathMessageItem(
            "&b%s &5danced with shadows and tripped in the darkness.",
            "Shadow Dancer", 25000, "EPIC", "&5",
            "&5Shadows are treacherous"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5defied fate, and fate taught them a lesson.",
            "Fate Defier", 25000, "EPIC", "&5",
            "&5Fate always wins"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5dreamed of being a titan, but the ground was their judge.",
            "Titan Aspirant", 25000, "EPIC", "&5",
            "&5The ground is relentless"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5wanted to write their saga, but the chapter ended early.",
            "Frustrated Writer", 25000, "EPIC", "&5",
            "&5An unfinished story"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5wanted to be a comet, but crashed like a meteor.",
            "Failed Comet", 25000, "EPIC", "&5",
            "&5Shone until the end"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5dreamed of touching the clouds, but the ground woke them up.",
            "Cloud Dreamer", 25000, "EPIC", "&5",
            "&5A harsh awakening"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5ran with their soul ablaze, but the fire went out.",
            "Blazing Soul", 25000, "EPIC", "&5",
            "&5Flames are fleeting"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5wanted to be a hero, but the arena wrote another story.",
            "Frustrated Hero", 25000, "EPIC", "&5",
            "&5The arena is cruel"
        ));

        // Mensajes Legendarios (7)
        items.add(new DeathMessageItem(
            "&b%s &6sought to conquer the sky, but the abyss claimed them.",
            "Fallen Conqueror", 50000, "LEGENDARY", "&6",
            "&6The abyss always claims"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6leaped into infinity, but infinity didn't answer.",
            "Void Jumper", 50000, "LEGENDARY", "&6",
            "&6Infinity is silent"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6defied the void, and the void gave a deadly embrace.",
            "Void Defier", 50000, "LEGENDARY", "&6",
            "&6The void always embraces"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6shone like lightning before fading in the storm.",
            "Fleeting Lightning", 50000, "LEGENDARY", "&6",
            "&6The storm consumes all"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6danced on the edge of danger, and the edge cut first.",
            "Edge Dancer", 50000, "LEGENDARY", "&6",
            "&6The edge doesn't forgive"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6defied the abyss, and the abyss whispered 'see you soon'.",
            "Abyss Whisper", 50000, "LEGENDARY", "&6",
            "&6The abyss always returns"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6wanted to be an epic poem, but rhymed with defeat.",
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
            "&7Current Balance: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Current Category: " + currentCategory));

        // Mostrar mensajes
        int slot = 10;
        for (DeathMessageItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupMessageButton(inv, slot, item, player, currentMessage);
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

    private void setupMessageButton(Inventory inv, int slot, DeathMessageItem item, Player player, String currentMessage) {
        boolean hasMessage = plugin.getCosmeticManager().hasPlayerDeathMessage(player.getUniqueId(), item.getName());
        boolean isSelected = currentMessage.equals(item.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rarity: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor("&7Message: " + item.getMessage().replace("%s", player.getName())));
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

        // Crear el botón con el material adecuado
        Material material = isSelected ? Material.ENCHANTED_BOOK : Material.PAPER;
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
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "death_message_categories");
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