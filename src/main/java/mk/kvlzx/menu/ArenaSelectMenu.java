package mk.kvlzx.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mk.kvlzx.MysthicKnockBack;
import mk.kvlzx.arena.Arena;
import mk.kvlzx.utils.MessageUtils;

public class ArenaSelectMenu extends Menu {

    public ArenaSelectMenu(MysthicKnockBack plugin) {
        super(plugin, "&9&lSelect Arena to Vote", 54);
    }

    @Override
    protected void setupItems(Player player, Inventory inv) {
        Collection<Arena> arenas = plugin.getArenaManager().getArenas();
        String currentArena = plugin.getArenaManager().getCurrentArena();
        
        int slot = 10;
        for (Arena arena : arenas) {
            if (slot >= 44) break; // Limitar slots
            
            String arenaName = arena.getName();
            boolean isCurrent = arenaName.equals(currentArena);
            
            Material material = isCurrent ? Material.EMERALD_BLOCK : Material.DIAMOND_BLOCK;
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (isCurrent) {
                lore.add("&a✓ Current Arena");
                lore.add("&7You are already here!");
            } else {
                lore.add("&7Click to start a vote");
                lore.add("&7to change to this arena!");
                lore.add("");
                lore.add("&e➤ Click to vote!");
            }
            lore.add("");
            
            ItemStack item = createItem(material, "&b&l" + arenaName, lore);
            inv.setItem(slot, item);
            
            // Calcular siguiente slot (patrón 3x7)
            slot++;
            if ((slot - 10) % 7 == 0) {
                slot += 2; // Saltar a siguiente fila
            }
        }
        
        // Info item
        ItemStack info = createItem(Material.PAPER, "&e&lArena Voting", 
            "&7Select an arena to start a vote!",
            "",
            "&7Current: &b" + (currentArena != null ? currentArena : plugin.getTabConfig().getScoreNullArena()),
            "&7Total Arenas: &b" + arenas.size()
        );
        inv.setItem(4, info);
        
        // Decoración
        ItemStack glass = createItem(Material.STAINED_GLASS_PANE, " ", (byte) 9);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!isValidClick(event)) {
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        
        if (item == null || !item.hasItemMeta()) return;
        
        String itemName = item.getItemMeta().getDisplayName();
        if (itemName == null) return;
        
        // Extraer nombre de arena
        String arenaName = MessageUtils.stripColor(itemName).trim();
        
        // Verificar que el jugador tiene permisos
        if (!player.hasPermission("mysthicknockback.vote.start")) {
            player.sendMessage(MessageUtils.getColor(MysthicKnockBack.getPrefix() + 
                "&cYou don't have permission to start votes!"));
            player.closeInventory();
            return;
        }
        
        // Verificar que la arena existe
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena != null) {
            player.closeInventory();
            plugin.getArenaVoteManager().startVote(player, arenaName);
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
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtils.getColor(name));
        
        if (lore != null && !lore.isEmpty()) {
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
