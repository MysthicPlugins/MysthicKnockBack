package mk.kvlzx.menu;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.HotbarMenuConfig;
import mk.kvlzx.cosmetics.KnockerShopItem;
import mk.kvlzx.hotbar.PlayerHotbar;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.items.CustomItem.ItemType;
import mk.kvlzx.utils.BlockUtils;
import mk.kvlzx.utils.MessageUtils;

public class HotbarEditMenu extends Menu {
    private final HotbarMenuConfig menuConfig;

    public HotbarEditMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getHotbarMenuConfig().getMenuTitle(), plugin.getHotbarMenuConfig().getMenuSize());
        this.menuConfig = plugin.getHotbarMenuConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Items visuales usando configuración
        inv.setItem(menuConfig.getKnockerSlot(), CustomItem.create(ItemType.KNOCKER));
        inv.setItem(menuConfig.getBlocksSlot(), CustomItem.create(ItemType.BLOCKS));
        inv.setItem(menuConfig.getBowSlot(), CustomItem.create(ItemType.BOW));
        inv.setItem(menuConfig.getPlateSlot(), CustomItem.create(ItemType.PLATE));
        inv.setItem(menuConfig.getFeatherSlot(), CustomItem.create(ItemType.FEATHER));
        inv.setItem(menuConfig.getPearlSlot(), CustomItem.create(ItemType.PEARL));

        // Separador usando configuración
        ItemStack separator = menuConfig.createMenuItem(
            menuConfig.getSeparatorMaterial(),
            player,
            menuConfig.getSeparatorName(),
            menuConfig.getSeparatorLore()
        );
        
        for (int slot : menuConfig.getSeparatorSlots()) {
            inv.setItem(slot, separator);
        }

        // Hotbar actual del jugador
        ItemStack[] currentLayout = PlayerHotbar.getPlayerLayout(player.getUniqueId());
        for (int i = 0; i < 9; i++) {
            inv.setItem(36 + i, currentLayout[i]);
        }

        // Botones usando configuración
        inv.setItem(menuConfig.getSaveSlot(), menuConfig.createMenuItem(
            menuConfig.getSaveMaterial(),
            player,
            menuConfig.getSaveName(),
            menuConfig.getSaveLore()
        ));

        inv.setItem(menuConfig.getResetSlot(), menuConfig.createMenuItem(
            menuConfig.getResetMaterial(),
            player,
            menuConfig.getResetName(),
            menuConfig.getResetLore()
        ));

        inv.setItem(menuConfig.getBackSlot(), menuConfig.createMenuItem(
            menuConfig.getBackMaterial(),
            player,
            menuConfig.getBackName(),
            menuConfig.getBackLore()
        ));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Validar que el click sea en el menú y no en el inventario del jugador
        if (!isValidClick(event)) {
            event.setCancelled(true);
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        int slot = event.getSlot();

        // Prevenir shift-clicks
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }

        // Prevenir dobles click
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }

        // Prevenir clicks en el inventario del jugador
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }

        // Prevenir mover items desde el área de muestra
        if (isDisplaySlot(slot)) {
            event.setCancelled(true);
            return;
        }

        // Permitir mover items solo en la zona de hotbar (slots 36-44)
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            if (slot < 36 || slot >= 45) {
                event.setCancelled(true);
            }
        }

        // Manejar botones usando configuración
        if (slot == menuConfig.getSaveSlot()) {
            handleSaveButton(event, player);
        } else if (slot == menuConfig.getResetSlot()) {
            handleResetButton(event, player);
        } else if (slot == menuConfig.getBackSlot()) {
            handleBackButton(event, player);
        }
    }

    private boolean isDisplaySlot(int slot) {
        return slot == menuConfig.getKnockerSlot() ||
                slot == menuConfig.getBlocksSlot() ||
                slot == menuConfig.getBowSlot() ||
                slot == menuConfig.getPlateSlot() ||
                slot == menuConfig.getFeatherSlot() ||
                slot == menuConfig.getPearlSlot() ||
                menuConfig.getSeparatorSlots().contains(slot);
    }

    private void handleSaveButton(InventoryClickEvent event, Player player) {
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        saveHotbar(player);
        player.closeInventory();
        player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + menuConfig.getHotbarSavedMessage()));
    }

    private void handleResetButton(InventoryClickEvent event, Player player) {
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        PlayerHotbar.resetLayout(player.getUniqueId());
        player.closeInventory();
        player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + menuConfig.getHotbarResetMessage()));
    }

    private void handleBackButton(InventoryClickEvent event, Player player) {
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        plugin.getMenuManager().openMenu(player, "main");
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
}