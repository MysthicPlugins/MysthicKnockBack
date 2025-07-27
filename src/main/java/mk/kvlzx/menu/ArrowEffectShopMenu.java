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

    public ArrowEffectShopMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &e&lEffects Shop &8•", 54);
        this.shopItems = initializeShopItems();
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

        // Balance actual
        inv.setItem(4, createItem(Material.EMERALD, "&a&lYour Balance",
            "&7Current Balance: &e" + stats.getKGCoins() + " KGCoins"));

        // Slots disponibles para efectos (evitando el balance y botón de volver)
        int[] availableSlots = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44,
            45, 46, 47, 48, 50, 51, 52, 53
        };

        int slotIndex = 0;
        for (ArrowEffectItem item : shopItems) {
            if (slotIndex >= availableSlots.length) break;
            
            int slot = availableSlots[slotIndex];
            setupEffectButton(inv, slot, item, player);
            slotIndex++;
        }

        // Botón para volver
        inv.setItem(49, createItem(Material.ARROW, "&c← Back", 
            "&7Click to return to the shop"));

        // Relleno
        fillEmptySlots(inv, createItem(Material.STAINED_GLASS_PANE, " ", (byte) 7));
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
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have deselected the effect."));
            } else {
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have selected the effect: " + effectItem.getName()));
            }
            player.closeInventory();
        } else {
            if (stats.getKGCoins() >= effectItem.getPrice()) {
                stats.removeKGCoins(effectItem.getPrice());
                plugin.getCosmeticManager().addPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                plugin.getCosmeticManager().setPlayerArrowEffect(player.getUniqueId(), effectItem.getName());
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&aYou have purchased and selected the effect " +
                    effectItem.getName() + " &afor &e" + effectItem.getPrice() + " KGCoins&a!"));
                player.closeInventory();
            } else {
                player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cYou don't have enough KGCoins to purchase this effect."));
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
