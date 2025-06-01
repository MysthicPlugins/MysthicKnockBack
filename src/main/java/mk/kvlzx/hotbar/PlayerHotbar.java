package mk.kvlzx.hotbar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.cosmetics.BlockShopItem;
import mk.kvlzx.cosmetics.KnockerShopItem;
import mk.kvlzx.data.InventoryData;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.items.CustomItem.ItemType;
import mk.kvlzx.utils.BlockUtils;

public class PlayerHotbar {
    private static final Map<UUID, ItemStack[]> playerLayouts = new HashMap<>();
    private static InventoryData inventoryData;

    // Eliminar el DEFAULT_LAYOUT estático y reemplazarlo con un método
    private static ItemStack[] getDefaultLayout(UUID uuid) {
        ItemStack[] layout = new ItemStack[9];
        
        // Obtener el bloque cosmético del jugador
        Material blockType = MysthicKnockBack.getInstance().getCosmeticManager().getPlayerBlock(uuid);
        // Obtener el knocker cosmético del jugador
        Material knockerType = MysthicKnockBack.getInstance().getCosmeticManager().getPlayerKnocker(uuid);
        
        // Crear el knocker
        KnockerShopItem knockerItem = KnockerShopItem.getByMaterial(knockerType);
        ItemStack knocker;
        if (knockerItem != null) {
            knocker = knockerItem.createItemStack();
        } else {
            knocker = CustomItem.create(ItemType.KNOCKER);
        }
        
        // Crear el bloque
        BlockShopItem shopItem = BlockShopItem.getByMaterial(blockType);
        ItemStack blocks;
        if (shopItem != null) {
            blocks = shopItem.createItemStack();
            blocks.setAmount(64);
        } else {
            blocks = new ItemStack(blockType, 64);
        }
        
        layout[0] = knocker.clone();
        layout[1] = blocks.clone();
        layout[2] = CustomItem.create(ItemType.BOW);
        layout[6] = CustomItem.create(ItemType.PLATE);
        layout[7] = CustomItem.create(ItemType.FEATHER);
        layout[8] = CustomItem.create(ItemType.PEARL);
        
        return layout;
    }

    public static void init(InventoryData data) {
        inventoryData = data;
    }

    public static void setPlayerLayout(UUID uuid, ItemStack[] layout) {
        ItemStack[] copy = new ItemStack[9];
        System.arraycopy(layout, 0, copy, 0, 9);
        playerLayouts.put(uuid, copy);
        
        // Guardar en archivo
        if (inventoryData != null) {
            inventoryData.saveLayout(uuid, copy);
        }
    }

    public static ItemStack[] getPlayerLayout(UUID uuid) {
        if (!playerLayouts.containsKey(uuid) && inventoryData != null) {
            // Intentar cargar del archivo si no está en memoria
            if (inventoryData.hasLayout(uuid)) {
                ItemStack[] layout = inventoryData.loadLayout(uuid);
                updateBlocksInLayout(layout, uuid); // Nuevo método para actualizar los bloques
                playerLayouts.put(uuid, layout);
                return layout.clone();
            }
        }
        return playerLayouts.getOrDefault(uuid, getDefaultLayout(uuid)).clone();
    }

    // Nuevo método para actualizar los bloques en un layout existente
    private static void updateBlocksInLayout(ItemStack[] layout, UUID uuid) {
        if (layout == null) return;
        
        Material blockType = MysthicKnockBack.getInstance().getCosmeticManager().getPlayerBlock(uuid);
        BlockShopItem shopItem = BlockShopItem.getByMaterial(blockType);
        
        if (shopItem != null) {
            ItemStack blocks = shopItem.createItemStack();
            blocks.setAmount(64);
            
            // Actualizar todos los slots que contengan el bloque
            for (int i = 0; i < layout.length; i++) {
                if (layout[i] != null && BlockUtils.isDecorativeBlock(layout[i].getType())) {
                    layout[i] = blocks.clone();
                }
            }
        }
    }

    public static void applyLayout(Player player) {
        ItemStack[] layout = getPlayerLayout(player.getUniqueId());
        for (int i = 0; i < 9; i++) {
            if (layout[i] != null) {
                ItemStack item = layout[i].clone();
                // Si es un bloque decorativo, recrear el item con los metadatos correctos
                if (BlockUtils.isDecorativeBlock(item.getType())) {
                    Material blockType = MysthicKnockBack.getInstance().getCosmeticManager().getPlayerBlock(player.getUniqueId());
                    BlockShopItem shopItem = BlockShopItem.getByMaterial(blockType);
                    if (shopItem != null) {
                        item = shopItem.createItemStack();
                        item.setAmount(64);
                    }
                }
                player.getInventory().setItem(i, item);
            }
        }
        // Flechas siempre en el slot 9
        player.getInventory().setItem(9, CustomItem.create(ItemType.ARROW));
        player.updateInventory();
    }

    public static void resetLayout(UUID uuid) {
        playerLayouts.put(uuid, getDefaultLayout(uuid));
        
        // También eliminar del archivo de configuración
        if (inventoryData != null) {
            inventoryData.removeLayout(uuid);
        }
    }
}
