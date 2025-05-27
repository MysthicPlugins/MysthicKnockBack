package kk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.utils.MessageUtils;

public class PlayerListMenu extends Menu {

    public PlayerListMenu(KvKnockback plugin) {
        super(plugin, "&8• &c&lJugadores Online &8•", 54);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player); // Remover al jugador que está reportando

        plugin.getLogger().info("[Menú de Reportes] Jugador " + player.getName() + " abrió la lista de jugadores");
        plugin.getLogger().info("[Menú de Reportes] Jugadores disponibles para reportar: " + onlinePlayers.size());

        int slot = 10;
        for (Player target : onlinePlayers) {
            if (slot > 43) break; // Límite de slots

            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(target.getName());
            meta.setDisplayName(MessageUtils.getColor("&c" + target.getName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.getColor("&7Click para reportar a este jugador"));
            meta.setLore(lore);
            
            skull.setItemMeta(meta);
            inv.setItem(slot, skull);

            // Incrementar slot, saltando los bordes
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        // Botón para volver
        inv.setItem(49, createItem(Material.ARROW, "&c← Volver", "&7Click para volver al menú"));

        // Relleno
        ItemStack filler = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 14);
        fillEmptySlots(inv, filler);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() != null) {
            if (event.getSlot() == 49) {
                plugin.getLogger().info("[Menú de Reportes] " + player.getName() + " volvió al menú principal");
                plugin.getMenuManager().openMenu(player, "main");
                return;
            }

            if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                String targetName = MessageUtils.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                plugin.getLogger().info("[Menú de Reportes] " + player.getName() + " seleccionó reportar a " + targetName);
                plugin.getReportManager().setReportTarget(player.getUniqueId(), targetName);
                plugin.getMenuManager().openMenu(player, "report_reason");
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
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

    private ItemStack createItem(Material material, String name, byte data, String... lore) {
        ItemStack item = createItem(material, name, lore);
        item.setDurability(data);
        return item;
    }
}
