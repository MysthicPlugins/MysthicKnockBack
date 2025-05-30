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
        super(plugin, "&8• &c&lSeleccionar Razón &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        String targetName = plugin.getReportManager().getReportTarget(player.getUniqueId());

        // Cabeza del jugador a reportar
        ItemStack targetHead = CustomItem.createSkullFromUUID(
            plugin.getServer().getPlayer(targetName).getUniqueId(),
            "&c&lReportando a: &f" + targetName,
            "&8▪ &7Selecciona una razón para el reporte",
            "",
            "&8➥ &7Elige cuidadosamente"
        );
        inv.setItem(4, targetHead);

        // Razones de reporte en forma de U
        int[] reasonSlots = {19, 20, 21, 22, 23, 24, 25, 34};
        int index = 0;
        for (ReportReason reason : ReportReason.values()) {
            if (index >= reasonSlots.length) break;
            
            List<String> lore = new ArrayList<>();
            lore.add("&8▪ &7Click para seleccionar este motivo");
            lore.add("");
            lore.add("&8➥ &7" + reason.getDescription());
            
            inv.setItem(reasonSlots[index], createItem(
                reason.getIcon(),
                reason.getDisplayName(),
                lore.toArray(new String[0])
            ));
            index++;
        }

        // Separadores decorativos
        ItemStack lightRed = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 1);
        for (int i : new int[]{1, 2, 3, 5, 6, 7}) {
            inv.setItem(i, lightRed);
        }

        // Botón para volver
        inv.setItem(40, createItem(Material.ARROW, "&c← Cancelar", 
            "&7Click para volver a la lista de jugadores"));

        // Crear bordes con patrón
        ItemStack darkRed = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 14);
        ItemStack red = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 6);
        
        // Patrón de borde exterior
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, i % 2 == 0 ? darkRed : red);
            inv.setItem(36 + i, i % 2 == 0 ? darkRed : red);
        }
        for (int i = 0; i < 45; i += 9) {
            inv.setItem(i, i % 18 == 0 ? darkRed : red);
            inv.setItem(i + 8, i % 18 == 0 ? darkRed : red);
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
            player.closeInventory();
            player.sendMessage(MessageUtils.getColor("&cError: No se encontró el jugador a reportar"));
            return;
        }

        if (event.getSlot() == 40) {
            plugin.getMenuManager().openMenu(player, "player_list");
            return;
        }

        // Buscar la razón que coincida con el ítem clickeado
        for (ReportReason reason : ReportReason.values()) {
            if (clicked.getType() == reason.getIcon()) {
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
