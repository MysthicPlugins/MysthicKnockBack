package kk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.reports.ReportReason;
import kk.kvlzx.utils.MessageUtils;

public class ReportReasonMenu extends Menu {

    public ReportReasonMenu(KvKnockback plugin) {
        super(plugin, "&8• &c&lSeleccionar Razón &8•", 36);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());
        
        // Información del jugador a reportar
        inv.setItem(4, createItem(Material.BOOK, "&cReportando a: &f" + targetName,
            "&7Selecciona una razón para el reporte"));

        // Razones de reporte
        int slot = 10;
        for (ReportReason reason : ReportReason.values()) {
            inv.setItem(slot++, createItem(reason.getIcon(), reason.getDisplayName(), 
                "&7Click para seleccionar esta razón",
                "",
                "&8" + reason.getDescription()));
            if ((slot - 9) % 9 == 8) slot += 2;
        }

        // Botón para volver
        inv.setItem(31, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver a la lista de jugadores"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 14);
        fillEmptySlots(inv, filler);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());
        if (targetName == null) {
            player.closeInventory();
            player.sendMessage(MessageUtils.getColor("&cError: No se encontró el jugador a reportar"));
            return;
        }

        if (event.getSlot() == 31) { // Botón volver
            plugin.getMenuManager().openMenu(player, "player_list");
            return;
        }

        // Verificar si clickeó una razón válida
        String clickedName = clicked.getItemMeta() != null ? 
            MessageUtils.stripColor(clicked.getItemMeta().getDisplayName()) : null;

        ReportReason reason = ReportReason.getByDisplayName(clickedName);
        if (reason != null) {
            plugin.getReportManager().submitReport(player, targetName, reason);
            player.closeInventory();
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
