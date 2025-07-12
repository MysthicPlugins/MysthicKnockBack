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
import mk.kvlzx.config.ReportMenuConfig;
import mk.kvlzx.utils.MessageUtils;

public class PlayerListMenu extends Menu {
    private final ReportMenuConfig menuConfig;

    public PlayerListMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getReportMenuConfig().getMenuPlayerListTitle(), plugin.getReportMenuConfig().getMenuPlayerListSize());
        this.menuConfig = plugin.getReportMenuConfig();
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player); // Remover al jugador que está reportando

        // Poner las cabezas de jugadores
        int slot = 10;
        for (Player target : onlinePlayers) {
            if (slot > 43) break; // Limite de slots

            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(target.getName());
            meta.setDisplayName(MessageUtils.getColor(
            menuConfig.getMenuPlayerListItemSkullName().replace("%player_name%", target.getName())
            ));

            List<String> lore = new ArrayList<>();
            for (String line : menuConfig.getMenuPlayerListItemSkullLore()) {
                lore.add(MessageUtils.getColor(line));
            }
            meta.setLore(lore);

            skull.setItemMeta(meta);
            inv.setItem(slot, skull);

            // Incrementar slots saltando bordes
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        // Boton de volver
        inv.setItem(
            menuConfig.getMenuPlayerListItemBackSlot(), 
            createItem(Material.valueOf(menuConfig.getMenuPlayerListItemBackId()),
            menuConfig.getMenuPlayerListItemBackName(),
            menuConfig.getMenuPlayerListItemBackLore().toArray(new String[0]))
        );

        // Bordes de cabezas de wither y redstone, dejando 2 espacios
        ItemStack witherSkull = createItem(Material.SKULL_ITEM, "&7", (byte) 1); // Cabeza de wither
        ItemStack redstone = createItem(Material.REDSTONE, "&7"); // Redstone

        // Definir los slots de los bordes
        List<Integer> borderSlotList = menuConfig.getMenuPlayerListFillerSlots();
        int[] borderSlots = borderSlotList.stream().mapToInt(Integer::intValue).toArray();

        // Poner los items con el patron de wither, espacio, espacio, redstone, espacio, espacio
        for (int i = 0; i < borderSlots.length; i++) {
            if (borderSlots[i] == 49) continue; // Evitar sobreescribir el boton de volver
            if (i % 6 == 0) {
                inv.setItem(borderSlots[i], witherSkull); // Cabeza de wither
            } else if (i % 6 == 3) {
                inv.setItem(borderSlots[i], redstone); // Redstone
            } // Slots i % 6 == 1, 2, 4, 5 quedan vacios (null)
        }
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

        if (event.getCurrentItem() != null) {
            // Ignorar clicks en items decorativos
            if ((event.getCurrentItem().getType() == Material.SKULL_ITEM && 
                event.getCurrentItem().getDurability() == 1) ||
                event.getCurrentItem().getType() == Material.REDSTONE) {
                return;
            }

            if (event.getSlot() == menuConfig.getMenuPlayerListItemBackSlot()) {
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
