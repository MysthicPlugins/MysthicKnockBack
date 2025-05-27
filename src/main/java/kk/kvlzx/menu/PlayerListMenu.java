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
import kk.kvlzx.items.CustomItem;
import kk.kvlzx.utils.MessageUtils;

public class PlayerListMenu extends Menu {

    public PlayerListMenu(KvKnockback plugin) {
        super(plugin, "&8• &c&lSeleccionar Jugador &8•", 54);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player); // Remover al jugador actual

        // Colocar las cabezas de los jugadores
        int slot = 10;
        for (Player target : onlinePlayers) {
            if (slot > 43) break; // Límite de slots
            
            if (slot % 9 == 8) slot += 2; // Saltar bordes
            
            inv.setItem(slot, CustomItem.createSkullFromUUID(
                target.getUniqueId(),
                "&c" + target.getName(),
                "&7Click para reportar a este jugador"
            ));
            
            slot++;
        }

        // Botón para volver al menú principal
        inv.setItem(49, createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver al menú principal"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 14);
        fillEmptySlots(inv, filler);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (event.getSlot() == 49) {
            plugin.getMenuManager().openMenu(player, "main");
            return;
        }

        // Si es una cabeza de jugador, abrir menú de reporte
        if (clicked.getType() == Material.SKULL_ITEM) {
            String targetName = MessageUtils.stripColor(clicked.getItemMeta().getDisplayName());
            Player target = Bukkit.getPlayer(targetName);
            
            if (target != null && target.isOnline()) {
                ReportMenu reportMenu = new ReportMenu(plugin, target);
                player.openInventory(reportMenu.getInventory(player));
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
