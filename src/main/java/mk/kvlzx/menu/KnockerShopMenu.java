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
import mk.kvlzx.cosmetics.KnockerShopItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;

public class KnockerShopMenu extends Menu {
    private static String currentCategory = "COMÚN";

    public KnockerShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lTienda de Knockers &8•", 45);
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        KnockerShopItem currentKnocker = plugin.getCosmeticManager().getPlayerKnocker(player.getUniqueId());

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lTu Balance",
            "&7Balance actual: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Categoría actual: " + currentCategory));

        // Mostrar knockers de la categoría actual
        int slot = 10;
        for (KnockerShopItem item : KnockerShopItem.getAllKnockers()) {
            if (item.getRarity().equals(currentCategory)) {
                setupKnockerButton(inv, slot, item, player, currentKnocker);
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver a las categorías"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15);
        fillEmptySlots(inv, filler);
    }

    private void setupKnockerButton(Inventory inv, int slot, KnockerShopItem item, Player player, KnockerShopItem currentKnocker) {
        boolean hasKnocker = item.getMaterial() == Material.STICK || plugin.getCosmeticManager().getPlayerKnocker(player.getUniqueId()) == item;
        boolean isSelected = currentKnocker != null && currentKnocker.getMaterial() == item.getMaterial() && currentKnocker.getData() == item.getData();
        
        List<String> lore = new ArrayList<>();
        
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getDescription()));
        lore.add("");
        
        if (item.getMaterial() == Material.STICK && item.getData() == 0) {
            lore.add("&aKnocker por defecto");
            lore.add("&8➥ Siempre disponible");
            if (isSelected) {
                lore.add("");
                lore.add("&aSeleccionado actualmente");
            } else {
                lore.add("");
                lore.add("&eClick para seleccionar");
            }
        } else if (hasKnocker) {
            if (isSelected) {
                lore.add("&aSeleccionado actualmente");
                lore.add("&8➥ Usando este knocker");
            } else {
                lore.add("&eClick para seleccionar");
                lore.add("&8➥ Ya posees este knocker");
            }
        } else {
            lore.add("&7Click para comprar");
            lore.add("");
            lore.add("&8➥ Precio: &e" + item.getPrice() + " KGCoins");
        }

        String displayName = (isSelected ? "&b" : item.getRarityColor()) + item.getName();
        ItemStack buttonItem = createItem(item.getMaterial(), displayName, item.getData(), lore.toArray(new String[0]));
        
        if (isSelected) {
            buttonItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            ItemMeta meta = buttonItem.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            buttonItem.setItemMeta(meta);
        }

        inv.setItem(slot, buttonItem);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "knocker_categories");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        KnockerShopItem shopItem = findKnockerItem(clicked);
        if (shopItem == null) return;

        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        KnockerShopItem currentKnocker = plugin.getCosmeticManager().getPlayerKnocker(player.getUniqueId());

        // Si ya tiene el knocker o es el palo por defecto
        if (shopItem.getMaterial() == Material.STICK || 
            (currentKnocker != null && currentKnocker.getMaterial() == shopItem.getMaterial() && currentKnocker.getData() == shopItem.getData())) {
            plugin.getCosmeticManager().setPlayerKnocker(player.getUniqueId(), shopItem);
            player.sendMessage(MessageUtils.getColor("&aHas seleccionado el knocker " + shopItem.getName()));
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= shopItem.getPrice()) {
                stats.removeKGCoins(shopItem.getPrice());
                plugin.getCosmeticManager().setPlayerKnocker(player.getUniqueId(), shopItem);
                player.sendMessage(MessageUtils.getColor("&a¡Has comprado y seleccionado el knocker " + 
                    shopItem.getName() + " &apor &e" + shopItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor("&cNo tienes suficientes KGCoins para comprar este knocker."));
            }
        }
    }

    private KnockerShopItem findKnockerItem(ItemStack clicked) {
        return KnockerShopItem.getAllKnockers().stream()
            .filter(item -> item.getMaterial() == clicked.getType() && item.getData() == clicked.getDurability())
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
