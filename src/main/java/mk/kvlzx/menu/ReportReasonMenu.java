package mk.kvlzx.menu;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.ReportMenuConfig;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.reports.ReportReason;
import mk.kvlzx.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ReportReasonMenu extends Menu {
    private final ReportMenuConfig menuConfig;

    public ReportReasonMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getReportMenuConfig().getMenuReportReasonTitle(), plugin.getReportMenuConfig().getMenuReportReasonSize());
        this.menuConfig = plugin.getReportMenuConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());

        // Cabeza del jugador a reportar
        ItemStack targetHead = CustomItem.createSkullFromUUID(
            plugin.getServer().getPlayer(targetName).getUniqueId(),
            menuConfig.getMenuReportReasonItemSkullName().replace("%target_name%", targetName),
            menuConfig.getMenuReportReasonItemSkullLore().toArray(new String[0])
        );
        inv.setItem(menuConfig.getMenuReportReasonItemSkullSlot(), targetHead);

        // Razones de reporte centradas
        List<Integer> reasonSlotList = menuConfig.getMenuReportReasonItemReasonSlots();
        int[] reasonSlots = reasonSlotList.stream().mapToInt(Integer::intValue).toArray();
        int index = 0;
        for (ReportReason reason : ReportReason.values()) {
            if (index >= reasonSlots.length) break;
            
            List<String> lore = new ArrayList<>();
            for (String line : menuConfig.getMenuReportReasonItemReasonLore()) {
                lore.add(MessageUtils.getColor(line.replace("%reason_lore%", reason.getDescription())));
            }
            
            inv.setItem(reasonSlots[index], createItem(
                reason.getIcon(),
                reason.getDisplayName(),
                lore.toArray(new String[0])
            ));
            index++;
        }

        // Crear lanas para el patrón
        ItemStack blackWool = createItem(Material.WOOL, "&8", (byte) 15); // Lana negra
        ItemStack yellowWool = createItem(Material.WOOL, "&e", (byte) 4); // Lana amarilla
        
        // Fila superior (slots 0-8)
        for (int i = 0; i < 9; i++) {
            if (i != 4) { // Slot 4 se deja para evitar conflictos, pero no tiene nada especial
                inv.setItem(i, (i % 2 == 0) ? blackWool : yellowWool);
            } else {
                inv.setItem(i, (i % 2 == 0) ? blackWool : yellowWool);
            }
        }
        
        // Filas intermedias - solo bordes laterales (filas 1-4)
        for (int row = 1; row < 5; row++) {
            // Borde izquierdo
            inv.setItem(row * 9, (row % 2 == 0) ? yellowWool : blackWool);
            // Borde derecho
            inv.setItem(row * 9 + 8, (row % 2 == 0) ? yellowWool : blackWool);
        }
        
        // Fila inferior (slots 45-53)
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, (i % 2 == 0) ? yellowWool : blackWool);
        }

        // Botón para cancelar
        inv.setItem(menuConfig.getMenuReportReasonItemBackSlot(), 
            createItem(Material.valueOf(menuConfig.getMenuReportReasonItemBackId()), 
            menuConfig.getMenuReportReasonItemBackName(), 
            menuConfig.getMenuReportReasonItemBackLore().toArray(new String[0])
        ));
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

        if (clicked == null || clicked.getType() == Material.WOOL || clicked.getType() == Material.SKULL_ITEM) return;

        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());
        if (targetName == null) {
            player.closeInventory();
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + menuConfig.getMenuReportReasonMessageError()));
            return;
        }

        // Botón cancelar
        if (event.getSlot() == menuConfig.getMenuReportReasonItemBackSlot() &&
        clicked.getType() == Material.valueOf(menuConfig.getMenuReportReasonItemBackId())) {
            plugin.getMenuManager().openMenu(player, "player_list");
            return;
        }

        // Verificar si es una razón de reporte (basado en los slots)
        List<Integer> reasonSlotList = menuConfig.getMenuReportReasonItemReasonSlots();
        int[] reasonSlots = reasonSlotList.stream().mapToInt(Integer::intValue).toArray();
        for (int i = 0; i < reasonSlots.length; i++) {
            if (event.getSlot() == reasonSlots[i] && i < ReportReason.values().length) {
                ReportReason selectedReason = ReportReason.values()[i];
                plugin.getReportManager().submitReport(player, targetName, selectedReason);
                player.closeInventory();
                return;
            }
        }
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
