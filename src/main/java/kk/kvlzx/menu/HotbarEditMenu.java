package kk.kvlzx.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.hotbar.PlayerHotbar;
import kk.kvlzx.items.CustomItem;
import kk.kvlzx.items.CustomItem.ItemType;
import kk.kvlzx.utils.MessageUtils;

public class HotbarEditMenu extends Menu {

    public HotbarEditMenu(KvKnockback plugin) {
        super(plugin, "&8• &e&lEditar Hotbar &8•", 54);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Items visuales
        inv.setItem(9, CustomItem.create(ItemType.KNOCKER));
        inv.setItem(10, CustomItem.create(ItemType.BLOCKS));
        inv.setItem(11, CustomItem.create(ItemType.BOW));
        inv.setItem(15, CustomItem.create(ItemType.PLATE));
        inv.setItem(16, CustomItem.create(ItemType.FEATHER));
        inv.setItem(17, CustomItem.create(ItemType.PEARL));

        // Separador
        ItemStack separator = createItem(Material.STAINED_GLASS_PANE, "&7• Tu Hotbar •", (byte) 15);
        for (int i = 27; i < 36; i++) {
            inv.setItem(i, separator);
        }

        // Hotbar actual del jugador
        ItemStack[] currentLayout = PlayerHotbar.getPlayerLayout(player.getUniqueId());
        for (int i = 0; i < 9; i++) {
            inv.setItem(36 + i, currentLayout[i]);
        }

        // Botones
        inv.setItem(45, createItem(Material.EMERALD_BLOCK, "&a&lGuardar", "&7Click para guardar tu hotbar"));
        inv.setItem(46, createItem(Material.REDSTONE_BLOCK, "&c&lRestablecer", "&7Click para restablecer al default"));
        inv.setItem(49, createItem(Material.ARROW, "&c← Volver", "&7Click para volver al menú"));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Cancelar shift clicks
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        // Permitir mover items solo en la zona de hotbar y solo dentro del inventario superior
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            if (slot < 36 || slot >= 45) {
                event.setCancelled(true);
            }
        }

        switch (slot) {
            case 45: // Guardar
                saveHotbar(player);
                player.closeInventory();
                player.sendMessage(MessageUtils.getColor("&aHotbar guardada correctamente"));
                break;
            case 46: // Restablecer
                PlayerHotbar.resetLayout(player.getUniqueId());
                player.closeInventory();
                player.sendMessage(MessageUtils.getColor("&aHotbar restablecida al default"));
                break;
            case 49: // Volver
                plugin.getMenuManager().openMenu(player, "main");
                break;
        }
    }

    private void saveHotbar(Player player) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        ItemStack[] hotbar = new ItemStack[9];
        
        for (int i = 0; i < 9; i++) {
            hotbar[i] = inv.getItem(36 + i);
        }

        // Validar que solo se usen items permitidos
        if (Arrays.stream(hotbar)
                    .filter(item -> item != null)
                    .allMatch(this::isValidItem)) {
            PlayerHotbar.setPlayerLayout(player.getUniqueId(), hotbar);
        }
    }

    private boolean isValidItem(ItemStack item) {
        return Arrays.stream(ItemType.values())
                    .anyMatch(type -> {
                        ItemStack validItem = CustomItem.create(type);
                        return validItem.getType() == item.getType() &&
                                validItem.getDurability() == item.getDurability();
                    });
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
