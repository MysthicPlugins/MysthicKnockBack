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
    private static String currentCategory = "COMMON";

    public JoinMessageShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lJoin Message Shop &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<JoinMessageItem> initializeShopItems() {
        List<JoinMessageItem> items = new ArrayList<>();
        // Comunes
        items.add(new JoinMessageItem("&a¡%player% &bha irrumpido en el servidor con fuerza!", "¡Irrumpe!", 15000, "COMMON", "&7"));
        items.add(new JoinMessageItem("&6%player% &ese une al reino, ¡preparen las espadas!", "Reino", 15000, "COMMON", "&7"));
        items.add(new JoinMessageItem("&c¡Atención! &f%player% &cse ha colado en la aventura.", "Atención", 15000, "COMMON", "&7"));
        items.add(new JoinMessageItem("&b%player% &3llega desde tierras lejanas al servidor.", "Lejanas", 15000, "COMMON", "&7"));
        items.add(new JoinMessageItem("&d¡Miren! &5%player% &dtrae caos y diversión.", "Caos", 15000, "COMMON", "&7"));
        items.add(new JoinMessageItem("&e%player% &6ha entrado, ¡que tiemblen los creepers!", "Creepers", 15000, "COMMON", "&7"));
        items.add(new JoinMessageItem("&f%player% &7se conectó, listo para dejar su marca.", "Marca", 15000, "COMMON", "&7"));
        // Épicos
        items.add(new JoinMessageItem("&a¡%player% &2ha llegado para sembrar épica!", "Épica", 35000, "EPIC", "&5"));
        items.add(new JoinMessageItem("&c%player% &4entra al servidor con sed de victoria.", "Victoria", 35000, "EPIC", "&5"));
        items.add(new JoinMessageItem("&b¡%player% &9desciende del cielo con un tridente!", "Tridente", 35000, "EPIC", "&5"));
        items.add(new JoinMessageItem("&d%player% &5se une, ¡magia en el horizonte!", "Magia", 35000, "EPIC", "&5"));
        items.add(new JoinMessageItem("&e¡Cuidado! &6%player% &eestá aquí para brillar.", "Brillar", 35000, "EPIC", "&5"));
        items.add(new JoinMessageItem("&a%player% &2entra con un arco y flechas listas.", "Arco", 35000, "EPIC", "&5"));
        // Legendarios
        items.add(new JoinMessageItem("&f¡%player% &bha llegado para forjar leyendas!", "Leyendas", 75000, "LEGENDARY", "&6"));
        items.add(new JoinMessageItem("&c%player% &4susurra: 'El Nether me teme...'.", "Nether", 75000, "LEGENDARY", "&6"));
        items.add(new JoinMessageItem("&b¡%player% &3se conecta con un plan maestro!", "Maestro", 75000, "LEGENDARY", "&6"));
        items.add(new JoinMessageItem("&d%player% &episa el servidor con botas encantadas.", "Botas", 75000, "LEGENDARY", "&6"));
        items.add(new JoinMessageItem("&6¡Atención! &c%player% &6trae fuego al juego.", "Fuego", 75000, "LEGENDARY", "&6"));
        items.add(new JoinMessageItem("&a%player% &bha llegado, ¡el End lo espera!", "End", 75000, "LEGENDARY", "&6"));
        items.add(new JoinMessageItem("&e%player% &7entra sigiloso, ¡cuidado con los diamantes!", "Diamantes", 75000, "LEGENDARY", "&6"));
        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current Balance: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Current Category: " + currentCategory));
        int slot = 10;
        for (JoinMessageItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupMessageButton(inv, slot, item, player);
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to categories"));
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
    }

    private void setupMessageButton(Inventory inv, int slot, JoinMessageItem item, Player player) {
        String currentMessage = plugin.getCosmeticManager().getPlayerJoinMessage(player.getUniqueId());
        boolean hasMessage = plugin.getCosmeticManager().hasPlayerJoinMessage(player.getUniqueId(), item.getMessage());
        boolean isSelected = currentMessage.equals(item.getMessage());
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
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

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "join_message_categories");
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
        if (plugin.getCosmeticManager().hasPlayerJoinMessage(player.getUniqueId(), messageItem.getMessage())) {
            if (currentMessage.equals(messageItem.getMessage())) {
                plugin.getCosmeticManager().setPlayerJoinMessage(player.getUniqueId(), "default");
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&aYou have deselected the message. Using default messages."));
            } else {
                plugin.getCosmeticManager().setPlayerJoinMessage(player.getUniqueId(), messageItem.getMessage());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&aYou have selected the message: " + messageItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= messageItem.getPrice()) {
                stats.removeKGCoins(messageItem.getPrice());
                plugin.getCosmeticManager().addPlayerJoinMessage(player.getUniqueId(), messageItem.getMessage());
                plugin.getCosmeticManager().setPlayerJoinMessage(player.getUniqueId(), messageItem.getMessage());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&aYou have purchased and selected the message " + 
                    messageItem.getName() + " &afor &e" + messageItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cYou don't have enough KGCoins to purchase this message."));
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
