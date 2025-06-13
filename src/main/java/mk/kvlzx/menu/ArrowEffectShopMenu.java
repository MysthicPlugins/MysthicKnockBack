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
    private static String currentCategory = "COMMON";

    public ArrowEffectShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lEffects Shop &8•", 45);
        this.shopItems = initializeShopItems();
    }

    public static void setCurrentCategory(String category) {
        currentCategory = category;
    }

    private List<ArrowEffectItem> initializeShopItems() {
        List<ArrowEffectItem> items = new ArrayList<>();

        // Common effects
        items.add(new ArrowEffectItem(
            "Flame Trail", 15000, "COMMON", "&7",
            "&cLeaves a trail of flames!", 
            Effect.FLAME, 0.0f, 1, 0f, 0f, 0f));
        
        items.add(new ArrowEffectItem(
            "Water Splash", 15000, "COMMON", "&7",
            "&bSplashes water while flying!", 
            Effect.WATERDRIP, 0.0f, 2, 0.1f, 0.1f, 0.1f));

        // Epic effects
        items.add(new ArrowEffectItem(
            "Ender Magic", 35000, "EPIC", "&5",
            "&5The power of the End!", 
            Effect.PORTAL, 0.2f, 3, 0.1f, 0.1f, 0.1f));
        
        items.add(new ArrowEffectItem(
            "Slime Trail", 35000, "EPIC", "&5",
            "&aLeaves a sticky trail!", 
            Effect.SLIME, 0.1f, 2, 0f, 0f, 0f));

        // Legendary effects
        items.add(new ArrowEffectItem(
            "Rainbow Spirit", 75000, "LEGENDARY", "&6",
            "&dA rainbow of power!", 
            Effect.COLOURED_DUST, 0.2f, 4, 0.2f, 0.2f, 0.2f));
        
        items.add(new ArrowEffectItem(
            "Firework Show", 75000, "LEGENDARY", "&6",
            "&eFireworks!", 
            Effect.FIREWORKS_SPARK, 0.1f, 3, 0.1f, 0.1f, 0.1f));

        return items;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        PlayerStats stats = PlayerStats.getStats(player.getUniqueId());

        // Mostrar el balance del jugador y la categoría actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current balance: &e" + stats.getKGCoins() + " KGCoins",
            "",
            "&7Current category: " + currentCategory));

        // Mostrar efectos por categoría
        int slot = 10;
        for (ArrowEffectItem item : shopItems) {
            if (item.getRarity().equals(currentCategory)) {
                if (slot > 34) break;
                setupEffectButton(inv, slot, item, player);
                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
            }
        }

        // Back button
        inv.setItem(40, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to categories"));

        // Filler
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
    }

    private void setupEffectButton(Inventory inv, int slot, ArrowEffectItem item, Player player) {
        boolean hasEffect = plugin.getCosmeticManager().hasPlayerArrowEffect(player.getUniqueId(), item.getName());
        boolean isSelected = plugin.getCosmeticManager().getPlayerArrowEffect(player.getUniqueId())
                            .equals(item.getName());

        List<String> lore = new ArrayList<>();
        lore.add(item.getRarityColor() + "✦ Rarity: " + item.getRarity());
        lore.add("");
        lore.add(MessageUtils.getColor(item.getDescription()));
        lore.add("");

        if (hasEffect) {
            if (isSelected) {
                lore.add("&aCurrently selected");
                lore.add("&eClick to deselect");
            } else {
                lore.add("&eClick to select");
            }
        } else {
            lore.add("&7Click to buy");
            lore.add("");
            lore.add("&8➥ Price: &e" + item.getPrice() + " KGCoins");
        }

        // Create the button with the appropriate material
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

        inv.setItem(slot, button);
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

        // Si ya tiene el efecto seleccionado
        if (plugin.getCosmeticManager().hasPlayerArrowEffect(player.getUniqueId(), effectItem.getName())) {
            // Si el efecto ya está seleccionado, lo deselecciona
            if (currentEffect.equals(effectItem.getName())) {
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), "none");
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&aYou have deselected the effect."));
            } else {
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&aYou have selected the effect: " + effectItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= effectItem.getPrice()) {
                stats.removeKGCoins(effectItem.getPrice());
                plugin.getCosmeticManager().addPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&aYou have purchased and selected the effect " + 
                    effectItem.getName() + " &afor &e" + effectItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.prefix + "&cYou don't have enough KGCoins to purchase this effect."));
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
