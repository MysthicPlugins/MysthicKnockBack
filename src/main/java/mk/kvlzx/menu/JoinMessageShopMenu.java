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
import mk.kvlzx.cosmetics.JoinMessageItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class JoinMessageShopMenu extends Menu {
    private final List<JoinMessageItem> shopItems;

    public JoinMessageShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lJoin Message Shop &8•", 54);
        this.shopItems = initializeShopItems();
    }

    private List<JoinMessageItem> initializeShopItems() {
        List<JoinMessageItem> items = new ArrayList<>();
        
        // Comunes
        items.add(new JoinMessageItem(
            "&a¡%player% &bhas burst into the server with force!", 
            "Burst", 15000, "COMMON", "&7",
            "Make a dramatic entrance with explosive energy!"
        ));
        items.add(new JoinMessageItem(
            "&6%player% &ejoins the kingdom, prepare your swords!", 
            "Kingdom", 15000, "COMMON", "&7",
            "Join the realm as a noble warrior ready for battle!"
        ));
        items.add(new JoinMessageItem(
            "&c¡Attention! &f%player% &chas snuck into the adventure.", 
            "Attention", 15000, "COMMON", "&7",
            "Alert everyone of your stealthy arrival!"
        ));
        items.add(new JoinMessageItem(
            "&b%player% &3arrives from distant lands to the server.", 
            "Distant", 15000, "COMMON", "&7",
            "Come from far away lands with tales to tell!"
        ));
        items.add(new JoinMessageItem(
            "&d¡Look! &5%player% &dbrings chaos and fun.", 
            "Chaos", 15000, "COMMON", "&7",
            "Bring excitement and unpredictable fun to the server!"
        ));
        items.add(new JoinMessageItem(
            "&e%player% &6has entered, let the creepers tremble!", 
            "Creepers", 15000, "COMMON", "&7",
            "Strike fear into the hearts of all hostile mobs!"
        ));
        items.add(new JoinMessageItem(
            "&f%player% &7connected, ready to leave their mark.", 
            "Mark", 15000, "COMMON", "&7",
            "Join with determination to make your presence known!"
        ));
        
        // Épicos
        items.add(new JoinMessageItem(
            "&a¡%player% &2has arrived to sow epic tales!", 
            "Epic", 35000, "EPIC", "&5",
            "Become the protagonist of legendary adventures!"
        ));
        items.add(new JoinMessageItem(
            "&c%player% &4enters the server with thirst for victory.", 
            "Victory", 35000, "EPIC", "&5",
            "Join with an unstoppable desire to conquer all challenges!"
        ));
        items.add(new JoinMessageItem(
            "&b¡%player% &9descends from the sky with a trident!", 
            "Trident", 35000, "EPIC", "&5",
            "Make a godly entrance wielding the power of the seas!"
        ));
        items.add(new JoinMessageItem(
            "&d%player% &5joins, magic on the horizon!", 
            "Magic", 35000, "EPIC", "&5",
            "Bring mystical powers and enchanting abilities!"
        ));
        items.add(new JoinMessageItem(
            "&e¡Careful! &6%player% &eis here to shine.", 
            "Shine", 35000, "EPIC", "&5",
            "Radiate brilliance and become the center of attention!"
        ));
        items.add(new JoinMessageItem(
            "&a%player% &2enters with a bow and arrows ready.", 
            "Archer", 35000, "EPIC", "&5",
            "Join as a skilled marksman ready for precise combat!"
        ));
        
        // Legendarios
        items.add(new JoinMessageItem(
            "&f¡%player% &bhas arrived to forge legends!", 
            "Legends", 75000, "LEGENDARY", "&6",
            "Create stories that will be remembered for generations!"
        ));
        items.add(new JoinMessageItem(
            "&c%player% &4whispers: 'The Nether fears me...'",
            "Nether", 75000, "LEGENDARY", "&6",
            "Command respect from the most dangerous dimensions!"
        ));
        items.add(new JoinMessageItem(
            "&b¡%player% &3connects with a master plan!", 
            "Master", 75000, "LEGENDARY", "&6",
            "Arrive with calculated strategies for ultimate success!"
        ));
        items.add(new JoinMessageItem(
            "&d%player% &esteps on the server with enchanted boots.", 
            "Boots", 75000, "LEGENDARY", "&6",
            "Walk with magical footwear that enhances your journey!"
        ));
        items.add(new JoinMessageItem(
            "&6¡Attention! &c%player% &6brings fire to the game.", 
            "Fire", 75000, "LEGENDARY", "&6",
            "Ignite passion and intensity in every adventure!"
        ));
        items.add(new JoinMessageItem(
            "&a%player% &bhas arrived, the End awaits!", 
            "End", 75000, "LEGENDARY", "&6",
            "Prepare for the ultimate challenge in the final dimension!"
        ));
        items.add(new JoinMessageItem(
            "&e%player% &7enters stealthily, beware of the diamonds!", 
            "Diamonds", 75000, "LEGENDARY", "&6",
            "Move with the grace of a master treasure hunter!"
        ));
        
        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

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
        for (JoinMessageItem item : shopItems) {
            if (slotIndex >= availableSlots.length) break;
            
            int slot = availableSlots[slotIndex];
            setupMessageButton(inv, slot, item, player);
            slotIndex++;
        }

        // Botón para volver
        inv.setItem(49, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
    }

    private void setupMessageButton(Inventory inv, int slot, JoinMessageItem item, Player player) {
        String currentMessage = plugin.getCosmeticManager().getPlayerJoinMessage(player.getUniqueId());
        boolean hasMessage = plugin.getCosmeticManager().hasPlayerJoinMessage(player.getUniqueId(), item.getMessage());
        boolean isSelected = currentMessage.equals(item.getMessage());
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rarity: " + item.getRarity());
        lore.add("");
        lore.add("&7Preview:");
        lore.add(MessageUtils.getColor(item.getMessage().replace("%player%", player.getName())));
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
                material = isSelected ? Material.ENCHANTED_BOOK : Material.WRITTEN_BOOK;
                break;
            case "LEGENDARY":
                material = isSelected ? Material.ENCHANTED_BOOK : Material.BOOK_AND_QUILL;
                break;
            default:
                material = isSelected ? Material.ENCHANTED_BOOK : Material.PAPER;
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
        JoinMessageItem messageItem = findMessageItem(itemName);
        if (messageItem == null) return;
        
        handleMessageSelection(player, messageItem);
    }

    private void handleMessageSelection(Player player, JoinMessageItem messageItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMessage = plugin.getCosmeticManager().getPlayerJoinMessage(player.getUniqueId());
        
        if (plugin.getCosmeticManager().hasPlayerJoinMessage(player.getUniqueId(), messageItem.getName())) {
            if (currentMessage.equals(messageItem.getName())) {
                plugin.getCosmeticManager().setPlayerJoinMessage(player.getUniqueId(), "default");
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have deselected the message. Using default messages."));
            } else {
                plugin.getCosmeticManager().setPlayerJoinMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have selected the message: " + messageItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= messageItem.getPrice()) {
                stats.removeKGCoins(messageItem.getPrice());
                plugin.getCosmeticManager().addPlayerJoinMessage(player.getUniqueId(), messageItem.getName());
                plugin.getCosmeticManager().setPlayerJoinMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have purchased and selected the message " +
                    messageItem.getName() + " &afor &e" + messageItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou don't have enough KGCoins to purchase this message."));
            }
        }
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

    private JoinMessageItem findMessageItem(String name) {
        return shopItems.stream()
            .filter(item -> MessageUtils.stripColor(item.getName()).equals(name))
            .findFirst()
            .orElse(null);
    }
}
