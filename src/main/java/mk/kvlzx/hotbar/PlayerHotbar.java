package mk.kvlzx.hotbar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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

    public static void init(InventoryData data) {
        inventoryData = data;
    }

    // Método para obtener el layout por defecto personalizado por jugador
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
        
        // Obtener el arma seleccionada por el jugador
        ItemType selectedWeapon = MysthicKnockBack.getInstance().getWeaponManager().getSelectedWeapon(uuid);
        
        layout[0] = knocker.clone();
        layout[1] = blocks.clone();
        layout[2] = CustomItem.create(selectedWeapon); // Usar el arma seleccionada
        layout[6] = CustomItem.create(ItemType.PLATE);
        layout[7] = CustomItem.create(ItemType.FEATHER);
        layout[8] = CustomItem.create(ItemType.PEARL);
        
        return layout;
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
                updateLayoutItems(layout, uuid); // Actualizar items del layout
                playerLayouts.put(uuid, layout);
                return layout.clone();
            }
        }
        
        ItemStack[] layout = playerLayouts.getOrDefault(uuid, getDefaultLayout(uuid));
        // Siempre actualizar los items antes de devolver el layout
        updateLayoutItems(layout, uuid);
        return layout.clone();
    }

    // Método para actualizar items específicos en un layout existente
    private static void updateLayoutItems(ItemStack[] layout, UUID uuid) {
        if (layout == null) return;
        
        // Actualizar bloques cosméticos
        updateBlocksInLayout(layout, uuid);
        
        // Actualizar arma seleccionada
        updateWeaponInLayout(layout, uuid);
        
        // Actualizar knocker cosmético
        updateKnockerInLayout(layout, uuid);
    }

    // Actualizar bloques cosméticos en el layout
    private static void updateBlocksInLayout(ItemStack[] layout, UUID uuid) {
        Material blockType = MysthicKnockBack.getInstance().getCosmeticManager().getPlayerBlock(uuid);
        BlockShopItem shopItem = BlockShopItem.getByMaterial(blockType);
        
        if (shopItem != null) {
            ItemStack blocks = shopItem.createItemStack();
            blocks.setAmount(64);
            
            // Actualizar todos los slots que contengan bloques decorativos
            for (int i = 0; i < layout.length; i++) {
                if (layout[i] != null && BlockUtils.isDecorativeBlock(layout[i].getType())) {
                    layout[i] = blocks.clone();
                }
            }
        }
    }

    // Actualizar arma seleccionada en el layout
    private static void updateWeaponInLayout(ItemStack[] layout, UUID uuid) {
        ItemType selectedWeapon = MysthicKnockBack.getInstance().getWeaponManager().getSelectedWeapon(uuid);
        
        for (int i = 0; i < layout.length; i++) {
            if (layout[i] != null) {
                // Verificar si es un arma (BOW o SLIME_BALL)
                if (isWeaponItem(layout[i])) {
                    layout[i] = CustomItem.create(selectedWeapon);
                }
            }
        }
    }

    // Actualizar knocker cosmético en el layout
    private static void updateKnockerInLayout(ItemStack[] layout, UUID uuid) {
        Material knockerType = MysthicKnockBack.getInstance().getCosmeticManager().getPlayerKnocker(uuid);
        KnockerShopItem knockerItem = KnockerShopItem.getByMaterial(knockerType);
        
        if (knockerItem != null) {
            ItemStack knocker = knockerItem.createItemStack();
            
            // Actualizar todos los slots que contengan knockers
            for (int i = 0; i < layout.length; i++) {
                if (layout[i] != null && layout[i].containsEnchantment(Enchantment.KNOCKBACK)) {
                    layout[i] = knocker.clone();
                }
            }
        }
    }

    // Verificar si un item es un arma
    private static boolean isWeaponItem(ItemStack item) {
        if (item == null) return false;
        
        // Verificar si es un BOW o SLIME_BALL (las armas del juego)
        return item.getType() == Material.BOW || item.getType() == Material.SLIME_BALL;
    }

    public static void applyLayout(Player player) {
        ItemStack[] layout = getPlayerLayout(player.getUniqueId());
        for (int i = 0; i < 9; i++) {
            if (layout[i] != null) {
                ItemStack item = layout[i].clone();
                
                // Aplicar metadatos específicos según el tipo de item
                if (BlockUtils.isDecorativeBlock(item.getType())) {
                    // Recrear bloque con metadatos correctos
                    Material blockType = MysthicKnockBack.getInstance().getCosmeticManager().getPlayerBlock(player.getUniqueId());
                    BlockShopItem shopItem = BlockShopItem.getByMaterial(blockType);
                    if (shopItem != null) {
                        item = shopItem.createItemStack();
                        item.setAmount(64);
                    }
                } else if (item.containsEnchantment(Enchantment.KNOCKBACK)) {
                    // Recrear knocker con metadatos correctos
                    Material knockerType = MysthicKnockBack.getInstance().getCosmeticManager().getPlayerKnocker(player.getUniqueId());
                    KnockerShopItem knockerItem = KnockerShopItem.getByMaterial(knockerType);
                    if (knockerItem != null) {
                        item = knockerItem.createItemStack();
                    }
                } else if (isWeaponItem(item)) {
                    // Recrear arma con el tipo seleccionado
                    ItemType selectedWeapon = MysthicKnockBack.getInstance().getWeaponManager().getSelectedWeapon(player.getUniqueId());
                    item = CustomItem.create(selectedWeapon);
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

    // Método para limpiar la caché de un jugador específico
    public static void clearPlayerCache(UUID uuid) {
        playerLayouts.remove(uuid);
    }

    // Método para obtener todos los jugadores con layouts guardados
    public static Set<UUID> getAllPlayersWithLayouts() {
        return new HashSet<>(playerLayouts.keySet());
    }
}
