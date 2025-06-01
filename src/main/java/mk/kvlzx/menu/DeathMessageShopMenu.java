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
    private static String currentCategory = "COMÚN";

    public DeathMessageShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lTienda de Mensajes &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<DeathMessageItem> initializeShopItems() {
        List<DeathMessageItem> items = new ArrayList<>();

        // Común
        items.add(new DeathMessageItem(
            "&b%s &ese resbaló con una cáscara de plátano. &6¡Qué torpe!",
            "Mensaje por defecto", 0, "COMÚN", "&7",
            "&7El clásico mensaje de siempre"
        ));
        
        items.add(new DeathMessageItem(
            "&b%s &equiso conquistar el cielo, pero el abismo lo reclamó.",
            "Conquistador caído", 10000, "COMÚN", "&7",
            "&7Una caída con estilo"
        ));

        // Épico
        items.add(new DeathMessageItem(
            "&b%s &5soñó con ser un titán, pero el suelo fue su juez.",
            "Sueños rotos", 25000, "ÉPICO", "&5",
            "&5Un final dramático"
        ));

        items.add(new DeathMessageItem(
            "&b%s &5brilló como un relámpago antes de apagarse en la tormenta.",
            "Brillo efímero", 25000, "ÉPICO", "&5",
            "&5Una muerte poética"
        ));

        // Legendario
        items.add(new DeathMessageItem(
            "&b%s &6desafió al destino y el destino le dio una lección.",
            "Desafiante del destino", 50000, "LEGENDARIO", "&6",
            "&6Una muerte legendaria"
        ));

        items.add(new DeathMessageItem(
            "&b%s &6quiso escribir su saga, pero el capítulo terminó pronto.",
            "Saga interrumpida", 50000, "LEGENDARIO", "&6",
            "&6Un final épico"
        ));

        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentMessage = plugin.getCosmeticManager().getPlayerDeathMessage(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lTu Balance",
            "&7Balance actual: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Categoría actual: " + currentCategory));

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
        inv.setItem(40, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver a las categorías"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
    }

    private void setupMessageButton(Inventory inv, int slot, DeathMessageItem item, Player player, String currentMessage) {
        boolean hasMessage = item.getName().equals("Mensaje por defecto") || 
                            plugin.getCosmeticManager().hasPlayerDeathMessage(player.getUniqueId(), item.getName());
        boolean isSelected = currentMessage.equals(item.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor("&7Mensaje: " + item.getMessage().replace("%s", "Jugador")));
        lore.add("");
        
        if (item.getName().equals("Mensaje por defecto")) {
            lore.add("&aMensaje por defecto");
            lore.add("&8➥ Siempre disponible");
        } else if (hasMessage) {
            if (isSelected) {
                lore.add("&aSeleccionado actualmente");
            } else {
                lore.add("&eClick para seleccionar");
            }
        } else {
            lore.add("&7Click para comprar");
            lore.add("");
            lore.add("&8➥ Precio: &e" + item.getPrice() + " KGCoins");
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

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        if (plugin.getCosmeticManager().hasPlayerDeathMessage(player.getUniqueId(), messageItem.getName())) {
            plugin.getCosmeticManager().setPlayerDeathMessage(player.getUniqueId(), messageItem.getName());
            player.sendMessage(MessageUtils.getColor("&aHas seleccionado el mensaje: " + messageItem.getName()));
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= messageItem.getPrice()) {
                stats.removeKGCoins(messageItem.getPrice());
                plugin.getCosmeticManager().addPlayerDeathMessage(player.getUniqueId(), messageItem.getName());
                plugin.getCosmeticManager().setPlayerDeathMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor("&a¡Has comprado y seleccionado el mensaje " + 
                    messageItem.getName() + " &apor &e" + messageItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor("&cNo tienes suficientes KGCoins para comprar este mensaje."));
            }
        }
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
