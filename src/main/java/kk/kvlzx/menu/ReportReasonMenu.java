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
        super(plugin, "&8• &c&lSeleccionar Razón &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());
        
        // Primero colocar los elementos principales
        // Información del jugador a reportar
        inv.setItem(4, createItem(Material.BOOK, "&c&lReportando a: &f" + targetName,
            "&8▪ &7Selecciona una razón para el reporte",
            "",
            "&8➥ &7Elige cuidadosamente"));

        // Razones de reporte en círculo
        int[] slots = {11, 12, 13, 14, 15, 21, 22, 23};
        int index = 0;
        for (ReportReason reason : ReportReason.values()) {
            if (index >= slots.length) break;
            
            inv.setItem(slots[index], createItem(reason.getIcon(), reason.getDisplayName(), 
                "&8▪ &7Click para seleccionar",
                "",
                "&8➥ &7" + reason.getDescription()));
            index++;
        }

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Cancelar", 
            "&7Click para volver a la lista"));

        // Después colocar el borde exterior (rojo oscuro)
        ItemStack darkRed = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 14);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, darkRed);
            inv.setItem(36 + i, darkRed);
        }
        for (int i = 0; i < 45; i += 9) {
            inv.setItem(i, darkRed);
            inv.setItem(i + 8, darkRed);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.STAINED_GLASS_PANE) return;

        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());
        if (targetName == null) {
            plugin.getLogger().info("[Menú de Reportes] Error: No se encontró objetivo para " + player.getName());
            player.closeInventory();
            player.sendMessage(MessageUtils.getColor("&cError: No se encontró el jugador a reportar"));
            return;
        }

        if (event.getSlot() == 40) {
            plugin.getLogger().info("[Menú de Reportes] " + player.getName() + " canceló el reporte y volvió a la lista");
            plugin.getMenuManager().openMenu(player, "player_list");
            return;
        }

        // Buscar la razón que coincida con el ítem clickeado
        for (ReportReason reason : ReportReason.values()) {
            if (clicked.getType() == reason.getIcon()) {
                plugin.getLogger().info("[Menú de Reportes] " + player.getName() + " reportó a " + targetName + " por " + reason.name());
                plugin.getReportManager().submitReport(player, targetName, reason);
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
