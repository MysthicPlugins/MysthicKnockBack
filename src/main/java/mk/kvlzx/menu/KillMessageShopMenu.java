package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import mk.kvlzx.cosmetics.KillMessageItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class KillMessageShopMenu extends Menu {
    private final List<KillMessageItem> shopItems;

    public KillMessageShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lKill Message Shop &8•", 54);
        this.shopItems = initializeShopItems();
    }

    private List<KillMessageItem> initializeShopItems() {
        List<KillMessageItem> items = new ArrayList<>();

        // Mensajes Comunes (15000 coins)
        items.add(new KillMessageItem(
            "&b%player% &7launched &b%victim% &7into the orbit of defeat!",
            "Orbital Launch", 15000, "COMMON", "&7",
            "&7A one-way space trip!"
        ));
        items.add(new KillMessageItem(
            "&b%victim% &7challenged &b%player%&7, but the ground hugged them first.",
            "Ground Hug", 15000, "COMMON", "&7",
            "&7The ground always wins!"
        ));
        items.add(new KillMessageItem(
            "&b%player% &7gave &b%victim% &7a ticket to the respawn screen!",
            "Respawn Ticket", 15000, "COMMON", "&7",
            "&7A direct trip to spawn!"
        ));
        items.add(new KillMessageItem(
            "&b%victim% &7learned to fly... &7but &b%player% &7cancelled the lesson.",
            "Flight School", 15000, "COMMON", "&7",
            "&7Flight lessons are dangerous!"
        ));

        // Mensajes Épicos (35000 coins)
        items.add(new KillMessageItem(
            "&b%player% &9transformed &b%victim% &9into a shooting star!",
            "Shooting Star", 35000, "EPIC", "&9",
            "&9Shines as it falls!"
        ));
        items.add(new KillMessageItem(
            "&b%player% &9helped &b%victim% &9reach the clouds... permanently.",
            "Cloud Reach", 35000, "EPIC", "&9",
            "&9A celestial journey!"
        ));
        items.add(new KillMessageItem(
            "&b%player% &9showed &b%victim% &9the meaning of true knockback!",
            "True Knockback", 35000, "EPIC", "&9",
            "&9The true essence of KB!"
        ));
        items.add(new KillMessageItem(
            "&b%player% &9sent &b%victim% &9on a journey to the stars!",
            "Star Journey", 35000, "EPIC", "&9",
            "&9An interplanetary trip!"
        ));

        // Mensajes Legendarios (75000 coins)
        items.add(new KillMessageItem(
            "&b%player% &6unleashed the power of knockback on &b%victim%&6!",
            "KB Master", 75000, "LEGENDARY", "&6",
            "&6The power of the true master!"
        ));
        items.add(new KillMessageItem(
            "&b%player% &6rewrote &b%victim%'s &6destiny with a legendary hit!",
            "Destiny Writer", 75000, "LEGENDARY", "&6",
            "&6Rewriting stories!"
        ));
        items.add(new KillMessageItem(
            "&b%player% &6showed &b%victim% &6what godlike knockback looks like!",
            "God of KB", 75000, "LEGENDARY", "&6",
            "&6The power of a god!"
        ));
        items.add(new KillMessageItem(
            "&b%player% &6sent &b%victim% &6to the hall of legends!",
            "Legend Maker", 75000, "LEGENDARY", "&6",
            "&6A place in history!"
        ));

        // Ordenar por rareza: COMMON → EPIC → LEGENDARY
        items.sort((a, b) -> {
            Map<String, Integer> rarityOrder = Map.of(
                "COMMON", 1,
                "EPIC", 2, 
                "LEGENDARY", 3
            );
            return rarityOrder.get(a.getRarity()).compareTo(rarityOrder.get(b.getRarity()));
        });
        
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
        
        // Mostrar todos los mensajes organizados por rareza
        for (KillMessageItem item : shopItems) {
            if (slotIndex >= availableSlots.length) break;
            
            int slot = availableSlots[slotIndex];
            setupMessageButton(inv, slot, item, player);
            slotIndex++;
        }

        // Botón para volver
        inv.setItem(49, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
    }

    private void setupMessageButton(Inventory inv, int slot, KillMessageItem item, Player player) {
        String currentMessage = plugin.getCosmeticManager().getPlayerKillMessage(player.getUniqueId());
        boolean hasMessage = plugin.getCosmeticManager().hasPlayerKillMessage(player.getUniqueId(), item.getName());
        boolean isSelected = currentMessage.equals(item.getName());

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        String victimName;
        if (players.isEmpty() || players.size() == 1 && players.get(0).getUniqueId().equals(player.getUniqueId())) {
            // Si no hay otros jugadores, usar un nombre default
            victimName = "Enemy";
        } else {
            Player randomPlayer = players.get((int) (Math.random() * players.size()));
            victimName = randomPlayer.getName();
        }
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rarity: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getMessage()
            .replace("%player%", player.getName())
            .replace("%victim%", victimName)));
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
        KillMessageItem messageItem = findMessageItem(MessageUtils.stripColor(itemName));
        if (messageItem == null) return;

        handleMessageSelection(player, messageItem);
    }

    private void handleMessageSelection(Player player, KillMessageItem messageItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMessage = plugin.getCosmeticManager().getPlayerKillMessage(player.getUniqueId());

        // Si ya tiene el mensaje
        if (plugin.getCosmeticManager().hasPlayerKillMessage(player.getUniqueId(), messageItem.getName())) {
            // Si está seleccionado, deseleccionar
            if (currentMessage.equals(messageItem.getName())) {
                plugin.getCosmeticManager().setPlayerKillMessage(player.getUniqueId(), "default");
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have deselected the message. Using default messages."));
            } else {
                plugin.getCosmeticManager().setPlayerKillMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have selected the message: " + messageItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= messageItem.getPrice()) {
                stats.removeKGCoins(messageItem.getPrice());
                plugin.getCosmeticManager().addPlayerKillMessage(player.getUniqueId(), messageItem.getName());
                plugin.getCosmeticManager().setPlayerKillMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have purchased and selected the message " + 
                    messageItem.getName() + " &afor &e" + messageItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou don't have enough KGCoins to purchase this message."));
            }
        }
    }

    private KillMessageItem findMessageItem(String name) {
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