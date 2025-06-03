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

        // Mensajes Comunes (5)
        items.add(new DeathMessageItem(
            "&b%s &fbuscó la gloria, pero encontró un respawn.",
            "Buscador de Gloria", 10000, "COMÚN", "&7",
            "&7Un destino inevitable"
        ));
        items.add(new DeathMessageItem(
            "&b%s &fcorrió hacia la eternidad, pero se quedó sin batería.",
            "Corredor Eterno", 10000, "COMÚN", "&7",
            "&7La energía es limitada"
        ));
        items.add(new DeathMessageItem(
            "&b%s &fintentó un salto estelar y acabó en un cráter.",
            "Saltador Estelar", 10000, "COMÚN", "&7",
            "&7Las estrellas estaban muy lejos"
        ));
        items.add(new DeathMessageItem(
            "&b%s &fsoñó con volar libre, pero olvidó el paracaídas.",
            "Soñador Imprudente", 10000, "COMÚN", "&7",
            "&7La gravedad no perdona"
        ));
        items.add(new DeathMessageItem(
            "&b%s &fintentó un salto al infinito y aterrizó en el spawn.",
            "Saltador Infinito", 10000, "COMÚN", "&7",
            "&7El spawn siempre espera"
        ));

        // Mensajes Épicos (8)
        items.add(new DeathMessageItem(
            "&b%s &5danzó con las sombras y tropezó en la oscuridad.",
            "Bailarín de Sombras", 25000, "ÉPICO", "&5",
            "&5Las sombras son traicioneras"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5desafió al destino y el destino le dio una lección.",
            "Desafiante del Destino", 25000, "ÉPICO", "&5",
            "&5El destino siempre gana"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5soñó con ser un titán, pero el suelo fue su juez.",
            "Aspirante a Titán", 25000, "ÉPICO", "&5",
            "&5El suelo es implacable"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5quiso escribir su saga, pero el capítulo terminó pronto.",
            "Escritor Frustrado", 25000, "ÉPICO", "&5",
            "&5Una historia inconclusa"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5quiso ser un cometa, pero se estrelló como meteorito.",
            "Cometa Fallido", 25000, "ÉPICO", "&5",
            "&5Brilló hasta el final"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5soñó con tocar las nubes, pero el suelo lo despertó.",
            "Soñador de Nubes", 25000, "ÉPICO", "&5",
            "&5Un despertar brusco"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5corrió con el alma en llamas, pero el fuego se apagó.",
            "Alma en Llamas", 25000, "ÉPICO", "&5",
            "&5Las llamas son efímeras"
        ));
        items.add(new DeathMessageItem(
            "&b%s &5quiso ser un héroe, pero la arena escribió otra historia.",
            "Héroe Frustrado", 25000, "ÉPICO", "&5",
            "&5La arena es cruel"
        ));

        // Mensajes Legendarios (7)
        items.add(new DeathMessageItem(
            "&b%s &6quiso conquistar el cielo, pero el abismo lo reclamó.",
            "Conquistador Caído", 50000, "LEGENDARIO", "&6",
            "&6El abismo siempre reclama"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6saltó al infinito, pero el infinito no respondió.",
            "Saltador del Vacío", 50000, "LEGENDARIO", "&6",
            "&6El infinito es silencioso"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6desafió al vacío y el vacío le dio un abrazo mortal.",
            "Desafiante del Vacío", 50000, "LEGENDARIO", "&6",
            "&6El vacío siempre abraza"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6brilló como un relámpago antes de apagarse en la tormenta.",
            "Relámpago Fugaz", 50000, "LEGENDARIO", "&6",
            "&6La tormenta consume todo"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6danzó en el filo del peligro y el filo cortó primero.",
            "Bailarín del Filo", 50000, "LEGENDARIO", "&6",
            "&6El filo no perdona"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6desafió al abismo y el abismo le susurró 'hasta pronto'.",
            "Susurro del Abismo", 50000, "LEGENDARIO", "&6",
            "&6El abismo siempre vuelve"
        ));
        items.add(new DeathMessageItem(
            "&b%s &6quiso ser un poema épico, pero rimó con derrota.",
            "Poeta Derrotado", 50000, "LEGENDARIO", "&6",
            "&6La derrota también rima"
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
        boolean hasMessage = plugin.getCosmeticManager().hasPlayerDeathMessage(player.getUniqueId(), item.getName());
        boolean isSelected = currentMessage.equals(item.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor("&7Mensaje: " + item.getMessage().replace("%s", player.getName())));
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
                player.sendMessage(MessageUtils.getColor("&aHas deseleccionado el mensaje. Usando mensajes por defecto."));
            } else {
                plugin.getCosmeticManager().setPlayerDeathMessage(player.getUniqueId(), messageItem.getName());
                player.sendMessage(MessageUtils.getColor("&aHas seleccionado el mensaje: " + messageItem.getName()));
            }
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
