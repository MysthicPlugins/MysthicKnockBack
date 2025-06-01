package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.KnockerShopItem;
import mk.kvlzx.hotbar.PlayerHotbar;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.items.CustomItem.ItemType;
import mk.kvlzx.utils.BlockUtils;
import mk.kvlzx.utils.MessageUtils;

public class HotbarEditMenu extends Menu {

    public HotbarEditMenu(MysthicKnockBack plugin) {
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

        // Prevenir shift-clicks
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        // Prevenir clicks en el inventario del jugador
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }

        // Prevenir mover items desde el área de muestra (slots 9-17)
        if (slot >= 9 && slot <= 17) {
            event.setCancelled(true);
            return;
        }

        // Permitir mover items solo en la zona de hotbar (slots 36-44)
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            if (slot < 36 || slot >= 45) {
                event.setCancelled(true);
            }
        }

        // Manejar botones
        switch (slot) {
            case 45: // Guardar
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    event.setCancelled(true);
                    return;
                }
                saveHotbar(player);
                player.closeInventory();
                player.sendMessage(MessageUtils.getColor("&aHotbar guardada correctamente"));
                break;
            case 46: // Restablecer
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    event.setCancelled(true);
                    return;
                }
                PlayerHotbar.resetLayout(player.getUniqueId());
                player.closeInventory();
                player.sendMessage(MessageUtils.getColor("&aHotbar restablecida al default"));
                break;
            case 49: // Volver
                // Si tiene un item en el cursor, no permitir salir
                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    event.setCancelled(true);
                    return;
                }
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
        // Si es un bloque decorativo
        if (BlockUtils.isDecorativeBlock(item.getType())) {
            return true;
        }

        // Si es un knocker personalizado (tiene encantamiento Knockback)
        if (item.containsEnchantment(Enchantment.KNOCKBACK)) {
            // Verificar si es un knocker válido comparando con los registrados
            return Arrays.stream(ItemType.values()).anyMatch(type -> type == ItemType.KNOCKER) || 
                    KnockerShopItem.getByMaterial(item.getType()) != null;
        }

        // Verificar otros items personalizados
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
