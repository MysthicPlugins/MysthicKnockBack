package mk.kvlzx.menu;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.ArrowEffectItem;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import java.util.ArrayList;
import java.util.List;

public class ArrowEffectShopMenu extends Menu {
    private final List<ArrowEffectItem> shopItems;
    private static String currentCategory = "COMÚN";

    public ArrowEffectShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lTienda de Efectos &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<ArrowEffectItem> initializeShopItems() {
        List<ArrowEffectItem> items = new ArrayList<>();

        // Efectos comunes
        items.add(new ArrowEffectItem(
            "Flame Trail", 15000, "COMÚN", "&7",
            "¡Deja un rastro de llamas!", 
            Effect.FLAME, 0.0f, 1, 0f, 0f, 0f));
        
        items.add(new ArrowEffectItem(
            "Water Splash", 15000, "COMÚN", "&7",
            "¡Salpica agua al volar!", 
            Effect.WATERDRIP, 0.0f, 2, 0.1f, 0.1f, 0.1f));

        // Efectos épicos
        items.add(new ArrowEffectItem(
            "Ender Magic", 35000, "ÉPICO", "&5",
            "¡El poder del End!", 
            Effect.PORTAL, 0.2f, 3, 0.1f, 0.1f, 0.1f));
        
        items.add(new ArrowEffectItem(
            "Slime Trail", 35000, "ÉPICO", "&5",
            "¡Deja un rastro pegajoso!", 
            Effect.SLIME, 0.1f, 2, 0f, 0f, 0f));

        // Efectos legendarios
        items.add(new ArrowEffectItem(
            "Rainbow Spirit", 75000, "LEGENDARIO", "&6",
            "¡Un arcoíris de poder!", 
            Effect.COLOURED_DUST, 0.2f, 4, 0.2f, 0.2f, 0.2f));
        
        items.add(new ArrowEffectItem(
            "Firework Show", 75000, "LEGENDARIO", "&6",
            "¡Fuegos artificiales!", 
            Effect.FIREWORKS_SPARK, 0.1f, 3, 0.1f, 0.1f, 0.1f));

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

        // Mostrar efectos
        int slot = 10;
        for (ArrowEffectItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                setupEffectButton(inv, slot, item, player);
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

    private void setupEffectButton(Inventory inv, int slot, ArrowEffectItem item, Player player) {
        boolean hasEffect = plugin.getCosmeticManager().hasPlayerArrowEffect(player.getUniqueId(), item.getName());
        boolean isSelected = plugin.getCosmeticManager().getPlayerArrowEffect(player.getUniqueId())
                            .equals(item.getName());

        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rareza: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getDescription()));
        lore.add("");

        if (hasEffect) {
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
        Material material = isSelected ? Material.ARROW : Material.ARROW;
        ItemStack button = createItem(material, 
            (isSelected ? "&b" : item.getRarityColor()) + item.getName(), 
            lore.toArray(new String[0]));

        if (isSelected) {
            ItemMeta meta = button.getItemMeta();
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            button.setItemMeta(meta);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "arrow_effect_categories");
            return;
        }

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE || 
            clicked.getType() == Material.EMERALD) return;

        String itemName = clicked.getItemMeta().getDisplayName();
        ArrowEffectItem effectItem = findEffectItem(MessageUtils.stripColor(itemName));
        if (effectItem == null) return;

        handleEffectSelection(player, effectItem);
    }

    private void handleEffectSelection(Player player, ArrowEffectItem effectItem) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());
        String currentEffect = plugin.getCosmeticManager().getPlayerArrowEffect(player.getUniqueId());

        // Si ya tiene el efecto
        if (plugin.getCosmeticManager().hasPlayerArrowEffect(player.getUniqueId(), effectItem.getName())) {
            // Si está seleccionado, deseleccionar
            if (currentEffect.equals(effectItem.getName())) {
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), "none");
                player.sendMessage(MessageUtils.getColor("&aHas deseleccionado el efecto."));
            } else {
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                player.sendMessage(MessageUtils.getColor("&aHas seleccionado el efecto: " + effectItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= effectItem.getPrice()) {
                stats.removeKGCoins(effectItem.getPrice());
                plugin.getCosmeticManager().addPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                player.sendMessage(MessageUtils.getColor("&a¡Has comprado y seleccionado el efecto " + 
                    effectItem.getName() + " &apor &e" + effectItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor("&cNo tienes suficientes KGCoins para comprar este efecto."));
            }
        }
    }

    private ArrowEffectItem findEffectItem(String name) {
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
