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
import mk.kvlzx.cosmetics.KillMessageItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class KillMessageShopMenu extends Menu {
    private final List<KillMessageItem> shopItems;
    private static String currentCategory = "COMÚN";

    public KillMessageShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lTienda de Mensajes de Kill &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<KillMessageItem> initializeShopItems() {
        List<KillMessageItem> items = new ArrayList<>();

        // Mensajes Comunes (15000 coins)
        items.add(new KillMessageItem(
            "&b{killer} &7launched &b{victim} &7into the orbit of defeat!",
            "Orbital Launch", 15000, "COMÚN", "&7",
            "&7¡Un viaje espacial sin retorno!"
        ));
        items.add(new KillMessageItem(
            "&b{victim} &7challenged &b{killer}&7, but the ground hugged them first.",
            "Ground Hug", 15000, "COMÚN", "&7",
            "&7¡El suelo siempre gana!"
        ));
        items.add(new KillMessageItem(
            "&b{killer} &7gave &b{victim} &7a ticket to the respawn screen!",
            "Respawn Ticket", 15000, "COMÚN", "&7",
            "&7¡Viaje directo al spawn!"
        ));
        items.add(new KillMessageItem(
            "&b{victim} &7learned to fly... &7but &b{killer} &7cancelled the lesson.",
            "Flight School", 15000, "COMÚN", "&7",
            "&7¡Las clases de vuelo son peligrosas!"
        ));

        // Mensajes Épicos (35000 coins)
        items.add(new KillMessageItem(
            "&b{killer} &9transformed &b{victim} &9into a shooting star!",
            "Shooting Star", 35000, "ÉPICO", "&9",
            "&9¡Brilla mientras cae!"
        ));
        items.add(new KillMessageItem(
            "&b{killer} &9helped &b{victim} &9reach the clouds... permanently.",
            "Cloud Reach", 35000, "ÉPICO", "&9",
            "&9¡Un viaje celestial!"
        ));
        items.add(new KillMessageItem(
            "&b{killer} &9showed &b{victim} &9the meaning of true knockback!",
            "True Knockback", 35000, "ÉPICO", "&9",
            "&9¡La verdadera esencia del KB!"
        ));
        items.add(new KillMessageItem(
            "&b{killer} &9sent &b{victim} &9on a journey to the stars!",
            "Star Journey", 35000, "ÉPICO", "&9",
            "&9¡Un viaje interplanetario!"
        ));

        // Mensajes Legendarios (75000 coins)
        items.add(new KillMessageItem(
            "&b{killer} &6unleashed the power of knockback on &b{victim}&6!",
            "KB Master", 75000, "LEGENDARIO", "&6",
            "&6¡El poder del verdadero maestro!"
        ));
        items.add(new KillMessageItem(
            "&b{killer} &6rewrote &b{victim}'s &6destiny with a legendary hit!",
            "Destiny Writer", 75000, "LEGENDARIO", "&6",
            "&6¡Reescribiendo historias!"
        ));
        items.add(new KillMessageItem(
            "&b{killer} &6showed &b{victim} &6what godlike knockback looks like!",
            "God of KB", 75000, "LEGENDARIO", "&6",
            "&6¡El poder de un dios!"
        ));
        items.add(new KillMessageItem(
            "&b{killer} &6sent &b{victim} &6to the hall of legends!",
            "Legend Maker", 75000, "LEGENDARIO", "&6",
            "&6¡Un lugar en la historia!"
        ));

        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lTu Balance",
            "&7Balance actual: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Categoría actual: " + currentCategory));

        // Mostrar mensajes de la categoría actual
        int slot = 10;
        for (KillMessageItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupMessageButton(inv, slot, item, player);
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver a las categorías"));

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
            victimName = "Enemigo";
        } else {
            Player randomPlayer = players.get((int) (Math.random() * players.size()));
            victimName = randomPlayer.getName();
        }
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getMessage()
            .replace("{killer}", player.getName())
            .replace("{victim}", victimName)));
        lore.add("");
        
        if (hasMessage) {
            if (isSelected) {
                lore.add("&aSeleccionado actualmente");
                lore.add("&eClick para deseleccionar");
            } else {
                lore.add("&eClick para seleccionar");
            }
        } else {
            lore.add("&7Click para comprar");
            lore.add("");
            lore.add("&8➥ Precio: &e" + item.getPrice() + " KGCoins");
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
            plugin.getMenuManager().openMenu(player, "kill_message_categories");
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
                player.sendMessage(MessageUtils.getColor("&aHas deseleccionado el mensaje. Usando mensajes por defecto."));
            } else {
                plugin.getCosmeticManager().setPlayerKillMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor("&aHas seleccionado el mensaje: " + messageItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= messageItem.getPrice()) {
                stats.removeKGCoins(messageItem.getPrice());
                plugin.getCosmeticManager().addPlayerKillMessage(player.getUniqueId(), messageItem.getName());
                plugin.getCosmeticManager().setPlayerKillMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor("&a¡Has comprado y seleccionado el mensaje " + 
                    messageItem.getName() + " &apor &e" + messageItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor("&cNo tienes suficientes KGCoins para comprar este mensaje."));
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
