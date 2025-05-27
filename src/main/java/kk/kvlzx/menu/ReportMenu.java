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
import kk.kvlzx.utils.MessageUtils;

public class ReportMenu extends Menu {
    private final Player reportedPlayer;

    public ReportMenu(KvKnockback plugin, Player reportedPlayer) {
        super(plugin, reportedPlayer != null ? "&8• &c&lReportar a " + reportedPlayer.getName() + " &8•" : "&8• &c&lReporte &8•", 36);
        this.reportedPlayer = reportedPlayer;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Items para hacks
        inv.setItem(10, createItem(Material.DIAMOND_SWORD, "&c&lKillAura", 
            "&7Click para reportar por KillAura",
            "",
            "&8➥ Uso de hacks de combate"));

        inv.setItem(11, createItem(Material.FEATHER, "&c&lSpeed/Fly", 
            "&7Click para reportar por Speed/Fly",
            "",
            "&8➥ Movimiento no natural"));

        inv.setItem(12, createItem(Material.STICK, "&c&lReach/Velocity", 
            "&7Click para reportar por Reach/Velocity",
            "",
            "&8➥ Alcance o KB sospechoso"));

        inv.setItem(13, createItem(Material.WOOL, "&c&lAntiKB", 
            "&7Click para reportar por AntiKnockback",
            "",
            "&8➥ No recibe knockback"));

        // Items para comportamiento
        inv.setItem(14, createItem(Material.GHAST_TEAR, "&6&lEx", 
            "&7Click para reportar a un ex",
            "",
            "&8➥ Es mi ex y me molesta"));

        inv.setItem(15, createItem(Material.BOOK_AND_QUILL, "&e&lToxicidad", 
            "&7Click para reportar por toxicidad",
            "",
            "&8➥ Insultos o acoso"));

        inv.setItem(16, createItem(Material.NAME_TAG, "&e&lTeaming", 
            "&7Click para reportar por team",
            "",
            "&8➥ Aliarse con otros jugadores"));

        // Botón para volver
        inv.setItem(31, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver",
            "",
            "&8➥ Volver a la lista de jugadores"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 14);
        fillEmptySlots(inv, filler);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player reporter = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (event.getSlot() == 31) {
            plugin.getMenuManager().openMenu(reporter, "player_list");
            return;
        }

        String reason = null;
        switch (event.getSlot()) {
            case 10: reason = "KillAura"; break;
            case 11: reason = "Speed/Fly"; break;
            case 12: reason = "Reach/Velocity"; break;
            case 13: reason = "AntiKB"; break;
            case 14: reason = "Es mi ex y me está acosando"; break;
            case 15: reason = "Toxicidad"; break;
            case 16: reason = "Teaming"; break;
        }

        if (reason != null) {
            notifyReport(reporter, reportedPlayer, reason);
            reporter.closeInventory();
        }
    }

    private void notifyReport(Player reporter, Player reported, String reason) {
        // Notificar al reportador
        reporter.sendMessage(MessageUtils.getColor("&aHas reportado a &f" + reported.getName() + " &apor &f" + reason));
        
        // Notificar a staff
        String staffMsg = String.format("&c[Reporte] &f%s &7ha reportado a &f%s &7por &f%s",
            reporter.getName(), reported.getName(), reason);
            
        for (Player staff : plugin.getServer().getOnlinePlayers()) {
            if (staff.hasPermission("kvknockback.reports.receive")) {
                staff.sendMessage(MessageUtils.getColor(staffMsg));
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
