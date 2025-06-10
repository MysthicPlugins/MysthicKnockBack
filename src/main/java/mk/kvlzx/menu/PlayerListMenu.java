package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.utils.MessageUtils;

public class PlayerListMenu extends Menu {

    public PlayerListMenu(MysthicKnockBack plugin) {
        super(plugin, "&8• &c&lJugadores Online &8•", 54);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player); // Remover al jugador que está reportando

        // Colocar cabezas de jugadores
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

        // Bordes con cabezas de esqueleto y zombi, alternando con dos espacios vacíos
        ItemStack skeletonSkull = createItem(Material.SKULL_ITEM, "&7", (byte) 0); // Cabeza de esqueleto
        ItemStack zombieSkull = createItem(Material.SKULL_ITEM, "&7", (byte) 2); // Cabeza de zombi

        // Definir los slots de los bordes
        int[] borderSlots = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,     // Fila superior
            9, 18, 27, 36,                   // Columna izquierda
            17, 26, 35, 44,                  // Columna derecha
            45, 46, 47, 48, 50, 51, 52, 53  // Fila inferior (excluye slot 49)
        };

        // Colocar cabezas con patrón: esqueleto, espacio, espacio, zombi, espacio, espacio
        for (int i = 0; i < borderSlots.length; i++) {
            if (borderSlots[i] == 49) continue; // Evitar sobrescribir el botón de volver
            if (i % 6 == 0) {
                inv.setItem(borderSlots[i], skeletonSkull); // Cabeza de esqueleto
            } else if (i % 6 == 3) {
                inv.setItem(borderSlots[i], zombieSkull); // Cabeza de zombi
            } // Los slots i % 6 == 1, 2, 4, 5 quedan vacíos (null)
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() != null) {
            // Ignorar clics en cabezas decorativas (esqueleto y zombi)
            if (event.getCurrentItem().getType() == Material.SKULL_ITEM && 
                (event.getCurrentItem().getDurability() == 0 || event.getCurrentItem().getDurability() == 2)) {
                return;
            }

            if (event.getSlot() == 49) {
                plugin.getMenuManager().openMenu(player, "main");
                return;
            }

            if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
                String targetName = MessageUtils.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
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
