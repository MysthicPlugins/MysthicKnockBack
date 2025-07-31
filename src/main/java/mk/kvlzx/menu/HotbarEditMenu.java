package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.HotbarMenuConfig;
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
        
        // Mostrar el arma seleccionada actualmente
        ItemType selectedWeapon = plugin.getWeaponManager().getSelectedWeapon(player.getUniqueId());
        inv.setItem(menuConfig.getWeaponSlot(), CustomItem.create(selectedWeapon));
        
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

        // Botón para cambiar arma - mostrar el arma seleccionada con lore personalizado
        ItemStack weaponToggle = CustomItem.create(selectedWeapon);
        ItemMeta toggleMeta = weaponToggle.getItemMeta();
        if (toggleMeta != null) {
            toggleMeta.setDisplayName(MessageUtils.getColor(menuConfig.getWeaponToggleName()));
            toggleMeta.setLore(getWeaponToggleLore(selectedWeapon));
            weaponToggle.setItemMeta(toggleMeta);
        }
        inv.setItem(menuConfig.getWeaponToggleSlot(), weaponToggle);
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

        // Manejar botones usando configuración
        if (slot == menuConfig.getSaveSlot()) {
            handleSaveButton(event, player);
            return;
        } else if (slot == menuConfig.getResetSlot()) {
            handleResetButton(event, player);
            return;
        } else if (slot == menuConfig.getBackSlot()) {
            handleBackButton(event, player);
            return;
        } else if (slot == menuConfig.getWeaponToggleSlot()) {
            handleWeaponToggleButton(event, player);
            return;
        }

        // Prevenir mover items desde el área de muestra
        if (isDisplaySlot(slot)) {
            event.setCancelled(true);
            return;
        }

        // Permitir mover items solo en la zona de hotbar (slots 36-44)
        if (slot < 36 || slot >= 45) {
            event.setCancelled(true);
        }
    }

    private boolean isDisplaySlot(int slot) {
        return slot == menuConfig.getKnockerSlot() ||
                slot == menuConfig.getBlocksSlot() ||
                slot == menuConfig.getWeaponSlot() ||
                slot == menuConfig.getPlateSlot() ||
                slot == menuConfig.getFeatherSlot() ||
                slot == menuConfig.getPearlSlot() ||
                slot == menuConfig.getWeaponToggleSlot() ||
                menuConfig.getSeparatorSlots().contains(slot);
    }

    private void handleSaveButton(InventoryClickEvent event, Player player) {
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        saveHotbar(player);
        player.closeInventory();
        player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + menuConfig.getHotbarSavedMessage()));
    }

    private void handleResetButton(InventoryClickEvent event, Player player) {
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        PlayerHotbar.resetLayout(player.getUniqueId());
        player.closeInventory();
        player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + menuConfig.getHotbarResetMessage()));
    }

    private void handleBackButton(InventoryClickEvent event, Player player) {
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        plugin.getMenuManager().openMenu(player, "main");
    }

    private void handleWeaponToggleButton(InventoryClickEvent event, Player player) {
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        
        // Cambiar el arma seleccionada
        plugin.getWeaponManager().toggleWeapon(player.getUniqueId());
        
        // Actualizar ambos items: el display del arma y el botón de cambio
        ItemType selectedWeapon = plugin.getWeaponManager().getSelectedWeapon(player.getUniqueId());
        event.getInventory().setItem(menuConfig.getWeaponSlot(), CustomItem.create(selectedWeapon));
        
        // Actualizar el botón de cambio para mostrar el arma seleccionada
        ItemStack toggleButton = CustomItem.create(selectedWeapon);
        ItemMeta meta = toggleButton.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getColor(menuConfig.getWeaponToggleName()));
            meta.setLore(getWeaponToggleLore(selectedWeapon));
            toggleButton.setItemMeta(meta);
        }
        event.getInventory().setItem(menuConfig.getWeaponToggleSlot(), toggleButton);

        player.closeInventory();
        // Mensaje de confirmación
        String weaponName = getWeaponDisplayName(selectedWeapon);
        player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
            menuConfig.getWeaponChangedMessage().replace("%weapon%", weaponName)));
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
            return plugin.getKnockersShopConfig().getKnockerItems().values().stream()
                    .anyMatch(knocker -> knocker.getMaterial() == item.getType() && knocker.getData() == item.getDurability());
        }

        // Verificar otros items personalizados
        return Arrays.stream(ItemType.values())
                    .anyMatch(type -> {
                        ItemStack validItem = CustomItem.create(type);
                        return validItem.getType() == item.getType() &&
                            validItem.getDurability() == item.getDurability();
                    });
    }

    private List<String> getWeaponToggleLore(ItemType selectedWeapon) {
        List<String> lore = new ArrayList<>();
        for (String line : menuConfig.getWeaponToggleLore()) {
            String weaponName = getWeaponDisplayName(selectedWeapon);
            lore.add(MessageUtils.getColor(line.replace("%weapon%", weaponName)));
        }
        return lore;
    }

    // Método para obtener el nombre configurable del arma
    private String getWeaponDisplayName(ItemType weaponType) {
        return weaponType == ItemType.BOW ? 
            menuConfig.getBowDisplayName() : 
            menuConfig.getSlimeDisplayName();
    }
}
