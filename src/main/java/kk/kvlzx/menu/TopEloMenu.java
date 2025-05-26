package kk.kvlzx.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kk.kvlzx.KvKnockback;
import kk.kvlzx.stats.PlayerStats;
import kk.kvlzx.utils.MessageUtils;
import kk.kvlzx.items.CustomItem;
import kk.kvlzx.managers.RankManager;

public class TopEloMenu extends Menu {

    public TopEloMenu(KvKnockback plugin) {
        super(plugin, "&8• &6&lTop ELO &8•", 45);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Crear los items de relleno
        ItemStack darkGold = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 1); // Naranja oscuro
        ItemStack lightGold = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 4); // Amarillo

        // Colocar el borde exterior (naranja oscuro)
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, darkGold);
            inv.setItem(36 + i, darkGold);
        }
        for (int i = 0; i < 45; i += 9) {
            inv.setItem(i, darkGold);
            inv.setItem(i + 8, darkGold);
        }

        // Colocar el borde interior (amarillo)
        for (int i = 1; i < 8; i++) {
            inv.setItem(9 + i, lightGold);
            inv.setItem(27 + i, lightGold);
        }
        for (int i = 9; i < 36; i += 9) {
            inv.setItem(i + 1, lightGold);
            inv.setItem(i + 7, lightGold);
        }

        List<UUID> topPlayers = new ArrayList<>(PlayerStats.getAllStats());
        topPlayers.sort((uuid1, uuid2) -> {
            PlayerStats stats1 = PlayerStats.getStats(uuid1);
            PlayerStats stats2 = PlayerStats.getStats(uuid2);
            return Integer.compare(stats2.getElo(), stats1.getElo());
        });

        // Colocar las cabezas en los slots centrales
        int[] slots = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};
        for (int i = 0; i < 10; i++) {
            ItemStack skull;
            if (i < topPlayers.size()) {
                UUID uuid = topPlayers.get(i);
                PlayerStats stats = PlayerStats.getStats(uuid);
                String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                String rankPrefix = RankManager.getRankPrefix(stats.getElo());
                
                List<String> lore = new ArrayList<>();
                lore.add("&7Posición: &f#" + (i + 1));
                lore.add("&7ELO: &6" + stats.getElo());
                
                skull = CustomItem.createSkullFromUUID(uuid, 
                    rankPrefix + " &f" + playerName,
                    lore.toArray(new String[0]));
            } else {
                skull = CustomItem.createEmptyTopSkull(i + 1, "&7Sin datos", 
                    "&7Posición: &f#" + (i + 1),
                    "&7ELO: &6500");
            }
            
            inv.setItem(slots[i], skull);
        }

        // Botón para volver centrado en la última fila
        ItemStack backButton = createItem(Material.ARROW, "&c← Volver", 
            "&7Click para volver al menú principal");
        inv.setItem(40, backButton);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (event.getSlot() == 40) { // Botón de volver
            Player player = (Player) event.getWhoClicked();
            plugin.getMenuManager().openMenu(player, "main");
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
