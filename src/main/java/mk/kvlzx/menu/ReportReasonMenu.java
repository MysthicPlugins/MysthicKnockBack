package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.items.CustomItem;
import mk.kvlzx.reports.ReportReason;
import mk.kvlzx.utils.MessageUtils;

public class ReportReasonMenu extends Menu {

    public ReportReasonMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &c&lSelect Reason &8•", 54);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());

        // Cabeza del jugador a reportar
        ItemStack targetHead = CustomItem.createSkullFromUUID(
            plugin.getServer().getPlayer(targetName).getUniqueId(),
            "&c&lReporting: &f" + targetName,
            "&8▪ &7Select a reason for the report",
            "",
            "&8➥ &7Choose carefully"
        );
        inv.setItem(13, targetHead);

        // Razones de reporte centradas (5 arriba y 1 abajo)
        int[] reasonSlots = {20, 21, 22, 23, 24, 31}; // 6 slots
        int index = 0;
        for (ReportReason reason : ReportReason.values()) {
            if (index >= reasonSlots.length) break;
            
            List<String> lore = new ArrayList<>();
            lore.add("&8▪ &7Click to select this reason");
            lore.add("");
            lore.add("&8➥ &7" + reason.getDescription());
            
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

        // Botón para cancelar (slot 49)
        inv.setItem(49, createItem(Material.ARROW, "&c← Cancel", 
            "&7Click to return to the player list"));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
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
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + "&cError: Player to report not found"));
            return;
        }

        // Botón cancelar
        if (event.getSlot() == 49 && clicked.getType() == Material.ARROW) {
            plugin.getMenuManager().openMenu(player, "player_list");
            return;
        }

        // Verificar si es una razón de reporte (basado en los slots)
        int[] reasonSlots = {20, 21, 22, 23, 24, 31};
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
        meta.setDisplayName(MessageUtils.getColor(reason.getName()));
        List<String> lore = new ArrayList<>();
        for (String line : reason.getLore()) {
            lore.add(MessageUtils.getColor(line));
            }
            meta.setLore(coloredLore);
        }
        
        item.setItemMeta(meta);
        return item;
    }
}
