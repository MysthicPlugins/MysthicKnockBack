package kk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
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
        super(plugin, "&8• &c&lReportar a " + reportedPlayer.getName() + " &8•", 27);
        this.reportedPlayer = reportedPlayer;
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Razones de hack
        inv.setItem(10, createItem(Material.DIAMOND_SWORD, "&c&lKillAura", "&7Click para reportar por KillAura"));
        inv.setItem(11, createItem(Material.FEATHER, "&c&lSpeed/Fly", "&7Click para reportar por Speed/Fly"));
        inv.setItem(12, createItem(Material.STICK, "&c&lReach", "&7Click para reportar por Reach"));
        inv.setItem(13, createItem(Material.WOOL, "&c&lAntiKB", "&7Click para reportar por AntiKnockback"));

        // Razones de comportamiento
        inv.setItem(14, createItem(Material.BOOK_AND_QUILL, "&e&lToxicidad", "&7Click para reportar por comportamiento tóxico"));
        inv.setItem(15, createItem(Material.PAPER, "&e&lSpam", "&7Click para reportar por spam"));
        inv.setItem(16, createItem(Material.NAME_TAG, "&e&lInsultos", "&7Click para reportar por insultos"));
        inv.setItem(17, createItem(Material.GHAST_TEAR, "&c&lEx", "&7Click para reportar por ex molesto"));

        // Botón para cancelar
        inv.setItem(22, createItem(Material.BARRIER, "&c&lCancelar", "&7Click para cancelar el reporte"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 14);
        fillEmptySlots(inv, filler);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player reporter = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) return;

        String reason = "";
        boolean isValid = true;

        switch (event.getSlot()) {
            case 10:
                reason = "KillAura";
                break;
            case 11:
                reason = "Speed/Fly";
                break;
            case 12:
                reason = "Reach";
                break;
            case 13:
                reason = "AntiKB";
                break;
            case 14:
                reason = "Toxicidad";
                break;
            case 15:
                reason = "Spam";
                break;
            case 16:
                reason = "Insultos";
                break;
            case 17:
                reason = "Es mi ex y me está acosando";
                break;
            case 22:
                reporter.closeInventory();
                reporter.sendMessage(MessageUtils.getColor("&cReporte cancelado."));
                return;
            default:
                isValid = false;
                break;
        }

        if (isValid) {
            reporter.closeInventory();
            notifyReport(reporter, reportedPlayer, reason);
        }
    }

    private void notifyReport(Player reporter, Player reported, String reason) {
        // Notificar al reportador
        reporter.sendMessage(MessageUtils.getColor("&aHas reportado a &f" + reported.getName() + " &apor &f" + reason));
        
        // Notificar a staff
        String staffMsg = String.format("&c[Reporte] &f%s &7ha reportado a &f%s &7por &f%s",
            reporter.getName(), reported.getName(), reason);
            
        for (Player staff : Bukkit.getOnlinePlayers()) {
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
