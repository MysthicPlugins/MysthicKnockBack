package mk.kvlzx.menu;

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

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.config.TopsMenuConfig;
import mk.kvlzx.stats.PlayerStats;
import mk.kvlzx.utils.MessageUtils;
import mk.kvlzx.items.CustomItem;

public class TopStreakMenu extends Menu {
    private final TopsMenuConfig menuConfig;

    public TopStreakMenu(MysthicKnockBack plugin) {
        super(plugin, plugin.getTopsMenuConfig().getTopStreaksTitle(), plugin.getTopsMenuConfig().getTopStreaksSize());
        this.menuConfig = plugin.getTopsMenuConfig();
    }

    private void applyFillPattern(Inventory inv, ItemStack outerMaterial, ItemStack innerMaterial) {
        int size = inv.getSize();
        int rows = size / 9;
        
        // Aplicar borde exterior
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = row * 9 + col;
                
                // Borde exterior: primera y última fila, primera y última columna
                if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                    if (inv.getItem(slot) == null) {
                        inv.setItem(slot, outerMaterial);
                    }
                }
                // Borde interior: segunda y penúltima fila, segunda y penúltima columna
                else if ((row == 1 || row == rows - 2) || (col == 1 || col == 7)) {
                    if (inv.getItem(slot) == null) {
                        inv.setItem(slot, innerMaterial);
                    }
                }
            }
        }
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        // Crear los items de relleno
        ItemStack darkPink = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 2); // Magenta
        ItemStack lightPink = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 6); // Rosa

        applyFillPattern(inv, darkPink, lightPink);

        // Obtener y ordenar los top jugadores por Max Streak
        List<UUID> topPlayers = new ArrayList<>(PlayerStats.getAllStats());
        topPlayers.sort((uuid1, uuid2) -> {
            PlayerStats stats1 = PlayerStats.getStats(uuid1);
            PlayerStats stats2 = PlayerStats.getStats(uuid2);
            return Integer.compare(stats2.getMaxStreak(), stats1.getMaxStreak());
        });

        // Obtener los slots configurados para los tops
        List<Integer> topSlots = menuConfig.getTopStreaksSlots();
        
        // Colocar las cabezas de los jugadores en los slots configurados
        for (int i = 0; i < topSlots.size(); i++) {
            int slot = topSlots.get(i);
            ItemStack skull;
            
            if (i < topPlayers.size()) {
                UUID uuid = topPlayers.get(i);
                PlayerStats stats = PlayerStats.getStats(uuid);
                String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                
                // Usar la configuración para el nombre y lore
                String configName = menuConfig.getTopStreaksPlayersName();
                List<String> configLore = new ArrayList<>(menuConfig.getTopStreaksPlayersLore());
                
                // Reemplazar placeholders en el nombre
                String displayName = configName
                    .replace("%player%", playerName)
                    .replace("%position%", String.valueOf(i + 1))
                    .replace("%max-streak%", String.valueOf(stats.getMaxStreak()))
                    .replace("%current-streak%", String.valueOf(stats.getCurrentStreak()));
                
                // Reemplazar placeholders en el lore
                List<String> finalLore = new ArrayList<>();
                for (String loreLine : configLore) {
                    String processedLine = loreLine
                        .replace("%player%", playerName)
                        .replace("%position%", String.valueOf(i + 1))
                        .replace("%max-streak%", String.valueOf(stats.getMaxStreak()))
                        .replace("%current-streak%", String.valueOf(stats.getCurrentStreak()));
                    finalLore.add(processedLine);
                }
                
                skull = CustomItem.createSkullFromUUID(uuid, displayName, finalLore.toArray(new String[0]));
            } else {
                // Usar la configuración para jugadores sin datos
                String configName = menuConfig.getTopStreaksNonDataName();
                List<String> configLore = new ArrayList<>(menuConfig.getTopStreaksNonDataLore());
                
                // Reemplazar placeholders en el nombre
                String displayName = configName.replace("%position%", String.valueOf(i + 1));
                
                // Reemplazar placeholders en el lore
                List<String> finalLore = new ArrayList<>();
                for (String loreLine : configLore) {
                    String processedLine = loreLine.replace("%position%", String.valueOf(i + 1));
                    finalLore.add(processedLine);
                }
                
                skull = CustomItem.createEmptyTopSkull(i + 1, displayName, finalLore.toArray(new String[0]));
            }
            
            inv.setItem(slot, skull);
        }

        // Botón de regreso usando la configuración
        ItemStack backButton = menuConfig.createMenuItem(
            menuConfig.getTopStreaksBackId(), 
            player, 
            menuConfig.getTopStreaksBackName(), 
            menuConfig.getTopStreaksBackLore()
        );
        inv.setItem(menuConfig.getTopStreaksBackSlot(), backButton);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Validar que el click sea en el menú y no en el inventario del jugador
        if (!isValidClick(event)) {
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        
        // Verificar si se hizo clic en el botón de regreso
        if (event.getSlot() == menuConfig.getTopStreaksBackSlot()) {
            Player player = (Player) event.getWhoClicked();
            plugin.getMenuManager().openMenu(player, "main");
        }
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
